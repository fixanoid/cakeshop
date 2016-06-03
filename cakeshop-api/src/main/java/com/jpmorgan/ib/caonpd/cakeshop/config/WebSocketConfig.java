package com.jpmorgan.ib.caonpd.cakeshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;


/**
 *
 * @author i629630
 */
@Configuration
@EnableWebSocketMessageBroker
@Profile("container")
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
     public void configureMessageBroker(MessageBrokerRegistry  registry) {
        registry.enableSimpleBroker("/topic");

//        registry.enableStompBrokerRelay("/topic")
//                .setAutoStartup(true)
//                .setSystemHeartbeatReceiveInterval(5000)
//                .setSystemHeartbeatSendInterval(5000)
//
//        registry.enableStompBrokerRelay("/topic/currentLocation")
//                .setRelayPort(properties.getStompPort())
//                .setClientLogin(properties.getUsername())
//                .setClientPasscode(properties.getPassword())
//                .setSystemLogin(properties.getUsername())
//                .setSystemPasscode(properties.getPassword());

        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("ws")
            .setAllowedOrigins("*")
            .withSockJS()
            .setClientLibraryUrl("/cakeshop/js/vendor/sockjs-1.0.3.min.js");
    }

}
