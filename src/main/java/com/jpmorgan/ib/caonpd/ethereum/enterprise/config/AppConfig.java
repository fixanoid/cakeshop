package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@Configuration
@EnableAsync
@Profile("container")
public class AppConfig implements AsyncConfigurer {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    public static final String API_VERSION = "1.0";

    public static final String CONFIG_FILE = "env.properties";

    public static final String APP_ROOT = FileUtils.expandPath(FileUtils.getClasspathPath(""), "..", "..");

    protected static final String TOMCAT_ROOT = FileUtils.expandPath(APP_ROOT, "..", "..");

    public static String getEnv() {
        String env = System.getProperty("eth.environment");
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
        String configPath = System.getenv("ETH_CONFIG");
        if (StringUtils.isBlank(configPath)) {
            configPath = System.getProperty("eth.config.dir");
        }
        if (!StringUtils.isBlank(configPath)) {
            return FileUtils.expandPath(configPath, getEnv());
        }
        return FileUtils.expandPath(TOMCAT_ROOT, "data", "enterprise-ethereum", getEnv());
    }


    @Bean
    @Profile("container")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        return createPropConfigurer(getEnv(), getConfigPath());
    }

    protected static PropertySourcesPlaceholderConfigurer createPropConfigurer(String env,
            String configDir) throws IOException {

        if (StringUtils.isBlank(env)) {
            throw new IOException("ENV var 'eth.environment' not set; unable to load config");
        }

        File configPath = new File(configDir);
        File configFile = new File(configPath.getPath() + File.separator + CONFIG_FILE);
        File vendorEnvConfigFile = FileUtils.getClasspathPath(env + File.separator + CONFIG_FILE).toFile();

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

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool()) // TODO tune via env props?
                .build();
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OkHttpClient okc) {
        return new OkHttp3ClientHttpRequestFactory(okc);
    }

    @Bean
    @Scope("prototype")
    public RestTemplate createRestTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return new RestTemplate(httpRequestFactory);
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
