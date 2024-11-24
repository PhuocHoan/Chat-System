module system.chatsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;


    opens com.haichutieu.chatsystem to javafx.fxml;
    exports com.haichutieu.chatsystem;
    exports com.haichutieu.chatsystem.gui.client;
    opens com.haichutieu.chatsystem.gui.client to javafx.fxml;
}