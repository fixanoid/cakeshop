package com.jpmorgan.ib.caonpd.ethereum.enterprise.dao;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Account;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WalletDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private HibernateTemplate hibernateTemplate;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public List<Account> list() {
        return hibernateTemplate.loadAll(Account.class);
    }

    public void save(Account account) {
        this.hibernateTemplate.save(account);
    }

    public void reset() {
        Session session = getCurrentSession();
        session.createSQLQuery("DELETE FROM ACCOUNTS").executeUpdate();
        session.flush();
        session.clear();
    }

}
