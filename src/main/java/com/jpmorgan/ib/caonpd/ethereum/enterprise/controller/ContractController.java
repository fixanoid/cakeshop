package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

@RestController
@RequestMapping(value = "/contract",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractController {

    @Autowired
    private ContractService contractService;

    @RequestMapping(value = "/get")
    public ResponseEntity<Contract> getContract(
            @JsonBodyParam String address) throws APIException {

        Contract contract = contractService.get(address);
        return new ResponseEntity<Contract>(contract, HttpStatus.OK);
    }

    @RequestMapping(value = "/create")
    public ResponseEntity<TransactionResult> create(
            @JsonBodyParam(required=false) String abi,
            @JsonBodyParam String code,
            @JsonBodyParam String code_type) throws APIException {

        TransactionResult tx = contractService.create(abi, code, CodeType.valueOf(code_type));
        return new ResponseEntity<TransactionResult>(tx, HttpStatus.OK);
    }

}
