/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.conditions;

import org.springframework.context.annotation.Condition;

/**
 *
 * @author I629630
 */
public abstract class BaseCondition implements Condition {
        
    public final String ORACLE = "oracle";
    public final String MYSQL = "mysql";
    public final String POSTGRES = "postgres";
    public final String HSQL = "hsqldb";
    public final String CASSANDRA = "cassandra";
    public String databaseName = System.getProperty("cakeshop.database.name");
    
}
