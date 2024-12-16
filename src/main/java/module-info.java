module system.chatsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires jdk.httpserver;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;
    requires java.mail;
    requires java.desktop;
    requires org.postgresql.jdbc;


    exports com.haichutieu.chatsystem.client.gui;
    opens com.haichutieu.chatsystem.client.gui to javafx.fxml;
    exports com.haichutieu.chatsystem.client;
    opens com.haichutieu.chatsystem.client to javafx.fxml;
    exports com.haichutieu.chatsystem.client.util;
    opens com.haichutieu.chatsystem.client.util to javafx.fxml;
}