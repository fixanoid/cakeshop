package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

import java.util.List;

public interface ContractRegistryService {

    public boolean deploy() throws APIException;

    public TransactionResult register(String id, String name, String abi, String code, CodeType codeType, Long createdDate) throws APIException;

    public Contract getById(String id) throws APIException;

    public Contract getByName(String name) throws APIException;

    public List<Contract> list() throws APIException;

    public List<Contract> listByOwner(String owner) throws APIException;

}
