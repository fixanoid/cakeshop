package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;

public class BaseApiTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(BaseApiTest.class);

    protected MockWebServer mockWebServer;

    protected ApiClient apiClient;

    @BeforeClass
    public void setupMockWebserver() throws IOException {
        if (mockWebServer != null) {
            return;
        }
        mockWebServer = new MockWebServer();
        QueueDispatcher dispatcher = new QueueDispatcher();
        dispatcher.setFailFast(true);
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();
    }

    @AfterClass(alwaysRun=true)
    public void stopMockWebserver() {
        if (mockWebServer != null) {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LOG.debug("MockWebServer shutdown failed", e);
            }
        }
    }

    @BeforeMethod
    public void createApiClient() {
        String uri = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/cakeshop/api";
        this.apiClient = new ApiClient().setBasePath(uri);
    }

}
