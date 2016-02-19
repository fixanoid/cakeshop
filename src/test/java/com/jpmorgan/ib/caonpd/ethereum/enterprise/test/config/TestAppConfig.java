package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.AppConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.WebAppInit;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.WebConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockScanner;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl.BlockScannerImpl;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.TestBlockScanner;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
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
              value = { WebConfig.class, WebAppInit.class, BlockScannerImpl.class }
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

    private String tempConfigPath;

    @Override
    public String getConfigPath() {
        // Use a temp folder since not in a container
        tempConfigPath = TempFileManager.getTempPath();
        return tempConfigPath;
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
