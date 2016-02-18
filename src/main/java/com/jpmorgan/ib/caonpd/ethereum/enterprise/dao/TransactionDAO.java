package com.jpmorgan.ib.caonpd.ethereum.enterprise.dao;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TransactionDAO {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    private HibernateTemplate hibernateTemplate;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public Transaction getById(String id) {
        return hibernateTemplate.get(Transaction.class, id);
    }

    public void save(Transaction tx) {
        hibernateTemplate.save(tx);
    }

    public void reset() {
        getCurrentSession().createSQLQuery("DELETE FROM PUBLIC.\"Transaction_logs\"").executeUpdate();
        getCurrentSession().createSQLQuery("DELETE FROM TRANSACTIONS").executeUpdate();
    }

}
