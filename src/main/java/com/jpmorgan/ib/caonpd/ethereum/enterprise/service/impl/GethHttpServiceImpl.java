package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final String PID_FILE = ROOT + File.separator + ".." + File.separator +  "meth.pid";

    //System.getProperty("user.dir");

    @Value("${geth.url}")
    private String url;
    @Value("${geth.networkid}")
    private Integer networkid;
    @Value("${geth.datadir}")
    private String datadir;

    @Value("${geth.node.port:30303}")
    private String gethNodePort;

    @Value("${geth.rpcport}")
    private String rpcport;
    @Value("${geth.rpcapi.list}")
    private String rpcApiList;
    @Value("${geth.auto.start:false}")
    private Boolean autoStart;
    @Value("${geth.auto.stop:false}")
    private Boolean autoStop;
    @Value("${geth.genesis}")
    private String genesis;
    @Value("${geth.log}")
    private String logdir;
    @Value("${geth.verbosity:}")
    private Integer verbosity;
    @Value("${geth.mining:}")
    private Boolean mining;
    @Value("${geth.identity:}")
    private String identity;

    @Override
    public String executeGethCall(String json) throws APIException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, POST, httpEntity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException {

        RequestModel request = new RequestModel(GethHttpService.GETH_API_VERSION, funcName, args, GethHttpService.USER_ID);
        String req = new Gson().toJson(request);
        String response = executeGethCall(req);

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(response.trim());
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data;
        try {
            data = mapper.readValue(response, Map.class);
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }

        if (data.containsKey("error") && data.get("error") != null) {
            String message;
            Map<String, String> error = (Map<String, String>) data.get("error");
            if (error.containsKey("message")) {
                message = error.get("message");
            } else {
                message = "RPC call failed";
            }
            throw new APIException(message);
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
    public Boolean stopGeth() {
        try {
            return killProcess(readPidFromFile(PID_FILE), "geth.exe");
        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot shutdown process " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Boolean deleteEthDatabase(String eth_datadir) {
        if (StringUtils.isEmpty(eth_datadir)) {
            eth_datadir = datadir.startsWith("/.") ? System.getProperty("user.home") + datadir : datadir;
        }
        try {
            FileUtils.deleteDirectory(new File(eth_datadir));
            return true;
        } catch (IOException ex) {
            LOG.error("Cannot delete directory " + ex.getMessage());
            return false;

        }
    }

    @PostConstruct
    public void autoStart() {
        if (!autoStart) {
            return;
        }
        LOG.info("Autostarting geth node");
        start();

    }

    //To restart geth when properties has been changed
    @Override
    public void start() {
        String genesisDir = ROOT + genesis;
        if (SystemUtils.IS_OS_WINDOWS) {
            genesisDir = genesisDir.replaceAll(File.separator + File.separator, "/").replaceFirst("/", "");
        }

        boolean isStarted = isProcessRunning(readPidFromFile(PID_FILE));
        if (!isStarted) {
            isStarted = startGeth(ROOT + File.separator, genesisDir, null, null);
            if (!isStarted) {
                LOG.error("Ethereum has NOT been started...");
            } else {
                LOG.info("Ethereum started ...");
            }
        } else if (isStarted) {
            LOG.info("Ethereum was already running");
        }
    }

    @Override
    public Boolean startGeth(String commandPrefix, String genesisDir, String eth_datadir, List<String> additionalParams) {
        Boolean started;
        if (StringUtils.isEmpty(eth_datadir)) {
            eth_datadir = datadir.startsWith("/.") ? System.getProperty("user.home") + datadir : datadir;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            LOG.info("Starting geth for windows");
            started = startProcess(commandPrefix + startWinCommand, eth_datadir, genesisDir, additionalParams, true);
        } else if (SystemUtils.IS_OS_LINUX) {
            LOG.info("Starting geth for linux");
            started = startProcess(commandPrefix + startXCommand, eth_datadir, genesisDir, additionalParams, false);
        } else {
            //Default to Mac
            LOG.info("Starting geth for mac");
            started = startProcess(commandPrefix + startMacCommand, eth_datadir, genesisDir, additionalParams, false);
        }
        return started;
    }

    @Override
    public Boolean deletePid() {
        File pidFile = new File(PID_FILE);
        Boolean deleted = pidFile.delete();
        return deleted;
    }

    @Override
    public void setNodeInfo(String identity, Boolean mining, Integer verbosity, Integer networkid) {
        if (null != networkid) {
            this.networkid = networkid;
        }

        if (null != mining) {
            this.mining = mining;
        }

        if (null != verbosity) {
            this.verbosity = verbosity;
        }else if(null == this.verbosity){
            this.verbosity = 0;
        }

        if (StringUtils.isNotEmpty(identity)) {
            this.identity = identity;
        }else if(null == this.identity){
            this.identity = "";
        }

    }

    @Override
    public NodeInfo getNodeInfo(){

        return new NodeInfo(this.identity,this.mining,this.networkid,this.verbosity);
    }

    private String getNodeIdentity() throws APIException{
        Map<String, Object> data = null;

        data = this.executeGethCall(AdminBean.ADMIN_NODE_INFO, new Object[]{ null, true });

        if(data != null)
            return (String)data.get("Name");

        return null;
    }

    @PreDestroy
    protected void autoStop () {
        if (autoStop) {
            stopGeth();
            deletePid();
        }
    }

    private Boolean startProcess(String command, String dataDir, String genesisDir, List<String> additionalParams, Boolean isWindows) {
        String ipcPipe = isWindows ? "\\\\.\\pipe\\geth-" + System.getProperty("eth.environment") + ".ipc" : dataDir + File.separator +  "geth.ipc";
        String passwordFile = new File(genesisDir).getParent() + File.separator + "geth_pass.txt";
        Boolean started;

        String nodePath = new File(command).getParent() + File.separator;
        String solcPath = new File(genesisDir).getParentFile().getParent() + File.separator + "solc" + File.separator + "node_modules" + File.separator + ".bin";
        ensureNodeBins(nodePath, solcPath);

        List<String> commands = Lists.newArrayList(command,
                "--port", gethNodePort, "--ipcpath", ipcPipe,
                "--datadir", dataDir, "--genesis", genesisDir,
                //"--verbosity", "6",
                //"--mine", "--minerthreads", "1",
                "--solc", solcPath + File.separator + "solc",
                "--nat", "none", "--nodiscover",
                "--unlock", "0 1 2", "--password", passwordFile,
                "--rpc", "--rpcaddr", "127.0.0.1", "--rpcport", rpcport, "--rpcapi", rpcApiList);

        if (null != additionalParams && !additionalParams.isEmpty()) {
            commands.addAll(additionalParams);
        }

        if(null != this.networkid) {
            commands.add("--networkid");
            commands.add(String.valueOf(networkid));
        }else{
            commands.add("--networkid");
            commands.add(String.valueOf(DEFAULT_NETWORK_ID));
        }

        if(null != this.verbosity) {
            commands.add("--verbosity");
            commands.add(String.valueOf(verbosity));
        }
        if (null != mining) {
           commands.add("--mine");
           commands.add("--minerthreads");
           commands.add("1");
        }
        if (StringUtils.isNotEmpty(identity)) {
            commands.add("--identity");
            commands.add(identity);
        }
        ProcessBuilder builder = new ProcessBuilder(commands);
        ensureFileIsExecutable(command);

        // need to modify PATH so it can locate compilers correctly
        final Map<String, String> env = builder.environment();
        String envPath = nodePath + File.pathSeparator + solcPath;
        if (env.get("PATH") != null && !env.get("PATH").trim().isEmpty()) {
        	 envPath = envPath + File.pathSeparator + env.get("PATH").trim();
        }
        env.put("PATH", envPath);

        builder.inheritIO();

        Process process;
        try {
            File dataDirectory = new File(dataDir);

            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            File keystoreDir = new File(dataDir + File.separator + "keystore");
            if (!keystoreDir.exists()) {
                String keystoreSrcPath = new File(genesisDir).getParent() + File.separator + "keystore";
                FileUtils.copyDirectory(new File(keystoreSrcPath), new File(dataDir + File.separator + "keystore"));
                //Collection<File> files = FileUtils.listFiles(new File(dataDir), FileFileFilter.FILE, TrueFileFilter.INSTANCE);
            }

            process = builder.start();

            Integer pid = getProcessPid(process);
            if (pid != null) {
                writePidToFile(pid, PID_FILE);
            }

            started = checkGethStarted();

            // FIXME add a watcher thread to make sure it doesn't die..

        } catch (IOException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
            started = false;
            return started;
        }
        return started;
    }

    /**
     * Make sure all node bins are executable, both for win & mac/linux
     * @param nodePath
     * @param solcPath
     */
    private void ensureNodeBins(String nodePath, String solcPath) {
        ensureFileIsExecutable(nodePath + File.separator + "node");
        ensureFileIsExecutable(nodePath + File.separator + "node.exe");
        ensureFileIsExecutable(solcPath + File.separator + "solc");
        ensureFileIsExecutable(solcPath + File.separator + "solc.cmd");
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

    private Boolean checkGethStarted() {

        long timeStart = System.currentTimeMillis();
        Boolean started = false;

        while (true) {
            try {
                if (checkConnection()) {
                    LOG.info("Geth started up successfully");
                    started = true;
                    break;
                }
            } catch (IOException ex) {
                LOG.debug(ex.getMessage());
                if (System.currentTimeMillis() - timeStart >= 10000) {
                    // Something went wrong and RPC did not start within 10
                    // sec
                    LOG.error("Geth did not start within 10 seconds");
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        return started;
    }

    private Boolean checkConnection() throws IOException {
        Boolean connected = false;
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                connected = true;
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage());
        }
        return connected;
    }
}
