package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import java.util.List;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface ContractApi extends ApiClient.Api {

    @RequestLine("POST /contract/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<Contract>, Contract> get(@Param("id") String id);

    @RequestLine("POST /contract/compile")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Contract>>, Contract> compile(ContractCompileCommand command);

    @RequestLine("POST /contract/create")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<Contract>, Contract> create(ContractCreateCommand command);

    @RequestLine("POST /contract/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Contract>>, Contract> list();


    @RequestLine("POST /contract/read")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<Contract>, Contract> read(ContractMethodCallCommand call);

    @RequestLine("POST /contract/transact")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<Contract>, Contract> transact(ContractMethodCallCommand call);

    @RequestLine("POST /contract/transactions/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Transaction>>, Transaction> listTransactions(@Param("id") String id);

}
