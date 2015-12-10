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
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

@RestController
@RequestMapping(value = "/api/contract",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractController {

    @Autowired
    private ContractService contractService;

    @RequestMapping(value = "/get")
    public ResponseEntity<APIResponse> getContract(
            @JsonBodyParam String address) throws APIException {

        Contract contract = contractService.get(address);

        APIResponse res = new APIResponse();

        if (contract != null) {
            res.setData(contract.toAPIData());
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Contract not found");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/create")
    public ResponseEntity<APIResponse> create(
            @JsonBodyParam(required=false) String abi,
            @JsonBodyParam String code,
            @JsonBodyParam String code_type) throws APIException {

        TransactionResult tx = contractService.create(abi, code, CodeType.valueOf(code_type));

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.BAD_REQUEST);
    }

}
