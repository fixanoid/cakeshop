package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("container")
public class AppConfig {

    public static final String API_VERSION = "1.0";

    @Autowired
    private Environment environment;

    private static final String ENV = System.getProperty("eth.environment");
    private static final String PROPS_FILE = File.separator + "env.properties";
    private static String ROOT = AppConfig.class.getClassLoader().getResource("").getPath();

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws FileNotFoundException, IOException {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        //Externilizing properties
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

}
