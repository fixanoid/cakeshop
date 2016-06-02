package com.jpmorgan.ib.caonpd.cakeshop.client.ws;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

public class WebSocketClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketClient.class);

    private static final long DEFAULT_RECONNECT_DELAY = 1000;

    private class ConnectionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession newStompSession, StompHeaders stompHeaders) {
            stompSession = newStompSession;
            if (topicHandlers != null && !topicHandlers.isEmpty()) {
                reconnectAllTopics();
            }
        }

        @Override
        public void handleException(StompSession stompSession, StompCommand stompCommand, StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {
        }

        @Override
        public void handleTransportError(StompSession stompSession, Throwable throwable) {
            if (throwable instanceof ConnectionLostException) {
                reconnect(); // reconnect the client
                return;
            }
        }
    }

    private Timer reconnectTimer;

    private long reconnectDelay;

    private TaskScheduler taskScheduler;

    private String wsUri;

    private WebSocketStompClient stompClient;

    private StompSession stompSession;

    private JettyWebSocketClient jettyWebSocketClient;

    private Map<String, List<EventHandler>> topicHandlers;

    public WebSocketClient(String wsUri) {
        this(wsUri, DEFAULT_RECONNECT_DELAY);
    }

    public WebSocketClient(String wsUri, long reconnectDelay) {
        this.wsUri = wsUri;
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.reconnectTimer = new Timer("ReconnectTimer");
        this.reconnectDelay = reconnectDelay;
        this.topicHandlers = new Hashtable<>();
    }

    public void subscribe(EventHandler handler) {
        if (topicHandlers.get(handler.getTopic()) == null) {
            topicHandlers.put(handler.getTopic(), (List) new Vector<>());
        }
        topicHandlers.get(handler.getTopic()).add(handler);
        if (stompSession != null && stompSession.isConnected()) {
            LOG.debug("Subscribing to " + handler.getTopic());
            stompSession.subscribe(handler.getTopic(), handler);
        }
    }

    public void reconnect() {
        if (stompClient != null) {
            LOG.debug("Reconnecting");
            disconnect(stompClient);
            createClient();
            doConnect();
            return;
        }
        start();
    }

    public void start() {
        LOG.debug("Connecting");
        createClient();
        doConnect();
    }

    public void disconnect() {
        if (stompClient != null && stompClient.isRunning()) {
            disconnect(stompClient);
            stompClient = null;
            stompSession = null;
        }
    }

    private void disconnect(final WebSocketStompClient stompClient) {
         if (stompClient != null && stompClient.isRunning()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        stompClient.stop();
                    } catch (Throwable t) {
                        LOG.warn("err stopping client: " + t.getMessage());
                    }
                }
            }).start();
        }
    }

    private void createClient() {

        // setup transports & socksjs
        jettyWebSocketClient = new JettyWebSocketClient();
        List<Transport> transports = new ArrayList<Transport>(2);
        transports.add(new WebSocketTransport(jettyWebSocketClient));
        //transports.add(new RestTemplateXhrTransport());

        SockJsClient sockJsClient = new SockJsClient(transports);

        // create stomp client
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JsonMessageConverter());
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.start();

    }

    private void doConnect() {
        ListenableFuture<StompSession> future = stompClient.connect(wsUri, new ConnectionHandler());
        future.addCallback(
            new SuccessCallback<StompSession>() {
                @Override
                public void onSuccess(StompSession stompSession) {
                }
            },
            new FailureCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    LOG.debug("Failed to connect: " + throwable.getMessage());
                    reconnectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            reconnect();
                        }
                    }, reconnectDelay);
                }
            }
        );

    }

    private void reconnectAllTopics() {
        if (topicHandlers == null || topicHandlers.isEmpty() || stompSession == null) {
            return;
        }
        for (String topic : topicHandlers.keySet()) {
            List<EventHandler> handlers = topicHandlers.get(topic);
            for (EventHandler handler : handlers) {
                LOG.debug("Resubscribing to " + topic);
                stompSession.subscribe(topic, handler);
            }
        }
    }

}
