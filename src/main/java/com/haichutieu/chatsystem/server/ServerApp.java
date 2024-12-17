package com.haichutieu.chatsystem.server;

import com.haichutieu.chatsystem.server.dal.HibernateUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class ServerApp {
    public static Properties properties;
    public static void main(String[] args) {
        // Load properties from external file
        properties = new Properties();
        // InputStream input = HibernateUtil.class.getClassLoader().getResourceAsStream("com/haichutieu/chatsystem/config.properties")
        try (InputStream input = HibernateUtil.class.getClassLoader().getResourceAsStream("com/haichutieu/chatsystem/config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("config.properties file not found in the classpath");
            }
            properties.load(input);
        } catch (Exception ex) {
            System.err.println("Failed to load database configuration properties: " + ex.getMessage());
            throw new RuntimeException("Could not load database configuration.", ex);
        }

        SocketServer.getInstance();
    }
}
