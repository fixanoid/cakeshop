package com.jpmorgan.ib.caonpd.cakeshop.dao;

import com.jpmorgan.ib.caonpd.cakeshop.model.Account;
import java.util.ArrayList;

import java.util.List;
import org.hibernate.Criteria;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WalletDAO extends BaseDAO {

    public List<Account> list() {
        if (null != getCurrentSession()) {
            Criteria criteria = getCurrentSession().createCriteria(Account.class);
            return criteria.list();
        }
        return new ArrayList();
    }

    public void save(Account account) {
        if (null != getCurrentSession()) {
            getCurrentSession().save(account);
        }
    }

    @Override
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("DELETE FROM ACCOUNTS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
