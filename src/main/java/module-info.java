module system.chatsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.haichutieu.chatsystem to javafx.fxml;
    exports com.haichutieu.chatsystem;
}