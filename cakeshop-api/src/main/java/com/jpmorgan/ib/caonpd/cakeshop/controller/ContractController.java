package com.jpmorgan.ib.caonpd.cakeshop.controller;

import com.jpmorgan.ib.caonpd.cakeshop.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIError;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionResult;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Function;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService.CodeType;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
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

	@Autowired
	private ContractRegistryService contractRegistry;

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
            @JsonBodyParam String from,
            @JsonBodyParam String code,
            @JsonBodyParam String code_type,
            @JsonBodyParam Object[] args,
            @JsonBodyParam String binary,
            @JsonBodyParam(required=false) Boolean optimize) throws APIException {

        TransactionResult tx = contractService.create(from, code, CodeType.valueOf(code_type), args, binary);

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
            @JsonBodyParam String from,
            @JsonBodyParam String id,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args,
            @JsonBodyParam(required=false) Object blockNumber) throws APIException {

        ContractABI abi = lookupABI(id);
        args = decodeArgs(abi.getFunction(method), args);

        Object result = contractService.read(id, abi, from, method, args, blockNumber);
        APIResponse res = new APIResponse();
        res.setData(result);

        return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
    }

    private Object[] decodeArgs(Function method, Object[] args) throws APIException {
        if (args == null || args.length == 0) {
            return args;
        }

        List<Param> params = method.inputs;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Param param = params.get(i);
            if (param.type instanceof Bytes32Type && arg instanceof String) {
                args[i] = new String(Base64.decode((String) arg));
            }
        }

        return args;
    }

    @RequestMapping("/transact")
    public ResponseEntity<APIResponse> transact(
            @JsonBodyParam String from,
            @JsonBodyParam String id,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args) throws APIException {

        ContractABI abi = lookupABI(id);
        args = decodeArgs(abi.getFunction(method), args);

        TransactionResult tr = contractService.transact(id, abi, from, method, args);
        APIResponse res = new APIResponse();
        res.setData(tr.toAPIData());

        return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
    }

    @RequestMapping("/transactions/list")
    public ResponseEntity<APIResponse> listTransactions(
            @JsonBodyParam String id) throws APIException {

        List<Transaction> txns = contractService.listTransactions(id);

        List<APIData> data = new ArrayList<>();
        for (Transaction tx : txns) {
            data.add(tx.toAPIData());
        }

        APIResponse res = new APIResponse();
        res.setData(data);

        return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
    }


    private APIData toAPIData(Contract c) {
        return new APIData(c.getId(), Contract.API_DATA_TYPE, c);
    }

    private List<APIData> toAPIData(List<Contract> contracts) {
        List<APIData> data = new ArrayList<>();
        for (Contract c : contracts) {
           data.add(toAPIData(c));
        }
        return data;
    }

    private ContractABI lookupABI(String id) throws APIException {
        Contract contract = contractRegistry.getById(id);
        if (contract == null) {
            throw new APIException("Contract not in registry " + id);
        }

        return ContractABI.fromJson(contract.getABI());
    }


}
