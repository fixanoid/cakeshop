package com.jpmorgan.ib.caonpd.cakeshop.client;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient.Api;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.BlockApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.ContractApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.NodeApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.TransactionApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.WalletApi;

import java.util.HashMap;
import java.util.Map;

public class ClientManager {

    private ApiClient apiClient;

    private final Map<Class<? extends Api>, Api> clients;

    @SuppressWarnings("unchecked")
    private static final Class<? extends Api>[] API_LIST = new Class[] {
            BlockApi.class, ContractApi.class, NodeApi.class,
            TransactionApi.class, WalletApi.class };

    public ClientManager() {
        this(new ApiClient());
    }

    public ClientManager(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.clients = new HashMap<>();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void init() {
        for (Class clazz : API_LIST) {
            this.clients.put(clazz, apiClient.buildClient(clazz));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Api> T getClient(Class<T> clientClass) {
        return (T) clients.get(clientClass);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

}
