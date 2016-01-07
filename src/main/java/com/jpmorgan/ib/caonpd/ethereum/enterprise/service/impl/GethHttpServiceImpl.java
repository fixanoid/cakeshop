package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    private final String PID_FILE = ROOT + File.separator + ".." + File.separator +  "meth.pid";
    
    //System.getProperty("user.dir");

    @Value("${geth.url}")
    private String url;
    @Value("${geth.networkid}")
    private String networkid;
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
            if (SystemUtils.IS_OS_WINDOWS) {
                Runtime.getRuntime().exec("taskkill /F /IM geth.exe").waitFor();
                return true;
            } else {
                Runtime.getRuntime().exec("kill -9 " + getProcessId()).waitFor();

                // wait for process to actually stop
                while (true) {
                    Process exec = Runtime.getRuntime().exec("kill -0 " + getProcessId());
                    exec.waitFor();
                    if (exec.exitValue() != 0) {
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(5);
                }

                return true;
            }
        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot shutdown process " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Boolean deletEthDatabase(String eth_datadir) {
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
        Boolean isStarted;
        if (SystemUtils.IS_OS_WINDOWS) {
            genesisDir = genesisDir.replaceAll(File.separator + File.separator, "/").replaceFirst("/", "");
            isStarted = isProcessRunningWin(getProcessId());
        } else {
            isStarted = isProcessRunningNix(getProcessId());
        }
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
            //start Windows geth
            started = startProcess(commandPrefix + startWinCommand, eth_datadir, genesisDir, additionalParams, true);
        } else if (SystemUtils.IS_OS_LINUX) {
            //start *nix geth
            started = startProcess(commandPrefix + startXCommand, eth_datadir, genesisDir, additionalParams, false);
            LOG.info("Starting *nix");
        } else {
            //Default to Mac
            started = startProcess(commandPrefix + startMacCommand, eth_datadir, genesisDir, additionalParams, false);
            LOG.info("Starting Mac");
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
            this.networkid = String.valueOf(networkid);
        }

        if (null != mining) {
            this.mining = mining;
        }

        if (null != verbosity) {
            this.verbosity = verbosity;
        }

        if (StringUtils.isNotEmpty(identity)) {
            this.identity = identity;
        }
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
                "--datadir", dataDir, "--networkid", networkid, "--genesis", genesisDir,
                //"--verbosity", "6",
                //"--mine", "--minerthreads", "1",
                "--solc", solcPath + File.separator + "solc",
                "--nat", "none", "--nodiscover",
                "--unlock", "0 1 2", "--password", passwordFile,
                "--rpc", "--rpcaddr", "127.0.0.1", "--rpcport", rpcport, "--rpcapi", rpcApiList);

        if (null != additionalParams && !additionalParams.isEmpty()) {
            commands.addAll(additionalParams);
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
                Collection<File> files = FileUtils.listFiles(new File(dataDir), FileFileFilter.FILE, TrueFileFilter.INSTANCE);
            }

            process = builder.start();

            if (!isWindows) {
                setUnixPID(process);
            } else {
                setWinPID(process);
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

    private void ensureFileIsExecutable(String filename) {
        File file = new File(filename);
        if (file.exists() && !file.canExecute()) {
            file.setExecutable(true);
        }
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

    private void setUnixPID(Process process) throws IOException {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Class<? extends Process> cl = process.getClass();
                Field field = cl.getDeclaredField("pid");
                field.setAccessible(true);
                Object pidObject = field.get(process);
                Integer gethpid = (Integer) pidObject;
                writePidToFile(gethpid);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                LOG.error("Cannot get UNIX pid" + ex.getMessage());
            }
        }
    }

    private void setWinPID(Process proc) throws IOException {
        if (proc.getClass().getName().equals("java.lang.Win32Process")
                || proc.getClass().getName().equals("java.lang.ProcessImpl")) {
            try {
                Field f = proc.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(proc);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                Integer pid = kernel.GetProcessId(handle);
                writePidToFile(pid);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private String getProcessId() {
        File pidFile = new File(PID_FILE);
        String pid = null;
        try {
            try (FileReader reader = new FileReader(pidFile); BufferedReader br = new BufferedReader(reader)) {
                String s;
                while ((s = br.readLine()) != null) {
                    pid = s;
                    break;
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return pid;
    }

    private Boolean isProcessRunningNix(String pid) {
        Boolean isRunning = false;
        if (StringUtils.isNotEmpty(pid)) {
            try {
                Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "ps aux | grep " + pid});
                try (InputStream stream = proc.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    //Parsing the input stream.
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("genesis_block.json")) {
                            isRunning = true;
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return isRunning;
    }

    private Boolean isProcessRunningWin(String pid) {
        Boolean isRunning = false;
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""});
            try (InputStream stream = proc.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                //Parsing the input stream.
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" " + pid + " ")) {
                        isRunning = true;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return isRunning;
    }

    private void writePidToFile(Integer pid) throws IOException {
        LOG.info("Creating pid file :" + PID_FILE);
        File pidFile = new File(PID_FILE);
        if (!pidFile.exists()) {
            pidFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(pidFile)) {
            writer.write(String.valueOf(pid));
            writer.flush();
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
