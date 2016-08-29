package com.jpmorgan.ib.caonpd.cakeshop.config;

import com.jpmorgan.ib.caonpd.cakeshop.config.cassandra.CassandraConfig;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.CassandraCondition;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.HsqlDataSourceConditon;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.MysqlDataSourceConditon;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.OracleDataSourceConditon;
import com.jpmorgan.ib.caonpd.cakeshop.conditions.PostgresDataSourceConditon;
import com.jpmorgan.ib.caonpd.cakeshop.config.rdbms.EmbeddedDbDataSourceConfig;
import com.jpmorgan.ib.caonpd.cakeshop.config.rdbms.MysqlDataSourceConfig;
import com.jpmorgan.ib.caonpd.cakeshop.config.rdbms.OracleDataSourceConfig;
import com.jpmorgan.ib.caonpd.cakeshop.config.rdbms.PostgresDataSourceConfig;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(0)
public class DatabaseConfig {
    
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Conditional(CassandraCondition.class)
    public CassandraConfig cassandra() {
        LOG.info("Loading CASSANDRA");
        return new CassandraConfig();
    }
    
    @Bean
    @Conditional(HsqlDataSourceConditon.class)
    public EmbeddedDbDataSourceConfig embeddedDbDataSourceConfig() {
        LOG.info("Loading HSQL");
        return new EmbeddedDbDataSourceConfig();
    }
    
    @Bean
    @Conditional(OracleDataSourceConditon.class)
    public OracleDataSourceConfig oracleDataSourceConfig() {
        LOG.info("Loading ORACLE");
        return new OracleDataSourceConfig();
    }
    
    @Bean
    @Conditional(MysqlDataSourceConditon.class)
    public MysqlDataSourceConfig mysqlDataSourceConfig() {
        LOG.info("Loading MYSQL");
        return new MysqlDataSourceConfig();
    }
    
    @Bean
    @Conditional(PostgresDataSourceConditon.class)
    public PostgresDataSourceConfig postgresDataSourceConfig() {
        LOG.info("Loading POSTGRES");
        return new PostgresDataSourceConfig();
    }        
}
