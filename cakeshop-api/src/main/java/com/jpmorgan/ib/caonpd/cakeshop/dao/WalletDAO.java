package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Account;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WalletDAO extends BaseDAO {

    public List<Account> list() {
        return hibernateTemplate.loadAll(Account.class);
    }

    public void save(Account account) {
        this.hibernateTemplate.save(account);
    }

    @Override
    public void reset() {
        Session session = getCurrentSession();
        session.createSQLQuery("DELETE FROM ACCOUNTS").executeUpdate();
        session.flush();
        session.clear();
    }

}
