package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ContractRegistrationTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContractRegistrationTask.class);

    @Autowired
    private ContractRegistryService contractRegistry;

    @Autowired
    private TransactionService transactionService;

    @Value("${contract.poll.delay.millis}")
    private Long pollDelayMillis;

    private final TransactionResult transactionResult;
    private final Contract contract;

    public ContractRegistrationTask(Contract contract, TransactionResult transactionResult) {
        this.contract = contract;
        this.transactionResult = transactionResult;
    }

    @Override
    public void run() {

        Transaction tx = null;

        LOG.debug("Waiting for contract to be comitted");

        while (tx == null) {
            try {
                tx = transactionService.waitForTx(
                        transactionResult, pollDelayMillis, TimeUnit.MILLISECONDS);

            } catch (APIException e) {
                LOG.warn("Error while waiting for contract to mine", e);
                // TODO add backoff delay if server is down?
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting for contract to mine", e);
                return;
            }
        }

        try {
            contract.setAddress(tx.getContractAddress());
            LOG.info("Registering newly mined contract at address " + contract.getAddress());
            contractRegistry.register(contract.getOwner(), tx.getContractAddress(), contract.getName(), contract.getABI(),
                    contract.getCode(), contract.getCodeType(), contract.getCreatedDate());
        } catch (APIException e) {
            LOG.warn("Failed to register contract at address " + tx.getContractAddress(), e);
        }

    }
}