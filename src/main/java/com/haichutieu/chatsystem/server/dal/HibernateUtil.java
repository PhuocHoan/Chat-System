package com.haichutieu.chatsystem.server.dal;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import static com.haichutieu.chatsystem.server.ServerApp.properties;

public class HibernateUtil {
    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        try {
            Configuration configuration = new Configuration();

            // Apply properties to Hibernate configuration
            configuration.addProperties(properties);

            // Load mappings from hibernate.cfg.xml
            configuration.configure("hibernate.cfg.xml");

            // Build session factory
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex.getMessage());
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
