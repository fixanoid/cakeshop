package com.jpmorgan.ib.caonpd.cakeshop.config.rdbms;


import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;

import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.jpmorgan.ib.caonpd.cakeshop.model"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.jpmorgan.ib.caonpd.cakeshop.cassandra.*"))
public abstract class AbstractDataSourceConfig implements ApplicationContextAware {
    
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractDataSourceConfig.class);
    private final String JNDI_NAME = System.getProperty("jndi.name");

    @Autowired
    protected Environment env;

    @Value("${config.path}")
    protected String CONFIG_ROOT; 

    protected ApplicationContext applicationContext;


    @Bean
    public HibernateTemplate hibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }
    
    @Bean
    public LocalSessionFactoryBean sessionFactory() throws ClassNotFoundException, NamingException {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(new String[]{"com.jpmorgan.ib.caonpd.cakeshop.model"});
        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }


    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) throws ClassNotFoundException, NamingException {
       HibernateTransactionManager txManager = new HibernateTransactionManager();
       txManager.setSessionFactory(sessionFactory);
       return txManager;
    }

    protected abstract Properties hibernateProperties();
    protected abstract DataSource getSimpleDataSource() throws ClassNotFoundException;
    
    @Bean
    @Order(0)
    public DataSource dataSource() throws ClassNotFoundException, NamingException {
        if (StringUtils.isNotBlank(JNDI_NAME)) {
            JndiTemplate jndi = new JndiTemplate();
            DataSource dataSource = (DataSource) jndi.lookup(JNDI_NAME);
            return dataSource;
        } else {
            return  getSimpleDataSource();
        } 
    }
}
