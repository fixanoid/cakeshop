package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Profile("container")
public class AppConfig implements AsyncConfigurer {

    public static final String API_VERSION = "1.0";

    private static final String ENV = System.getProperty("eth.environment");
    private static final String PROPS_FILE = File.separator + "env.properties";
    private static String ROOT = AppConfig.class.getClassLoader().getResource("").getPath();

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws FileNotFoundException, IOException {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        //Externalizing properties
        if (SystemUtils.IS_OS_WINDOWS && ROOT.startsWith("/")) {
            ROOT = ROOT.replaceFirst("/", "");
        }
        Path path = Paths.get(ROOT.replace("/WEB-INF/classes/", "") + File.separator + ".." + PROPS_FILE);
        if (!Files.exists(path)) {
            try (FileInputStream input = new FileInputStream(ROOT + ENV + PROPS_FILE);
                    FileOutputStream output = new FileOutputStream(ROOT.replace("/WEB-INF/classes/", "") + File.separator + ".." + PROPS_FILE)) {
                IOUtils.copy(input, output);
            }
        }
        propertySourcesPlaceholderConfigurer.setLocation(new FileSystemResource(ROOT.replace("/WEB-INF/classes/", "") + File.separator + ".." + PROPS_FILE));
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
