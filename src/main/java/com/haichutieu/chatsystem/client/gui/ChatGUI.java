package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class ChatGUI {

    @FXML
    private GridPane screen;

    @FXML
    private Button btn_send_message;

    @FXML
    private VBox chatArea;

    @FXML
    private TextField chatField;

    @FXML
    private ImageView friendsBtn;

    @FXML
    private TextField searchChatList;

    @FXML
    private VBox chatList;

    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        screen.setOnMouseClicked(event -> screen.requestFocus());
        // Handle the enter key event
        btn_send_message.setOnMouseClicked(e -> sendMessage());
        chatField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        friendsBtn.setOnMouseClicked(event -> {
            try {
                switchToFriendsTab(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    void sendMessage() {

    }

    @FXML
    public void switchToProfileTab() {

    }

    public void renderChatList() {
        
    }

    public void renderChatArea() {

    }

    @FXML
    public void switchToFriendsTab(MouseEvent event) throws IOException {
        SceneController.addScene("friends", "gui/client/friends.fxml", "stylesheets/style.css");
        SceneController.setScene("friends");
    }


}
