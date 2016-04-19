package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WalletService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/wallet",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController extends BaseController {

    @Autowired
    WalletService walletService;

    @RequestMapping("/list")
    public ResponseEntity<APIResponse> getAccounts() throws APIException {

        APIResponse res = new APIResponse();
        APIData data = new APIData();

        List<Account> accounts = walletService.list();

        if(accounts != null){
            data.setAttributes(accounts);
            data.setType("wallet");
            res.setData(data);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Accounts not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

}
