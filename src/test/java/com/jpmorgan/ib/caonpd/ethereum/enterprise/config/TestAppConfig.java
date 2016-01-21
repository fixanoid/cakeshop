package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.PreDestroy;

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

    public static List<String> tempFiles = new ArrayList<String>();

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);

    static {
        System.setProperty("eth.environment", "test");
    }

    private static final String ENV = System.getProperty("eth.environment");

    private String tempConfigPath;

    public static String getTempPath() {
        String t = FileUtils.getTempPath();
        tempFiles.add(t);
        return t;
    }

    public static void cleanupTempPaths() {
        for (String t : tempFiles) {
            File f = new File(t);
            if (f.exists()) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                }
            }
        }
        tempFiles.clear();
    }

    @Override
    public String getConfigPath() {
        // Use a temp folder since not in a container
        System.out.println("GETTING NEW CONFIG PATH TEMP DIR");
        tempConfigPath = getTempPath();
        return tempConfigPath;
    }

    @Override
    public Executor getAsyncExecutor() {
        LOG.info("Creating SyncTaskExecutor");
        return new SyncTaskExecutor();
    }

    @PreDestroy
    public void cleanupConfigDir() {
        try {
            FileUtils.deleteDirectory(new File(tempConfigPath));
        } catch (IOException e) {
        }
    }

}
