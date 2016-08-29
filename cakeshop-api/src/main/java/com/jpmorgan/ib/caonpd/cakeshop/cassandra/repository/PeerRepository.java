package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Peer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;

/**
 *
 * @author I629630
 */
@Repository
public class PeerRepository {
    
    @Autowired(required = false)
    private CassandraOperations cassandraTemplate;
    
    public void save (Peer peer) {
        cassandraTemplate.insert(peer);
    }
    
    public void reset () {
        cassandraTemplate.deleteAll(Peer.class);
    }
    
    public List<Peer> list () {
        return cassandraTemplate.select(
                QueryBuilder.select().all().from("peer"), 
                Peer.class);
    }
    
    public Peer getById(String id) {
        return cassandraTemplate.selectOneById(Peer.class, id);
    }
    
}
