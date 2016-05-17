package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.jpmorgan.ib.caonpd.ethereum.enterprise")
public class SpringBootApplication {

    public static void main(String[] args) {
        WebAppInit.setLoggingPath(true);
        System.setProperty("eth.config.dir", FileUtils.expandPath(SystemUtils.USER_DIR, "data"));
        new SpringApplicationBuilder(SpringBootApplication.class)
            .profiles("container", "spring-boot")
            .run(args);
    }

    @Bean
    @Profile("spring-boot")
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        return new JettyEmbeddedServletContainerFactory();
    }

}
