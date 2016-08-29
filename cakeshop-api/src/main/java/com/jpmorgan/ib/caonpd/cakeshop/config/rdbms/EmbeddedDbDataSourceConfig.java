/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.config.rdbms;

import com.jpmorgan.ib.caonpd.cakeshop.util.FileUtils;

import java.sql.Driver;
import java.util.Properties;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 *
 * @author I629630
 */
@Configuration
public class EmbeddedDbDataSourceConfig extends AbstractDataSourceConfig {
    
    // Embedded DB instance, IF we are using one
    protected EmbeddedDatabase embeddedDb;
    
    @Bean(name="hsql")
    @Override
    public DataSource getSimpleDataSource() {
        DataSourceFactory dataSourceFactory = new DataSourceFactory() {
            @Override
            public DataSource getDataSource() {
                try {
                    return createDataSource();
                } catch (ClassNotFoundException ex) {
                    LOG.error(ex.getMessage());
                }
                return null;
            }

            @Override
            public ConnectionProperties getConnectionProperties() {
                return new ConnectionProperties() {
                    @Override
                    public void setUsername(String username) {
                    }
                    @Override
                    public void setUrl(String url) {
                    }
                    @Override
                    public void setPassword(String password) {
                    }
                    @Override
                    public void setDriverClass(Class<? extends Driver> driverClass) {
                    }
                };
            }
        };

        this.embeddedDb = new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.HSQL)
            .setScriptEncoding("UTF-8")
            .setDataSourceFactory(dataSourceFactory)
            .ignoreFailedDrops(true)
            .build();

        return this.embeddedDb;
    }

    @PreDestroy
    public void shutdownDb() {
        if (this.embeddedDb != null) {
            this.embeddedDb.shutdown();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;   

    }

    @Override
    protected Properties hibernateProperties() { 
        LOG.info("USING HSQL HIBERNATE DIALECT");
        return new Properties() {
            {
                setProperty("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size", "500"));
                setProperty("hibernate.default_schema", "PUBLIC");
                setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "update"));
                setProperty("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect"));
                setProperty("hibernate.globally_quoted_identifiers", "true");
            }
        };
    }
    
    protected SimpleDriverDataSource createDataSource() throws ClassNotFoundException {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.hsqldb.jdbcDriver.class);
        dataSource.setUrl("jdbc:hsqldb:file:" + getDbStoragePath() + ";hsqldb.default_table_type=cached");
        dataSource.setUsername("sdk");
        dataSource.setPassword("sdk");
        return dataSource;
    }

    protected String getDbStoragePath() {
       return FileUtils.expandPath(CONFIG_ROOT, "db", "sdk");
    }  

}
