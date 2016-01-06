package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

import java.util.List;

public interface ContractRegistryService {

    public boolean register(String name, String abi, String code, CodeType codeType);

    public Contract getById(String id);

    public Contract getByName(String name);

    public List<Contract> list();

    public List<Contract> listByOwner(String owner);

}
