package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.bus.ChatAppController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatGUI {

    private static ChatGUI instance;
    private final ObservableList<ChatList> conversations = FXCollections.observableArrayList();
    private final Map<Long, GridPane> conversationGridPaneMap = new HashMap<>(); // Map<conversation_id, GridPane>
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
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

    public ChatGUI() {
        instance = this;
    }

    public static ChatGUI getInstance() {
        return instance;
    }

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

        FilteredList<ChatList> filteredConversations = new FilteredList<>(conversations, p -> true);
        searchChatList.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredConversations.setPredicate(chat -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Filter by the group name
                String lowerCaseFilter = newValue.toLowerCase();
                return chat.conversationName.toLowerCase().contains(lowerCaseFilter);
            });
        });

        filteredConversations.addListener((ListChangeListener<ChatList>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (ChatList removedItem : change.getRemoved()) {
                        removeItemFromChatList(removedItem);
                    }
                }
                if (change.wasAdded()) {
                    for (ChatList addedItem : change.getAddedSubList()) {
                        addItemToChatList(addedItem);
                    }
                }
            }
        });

        ChatAppController.getChatList();
    }

    @FXML
    public void switchToProfileTab() {

    }

    @FXML
    public void switchToFriendsTab(MouseEvent event) throws IOException {
        SceneController.addScene("friends", "gui/client/friends.fxml", "stylesheets/style.css");
        SceneController.setScene("friends");
    }

    private void addItemToChatList(ChatList item) {
        Platform.runLater(() -> {
            GridPane conversation = createSingleConversation(item);
            conversationGridPaneMap.put(item.conversationId, conversation);
            chatList.getChildren().addFirst(conversation);
        });
    }

    private void removeItemFromChatList(ChatList item) {
        Platform.runLater(() -> {
            GridPane conversation = conversationGridPaneMap.remove(item.conversationId);
            if (conversation != null) {
                chatList.getChildren().remove(conversation);
            }
        });
    }

    // when user chat to a conversation or message from another conversation comes in
    private void addNewConversation(ChatList chat) {
        conversations.removeIf(c -> c.conversationId == chat.conversationId);
        conversations.add(chat);
    }

    public GridPane createSingleConversation(ChatList chat) {
        GridPane conversation = new GridPane();
        conversation.setMinHeight(80);
        conversation.setStyle("-fx-background-color: rgba(127, 81, 255, .8); -fx-background-radius: 8;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col1.setMinWidth(10);
        col1.setPercentWidth(25);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col2.setMinWidth(10);
        col2.setPercentWidth(75);

        conversation.getColumnConstraints().addAll(col1, col2);

        // Define row constraints
        RowConstraints row1 = new RowConstraints();
        row1.setMinHeight(10);
        row1.setPrefHeight(30);
        row1.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        RowConstraints row2 = new RowConstraints();
        row2.setMinHeight(10);
        row2.setPrefHeight(30);
        row2.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        conversation.getRowConstraints().addAll(row1, row2);

        ImageView conversationAvatar = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(chat.isGroup ? "../../assets/group.png" : "../../assets/avatar.png")).toExternalForm()));
        conversationAvatar.setFitHeight(59);
        conversationAvatar.setFitWidth(55);
        conversationAvatar.setPreserveRatio(true);
        GridPane.setRowSpan(conversationAvatar, 2);
        GridPane.setHalignment(conversationAvatar, javafx.geometry.HPos.CENTER);
        conversation.add(conversationAvatar, 0, 0);

        Circle circle = new Circle(6, Paint.valueOf("#6be150e0"));
        GridPane.setMargin(circle, new Insets(0, 0, 0, 57));
        circle.setStroke(Paint.valueOf("BLACK"));
        conversation.add(circle, 0, 1);

        Text content = new Text();
        String text;
        if (chat.senderName == null) {
            text = "";
        } else if (chat.senderName.equals(SessionManager.getInstance().getCurrentUser().getUsername())) {
            text = "You: " + chat.latestMessage;
            if (text.length() > 30) {
                text = text.substring(0, 27) + "...";
            }
        } else {
            text = chat.senderName + ": " + chat.latestMessage;
            if (text.length() > 30) {
                text = text.substring(0, 27) + "...";
            }
        }
        content.setText(text);
        GridPane.setValignment(content, javafx.geometry.VPos.TOP);
        content.setFont(new Font(14));
        conversation.add(content, 1, 1);

        HBox conversationInfo = new HBox();
        conversationInfo.setAlignment(Pos.CENTER_LEFT);

        Text conversationName = new Text(chat.conversationName);
        conversationName.setFont(Font.font("System", FontWeight.BOLD, 18));
        HBox.setMargin(conversationName, new Insets(0, 0, -15, 0));
        Text conversationTimeStamp = new Text();
        HBox.setMargin(conversationTimeStamp, new Insets(0, 0, 15, 0));
        // update conversation time, and check for user online every 1 minute
        scheduler.scheduleAtFixedRate(() -> {
            conversationTimeStamp.setText(formatConversationTimeStamp(chat.latestTime));
            if (conversationTimeStamp.getText().equals("Just now")) {
                conversationName.setWrappingWidth(210);
            } else {
                conversationName.setWrappingWidth(240);
            }
            // check online

        }, 0, 1, TimeUnit.MINUTES);
        conversationInfo.getChildren().addAll(conversationName, conversationTimeStamp);

        conversation.add(conversationInfo, 1, 0);
        return conversation;
    }

    public String formatConversationTimeStamp(Timestamp timestamp) {
        long minute = (System.currentTimeMillis() - timestamp.getTime()) / (1000 * 60);
        if (minute < 1) {
            return "Just now";
        }
        if (minute < 60) {
            return (int) (minute) + "m";
        }
        if (minute < 60 * 24) {
            return (int) ((double) minute / 60) + "h";
        }
        if (minute < 60 * 24 * 7) {
            return (int) ((double) minute / (60 * 24)) + "d";
        }
        if (minute < 60 * 24 * 365) {
            return (int) ((double) minute / (60 * 24 * 7)) + "w";
        }
        return (int) ((double) minute / (60 * 24 * 365)) + "y";
    }

    public String formatChatTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp);
    }

    public void chatListResult(String message) {
        // Update the chat list for the first time
        conversations.addAll(Util.deserializeListObject(message, ChatList.class));
        // test
//        ChatList test2 = new ChatList();
//        test2.conversationId = 90;
//        test2.senderName = "fds";
//        test2.latestTime = new Timestamp(System.currentTimeMillis());
//        test2.conversationName = "cucu";
//        test2.latestMessage = "helo be";
//        addNewConversation(test2);
//        ChatList test3 = new ChatList();
//        test3.conversationId = 120;
//        test3.senderName = "con cac";
//        test3.latestTime = new Timestamp(System.currentTimeMillis());
//        test3.conversationName = "lelo leo";
//        test3.latestMessage = "helo bevsadf";
//        addNewConversation(test3);
    }

    void sendMessage() {

    }

    public void renderChatArea() {

    }
}
