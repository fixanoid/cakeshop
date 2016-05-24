package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import com.jpmorgan.ib.caonpd.cakeshop.dao.WalletDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Account;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.service.BlockService;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.cakeshop.service.NodeService;
import com.jpmorgan.ib.caonpd.cakeshop.service.WalletService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BlockchainInitializerTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainInitializerTask.class);

    @Autowired
    private BlockService blockService;

    @Autowired
    private ContractRegistryService contractRegistry;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletDAO walletDAO;

    @Override
    public void run() {

        try {
            Block block = blockService.get(null, null, "latest");
            if (block.getNumber() > 0) {
                LOG.warn("Blockchain not on block zero (on #" + block.getNumber() + "); not initializing");
                return;
            }

        } catch (APIException e) {
            LOG.error("Error reading block number: " + e.getMessage());
            LOG.error("Unable to initialize");
            return;
        }


        LOG.info("Initializing empty blockchain");
        try {
            nodeService.update(null, null, null, true, null, null);

            LOG.info("Deploying ContractRegistry to chain");
            contractRegistry.deploy();

        } catch (APIException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }


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
