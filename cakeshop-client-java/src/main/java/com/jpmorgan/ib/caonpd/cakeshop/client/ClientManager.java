package com.jpmorgan.ib.caonpd.cakeshop.client;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient.Api;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.BlockApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.ContractApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.NodeApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.TransactionApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.api.WalletApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.ws.EventHandler;
import com.jpmorgan.ib.caonpd.cakeshop.client.ws.WebSocketClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ClientManager {

    private ApiClient apiClient;

    private WebSocketClient wsClient;

    private final Map<Class<? extends Api>, Api> clients;

    @SuppressWarnings("unchecked")
    private static final Class<? extends Api>[] API_LIST = new Class[] {
            BlockApi.class,
            ContractApi.class,
            NodeApi.class,
            TransactionApi.class,
            WalletApi.class
            };

    public static ClientManager create(String cakeshopBaseUri) {
        // create URIs
        URI cakeshopUri = URI.create(cakeshopBaseUri);
        int port = cakeshopUri.getPort() > 0 ? cakeshopUri.getPort() : 80;
        String scheme = cakeshopUri.getScheme();
        if (StringUtils.isBlank(scheme)) {
            if (port == 443 || port == 8443) {
                scheme = "https";
            } else {
                scheme = "http";
            }
        }

        String baseUri = cakeshopUri.getHost() + ":" + port + cakeshopUri.getPath();
        String apiUri = scheme + "://" + baseUri + "/api";
        String wsUri = (scheme.contentEquals("http") ? "ws" : "wss") + "://" + baseUri + "/ws";

        return new ClientManager(
                new ApiClient().setBasePath(apiUri),
                new WebSocketClient(wsUri));
    }

    public ClientManager(ApiClient apiClient, WebSocketClient wsClient) {
        this.apiClient = apiClient;
        this.wsClient = wsClient;
        this.clients = new HashMap<>();
        createApiClients();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void createApiClients() {
        for (Class clazz : API_LIST) {
            this.clients.put(clazz, apiClient.buildClient(clazz));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Api> T getClient(Class<T> clientClass) {
        return (T) clients.get(clientClass);
    }

    private void lazyStartWsClient() {
        if (!wsClient.isStarted()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    wsClient.start();
                }
            }).start();
        }
    }

    public void subscribe(EventHandler<?> handler) {
        lazyStartWsClient();
        wsClient.subscribe(handler);
    }

}
