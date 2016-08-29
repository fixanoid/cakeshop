package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Peer;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PeerDAO extends BaseDAO {

    public Peer getById(String id) {
        return hibernateTemplate.get(Peer.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Peer> list() {
        Criteria c = getCurrentSession().createCriteria(Peer.class);
        return c.list();
    }

    public void save(Peer peer) {
        hibernateTemplate.save(peer);
    }

    public void save(List<Peer> peers) {
        Session session = getCurrentSession();
        for (int i = 0; i < peers.size(); i++) {
            Peer peer = peers.get(i);
            session.save(peer);
            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }
    }

    @Override
    public void reset() {
        Session session = getCurrentSession();
        session.createSQLQuery("DELETE FROM PEERS").executeUpdate();
        session.flush();
        session.clear();
    }

}
