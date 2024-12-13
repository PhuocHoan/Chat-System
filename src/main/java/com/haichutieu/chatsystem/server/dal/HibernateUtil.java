package com.haichutieu.chatsystem.server.dal;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateUtil {
    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        try {
            Configuration configuration = new Configuration();

            // Load properties from external file
            Properties properties = new Properties();
            try (InputStream input = HibernateUtil.class.getClassLoader().getResourceAsStream("com/haichutieu/chatsystem/config.properties")) {
                if (input == null) {
                    throw new FileNotFoundException("config.properties file not found in the classpath");
                }
                properties.load(input);
            } catch (Exception ex) {
                System.err.println("Failed to load database configuration properties: " + ex.getMessage());
                throw new RuntimeException("Could not load database configuration.", ex);
            }

            // Apply properties to Hibernate configuration
            configuration.addProperties(properties);

            // Load mappings from hibernate.cfg.xml
            configuration.configure(HibernateUtil.class.getResource("../../hibernate.cfg.xml"));

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
