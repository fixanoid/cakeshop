package com.jpmorgan.ib.caonpd.cakeshop.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;

public abstract class BaseDAO {

    @Autowired(required=false)
    private SessionFactory sessionFactory;

    @Autowired(required=false)
    protected HibernateTemplate hibernateTemplate;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public abstract void reset();

}
