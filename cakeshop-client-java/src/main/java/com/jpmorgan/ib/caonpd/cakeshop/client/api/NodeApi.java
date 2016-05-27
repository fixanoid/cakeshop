package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Node;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import feign.Headers;
import feign.RequestLine;

public interface NodeApi extends ApiClient.Api {

    /**
     * Get node info and status
     *
     * @return APIResponse<APIData<Node>>
     */
    @RequestLine("POST /node/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<APIData<Node>, Node> get();

}
