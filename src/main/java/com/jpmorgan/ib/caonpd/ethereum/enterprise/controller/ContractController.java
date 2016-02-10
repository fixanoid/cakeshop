package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/contract",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractController extends BaseController {

    @Autowired
    private ContractService contractService;

    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getContract(
            @JsonBodyParam String id) throws APIException {

        Contract contract = contractService.get(id);

        APIResponse res = new APIResponse();

        if (contract != null) {
            res.setData(toAPIData(contract));
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Contract not found");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND);
    }

    @RequestMapping("/compile")
    public ResponseEntity<APIResponse> compile(
            @JsonBodyParam String code,
            @JsonBodyParam(required=false) String code_type,
            @JsonBodyParam(required=false) Boolean optimize) throws APIException {

        List<Contract> contracts = contractService.compile(code, CodeType.valueOf(code_type), optimize);

        APIResponse res = new APIResponse();

        if (contracts != null) {
            res.setData(toAPIData(contracts));
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/create")
    public ResponseEntity<APIResponse> create(
            @JsonBodyParam String code,
            @JsonBodyParam String code_type) throws APIException {

        TransactionResult tx = contractService.create(code, CodeType.valueOf(code_type));

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

    @RequestMapping("/list")
    public ResponseEntity<APIResponse> list() throws APIException {
        List<Contract> contracts = contractService.list();
        APIResponse res = new APIResponse();
        res.setData(toAPIData(contracts));

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("/read")
    public ResponseEntity<APIResponse> read(
            @JsonBodyParam String id,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args) throws APIException {

        Object result = contractService.read(id, method, args);
        APIResponse res = new APIResponse();
        res.setData(result);

        return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
    }

    @RequestMapping("/transact")
    public ResponseEntity<APIResponse> transact(
            @JsonBodyParam String id,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args) throws APIException {

        RpcUtil.puts(id);
        RpcUtil.puts(method);
        RpcUtil.puts(args);

        TransactionResult tr = contractService.transact(id, method, args);
        APIResponse res = new APIResponse();
        res.setData(tr.toAPIData());

        return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
    }

    private APIData toAPIData(Contract c) {
        return new APIData(c.getAddress(), Contract.API_DATA_TYPE, c);
    }

    private List<APIData> toAPIData(List<Contract> contracts) {
        List<APIData> data = new ArrayList<>();
        for (Contract c : contracts) {
           data.add(toAPIData(c));
        }
        return data;
    }

}
