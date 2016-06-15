package com.jpmorgan.ib.caonpd.cakeshop.config;

import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.ServletMfs;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAutoConfiguration
@Profile("container")
@ComponentScan(basePackages = "com.jpmorgan.ib.caonpd.cakeshop")
public class WebAppInit extends SpringBootServletInitializer {

    public static void setLoggingPath(boolean isSpringBoot) {
        // setup logging path for spring-boot
        if (StringUtils.isNotBlank(System.getProperty("logging.path"))) {
            return;
        }
        if (isSpringBoot) {
            System.setProperty("logging.path", ".");
            return;
        }

        // container
        System.setProperty("logging.path", System.getProperty("catalina.home"));
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.profiles("container");
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        setLoggingPath(false);
        try {
            Manifests.DEFAULT.append(new ServletMfs(container));
        } catch (IOException e) {
            System.err.println("Failed to load servlet manifest: " + e.getMessage());
        }
        super.onStartup(container);
    }

}
