package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
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
public class TestAppConfig extends AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);

    static {
        System.setProperty("eth.environment", "test");
    }

    private static final String ENV = System.getProperty("eth.environment");

    @Override
    public String getConfigPath() {
        // Use a temp folder since not in a container
        return FileUtils.getTempPath();
    }

    @Override
    public Executor getAsyncExecutor() {
        LOG.info("Creating SyncTaskExecutor");
        return new SyncTaskExecutor();
    }

}
