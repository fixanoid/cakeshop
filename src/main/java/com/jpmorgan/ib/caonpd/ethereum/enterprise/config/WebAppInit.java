/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.context.annotation.Profile;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *
 * @author I629630
 */
@Profile("container")
public class WebAppInit implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {
        //Load Annotation Based Configs
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        container.addListener(new ContextLoaderListener(rootContext));
        rootContext.register(AppConfig.class);

        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(WebConfig.class);
        dispatcherContext.scan("com.jpmorgan.ib.caonpd.ethereum.enterprise");

        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcher =
                container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setInitParameter("spring.profiles.active", "container");

    }

//    @Override
//    protected Class<?>[] getRootConfigClasses() {
//        return new Class<?>[] { };
//    }
//
//    @Override
//    protected Class<?>[] getServletConfigClasses() {
//        return new Class<?>[] { WebConfig.class };
//    }
//
//    @Override
//    protected String[] getServletMappings() {
//        return new String [] {"/"};
//    }

}
