package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContractRegistryServiceImpl.class);

    public static final String CONTRACT_REGISTRY_ADDRESS = "0x81635fe3d9cecbcf44aa58e967af1ab7ceefb817";

    @Autowired
    private ContractService contractService;

    private final ContractABI abi;

    public ContractRegistryServiceImpl() throws IOException {
        InputStream abiStream = getClass().getClassLoader().getResourceAsStream("contracts" + File.separator + "ContractRegistry.abi.json");
        this.abi = new ContractABI(abiStream);
    }

    @Override
    public TransactionResult register(String id, String name, String abi, String code, CodeType codeType, Long createdDate) throws APIException {
        return contractService.transact(
                CONTRACT_REGISTRY_ADDRESS, this.abi,
                "register",
                new Object[] { id, name, abi, code, codeType.toString(), createdDate });
    }

    @Override
    public Contract getById(String id) throws APIException {

        Object[] res = (Object[]) contractService.read(
                CONTRACT_REGISTRY_ADDRESS, this.abi,
                "getById",
                new Object[] { id });

        return new Contract(
                RpcUtil.addrToHex((BigInteger) res[0]),
                (String) res[2],
                (String) res[3],
                CodeType.valueOf((String) res[4]),
                null,
                ((BigInteger) res[5]).longValue());
    }

    @Override
    public Contract getByName(String name) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() throws APIException {

        Object[] res = (Object[]) contractService.read(
                CONTRACT_REGISTRY_ADDRESS, this.abi,
                "listAddrs", null);

        List<Contract> contracts = new ArrayList<>();
        for (int i = 0; i < res.length; i++) {
            BigInteger intAddr = (BigInteger) res[i];
            String addr = RpcUtil.addrToHex(intAddr);
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
