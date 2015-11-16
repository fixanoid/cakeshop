/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterperise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterperise.service.GethHttpService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.testng.collections.Lists;

/**
 *
 * @author I629630
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    private Integer gethpid;

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
    @Value("${geth.log}")
    private String logdir;

    @Override
    public String executeGethCall(String json) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, POST, httpEntity, String.class);
        return response.getBody();
    }

    @Override
    public Boolean stopGeth() {
        if (SystemUtils.IS_OS_WINDOWS) {
            throw new IllegalArgumentException("Windows shutdows is not yet implemented");
        } else {
            try {
                Runtime.getRuntime().exec("kill -9 " + gethpid).waitFor();
                return true;
            } catch (IOException | InterruptedException ex) {
                LOG.error("Cannot shutdown process " + ex.getMessage());
                return false;
            }
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

    @Override
    public Boolean startGeth(String commandPrefix, String genesisDir, String eth_datadir, List <String> additionalParams) {
        Boolean started;
        if (StringUtils.isEmpty(eth_datadir)) {
            eth_datadir = datadir.startsWith("/.") ? System.getProperty("user.home") + datadir : datadir;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            //start Windows geth
            started = startProcess(commandPrefix + startWinCommand, eth_datadir, genesisDir, additionalParams);
        } else if (SystemUtils.IS_OS_LINUX) {
            //start *nix geth
            started = startProcess(commandPrefix + startXCommand, eth_datadir, genesisDir, additionalParams);
            LOG.info("Starting *nix");
        } else {
            //Default to Mac
            started = startProcess(commandPrefix + startMacCommand, eth_datadir, genesisDir, additionalParams);
            LOG.info("Starting *Mac");
        }
        return started;
    }

    private Boolean startProcess(String command, String dataDir, String genesisDir, List <String> additionalParams) {
        List <String> commands = Lists.newArrayList(command, "--datadir", dataDir, "--networkid", networkid, "--genesis", 
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
        Process process;
        try {
            process = builder.start();
            File dataDirectory = new File(dataDir);
            if (!dataDirectory.exists()) {
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
            setUnixPID(process);
            TimeUnit.SECONDS.sleep(3);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
            return false;
        }
        return true;
    }

    private void setUnixPID(Process process) {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Class cl = process.getClass();
                Field field = cl.getDeclaredField("pid");
                field.setAccessible(true);
                Object pidObject = field.get(process);
                gethpid = (Integer) pidObject;
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                LOG.error("Cannot get UNIX pid" + ex.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }
}
