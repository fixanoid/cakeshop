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
    public void startGeth() {
        if (SystemUtils.IS_OS_WINDOWS) {
            //start Windows geth
            startProcess(startWinCommand, "/Users/I629630/.ethereum_test");
        } else {
            //start *nix geth
            startProcess(startXCommand, "/Users/I629630/.ethereum_test");
            LOG.info("Starting *nix");
        }
    }

    private void startProcess(String command, String dataDir) {
        String commands[] = {command, "--datadir", dataDir, "--networkid", "1006", "--genesis", "/Users/I629630/genesis/genesis_block.json",
            "--rpc", "--rpcport", "8102", "--rpcapi", "admin,db,eth,debug,miner,net,shh,txpool,personal,web3", "--logfile", "/Users/I629630/log/geth.log"};
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
