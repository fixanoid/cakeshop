package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author I629630
 */
@Configuration
@Profile("container")
@EnableScheduling
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @Value("${geth.apistore.url}")
    private String appStoreUrl;

    @Value("${geth.cors.enabled:true}")
    private boolean corsEnabled;

    @Autowired
    private ApplicationContext appContext;

    @Bean
    public MappedInterceptor healthcheckInterceptor() {
        return new MappedInterceptor(new String[] { "/*" }, appContext.getBean(HealthCheckInterceptor.class));
    }

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
    public void addCorsMappings(CorsRegistry registry) {
        if (corsEnabled) {
            registry.addMapping("/**")
                    .allowedOrigins(appStoreUrl)
                    .allowedMethods("POST");
        }
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Enable DefaultServlet handler for static resources at /**
        configurer.enable();
    }

}
