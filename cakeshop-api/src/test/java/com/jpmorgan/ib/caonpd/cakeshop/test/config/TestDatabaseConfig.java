/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.test.config;

import com.jpmorgan.ib.caonpd.cakeshop.conditions.CassandraCondition;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.HsqlDataSourceConditon;
import com.jpmorgan.ib.caonpd.cakeshop.config.rdbms.EmbeddedDbDataSourceConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 *
 * @author I629630
 */
@Configuration
@Order(1)
public class TestDatabaseConfig implements ApplicationContextAware{
    
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestDatabaseConfig.class);
    protected ApplicationContext applicationContext;
    
    @Bean
    @Conditional(CassandraCondition.class)
    public TestCassandraConfig cassandra() {
        LOG.info("Loading CASSANDRA");
        return new TestCassandraConfig();
    }
    
    @Bean
    @Conditional(HsqlDataSourceConditon.class)
    public EmbeddedDbDataSourceConfig embeddedDbDataSourceConfig() {
        LOG.info("Loading HSQL");
        return new EmbeddedDbDataSourceConfig();
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
    
}
