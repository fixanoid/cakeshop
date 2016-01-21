package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
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

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    public static final String API_VERSION = "1.0";

    protected static final String ENV = System.getProperty("eth.environment");
    protected static final String CONFIG_FILE = "env.properties";

    protected static final String PROPS_FILE = File.separator + "env.properties";
//    protected static String ROOT = AppConfig.class.getClassLoader().getResource("").getPath();


    protected static final String APP_ROOT;
    static {
        String root = null;
        try {
            root = com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.expandPath(com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.getClasspathPath(""), "..", "..");
        } catch (IOException e) {
        }
        APP_ROOT = root;
    }

    protected static final String TOMCAT_ROOT;
    static {
        String root = null;
        try {
            root = com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.expandPath(com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.getClasspathPath(""), "..", "..", "..", "..");
        } catch (IOException e) {
        }
        TOMCAT_ROOT = root;
    }

    protected static final String CONFIG_ROOT = com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.expandPath(TOMCAT_ROOT, "data", "enterprise-ethereum", ENV);

    public String getConfigPath() {
        return CONFIG_ROOT;
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws FileNotFoundException, IOException {

//        if (SystemUtils.IS_OS_WINDOWS && ROOT.startsWith("/")) {
//            ROOT = ROOT.replaceFirst("/", "");
//        }

        File configPath = new File(getConfigPath());
        File configFile = new File(configPath.getPath() + File.separator + CONFIG_FILE);
        File vendorEnvConfigFile = com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils.getClasspathPath(ENV + File.separator + CONFIG_FILE).toFile();

        if (!configPath.exists() || !configFile.exists()) {
            LOG.debug("Config dir does not exist, will init");

            configPath.mkdirs();
            if (!configPath.exists()) {
               throw new IOException("Unable to create config dir: " + configPath.getAbsolutePath());
            }

            LOG.info("Initializing new config from " + vendorEnvConfigFile.getPath());
            FileUtils.copyFile(vendorEnvConfigFile, configFile);

        } else {
            Properties mergedProps = new Properties();
            mergedProps.load(new FileInputStream(vendorEnvConfigFile));
            mergedProps.load(new FileInputStream(configFile)); // overwrite vendor props with our configs
            mergedProps.store(new FileOutputStream(configFile), null);
        }

        // Finally create the configurer and return it
        Properties localProps = new Properties();
        localProps.setProperty("config.path", configPath.getPath());
        localProps.setProperty("app.path", APP_ROOT);

        PropertySourcesPlaceholderConfigurer propConfig = new PropertySourcesPlaceholderConfigurer();
        propConfig.setLocation(new FileSystemResource(configFile));
        propConfig.setProperties(localProps);
        return propConfig;
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

    private static void checkPropsChangesNeeded(Properties existingProperties, Properties newProperties, FileWriter writer) {
        Boolean updated = false;
        for (String key : newProperties.stringPropertyNames()) {
            if (StringUtils.isBlank(existingProperties.getProperty(key))) {
                existingProperties.setProperty(key, newProperties.getProperty(key));
                updated = true;
            }
        }
        if (updated) {
            try {
                existingProperties.store(writer, null);
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

}
