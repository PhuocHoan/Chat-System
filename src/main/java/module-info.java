module system.chatsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens system.chatsystem to javafx.fxml;
    exports system.chatsystem;
}