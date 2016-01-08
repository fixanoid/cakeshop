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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    public static final String CONTRACT_REGISTRY_ADDRESS = "0x81635fe3d9cecbcf44aa58e967af1ab7ceefb817";

    @Autowired
    private ContractService contractService;

    private final ContractABI abi;

    public ContractRegistryServiceImpl() throws IOException {
        InputStream abiStream = getClass().getClassLoader().getResourceAsStream("contracts" + File.separator + "ContractRegistry.abi.json");
        this.abi = new ContractABI(abiStream);
    }

    @Override
    public TransactionResult register(String id, String name, String abi, String code, CodeType codeType) throws APIException {
        return contractService.transact(
                CONTRACT_REGISTRY_ADDRESS, this.abi,
                "register",
                new Object[] { id, name, abi, code, codeType.toString() });
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
                null);
    }

    @Override
    public Contract getByName(String name) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> listByOwner(String owner) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

}
