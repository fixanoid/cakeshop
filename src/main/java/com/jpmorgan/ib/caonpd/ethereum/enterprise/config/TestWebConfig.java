package com.jpmorgan.ib.caonpd.ethereum.enterprise.config;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ComponentScan(basePackages="com.jpmorgan.ib.caonpd.ethereum.enterprise",
    excludeFilters = {
        @ComponentScan.Filter(
              type = FilterType.ASSIGNABLE_TYPE,
              value = { WebConfig.class, WebAppInit.class }
        )
    }
)
@ActiveProfiles("integration-test")
@Order(1)
public class TestWebConfig  {
    
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
