package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.req.BlockGetCommand;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import feign.Headers;
import feign.RequestLine;

public interface BlockApi extends ApiClient.Api {

    /**
     * Retrieve a block by id, number or tag
     *
     * @param command
     *
     * @return APIResponse<APIData<Block>>
     */
    @RequestLine("POST /block/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<APIData<Block>, Block> get(BlockGetCommand command);

}
