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
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
    @Value("${geth.genesis}")
    private String genesis;
    @Value("${geth.rpcport}")
    private String rpcport;
    @Value("${geth.rpcapi.list}")
    private String rpcApiList;
    @Value("${geth.log}")
    private String logdir;
    

    //TODO: Method to add coinbase upon start of the app. @PostCreate
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
    public void startGeth(String command, String genesisDir) {
        String eth_datadir = datadir.startsWith("/.") ? System.getProperty("user.home") + datadir : datadir;
        if (SystemUtils.IS_OS_WINDOWS) {
            //start Windows geth
            startProcess(command, eth_datadir, genesisDir);
        } else {
            //start *nix geth
            startProcess(command, eth_datadir, genesisDir);
            LOG.info("Starting *nix");
        }
    }

    private void startProcess(String command, String dataDir, String genesisDir) {
        String commands[] = {command, "--datadir", dataDir, "--networkid", networkid, "--genesis", genesisDir, "--rpc", "--rpcport", 
            rpcport, "--rpcapi", rpcApiList};
        ProcessBuilder builder = new ProcessBuilder(commands);
        try {
            Process process = builder.start();
            process.waitFor(3, TimeUnit.SECONDS);
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
        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
        }
    }
}
