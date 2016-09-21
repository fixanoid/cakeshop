package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

//import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Peer;
import java.util.ArrayList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author I629630
 */
//@Repository
public class PeerRepository extends BaseRepository{


    public void save(Peer peer) {
//        if (null != getCassandraTemplate()) {
//            getCassandraTemplate().insert(peer);
//        }
    }

    public void reset() {
//        if (null != getCassandraTemplate()) {
//            getCassandraTemplate().deleteAll(Peer.class);
//        }
    }

    public List<Peer> list() {
//        if (null != getCassandraTemplate()) {
//            return getCassandraTemplate().select(
//                    QueryBuilder.select().all().from("peer"),
//                    Peer.class);
//        }
        return new ArrayList();
    }

    public Peer getById(String id) {
//        if (null != getCassandraTemplate()) {
//            return getCassandraTemplate().selectOneById(Peer.class, id);
//        }
        return null;
    }

}
