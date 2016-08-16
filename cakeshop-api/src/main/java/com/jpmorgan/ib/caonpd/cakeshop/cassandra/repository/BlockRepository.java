/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

/**
 *
 * @author I629630
 */
public interface BlockRepository extends CassandraRepository <Block> {
    
    Block getByBlockNumber (Long blockNumber);
//    @Query("SELECT max(id) FROM block")
//    UUID getLatest();
    
}
