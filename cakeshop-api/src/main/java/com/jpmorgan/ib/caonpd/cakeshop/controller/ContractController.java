package com.jpmorgan.ib.caonpd.cakeshop.controller;

import com.jpmorgan.ib.caonpd.cakeshop.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIError;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Function;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionRequest;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionResult;
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

    private static final String DEFAULT_CODE_TYPE = "solidity";

    @Autowired
    private ContractService contractService;

	@Autowired
	private ContractRegistryService contractRegistry;

    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getContract(
            @JsonBodyParam String address) throws APIException {

        Contract contract = contractService.get(address);

        APIResponse res = new APIResponse();

        if (contract != null) {
            res.setData(toAPIData(contract));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Contract not found");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @RequestMapping("/compile")
    public ResponseEntity<APIResponse> compile(
            @JsonBodyParam String code,
            @JsonBodyParam(defaultValue=DEFAULT_CODE_TYPE) String code_type,
            @JsonBodyParam(required=false) Boolean optimize) throws APIException {

        List<Contract> contracts = contractService.compile(code, CodeType.valueOf(code_type), optimize);

        APIResponse res = new APIResponse();

        if (contracts != null) {
            res.setData(toAPIData(contracts));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/create")
    public ResponseEntity<APIResponse> create(
            @JsonBodyParam String from,
            @JsonBodyParam String code,
            @JsonBodyParam(defaultValue=DEFAULT_CODE_TYPE) String code_type,
            @JsonBodyParam(required=false) Object[] args,
            @JsonBodyParam(required=false) String binary,
            @JsonBodyParam(required=false) Boolean optimize) throws APIException {

        TransactionResult tx = contractService.create(from, code, CodeType.valueOf(code_type), args, binary);

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
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
            @JsonBodyParam String address,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args,
            @JsonBodyParam(required=false) Object blockNumber) throws APIException {

        Object result = contractService.read(createTransactionRequest(from, address, method, args, true, blockNumber));
        APIResponse res = new APIResponse();
        res.setData(result);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private TransactionRequest createTransactionRequest(String from, String address, String method, Object[] args, boolean isRead, Object blockNumber) throws APIException {
        ContractABI abi = contractService.get(address).getContractAbi();
        if (abi == null) {
            throw new APIException("Contract adddress " + address + " is not in the registry");
        }

        Function func = abi.getFunction(method);
        if (func == null) {
            throw new APIException("Method '" + method + "' does not exist in contract at " + address);
        }

        args = decodeArgs(func, args);

        return new TransactionRequest(from, address, abi, method, args, isRead);
    }

    /**
     * Handle Base64 encoded byte/string inputs (byte arrays must be base64 encoded to put them on
     * the wire w/ JSON)
     *
     * @param method
     * @param args
     * @return
     * @throws APIException
     */
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
            @JsonBodyParam String address,
            @JsonBodyParam String method,
            @JsonBodyParam Object[] args,
            @JsonBodyParam List<String> geminiTo) throws APIException {

        TransactionRequest req = createTransactionRequest(from, address, method, args, false, null);
        req.setGeminiTo(geminiTo);

        TransactionResult tr = contractService.transact(req);
        APIResponse res = new APIResponse();
        res.setData(tr.toAPIData());

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("/transactions/list")
    public ResponseEntity<APIResponse> listTransactions(
            @JsonBodyParam String address) throws APIException {

        List<Transaction> txns = contractService.listTransactions(address);

        List<APIData> data = new ArrayList<>();
        for (Transaction tx : txns) {
            data.add(tx.toAPIData());
        }

        APIResponse res = new APIResponse();
        res.setData(data);

        return new ResponseEntity<>(res, HttpStatus.OK);
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
