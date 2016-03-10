package com.jpmorgan.ib.caonpd.ethereum.enterprise.dao;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BlockDAO {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    private HibernateTemplate hibernateTemplate;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public Block getById(String id) {
        return hibernateTemplate.get(Block.class, id);
    }

    public Block getByNumber(Long number) {
        Criteria c = getCurrentSession().createCriteria(Block.class);
        c.add(Restrictions.eq("number", number));
        List list = c.list();

        if (list == null || list.isEmpty()) {
            return null;
        }

        Block block = (Block) list.get(0);

        hibernateTemplate.initialize(block.getTransactions());
        hibernateTemplate.initialize(block.getUncles());

        return block;
    }

    public Block getLatest() {
        Criteria c = getCurrentSession().createCriteria(Block.class);
        c.setProjection(Projections.max("number"));
        List list = c.list();

        if (list == null || list.isEmpty() || list.get(0) == null) {
            return null;
        }

        return getByNumber((Long) list.get(0));
    }

    public void save(Block block) {
        hibernateTemplate.save(block);
    }

    public void reset() {
        getCurrentSession().createSQLQuery("DELETE FROM PUBLIC.\"Block_transactions\"").executeUpdate();
        getCurrentSession().createSQLQuery("DELETE FROM PUBLIC.\"Block_uncles\"").executeUpdate();
        getCurrentSession().createSQLQuery("DELETE FROM PUBLIC.BLOCKS").executeUpdate();
    }

}
