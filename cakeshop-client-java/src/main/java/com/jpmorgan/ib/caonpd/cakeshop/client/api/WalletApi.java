package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Account;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import java.util.List;

import feign.Headers;
import feign.RequestLine;

public interface WalletApi extends ApiClient.Api {

    /**
     * Retrieve a list of accounts in the wallet
     *
     * @return APIResponse<APIData<List<Account>>>
     */
    @RequestLine("POST /wallet/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<List<APIData<Account>>, Account> list();

    /**
     * Create a new account in the wallet
     *
     * @return APIResponse<APIData<Account>>
     */
    @RequestLine("POST /wallet/create")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<APIData<Account>, Account> create();

}
