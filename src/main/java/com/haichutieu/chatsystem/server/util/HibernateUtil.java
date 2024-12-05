package com.haichutieu.chatsystem.server.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        try {
            sessionFactory = new Configuration().configure(HibernateUtil.class.getResource("../../hibernate.cfg.xml")).buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static HibernateUtil getInstance() {
        return HibernateUtil.SessionManagerHelper.INSTANCE;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static class SessionManagerHelper {
        private static final HibernateUtil INSTANCE = new HibernateUtil();
    }
}