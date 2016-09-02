package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Event;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TransactionDAO extends BaseDAO {

    public Transaction getById(String id) {
        if (null != getCurrentSession()) {
            return getCurrentSession().get(Transaction.class, id);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Transaction> getContractCreation(String id) {
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Transaction.class);
            c.add(Restrictions.eq("contractAddress", id));
            return c.list();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Transaction> getContractTransactions(String id) {
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Transaction.class);
            c.add(Restrictions.eq("to", id));
            c.addOrder(Order.asc("blockNumber"));
            return c.list();
        }
        return new ArrayList<>();
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
        if (null != getCurrentSession()) {
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
    }

    public void save(Transaction tx) {
        if (null != getCurrentSession()) {
            getCurrentSession().save(tx);
        }
    }

    @Override
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("DELETE FROM TRANSACTIONS_EVENTS").executeUpdate();
            session.createSQLQuery("DELETE FROM EVENTS").executeUpdate();
            session.createSQLQuery("DELETE FROM TRANSACTIONS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
