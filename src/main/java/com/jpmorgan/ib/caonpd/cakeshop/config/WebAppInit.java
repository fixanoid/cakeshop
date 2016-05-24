package com.jpmorgan.ib.caonpd.cakeshop.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
@Profile("container")
@ComponentScan(basePackages = "com.jpmorgan.ib.caonpd.cakeshop")
public class WebAppInit implements WebApplicationInitializer {

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
    public void onStartup(ServletContext container) throws ServletException {
        // super.onStartup(container); // don't use spring-boot startup, use ours

        setLoggingPath(false);

        //Load Annotation Based Configs
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);
        container.addListener(new ContextLoaderListener(rootContext));

        // Dispatcher servlet for our app
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();

        // all @annotated beans will be scanned and configured here
        dispatcherContext.scan("com.jpmorgan.ib.caonpd.cakeshop");

        ServletRegistration.Dynamic dispatcher =
                container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));

        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setInitParameter("spring.profiles.active", "container");
        dispatcher.setAsyncSupported(true);
    }

}
