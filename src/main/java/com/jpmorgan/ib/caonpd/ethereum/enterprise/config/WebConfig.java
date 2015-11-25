/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author I629630
 */
@Configuration
@Profile("container")
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @PostConstruct
    public void prioritizeCustomArgumentMethodHandlers() {

        // existing resolvers
        List<HandlerMethodArgumentResolver> argumentResolvers =
                new ArrayList<>(adapter.getArgumentResolvers());

        // add our resolvers at pos 0
        List<HandlerMethodArgumentResolver> customResolvers =
                adapter.getCustomArgumentResolvers();

        // empty and re-add our custom list
        argumentResolvers.removeAll(customResolvers);
        argumentResolvers.addAll(0, customResolvers);

        adapter.setArgumentResolvers(argumentResolvers);
    }

    /*
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        List<HandlerMethodArgumentResolver> customResolvers = new ArrayList<HandlerMethodArgumentResolver>();
        customResolvers.add(new JsonMethodArgumentResolver());
        customResolvers.addAll(argumentResolvers);

        // empty and re-add our custom list
        argumentResolvers.clear();
        argumentResolvers.addAll(0, customResolvers);
    }
    */

}
