package com.jpmorgan.ib.caonpd.cakeshop.controller;

import com.jpmorgan.ib.caonpd.cakeshop.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIError;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionResult;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

@RestController
@RequestMapping(value = "/api/transaction",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController extends BaseController {

    @Autowired
    private TransactionService transactionService;
    

    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getTransaction(
            @JsonBodyParam(required=true) String id) throws APIException {

        Transaction tx = transactionService.get(id);

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Transaction not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
    
    @RequestMapping("/list")
    public ResponseEntity<APIResponse> getTransactionList(
            @JsonBodyParam(required=true) List<String> ids) throws APIException {

        List <Transaction> txns = transactionService.get(ids);
        List <APIData> data = new ArrayList<>();
        APIResponse res = new APIResponse();

        if (txns != null && !txns.isEmpty()) {
            for (Transaction txn : txns) {
                data.add(txn.toAPIData());                
            }
            res.setData(data);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Transaction not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
    
    @RequestMapping("/save")
    public WebAsyncTask<ResponseEntity<APIResponse>> transact(
            @JsonBodyParam final String from,
            @JsonBodyParam final String to,
            @JsonBodyParam final String data,
            @JsonBodyParam final List<String> geminiTo) throws APIException {

        Callable<ResponseEntity<APIResponse>> callable = new Callable<ResponseEntity<APIResponse>>() {
            @Override
            public ResponseEntity<APIResponse> call() throws Exception {
                DirectTransactionRequest req =  new DirectTransactionRequest(from, to, data, false);
                req.setGeminiTo(geminiTo);
                
                TransactionResult result = transactionService.directTransact(req);
                APIResponse res = new APIResponse();
                res.setData(result.toAPIData());
                ResponseEntity<APIResponse> response = new ResponseEntity<>(res, HttpStatus.OK);
                return response;
            }
        };
        WebAsyncTask asyncTask = new WebAsyncTask(callable);
        return asyncTask;
    }
    

}
