package com.jpmorgan.ib.caonpd.cakeshop.config;

import com.jpmorgan.ib.caonpd.cakeshop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

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

    public static final String CONFIG_FILE = "env.properties";


    /**
     * Return the configured environment name
     *
     * Search order:
     * - ETH_ENV environment variable
     * - eth.environment system property (-Deth.environment param)
     * - Default to 'local' if none found
     *
     * @return Environment name
     */
    public static String getEnv() {
        String env = getProp("ETH_ENV", "eth.environment");
        if (StringUtils.isBlank(env)) {
            // FIXME only default to local based on a flag passed down from maven build?
            LOG.warn("defaulting to 'local' env");
            System.setProperty("eth.environment", "local");
            return "local";
        }
        return env;
    }

    /**
     * Return the configured config location
     *
     * Search order:
     * - ETH_CONFIG environment variable
     * - eth.config.dir system property (-Deth.config.dir param)
     * - Detect tomcat (container) root relative to classpath
     *
     * @return
     */
    public static String getConfigPath() {
        String configPath = getProp("ETH_CONFIG", "eth.config.dir");
        if (!StringUtils.isBlank(configPath)) {
            return FileUtils.expandPath(configPath, getEnv());
        }

        String webappRoot = FileUtils.expandPath(FileUtils.getClasspathPath(""), "..", "..");
        String tomcatRoot = FileUtils.expandPath(webappRoot, "..", "..");

        // migrate conf dir to new name
        File oldPath = new File(FileUtils.expandPath(tomcatRoot, "data", "enterprise-ethereum"));
        File newPath = new File(FileUtils.expandPath(tomcatRoot, "data", "cakeshop"));
        if (oldPath.exists() && !newPath.exists()) {
            oldPath.renameTo(newPath);
        }

        return FileUtils.expandPath(tomcatRoot, "data", "cakeshop", getEnv());
    }

    private static String getProp(String env, String java) {
        String str = System.getenv(env);
        if (StringUtils.isBlank(str)) {
            str = System.getProperty(java);
        }
        return str;
    }

    @Bean
    @Profile("container")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        return createPropConfigurer(getEnv(), getConfigPath());
    }

    public static String getVendorConfigFile() {
        return getEnv() + File.separator + CONFIG_FILE;
    }

    public static void initVendorConfig(File configFile) throws IOException {
        LOG.info("Initializing new config from " + FileUtils.getClasspathPath(getVendorConfigFile()).toString());
        FileUtils.copyInputStreamToFile(FileUtils.getClasspathStream(getVendorConfigFile()), configFile);
    }

    protected static PropertySourcesPlaceholderConfigurer createPropConfigurer(String env,
            String configDir) throws IOException {

        if (StringUtils.isBlank(env)) {
            throw new IOException("ENV var 'eth.environment' not set; unable to load config");
        }

        LOG.info("eth.environment=" + env);
        LOG.info("eth.config.dir=" + configDir);

        File configPath = new File(configDir);
        File configFile = new File(configPath.getPath() + File.separator + CONFIG_FILE);

        if (!configPath.exists() || !configFile.exists()) {
            LOG.debug("Config dir does not exist, will init");

            configPath.mkdirs();
            if (!configPath.exists()) {
               throw new IOException("Unable to create config dir: " + configPath.getAbsolutePath());
            }

            initVendorConfig(configFile);

        } else {
            Properties mergedProps = new Properties();
            mergedProps.load(FileUtils.getClasspathStream(getVendorConfigFile()));
            mergedProps.load(new FileInputStream(configFile)); // overwrite vendor props with our configs
            mergedProps.store(new FileOutputStream(configFile), null);
        }

        // Finally create the configurer and return it
        Properties localProps = new Properties();
        localProps.setProperty("config.path", configPath.getPath());

        PropertySourcesPlaceholderConfigurer propConfig = new PropertySourcesPlaceholderConfigurer();
        propConfig.setLocation(new FileSystemResource(configFile));
        propConfig.setProperties(localProps);
        return propConfig;
    }

    @Bean(name="asyncExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(10);
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
