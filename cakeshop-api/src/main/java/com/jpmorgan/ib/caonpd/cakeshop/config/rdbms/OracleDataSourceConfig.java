/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.config.rdbms;


import com.jpmorgan.ib.caonpd.cakeshop.conditions.OracleDataSourceConditon;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 *
 * @author I629630
 */
@Configuration
@Conditional(OracleDataSourceConditon.class)
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
                setProperty("hibernate.jdbc.batch_size", StringUtils.isNotBlank(System.getProperty(JDBC_BATCH_SIZE)) 
                        ? System.getProperty(JDBC_BATCH_SIZE) : env.getProperty(JDBC_BATCH_SIZE, "20"));
                setProperty("hibernate.hbm2ddl.auto", StringUtils.isNotBlank(System.getProperty(HBM_2DDL_AUTO)) 
                        ? System.getProperty(HBM_2DDL_AUTO) : env.getProperty(HBM_2DDL_AUTO, "update"));
                setProperty("hibernate.dialect", StringUtils.isNotBlank(System.getProperty(HIBERNATE_DIALECT))
                        ? System.getProperty(HIBERNATE_DIALECT) : env.getProperty(HIBERNATE_DIALECT, "org.hibernate.dialect.Oracle10gDialect"));
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
        dataSource.setUrl(StringUtils.isNotBlank(System.getProperty(JDBC_URL)) 
                ? System.getProperty(JDBC_URL)  : env.getProperty(JDBC_URL));
        dataSource.setUsername(StringUtils.isNotBlank(System.getProperty(JDBC_USER)) 
                ? System.getProperty(JDBC_USER) : env.getProperty(JDBC_USER));
        dataSource.setPassword(StringUtils.isNotBlank(System.getProperty(JDBC_PASS)) 
                ? System.getProperty(JDBC_PASS) : env.getProperty(JDBC_PASS));
        return dataSource;
    }

}
