/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author I629630
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.jpmorgan.ib.caonpd.ethereum.enterprise")
public class WebConfig extends WebMvcConfigurerAdapter {

    private static final String ENV = System.getProperty("eth.environment");
    private @Inject RequestMappingHandlerAdapter adapter;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource(ENV + "/env.properties"));
        return propertySourcesPlaceholderConfigurer;
    }


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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        JsonMethodArgumentResolver jsonResolver = new JsonMethodArgumentResolver();
        argumentResolvers.add(jsonResolver);
    }

}
