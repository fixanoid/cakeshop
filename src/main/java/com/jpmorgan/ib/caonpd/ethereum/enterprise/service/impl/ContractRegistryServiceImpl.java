package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContractRegistryServiceImpl.class);

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Value("${contract.registry.addr:}")
    private String contractRegistryAddress;

    @Autowired
    private GethConfigBean gethConfig;

    private final ContractABI abi;

    public ContractRegistryServiceImpl() throws IOException {
        InputStream abiStream = getClass().getClassLoader().getResourceAsStream("contracts" + File.separator + "ContractRegistry.abi.json");
        this.abi = new ContractABI(abiStream);
    }

    @Override
    public boolean deploy() throws APIException {

        try {
            String code = FileUtils.readClasspathFile("contracts/ContractRegistry.sol");
            TransactionResult txr = contractService.create(code, CodeType.solidity, null, null);
            Transaction tx = transactionService.waitForTx(txr, 200, TimeUnit.MILLISECONDS);
            this.contractRegistryAddress = tx.getContractAddress();
            saveContractRegistryAddress(this.contractRegistryAddress);
            return true;

        } catch (IOException | InterruptedException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }

        return false;
    }

    private void saveContractRegistryAddress(String addr) throws APIException {
        try {
            gethConfig.setProperty("contract.registry.addr", addr);
            gethConfig.save();
        } catch (IOException e) {
            LOG.warn("Unable to update env.properties", e);
            throw new APIException("Unable to update env.properties", e);
        }
    }

    @Override
    public TransactionResult register(String id, String name, String abi, String code, CodeType codeType, Long createdDate) throws APIException {

        if (name.equalsIgnoreCase("ContractRegistry") ||
                this.contractRegistryAddress == null || this.contractRegistryAddress.isEmpty()) {

            LOG.warn("Not going to register contract since ContractRegistry address is null");

            return null; // FIXME return silently because registry hasn't yet been registered
        }

        return contractService.transact(
                contractRegistryAddress, this.abi,
                "register",
                new Object[] { id, name, abi, code, codeType.toString(), createdDate });
    }

    @Override
    public Contract getById(String id) throws APIException {

        Object[] res = contractService.read(
                contractRegistryAddress, this.abi,
                "getById",
                new Object[] { id },
                null);

        if (res == null || res.length < 6 || ArrayUtils.contains(res, null)) {
            return null; // extra null checks
        }

        long createdDate = ((BigInteger) res[5]).longValue();
        if (((String) res[0]).contentEquals("0x00") || createdDate == 0) {
            return null; // contract is not [yet] registered
        }

        return new Contract(
                (String) res[0],
                (String) res[1],
                (String) res[2],
                (String) res[3],
                CodeType.valueOf((String) res[4]),
                null,
                createdDate);
    }

    @Override
    public Contract getByName(String name) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() throws APIException {

        Object[] res = contractService.read(
                contractRegistryAddress, this.abi,
                "listAddrs", null, null);

        Object[] addrs = (Object[]) res[0];

        List<Contract> contracts = new ArrayList<>();
        for (int i = 0; i < addrs.length; i++) {
            String addr = (String) addrs[i];
            try {
                contracts.add(getById(addr));
            } catch (APIException ex) {
                LOG.warn("error loading contract details for " + addr, ex);
            }
        }

        return contracts;
    }

    @Override
    public List<Contract> listByOwner(String owner) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

}
