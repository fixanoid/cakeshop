package com.jpmorgan.ib.caonpd.cakeshop.test.config;

import com.jpmorgan.ib.caonpd.cakeshop.config.AppConfig;
import com.jpmorgan.ib.caonpd.cakeshop.config.SpringBootApplication;
import com.jpmorgan.ib.caonpd.cakeshop.config.WebAppInit;
import com.jpmorgan.ib.caonpd.cakeshop.config.WebConfig;
import com.jpmorgan.ib.caonpd.cakeshop.db.BlockScanner;
import com.jpmorgan.ib.caonpd.cakeshop.test.TestBlockScanner;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ComponentScan(basePackages="com.jpmorgan.ib.caonpd.cakeshop",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            value = { SpringBootApplication.class, WebConfig.class, WebAppInit.class,
                            BlockScanner.class }
        )
    }
)
@ActiveProfiles("integration-test")
@Order(1)
@EnableAsync
public class TestAppConfig extends AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);

    static {
        System.setProperty("eth.environment", "test");
    }

    @Bean
    @Profile("integration-test")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        return createPropConfigurer(getEnv(), TempFileManager.getTempPath());
    }

    @Override
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    public BlockScanner createBlockScanner() {
        return new TestBlockScanner();
    }

}
