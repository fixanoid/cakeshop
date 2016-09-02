package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import java.math.BigInteger;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BlockDAO extends BaseDAO {

    public Block getById(String id) {
        if (null != getCurrentSession()) {
        return getCurrentSession().get(Block.class, id);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Block getByNumber(BigInteger number) {
        Block block = null;
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Block.class);
            c.add(Restrictions.eq("number", number));
            List list = c.list();

            if (list == null || list.isEmpty()) {
                return null;
            }

            block = (Block) list.get(0);
            if (null != getCurrentSession()) {
                Hibernate.initialize(block.getTransactions());
                Hibernate.initialize(block.getUncles());
            }  
        }
        return block;
    }

    @SuppressWarnings("rawtypes")
    public Block getLatest() {
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Block.class);
            c.setProjection(Projections.max("number"));
            List list = c.list();

            if (list == null || list.isEmpty() || list.get(0) == null) {
                return null;
            }

            return getByNumber((BigInteger) list.get(0));
        } else {
            return null;
        }
    }

    public void save(Block block) {
        if (null != getCurrentSession()) {
            getCurrentSession().save(block);
        }
    }

    @Override
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("DELETE FROM Block_transactions").executeUpdate();
            session.createSQLQuery("DELETE FROM Block_uncles").executeUpdate();
            session.createSQLQuery("DELETE FROM BLOCKS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
