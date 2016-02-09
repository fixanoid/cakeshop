/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

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
        //String [] endpoints = {"/node", "/block", "transaction"};
        registry.addEndpoint("ws").setAllowedOrigins("*").withSockJS();
        
    }
    
}
