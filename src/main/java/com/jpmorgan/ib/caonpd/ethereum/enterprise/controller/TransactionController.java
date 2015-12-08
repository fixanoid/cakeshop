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
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

@RestController
@RequestMapping(value = "/transaction",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @RequestMapping(value = "/get")
    public ResponseEntity<APIResponse> getTransaction(
            @JsonBodyParam(required=true) String address) throws APIException {

        Transaction tx = transactionService.get(address);

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Transaction not found");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND);
    }

}
