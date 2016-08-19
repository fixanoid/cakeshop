/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.test.config;

import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Account;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.LatestBlockNumber;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Peer;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.LoggerFactory;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 *
 * @author I629630
 */
@Configuration
@EnableCassandraRepositories(
        basePackages = "com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository")
public class TestCassandraConfig extends AbstractCassandraConfiguration {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestCassandraConfig.class);
     
    @Override
    protected String getKeyspaceName() {
        return "testkeyspace";
    }
 
    @Bean
    @Override
    public CassandraClusterFactoryBean cluster() {

        LOG.info("STARTING CASSANDRA");

        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        } catch (TTransportException | IOException | InterruptedException | ConfigurationException ex) {
            Logger.getLogger(TestCassandraConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints("127.0.0.1");
        cluster.setPort(9142);
        try {
            cluster.afterPropertiesSet();
        } catch (Exception ex) {
             LOG.info(ex.getMessage());
        }
        try {
            if (null == cluster.getObject() || null == cluster.getObject().getMetadata().getKeyspace(getKeyspaceName())) {
                CreateKeyspaceSpecification spec = new CreateKeyspaceSpecification(getKeyspaceName());
                List<CreateKeyspaceSpecification> specifications = new ArrayList();
                specifications.add(spec);
                cluster.setKeyspaceCreations(specifications);
            }
        } catch (Exception ex) {
            Logger.getLogger(TestCassandraConfig.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cluster;
    }

    @Bean
    @Override
    public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {
        BasicCassandraMappingContext mapping = new BasicCassandraMappingContext();
        Set <Class<?>> entities = new HashSet();
        entities.add(Account.class);
        entities.add(Block.class);
        entities.add(Event.class);
        entities.add(LatestBlockNumber.class);
        entities.add(Peer.class);
        entities.add(Transaction.class);        
        mapping.setInitialEntitySet(entities);
        mapping.afterPropertiesSet();
        return mapping;
    }
    
    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }
    
    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity"};
    }
}
