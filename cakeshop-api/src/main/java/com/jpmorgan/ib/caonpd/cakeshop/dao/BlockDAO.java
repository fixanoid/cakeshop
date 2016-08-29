package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import java.math.BigInteger;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BlockDAO extends BaseDAO {

    public Block getById(String id) {
        return hibernateTemplate.get(Block.class, id);
    }

    @SuppressWarnings("rawtypes")
    public Block getByNumber(BigInteger number) {
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

    @SuppressWarnings("rawtypes")
    public Block getLatest() {
        Criteria c = getCurrentSession().createCriteria(Block.class);
        c.setProjection(Projections.max("number"));
        List list = c.list();

        if (list == null || list.isEmpty() || list.get(0) == null) {
            return null;
        }

        return getByNumber((BigInteger) list.get(0));
    }

    public void save(Block block) {
        hibernateTemplate.save(block);
    }

    @Override
    public void reset() {
        Session session = getCurrentSession();
        session.createSQLQuery("DELETE FROM PUBLIC.\"Block_transactions\"").executeUpdate();
        session.createSQLQuery("DELETE FROM PUBLIC.\"Block_uncles\"").executeUpdate();
        session.createSQLQuery("DELETE FROM PUBLIC.BLOCKS").executeUpdate();
        session.flush();
        session.clear();
    }

}
