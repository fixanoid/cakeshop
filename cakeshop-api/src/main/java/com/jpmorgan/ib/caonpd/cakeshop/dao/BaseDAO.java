package com.jpmorgan.ib.caonpd.cakeshop.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDAO {

    @Autowired(required=false)
    private SessionFactory sessionFactory;

    protected Session getCurrentSession() {
        Session session = null != sessionFactory ?  sessionFactory.getCurrentSession() : null;
        return session;
    }

    public abstract void reset();

}
