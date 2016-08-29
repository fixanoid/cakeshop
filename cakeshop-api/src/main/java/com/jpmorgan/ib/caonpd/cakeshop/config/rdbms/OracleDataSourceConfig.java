/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.config.rdbms;


import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 *
 * @author I629630
 */
@Configuration
public class OracleDataSourceConfig extends AbstractDataSourceConfig {    

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Override
    protected Properties hibernateProperties() {
        LOG.info("USING ORACLE HIBERNATE DIALECT");
        return new Properties() {
            {
                setProperty("hibernate.jdbc.batch_size", env.getProperty("cakeshop.hibernate.jdbc.batch_size", "500"));
                setProperty("hibernate.hbm2ddl.auto", env.getProperty("cakeshop.hibernate.hbm2ddl.auto", "update"));
                setProperty("hibernate.dialect", env.getProperty("cakeshop.hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect"));
                setProperty("hibernate.globally_quoted_identifiers", "true");
            }
        };
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected DataSource getSimpleDataSource() throws ClassNotFoundException {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(oracle.jdbc.OracleDriver.class);
        dataSource.setUrl(env.getProperty("cakeshop.jdbc.url"));
        dataSource.setUsername(env.getProperty("cakeshop.jdbc.user"));
        dataSource.setPassword(env.getProperty("cakeshop.jdbc.pass"));
        return dataSource;
    }

}
