package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.bus.ChatAppController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.MessageConversation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatGUI {
    private static ChatGUI instance;
    public ObservableList<ChatList> conversations = FXCollections.observableArrayList(); // conversations are in chat
    public Map<Long, GridPane> conversationGridPaneMap = new HashMap<>(); // Map<conversation_id, GridPane>
    public ObservableList<MessageConversation> messages = FXCollections.observableArrayList();
    public Map<Long, VBox> messageVBoxMap = new HashMap<>(); // Map<message_id, VBox>
    public Map<Long, List<Integer>> memberConversation = new HashMap<>(); // store list of member in all conversation
    public ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
    public ChatList isFocusingConversation;

    @FXML
    private ImageView avatar;

    @FXML
    private VBox chatArea;

    @FXML
    private TextField chatField;

    @FXML
    private VBox chatList;

    @FXML
    private HBox deleteAllMessages;

    @FXML
    private Text headerName;

    @FXML
    private Text headerStatus;

    @FXML
    private VBox rightSideBar;

    @FXML
    private StackPane mainChatContainer;

    @FXML
    private Text rightSideBarName;

    @FXML
    private Text rightSideBarStatus;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private GridPane screen;

    @FXML
    private TextField searchChatList;

    @FXML
    private TextField searchMessage;

    public ChatGUI() {
        instance = this;
    }

    public static ChatGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // Handle the enter key event
        chatField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // auto scroll to bottom in scrollpane
        chatArea.heightProperty().addListener((observable, oldValue, newValue) -> chatScrollPane.setVvalue((Double) newValue));

        FilteredList<ChatList> filteredConversations = new FilteredList<>(conversations, p -> true);
        searchChatList.textProperty().addListener((observable, oldValue, newValue) -> filteredConversations.setPredicate(chat -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Filter by the group name
            String lowerCaseFilter = newValue.toLowerCase();
            return chat.conversationName.toLowerCase().contains(lowerCaseFilter);
        }));

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

        // search message field
        VBox searchResultsBox = new VBox();
        searchResultsBox.setPrefWidth(732);
        searchResultsBox.setStyle("-fx-background-color: rgb(161, 112, 228);");
        ScrollPane searchScrollPane = new ScrollPane(searchResultsBox);
        searchScrollPane.setVisible(false); // Initially hidden
        // Adjust position and size of searchScrollPane dynamically
        searchScrollPane.setMaxHeight(300); // Default max height
        searchScrollPane.setStyle("-fx-border-color: gray; -fx-border-width: 1px;");
        // Position searchScrollPane above the chat content
        StackPane.setAlignment(searchScrollPane, Pos.TOP_CENTER);
        StackPane.setMargin(searchScrollPane, new Insets(78, 18, 0, 10)); // Adjust top margin

        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        screen.setOnMouseClicked(event -> {
            screen.requestFocus();
            searchScrollPane.setVisible(false);
        });

        searchMessage.textProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            searchResultsBox.getChildren().clear();
            if (newValue.isEmpty()) {
                searchScrollPane.setVisible(false);
            } else {
                searchScrollPane.setVisible(true);
                messages.forEach(message -> {
                    if (message.message.toLowerCase().contains(newValue.toLowerCase())) {
                        // create message UI in search scrollpane
                        Text content = new Text(message.message);
                        String text = message.message;
                        if (text.length() > 90) {
                            text = text.substring(0, 87) + "...";
                        }
                        content.setText(text);
                        content.setFontSmoothingType(FontSmoothingType.LCD);
                        TextFlow textFlow = new TextFlow(content);
                        textFlow.setPrefWidth(600);
                        textFlow.getStyleClass().add("message-search");
                        textFlow.setPadding(new Insets(10, 10, 10, 10));
                        Text time = new Text(formatChatTimeStamp(message.time));
                        HBox.setMargin(time, new Insets(0, 10, 0, 10));
                        HBox hbox = new HBox(textFlow, time);
                        hbox.setAlignment(Pos.CENTER_LEFT);

                        hbox.setOnMouseClicked(event -> {
                            // jump to clicked message
                            VBox targetMessage = messageVBoxMap.get(message.id);
                            double scrollPosition = targetMessage.getBoundsInParent().getMinY() / chatArea.getHeight();
                            chatScrollPane.setVvalue(scrollPosition);

                            // Highlight the target message
                            targetMessage.setStyle("-fx-background-color: yellow;");
                            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), targetMessage);
                            fadeTransition.setFromValue(1);
                            fadeTransition.setToValue(0.8);
                            fadeTransition.setOnFinished(e -> {
                                targetMessage.setStyle(null);
                                targetMessage.setOpacity(1);
                            }); // Restore original style
                            fadeTransition.play();
                            searchScrollPane.setVisible(false); // Hide the searchScrollPane after clicking
                        });
                        Text sender = new Text(message.senderName);
                        VBox vBox = new VBox(sender, hbox);
                        VBox.setMargin(vBox, new Insets(5, 5, 5, 5));
                        searchResultsBox.getChildren().add(vBox);
                    }
                });
            }
        }));

        searchMessage.setOnMouseClicked(event -> {
            if (!searchMessage.getText().isEmpty()) {
                Platform.runLater(() -> searchScrollPane.setVisible(true));
            }
        });

        // Add components to StackPane
        mainChatContainer.getChildren().add(searchScrollPane);

        ChatAppController.getOnlineUsers();
        ChatAppController.getAllMemberConversation();
        ChatAppController.getChatList();
    }

    @FXML
    void logout() {
        ChatAppController.offlineUser();
        SocketClient.getInstance().handleLogout();
        SceneController.scenes.clear();
        SceneController.initScenes();
        SceneController.setScene("login");
    }

    @FXML
    public void switchToFriendsTab() {
        SceneController.setScene("friends");
    }

    @FXML
    void switchToAccountTab() {
        SceneController.setScene("account");
    }

    private void addItemToChatList(ChatList item) {
        Platform.runLater(() -> {
            GridPane conversation = createSingleConversation(item);
            conversationGridPaneMap.put(item.conversationID, conversation);
            chatList.getChildren().addFirst(conversation);
        });
    }

    private void removeItemFromChatList(ChatList item) {
        Platform.runLater(() -> {
            GridPane conversation = conversationGridPaneMap.remove(item.conversationID);
            if (conversation != null) {
                chatList.getChildren().remove(conversation);
            }
        });
    }

    private void addItemToMessageConversation(MessageConversation message) {
        Platform.runLater(() -> {
            VBox item;
            if (message.senderName == null) {
                item = createMessageSystem(message);
            } else {
                item = message.senderName.equals(SessionManager.getInstance().getCurrentUser().getName()) ? createMessageTo(message) : createMessageFrom(message);
            }
            messageVBoxMap.put(message.id, item);
            chatArea.getChildren().add(item);
        });
    }

    private void removeItemFromMessageConversation(MessageConversation message) {
        Platform.runLater(() -> {
            VBox item = messageVBoxMap.remove(message.id);
            if (item != null) {
                chatArea.getChildren().remove(item);
            }
        });
    }

    private void removeAllItemFromMessageConversation() {
        Platform.runLater(() -> {
            messageVBoxMap.clear();
            chatArea.getChildren().clear();
        });
    }

    // when user chat to a conversation or message from another conversation comes in
    private void addNewConversation(ChatList chat) {
        conversations.removeIf(c -> c.conversationID == chat.conversationID);
        conversations.add(chat);
    }

    public GridPane createSingleConversation(ChatList chat) {
        GridPane conversation = new GridPane();
        conversation.setMinHeight(80);

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
        circle.setVisible(false);
        conversation.add(circle, 0, 1);

        Text content = new Text();
        String text;
        if (chat.senderName == null) {
            if (!chat.latestMessage.isEmpty()) {
                text = chat.latestMessage; // system message
            } else {
                text = "";
            }
        } else {
            if (chat.senderName.equals(SessionManager.getInstance().getCurrentUser().getName())) {
                text = "You: " + chat.latestMessage;
            } else {
                if (chat.isGroup) {
                    text = chat.senderName + ": " + chat.latestMessage;
                } else {
                    text = chat.latestMessage;
                }
            }
        }
        if (text.length() > 30) {
            text = text.substring(0, 27) + "...";
        }
        if (isFocusingConversation != null) {
            content.setFill(chat.isSeen || chat.conversationID == isFocusingConversation.conversationID ? Color.BLACK : Color.YELLOW);
        } else {
            content.setFill(chat.isSeen ? Color.BLACK : Color.YELLOW);
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
            // update conversation time
            if (chat.latestTime != null) {
                conversationTimeStamp.setText(formatConversationTimeStamp(chat.latestTime));
                if (conversationTimeStamp.getText().equals("Just now")) {
                    conversationName.setWrappingWidth(210);
                } else {
                    conversationName.setWrappingWidth(230);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(() -> {
            // check online conversation
            // if exist an online member in this conversation -> conversation status: online
            if (!memberConversation.isEmpty()) {
                circle.setVisible(memberConversation.get(chat.conversationID).stream().anyMatch(user ->
                        SessionManager.getInstance().onlineUsers.contains(user)
                ));
            } else {
                circle.setVisible(false);
            }
            if (isFocusingConversation != null && isFocusingConversation.conversationID == chat.conversationID) {
                String status = circle.isVisible() ? "Online" : "Offline";
                headerStatus.setText(status);
                rightSideBarStatus.setText(status);
            }
        }, 2, 5, TimeUnit.SECONDS); // delay first 2s

        conversationInfo.getChildren().addAll(conversationName, conversationTimeStamp);

        conversation.add(conversationInfo, 1, 0);

        conversation.getStyleClass().add("conversation"); // styling for conversation

        conversation.setOnMouseClicked(event -> {
            if (isFocusingConversation != null && isFocusingConversation.conversationID == chat.conversationID) {
                return;
            }
            content.setFill(Color.BLACK);
            avatar.setImage(new Image(Objects.requireNonNull(getClass().getResource(chat.isGroup ? "../../assets/group.png" : "../../assets/avatar.png")).toExternalForm()));
            avatar.setFitHeight(131);
            avatar.setFitWidth(95);
            avatar.setPreserveRatio(true);
            isFocusingConversation = new ChatList(chat);
            ChatAppController.getMessageConversation(chat.conversationID);
            ChatAppController.updateStatusConversation(chat.conversationID); // update has seen status conversation
            headerName.setText(chat.conversationName);
            rightSideBarName.setText(chat.conversationName);
            mainChatContainer.setVisible(true);
            rightSideBar.setVisible(true);
            deleteAllMessages.setOnMouseClicked(e -> onRemoveAll());
        });

        return conversation;
    }

    public void getAllMemberConversation(Map<Long, List<Integer>> memberConversationServer) {
        memberConversation.putAll(memberConversationServer);
    }

    public void getMessageConversation(List<MessageConversation> messagesServer) {
        messages.setAll(messagesServer);
        removeAllItemFromMessageConversation();
        messages.forEach(this::addItemToMessageConversation);
    }

    // create a message style from user to others
    public VBox createMessageTo(MessageConversation message) {
        Text messageTo = new Text(message.message);
        messageTo.setFont(Font.font(14));
        messageTo.setFontSmoothingType(FontSmoothingType.LCD);
        TextFlow textFlow = new TextFlow(messageTo);
        textFlow.setPrefWidth(525);
        textFlow.getStyleClass().add("message-to");
        textFlow.setPadding(new Insets(10, 10, 10, 10));
        Button remove = new Button("remove");
        remove.setOnMouseClicked(event -> onRemove(message));
        Text time = new Text(formatChatTimeStamp(message.time));
        HBox.setMargin(time, new Insets(0, 10, 0, 10));
        HBox hbox = new HBox(remove, time, textFlow);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return new VBox(hbox);
    }

    // create a message style from others to user
    public VBox createMessageFrom(MessageConversation message) {
        Text messageFrom = new Text(message.message);
        messageFrom.setFont(Font.font(14));
        messageFrom.setFontSmoothingType(FontSmoothingType.LCD);
        TextFlow textFlow = new TextFlow(messageFrom);
        textFlow.setPrefWidth(525);
        textFlow.getStyleClass().add("message-from");
        textFlow.setPadding(new Insets(10, 10, 10, 10));
        Button remove = new Button("remove");
        remove.setOnMouseClicked(event -> onRemove(message));
        Text time = new Text(formatChatTimeStamp(message.time));
        HBox.setMargin(time, new Insets(0, 10, 0, 10));
        HBox hbox = new HBox(textFlow, time, remove);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Text sender = new Text(message.senderName);
        return new VBox(sender, hbox);
    }

    // create a message style from others to user
    public VBox createMessageSystem(MessageConversation message) {
        Text messageSystem = new Text(message.message);
        messageSystem.setFont(Font.font(14));
        messageSystem.setFontSmoothingType(FontSmoothingType.LCD);
        messageSystem.getStyleClass().add("message-system");
        TextFlow textFlow = new TextFlow(messageSystem);
        textFlow.setPrefWidth(525);
        textFlow.setPadding(new Insets(10, 10, 10, 10));
        textFlow.setTextAlignment(TextAlignment.CENTER);
        return new VBox(textFlow);
    }

    void onRemove(MessageConversation message) {
        if (message.senderID != SessionManager.getInstance().getCurrentUser().getId()) {
            // if message is not mine then just remove on my side
            removeMessageMe(message);
        } else {
            // my message
            if ((System.currentTimeMillis() - message.time.getTime()) / (1000 * 60 * 60 * 24) > 1) {
                // if message is over 1 day then just remove on my side
                removeMessageMe(message);
            } else {
                // remove for all members
                ChatAppController.removeMessage(message);
            }
        }
    }

    void onRemoveAll() {
        ChatAppController.removeAllMessage(isFocusingConversation.conversationID);
        messages.clear();
        removeAllItemFromMessageConversation();
        conversations.removeIf(c -> c.conversationID == isFocusingConversation.conversationID);
        Platform.runLater(() -> {
            mainChatContainer.setVisible(false);
            rightSideBar.setVisible(false);
        });
    }

    // remove message on my side
    void removeMessageMe(MessageConversation message) {
        ChatAppController.removeMessage(message.id);
        messages.remove(message);
        removeItemFromMessageConversation(message);
        // if remove the latest message then update the latest message in conversation
        if (message.time.equals(isFocusingConversation.latestTime)) {
            updateConversation(isFocusingConversation, messages.isEmpty() ? null : messages.getLast());
        }
    }

    // for remove message
    void updateConversation(ChatList conversation, MessageConversation messagesServer) {
        ChatList updateConversation = new ChatList(conversation);
        if (messagesServer == null) {
            updateConversation.latestMessage = "";
            updateConversation.latestTime = null;
            updateConversation.senderName = null;
        } else {
            updateConversation.latestMessage = messagesServer.message;
            updateConversation.latestTime = messagesServer.time;
            updateConversation.senderName = messagesServer.senderName;
        }
        if (isFocusingConversation != null && conversation.conversationID == isFocusingConversation.conversationID) {
            updateConversation.isSeen = true;
            isFocusingConversation = new ChatList(updateConversation);
        }
        addNewConversation(updateConversation);
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

    public void chatListResult(List<ChatList> conversation) {
        // Update the chat list for the first time
        conversations.setAll(conversation);
    }

    void sendMessage() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        String sendingMessage = chatField.getText();
        MessageConversation message = new MessageConversation(isFocusingConversation.conversationID, SessionManager.getInstance().getCurrentUser().getId(), SessionManager.getInstance().getCurrentUser().getName(), time, sendingMessage);
        ChatList conversation = new ChatList(isFocusingConversation.conversationID, isFocusingConversation.isGroup ? isFocusingConversation.conversationName : message.senderName, message.senderName, sendingMessage, time, isFocusingConversation.isGroup, false);
        ChatAppController.sendMessage(conversation, message);
        if (isFocusingConversation.isGroup) {
            ++SessionManager.numberGroupChatWith;
        } else {
            ++SessionManager.numberPeopleChatWith;
        }
        chatField.clear();
    }

    // when user send message to a conversation or message from another conversation comes in
    public void receiveMessage(ChatList conversation, MessageConversation message) {
        if (message.senderID == SessionManager.getInstance().getCurrentUser().getId()) {
            conversation.isSeen = true;
            conversation.conversationName = isFocusingConversation.conversationName;
            isFocusingConversation = new ChatList(conversation);
        }
        addNewConversation(conversation);
        messages.add(message);
        addItemToMessageConversation(message);
    }

    // receive remove message from server, this message can be from this user remove or from another user remove
    public void removeMessageAll(MessageConversation message, List<MessageConversation> messagesServer) {
        if (isFocusingConversation != null && message.conversation_id == isFocusingConversation.conversationID) {
            // if user is focusing conversation that someone removes message
            messages.removeIf((oldMessage) ->
                    {
                        if (oldMessage.id == message.id) {
                            removeItemFromMessageConversation(message);
                            return true;
                        }
                        return false;
                    }
            );
        }
        for (ChatList conversation : conversations) {
            if (conversation.conversationID == message.conversation_id) {
                if (message.time.equals(conversation.latestTime)) {
                    updateConversation(conversation, messagesServer.isEmpty() ? null : messagesServer.getLast());
                }
                break;
            }
        }
    }
}
