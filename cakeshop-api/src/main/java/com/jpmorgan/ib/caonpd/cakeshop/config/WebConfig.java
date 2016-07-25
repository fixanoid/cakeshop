package com.jpmorgan.ib.caonpd.cakeshop.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
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

    @Value("${cakeshop.mvc.async.pool.threads.core}")
    private Integer coreSize;

    @Value("${cakeshop.mvc.async.pool.threads.max}")
    private Integer maxSize;

    @Value("${cakeshop.mvc.async.pool.queue.max}")
    private Integer queueCapacity;


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
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(createMvcAsyncExecutor());
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

    /**
     * Thread pool used by Spring WebMVC async 'Callable'
     * https://spring.io/blog/2012/05/10/spring-mvc-3-2-preview-making-a-controller-method-asynchronous/
     *
     * @return
     */
    private AsyncTaskExecutor createMvcAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(coreSize);
        exec.setMaxPoolSize(maxSize);
        exec.setQueueCapacity(queueCapacity);
        exec.setThreadNamePrefix("WebMvc-");
        exec.afterPropertiesSet();
        return exec;
    }

}
