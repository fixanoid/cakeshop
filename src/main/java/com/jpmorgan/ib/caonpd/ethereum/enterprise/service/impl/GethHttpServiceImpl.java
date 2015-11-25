/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import java.io.BufferedReader;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;


/**
 *
 * @author I629630
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);

    @Value("${geth.url}")
    private String url;
    @Value("${geth.networkid}")
    private String networkid;
    @Value("${geth.datadir}")
    private String datadir;
    @Value("${geth.rpcport}")
    private String rpcport;
    @Value("${geth.rpcapi.list}")
    private String rpcApiList;
    @Value("${geth.auto.start:false}")
    private Boolean autoStart;
    @Value("${geth.genesis}")
    private String genesis;
    @Value("${geth.log}")
    private String logdir;

    @Override
    public String executeGethCall(String json) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, POST, httpEntity, String.class);
        return response.getBody();
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws JsonParseException, JsonMappingException, IOException {
        RequestModel request = new RequestModel("2.0", funcName, args, "id");

        Gson gson = new Gson();
        String req = gson.toJson(request);
        String response = executeGethCall(req);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(response, Map.class);

        // FIXME rpc error handling
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
        String root = this.getClass().getClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/", "");
        String genesisDir = root + genesis;
        Boolean isStarted;
        if (SystemUtils.IS_OS_WINDOWS) {
            isStarted = isProcessRunningWin(getProcessId());
        } else {
            isStarted = isProcessRunningNix(getProcessId());
        }
        if (!isStarted && autoStart) {
            isStarted = startGeth(root + "/", genesisDir, null, null);
            if (!isStarted) {
                LOG.error("Ethereum has NOT beed started...");
            } else {
                LOG.info("Ethereum started ...");
            }
        } else if(isStarted){
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
    public void deletePid() {
        String root = this.getClass().getClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/", "");
        File pidFile = new File(root + "../logs/meth.pid");
        pidFile.delete();
    }

    private Boolean startProcess(String command, String dataDir, String genesisDir, List<String> additionalParams, Boolean isWindows) {
        List<String> commands = Lists.newArrayList(command, "--datadir", dataDir, "--networkid", networkid, "--genesis",
                genesisDir, "--rpc", "--rpcport", rpcport, "--rpcapi", rpcApiList);

        if (null != additionalParams && !additionalParams.isEmpty()) {
            commands.addAll(additionalParams);
        }

        //String commands[] = {command, "--datadir", dataDir, "--networkid", networkid, "--genesis", genesisDir, "--rpc", "--rpcport", rpcport, "--rpcapi", rpcApiList};
        ProcessBuilder builder = new ProcessBuilder(commands);
        File file = new File(command);
        if (!file.canExecute()) {
            file.setExecutable(true);
        }

        builder.inheritIO();

        Process process;
        try {

            process = builder.start();
            File dataDirectory = new File(dataDir);

            /*
            if (!dataDirectory.exists()) {
                answerLegalese(process);
            }
            */

            if (!isWindows) {
                setUnixPID(process);
            } else {
                setWinPID(process);
            }

            TimeUnit.SECONDS.sleep(3);

            // FIXME add a watcher thread to make sure it doesn't die..

        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
            return false;
        }
        return true;
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
        String root = this.getClass().getClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/", "");
        File pidFile = new File(root + File.separator + ".." + File.separator + ".." + File.separator + "logs" + File.separator + "meth.pid");
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
                        LOG.info(line);
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
        String root = this.getClass().getClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/", "");
        File directory = new File(root + File.separator + ".." + File.separator + ".." + File.separator + "logs" + File.separator);
        File pidFile = new File(root + File.separator + ".." + File.separator + ".." + File.separator + "logs" + File.separator + "meth.pid");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!pidFile.exists()) {
            pidFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(pidFile)) {
            writer.write(String.valueOf(pid));
            writer.flush();
        }
    }
}
