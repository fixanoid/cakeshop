package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface TransactionApi extends ApiClient.Api {

    /**
     * Retrieve a transaction by id
     *
     * @param id
     *
     * @return APIResponse<APIData<Transaction>>
     */
    @RequestLine("POST /transaction/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<APIData<Transaction>, Transaction> get(@Param("id") String id);

}
