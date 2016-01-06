package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    @Override
    public boolean register(String name, String abi, String code, CodeType codeType) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Contract getById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Contract getByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> listByOwner(String owner) {
        // TODO Auto-generated method stub
        return null;
    }

}
