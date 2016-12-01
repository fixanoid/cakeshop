package com.jpmorgan.cakeshop.service.task;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.BlockService;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BlockchainInitializerTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainInitializerTask.class);

    @Autowired
    private BlockService blockService;

    @Autowired
    private GethHttpService geth;

    @Autowired
    private ContractRegistryService contractRegistry;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletDAO walletDAO;

    @Value("${contract.registry.addr:}")
    private String contractRegistryAddress;

    @Autowired
    private GethConfigBean gethConfig;

    @Override
    public void run() {

        if (StringUtils.isNotBlank(contractRegistryAddress)) {
            Map<String, Object> contractRes;
            try {
                contractRes = geth.executeGethCall("eth_getCode", new Object[]{contractRegistryAddress, "latest"});
            } catch (APIException e) {
                LOG.error("Failed to verify contract " + contractRegistryAddress, e);
                return;
            }

            String binaryCode = (String) contractRes.get("_result");
            if (!binaryCode.contentEquals("0x")) {
                return; // already exists on chain
            }

            // continue to deploy
        }

        LOG.info("Initializing empty blockchain");
        try {
            nodeService.update(null, null, null, true, null, null); // make sure mining is enabled

            LOG.info("Deploying ContractRegistry to chain");
            contractRegistry.deploy();

        } catch (APIException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }


        LOG.info("Deploying sample contract (SimpleStorage) to chain");
        try {
            String code = FileUtils.readClasspathFile("contracts/SimpleStorage.sol");
            TransactionResult txr = contractService.create(null, code, CodeType.solidity, null, null, null, null);
            transactionService.waitForTx(txr, 200, TimeUnit.MILLISECONDS);

        } catch (IOException | InterruptedException e) {
            LOG.error("Error deploying SimpleStorage to chain: " + e.getMessage(), e);
        }


        if (gethConfig.isDbEnabled()) {
            LOG.info("Storing existing wallet accounts");
            try {
                List<Account> list = walletService.list();
                for (Account account : list) {
                    walletDAO.save(account);
                }
            } catch (APIException e) {
                LOG.error("Error reading local wallet", e);
            }
        }

    }

}
