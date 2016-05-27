package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import static com.jpmorgan.ib.caonpd.cakeshop.util.ProcessUtils.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.cakeshop.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.cakeshop.dao.WalletDAO;
import com.jpmorgan.ib.caonpd.cakeshop.db.BlockScanner;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Account;
import com.jpmorgan.ib.caonpd.cakeshop.model.RequestModel;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.service.WalletService;
import com.jpmorgan.ib.caonpd.cakeshop.util.FileUtils;
import com.jpmorgan.ib.caonpd.cakeshop.util.ProcessUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author I629630
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    public static final String SIMPLE_RESULT = "_result";
    public static final Integer DEFAULT_NETWORK_ID = 1006;

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);

    @Autowired
    private GethConfigBean gethConfig;

    @Autowired
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private WalletDAO walletDAO;

    @Autowired
    private ApplicationContext applicationContext;

    private BlockScanner blockScanner;

    private Boolean running;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GethHttpServiceImpl() {
        this.running = false;
    }

    private String executeGethCall(String json) throws APIException {
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("> " + json);
            }

            RestTemplate restTemplate = applicationContext.getBean(RestTemplate.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(gethConfig.getRpcUrl(), POST, httpEntity, String.class);

            String res = response.getBody();

            if (LOG.isDebugEnabled()) {
                LOG.debug("< " + res.trim());
            }

            return res;

        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }

    private String requestToJson(Object request) throws APIException {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new APIException("Failed to serialize request(s)", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException {

        RequestModel request = new RequestModel(funcName, args, GETH_API_VERSION, GETH_REQUEST_ID);
        String response = executeGethCall(requestToJson(request));

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        Map<String, Object> data;
        try {
            data = objectMapper.readValue(response, Map.class);
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }

        return processResponse(data);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> batchExecuteGethCall(List<RequestModel> requests) throws APIException {
        String response = executeGethCall(requestToJson(requests));

        List<Map<String, Object>> responses;
        try {
            responses = objectMapper.readValue(response, List.class);

            List<Map<String, Object>> results = new ArrayList<>(responses.size());
            for (Map<String, Object> data : responses) {
                results.add(processResponse(data));
            }
            return results;

        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> processResponse(Map<String, Object> data) throws APIException {

        if (data.containsKey("error") && data.get("error") != null) {
            String message;
            Map<String, String> error = (Map<String, String>) data.get("error");
            if (error.containsKey("message")) {
                message = error.get("message");
            } else {
                message = "RPC call failed";
            }
            throw new APIException("RPC request failed: " + message);
        }

        Object result = data.get("result");
        if (result == null) {
            return null;
        }

        if (!(result instanceof Map)) {
            // Handle case where a simple value is returned instead of a map (int, bool, or string)
            Map<String, Object> res = new HashMap<>();
            res.put(SIMPLE_RESULT, data.get("result"));
            return res;
        }

        return (Map<String, Object>) data.get("result");
    }

    @Override
    public Boolean stop() {
        LOG.info("Stopping geth");

        try {
            if (blockScanner != null) {
                blockScanner.shutdown();
            }

            return killProcess(readPidFromFile(gethConfig.getGethPidFilename()), "geth.exe");

        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot shutdown process " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Boolean reset() {

        boolean stopped = this.stop();
        if (!stopped) {
            return stopped;
        }

        this.deletePid();

        try {
            FileUtils.deleteDirectory(new File(gethConfig.getDataDirPath()));
        } catch (IOException ex) {
            LOG.error("Cannot delete directory " + ex.getMessage());
            return false;
        }

        // delete db
        blockDAO.reset();
        txDAO.reset();
        walletDAO.reset();

        return this.start();
    }

    @Override
    public Boolean deletePid() {
        return new File(gethConfig.getGethPidFilename()).delete();
    }

    @PreDestroy
    protected void autoStop () {
        if (!gethConfig.isAutoStop()) {
            return;
        }

        stop();
        deletePid();

        // stop solc server
        List<String> args = Lists.newArrayList(
                gethConfig.getNodePath(),
                gethConfig.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethConfig, args);
        try {
            builder.start();
        } catch (IOException e) {
        }
    }

    @Override
    public Boolean isRunning() {
        return running;
    }

    @Override
    public Boolean start(String... additionalParams) {

        if (isProcessRunning(readPidFromFile(gethConfig.getGethPidFilename()))) {
            LOG.info("Ethereum was already running; not starting again");
            return this.running = true;
        }

        try {
            String dataDir = gethConfig.getDataDirPath();
            boolean newGethInstall = false;

            File chainDataDir = new File(FileUtils.expandPath(dataDir, "chaindata"));
            if (!chainDataDir.exists()) {
                chainDataDir.mkdirs();
                initGeth();

                File keystoreDir = new File(FileUtils.expandPath(dataDir, "keystore"));
                if (!keystoreDir.exists()) {
                    LOG.debug("Initializing keystore");
                    FileUtils.copyDirectory(new File(gethConfig.getKeystorePath()), keystoreDir);
                }
                newGethInstall = true;
            }


            ProcessBuilder builder = createProcessBuilder(gethConfig, createGethCommand(additionalParams));
            builder.inheritIO();
            Process process = builder.start();

            Integer pid = getProcessPid(process);
            if (pid != null) {
                writePidToFile(pid, gethConfig.getGethPidFilename());
            }

            if (!(checkGethStarted() && checkWalletUnlocked())) {
                LOG.error("Ethereum failed to start");
                return this.running = false;
            }

            if (newGethInstall) {
                BlockchainInitializerTask init = applicationContext.getBean(BlockchainInitializerTask.class);
                init.run();
            }

            // FIXME add a watcher thread to make sure it doesn't die..

        } catch (IOException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
            return this.running = false;
        }

        this.blockScanner = applicationContext.getBean(BlockScanner.class);
        blockScanner.start();

        LOG.info("Ethereum started successfully");
        return this.running = true;
    }

    /**
     * Initialize geth datadir via "geth init" command, using the configured genesis block
     * @return
     * @throws IOException
     */
    private boolean initGeth() throws IOException {
        ProcessBuilder builder = createProcessBuilder(gethConfig, createGethInitCommand());
        builder.inheritIO();
        try {
            return (builder.start().waitFor() == 0);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for geth init", e);
        }
        return false;
    }

    private List<String> createGethInitCommand() {
        return Lists.newArrayList(gethConfig.getGethPath(),
                "--datadir", gethConfig.getDataDirPath(),
                "init", gethConfig.getGenesisBlockFilename()
                );
    }

    private List<String> createGethCommand(String... additionalParams) {

        // Figure out how many accounts need unlocking
        String accountsToUnlock = "";
        int numAccounts = walletDAO.list().size();
        if (numAccounts == 0) {
            accountsToUnlock = "0,1,2"; // default to accounts we ship

        } else {
            for (int i = 0; i < numAccounts; i++) {
                if (accountsToUnlock.length() > 0) {
                    accountsToUnlock += ",";
                }
                accountsToUnlock += i;
            }
        }

        List<String> commands = Lists.newArrayList(gethConfig.getGethPath(),
                "--port", gethConfig.getGethNodePort(),
                "--datadir", gethConfig.getDataDirPath(),
                //"--verbosity", "6",
                //"--mine", "--minerthreads", "1",
                //"--jpmtest",
                "--solc", gethConfig.getSolcPath(),
                "--nat", "none", "--nodiscover",
                "--unlock", accountsToUnlock, "--password", gethConfig.getGethPasswordFile(),
                "--rpc", "--rpcaddr", "127.0.0.1", "--rpcport", gethConfig.getRpcPort(), "--rpcapi", gethConfig.getRpcApiList(),
                "--ipcdisable",
                "--fakepow"
                );

        if (null != additionalParams && additionalParams.length > 0) {
            commands.addAll(Lists.newArrayList(additionalParams));
        }

        commands.add("--networkid");
        commands.add(String.valueOf(gethConfig.getNetworkId() == null ? DEFAULT_NETWORK_ID : gethConfig.getNetworkId()));

        commands.add("--verbosity");
        commands.add(String.valueOf(gethConfig.getVerbosity() == null ? "3" : gethConfig.getVerbosity()));

        if (null != gethConfig.isMining() && gethConfig.isMining() == true) {
            commands.add("--mine");
            commands.add("--minerthreads");
            commands.add("1");
        }
        if (StringUtils.isNotEmpty(gethConfig.getIdentity())) {
            commands.add("--identity");
            commands.add(gethConfig.getIdentity());
        }

        // add custom params
        if (StringUtils.isNotBlank(gethConfig.getExtraParams())) {
            String[] params = gethConfig.getExtraParams().split(" ");
            for (String param : params) {
                if (StringUtils.isNotBlank(param)) {
                    commands.add(param);
                }
            }
        }

        return commands;
    }

    /**
     * Answer "YES" to the GPL agreement prompt on Geth init
     *
     * NOTE: In our "meth" build, this prompt is complete disabled
     *
     * @param process
     * @throws IOException
     */
    private void answerLegalese(Process process) throws IOException {
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            boolean flag = scanner.hasNext();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                while (flag) {
                    String readline = scanner.next();
                    if (readline.isEmpty()) {
                        continue;
                    }
                    if (readline.contains("[y/N]")) {
                        writer.write("y");
                        writer.flush();
                        writer.newLine();
                        writer.flush();
                        flag = false;
                    }
                }
            }
        }
    }

    private boolean checkWalletUnlocked() {
        WalletService wallet = applicationContext.getBean(WalletService.class);
        List<Account> accounts = null;
        try {
            accounts = wallet.list();
        } catch (APIException e) {
            LOG.warn("Failed to list wallet accounts", e);
            return false;
        }


        long timeStart = System.currentTimeMillis();
        long timeout = 2000 * accounts.size(); // 2 sec per account

        for (Account account : accounts) {
            while (true) {
                try {
                    if (wallet.isUnlocked(account.getAddress())) {
                        LOG.debug("Account " + account.getAddress() + " unlocked");
                        break;
                    }
                } catch (APIException e) {
                    LOG.debug("Address " + account.getAddress() + " is not unlocked", e);
                }

                if (System.currentTimeMillis() - timeStart >= timeout) {
                    LOG.error("Wallet did not unlock in a timely manner");
                    return false;
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }

        LOG.info("Geth wallet accounts unlocked");
        return true;
    }

    private boolean checkGethStarted() {
        long timeStart = System.currentTimeMillis();

        while (true) {
            if (checkConnection()) {
                LOG.info("Geth RPC endpoint is up");
                return true;
            }

            if (System.currentTimeMillis() - timeStart >= gethConfig.getGethStartTimeout()) {
                // Something went wrong and RPC did not start within timeout
                LOG.error("Geth did not start within " + gethConfig.getGethStartTimeout() + "ms");
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }

        return false;
    }

    private Boolean checkConnection() {

        try {
            Map<String, Object> info = executeGethCall("admin_nodeInfo", null);
            if (info != null && StringUtils.isNotBlank((String) info.get("id"))) {
                return true;
            }
        } catch (APIException e) {
            LOG.debug("geth not yet up: " + e.getMessage());
        }
        return false;
    }

}
