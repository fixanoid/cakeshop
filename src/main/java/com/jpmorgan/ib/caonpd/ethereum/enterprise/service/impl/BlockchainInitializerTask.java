package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

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
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NodeService nodeService;

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
            LOG.debug("Turning on mining");
            nodeService.update(null, null, null, true, null, null);

            LOG.info("Deploying ContractRegistry to chain");
            contractRegistry.deploy();

            LOG.debug("Turning off mining");
            //nodeService.update(null, null, "", false);

        } catch (APIException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }
    }

}
