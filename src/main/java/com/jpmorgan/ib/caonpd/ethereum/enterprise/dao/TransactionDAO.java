package com.jpmorgan.ib.caonpd.ethereum.enterprise.dao;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Event;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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

    @SuppressWarnings("unchecked")
    private List<Transaction> getContractCreation(String id) {
        Criteria c = getCurrentSession().createCriteria(Transaction.class);
        c.add(Restrictions.eq("contractAddress", id));
        return c.list();
    }

    @SuppressWarnings("unchecked")
    private List<Transaction> getContractTransactions(String id) {
        Criteria c = getCurrentSession().createCriteria(Transaction.class);
        c.add(Restrictions.eq("to", id));
        c.addOrder(Order.asc("blockNumber"));
        return c.list();
    }

    public List<Transaction> listForContractId(String id) {
        List<Transaction> creationList = getContractCreation(id);
        List<Transaction> txList = getContractTransactions(id);

        // merge lists

        List<Transaction> allTx = new ArrayList<>();

        if (creationList != null && !creationList.isEmpty()) {
            allTx.addAll(creationList);
        }

        if (txList != null && !txList.isEmpty()) {
            allTx.addAll(txList);
        }

        return allTx;
    }

    public void save(List<Transaction> txns) {
        Session session = getCurrentSession();
        for (int i = 0; i < txns.size(); i++) {
            Transaction txn = txns.get(i);

            if (txn.getLogs() != null && !txn.getLogs().isEmpty()) {
                for (Event event : txn.getLogs()) {
                    session.save(event);
                }
            }

            session.save(txn);
            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }
    }

    public void save(Transaction tx) {
        hibernateTemplate.save(tx);
    }

    public void reset() {
        Session session = getCurrentSession();
        session.createSQLQuery("DELETE FROM PUBLIC.\"Transaction_logs\"").executeUpdate();
        session.createSQLQuery("DELETE FROM TRANSACTIONS").executeUpdate();
        session.flush();
        session.clear();
    }

}
