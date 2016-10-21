package com.jpmorgan.ib.caonpd.cakeshop.conditions;

import org.springframework.context.annotation.ConfigurationCondition;

/**
 *
 * @author I629630
 */
public abstract class BaseCondition implements ConfigurationCondition {
        
    public final String ORACLE = "oracle";      
    public final String MYSQL = "mysql";
    public final String POSTGRES = "postgres";
    public final String HSQL = "hsqldb";
    public final String CASSANDRA = "cassandra";
    public String databaseName = System.getProperty("cakeshop.database.vendor");
    
}
