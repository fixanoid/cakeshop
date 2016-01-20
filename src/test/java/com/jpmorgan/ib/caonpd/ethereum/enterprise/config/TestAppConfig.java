package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ComponentScan(basePackages="com.jpmorgan.ib.caonpd.ethereum.enterprise",
    excludeFilters = {
        @ComponentScan.Filter(
              type = FilterType.ASSIGNABLE_TYPE,
              value = { WebConfig.class, WebAppInit.class }
        )
    }
)
@ActiveProfiles("integration-test")
@Order(1)
@EnableAsync
public class TestAppConfig implements AsyncConfigurer {

    private static final String ENV = System.getProperty("eth.environment");

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource(ENV + "/env.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public static AdminBean adminBean() {
        return new AdminBean();
    }

    @Bean(name="asyncExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(250);
        exec.setMaxPoolSize(500);
        exec.setQueueCapacity(10000);
        exec.setThreadNamePrefix("EE-SDK-");
        exec.afterPropertiesSet();
        return exec;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
