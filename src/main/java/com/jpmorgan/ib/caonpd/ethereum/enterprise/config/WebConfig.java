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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author I629630
 */
@Configuration
@Profile("container")
@EnableWebMvc
@ComponentScan(basePackages = "com.jpmorgan.ib.caonpd.ethereum.enterprise",
        excludeFilters = {
            @Filter(type = ASSIGNABLE_TYPE,
                    value = {TestWebConfig.class, TestAppConfig.class
                    })
        })
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @Value("${geth.apistore.url}")
    private String appStoreUrl;

    @Value("${geth.cors.enabled:true}")
    private boolean corsEnabled;

    @PostConstruct
    public void prioritizeCustomArgumentMethodHandlers() {
        // existing resolvers
        List<HandlerMethodArgumentResolver> argumentResolvers
                = new ArrayList<>(adapter.getArgumentResolvers());

        // add our resolvers at pos 0
        List<HandlerMethodArgumentResolver> customResolvers
                = adapter.getCustomArgumentResolvers();

        // empty and re-add our custom list
        argumentResolvers.removeAll(customResolvers);
        argumentResolvers.addAll(0, customResolvers);

        adapter.setArgumentResolvers(argumentResolvers);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(new JsonMethodArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/local-console/**").addResourceLocations("/local-console/").setCachePeriod(31556926);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if (corsEnabled) {
            registry.addMapping("/**")
                    .allowedOrigins(appStoreUrl)
                    .allowedMethods("POST");
        }
    }

}
