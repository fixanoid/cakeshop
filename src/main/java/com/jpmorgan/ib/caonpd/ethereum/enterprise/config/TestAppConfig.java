/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author I629630
 */
@Configuration
public class TestAppConfig {
    
     private static final String ENV = System.getProperty("eth.environment");
     
     @Bean
     public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
         PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
         propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource(ENV + "/env.properties"));
         return propertySourcesPlaceholderConfigurer;
     }
     
     @Bean
    public static AdminBean adminBean() {
        return new AdminBean();
    }
    
}
