package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.SpamList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javafx.geometry.VPos.*;
import static javafx.scene.layout.GridPane.*;

public class FriendGUI {

    private static FriendGUI instance;
    private final ProgressIndicator loading = new ProgressIndicator();
    @FXML
    private GridPane screen;
    @FXML
    private VBox friendContainer;
    @FXML
    private VBox userListContainer;
    @FXML
    private VBox invitationContainer;
    @FXML
    private TextField friendSearchField;
    @FXML
    private TextField friendRequestSearch;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private TextField userSearchField;
    @FXML
    private Button userSearchButton;

    public ObservableList<Customer> friends = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredFriends;
    private Map<Integer, GridPane> friendGridPaneMap = new HashMap<>();

    private ObservableList<Customer> friendInvitations = FXCollections.observableArrayList();
    private Map<Integer, GridPane> friendInvitationMap = new HashMap<>();

    public FriendGUI() {
        instance = this;
    }

    public static FriendGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        screen.setOnMouseClicked(event -> screen.requestFocus());
        // Fetch for initial friend list
        loading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        friendContainer.getChildren().add(loading);
        invitationContainer.getChildren().add(loading);

        // Initialize ChoiceBox
        statusFilter.setItems(FXCollections.observableArrayList("All", "Online", "Offline"));
        statusFilter.setValue("All");

        // Bind ChoiceBox and TextField to FilteredList
        friendSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateFriendSearchAndFilter());
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateFriendSearchAndFilter());

        long userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_LIST USER " + userId);
        SocketClient.getInstance().sendMessages("GET_FRIEND_REQUEST " + userId);
    }

    @FXML
    void switchToAccountTab() {
        SceneController.setScene("account");
    }

    @FXML
    void switchToChatTab() {
        SceneController.setScene("chat");
    }

    @FXML
    void logout() {
        ChatGUI.getInstance().logout();
    }

    public void onReceiveFriendList(boolean success, List<Customer> friendList) {
        Platform.runLater(() -> {
            friendContainer.getChildren().clear();
            filteredFriends = new FilteredList<>(friends, p -> true);
            filteredFriends.addListener((ListChangeListener<Customer>) c -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        Platform.runLater(() -> {
                            for (Customer friend : c.getRemoved()) {
                                GridPane pane = friendGridPaneMap.remove(friend.getId());
                                friendContainer.getChildren().remove(pane);
                            }
                        });
                    }
                    if (c.wasAdded()) {
                        Platform.runLater(() -> {
                            for (Customer friend : c.getAddedSubList()) {
                                GridPane friendPane = createFriendCard(friend);
                                friendGridPaneMap.put(friend.getId(), friendPane);
                                friendContainer.getChildren().add(friendPane);
                            }
                        });
                    }
                }
            });

            friends.setAll(friendList);
        });
    }

    public void onSendFriendRequest(boolean success) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Request");
            alert.setHeaderText(success ? "Friend request sent!" : "Failed to send friend request. Please try again!");
            alert.showAndWait();
        });
    }

    // Called when your friend request if accepted from a user
    public void onAcceptInvitation(Customer friend) {
        Platform.runLater(() -> {
            friends.add(friend);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Request");
            alert.setHeaderText(friend.getUsername() + " accepted your friend request!");
            alert.showAndWait();
        });
    }

    // Called when your friend request if rejected from a user
    public void onRejectInvitation(String username) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Request");
            alert.setHeaderText(username + " rejected your friend request!");
            alert.showAndWait();
        });
    }

    public void onUnfriendError(int friendID) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to unfriend user" + friendID);
            alert.showAndWait();
        });
    }

    public void onUnfriendSuccess(int friendId) {
        Platform.runLater(() -> {
            // show success message including friend's name
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("You have successfully unfriended " + friends.stream().filter(f -> f.getId() == friendId).findFirst().orElse(new Customer()).getName());
            alert.showAndWait();

            friends.removeIf(f -> f.getId() == friendId);
            //displayFriends();
        });
    }

    private void updateFriendSearchAndFilter() {
        filteredFriends.setPredicate(friend -> {
            String searchText = friendSearchField.getText().toLowerCase();
            String status = statusFilter.getValue();

            boolean matchesSearch = friend.getName().toLowerCase().contains(searchText);
            boolean matchesStatus = switch (status) {
                case "Online" -> SessionManager.getInstance().onlineUsers.contains(friend.getId());
                case "Offline" -> !SessionManager.getInstance().onlineUsers.contains(friend.getId());
                default -> true;
            };

            return matchesSearch && matchesStatus;
        });
    }

//    private void displayFriends() {
//        friendContainer.getChildren().clear();
//
//        for (Customer customer : filteredFriends) {
//            GridPane friendPane = createFriendCard(customer, SessionManager.getInstance().onlineUsers.contains(customer.getId()));
//            friendContainer.getChildren().add(friendPane);
//        }
//    }

    private GridPane createFriendCard(Customer friend) {
        GridPane friendCard = new GridPane();
        friendCard.setPrefHeight(70);

        ColumnConstraints colConst1 = new ColumnConstraints();
        colConst1.setFillWidth(true);
        colConst1.setHgrow(Priority.ALWAYS);
        colConst1.setPercentWidth(70.0);
        friendCard.getColumnConstraints().add(colConst1);
        ColumnConstraints colConst2 = new ColumnConstraints();
        colConst2.setFillWidth(true);
        colConst2.setHgrow(Priority.ALWAYS);
        colConst2.setPercentWidth(30.0);
        friendCard.getColumnConstraints().add(colConst2);

        RowConstraints rowConst1 = new RowConstraints();
        rowConst1.setPercentHeight(50.0);
        friendCard.getRowConstraints().add(rowConst1);
        RowConstraints rowConst2 = new RowConstraints();
        rowConst2.setPercentHeight(50.0);
        friendCard.getRowConstraints().add(rowConst2);

        friendCard.setStyle("-fx-background-color: rgba(127, 81, 255, .8); -fx-background-radius: 8;");
        friendCard.setPadding(new Insets(5, 5, 5, 15));

        Text name = new Text(friend.getName());
        name.setFont(Font.font(null, FontWeight.BOLD, 14));
        name.setStyle("-fx-fill: white;");

        friendCard.add(name, 0, 0);
        GridPane.setMargin(name, new Insets(0, 0, 2, 0));
        GridPane.setValignment(name, BOTTOM);

        TextFlow status = new TextFlow();

        Label statusLabel = new Label("Status: ");
        statusLabel.setFont(Font.font(14));
        statusLabel.setStyle("-fx-fill: white;");

        boolean isOnline = SessionManager.getInstance().onlineUsers.contains(friend.getId());
        Text statusText = new Text(isOnline ? "Online" : "Offline");
        statusText.setFont(Font.font(null, FontWeight.BOLD, 14));
        String color = isOnline ? "#99FF66" : "#DDDDDD";
        statusText.setStyle("-fx-fill: " + color + ";");

        status.getChildren().add(statusLabel);
        status.getChildren().add(statusText);


        friendCard.add(status, 0, 1);
        GridPane.setMargin(status, new Insets(2, 0, 0, 0));
        GridPane.setValignment(status, TOP);

        MenuButton actions = new MenuButton("Actions");
        MenuItem m1 = new MenuItem("Chat");
        m1.setOnAction(e -> chatWithPerson(friend));
        MenuItem m2 = new MenuItem("New Group");
        m2.setOnAction(e -> createNewGroup(friend));
        MenuItem m3 = new MenuItem("Unfriend");
        m3.setOnAction(e -> unfriendFriend(friend, friendCard));
        MenuItem m4 = new MenuItem("Block");
        m4.setOnAction(e -> blockFriend(friend, friendCard));
        MenuItem m5 = new MenuItem("Report Spam");
        m5.setOnAction(e -> reportSpam(friend));

        actions.getItems().addAll(m1, m2, m3, m4, m5);

        friendCard.add(actions, 1, 0, 1, 2);
        GridPane.setValignment(actions, CENTER);
        GridPane.setHalignment(actions, HPos.CENTER);

        return friendCard;
    }

    private void chatWithPerson(Customer friend) {
        int myId = SessionManager.getInstance().getCurrentUser().getId();
        FriendsController.openChatWith(myId, friend.getId());
    }

    private void blockFriend(Customer friend, GridPane friendCard) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm");
        confirmation.setHeaderText("Are you sure you want to block " + friend.getName() + "?");
        confirmation.showAndWait();
        if (confirmation.getResult() != ButtonType.OK) {
            return;
        }

        // BLOCK <userId> <friendId>
        SocketClient.getInstance().sendMessages("BLOCK " + SessionManager.getInstance().getCurrentUser().getId() + " " + friend.getId());
    }

    private void reportSpam(Customer user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm");
        confirmation.setHeaderText("Are you sure you want to report " + user.getName() + " for spam?");
        confirmation.showAndWait();
        if (confirmation.getResult() != ButtonType.OK) {
            return;
        }

        // SPAM <userId> <friendId>
        SocketClient.getInstance().sendMessages("SPAM " + SessionManager.getInstance().getCurrentUser().getId() + " " + user.getId());
    }


    private void unfriendFriend(Customer friend, GridPane friendCard) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm");
        confirmation.setHeaderText("Are you sure you want to unfriend " + friend.getName() + "?");
        confirmation.showAndWait();
        if (confirmation.getResult() != ButtonType.OK) {
            return;
        }

        // UNFRIEND <userId> <friendId>
        SocketClient.getInstance().sendMessages("UNFRIEND " + SessionManager.getInstance().getCurrentUser().getId() + " " + friend.getId());
    }

    private void createNewGroup(Customer friend) {
        Stage createGroupStage = new Stage();
        createGroupStage.initOwner(SceneController.primaryStage);
        createGroupStage.initModality(Modality.NONE);
        createGroupStage.setTitle("Create New Group");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label groupNameLabel = new Label("Group Name:");
        grid.add(groupNameLabel, 0, 1);
        TextField groupNameField = new TextField();
        grid.add(groupNameField, 1, 1);


        Label addedMemberLabel = new Label("Group members");
        grid.add(addedMemberLabel, 0, 2);
        Label addNewMemberLabel = new Label("Add new members");
        addNewMemberLabel.setAlignment(Pos.CENTER);
        grid.add(addNewMemberLabel, 1, 2);

        ObservableList<Customer> addedMembersList = FXCollections.observableArrayList();
        ObservableList<Customer> membersToAddList = FXCollections.observableArrayList();
        addedMembersList.add(friend);
        for (Customer f : friends) {
            if (f.getId() != friend.getId()) {
                membersToAddList.add(f);
            }
        }

        TableView<Customer> addedMembers = new TableView<>();
        addedMembers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Customer, String> nameColumn = new TableColumn<>("Username");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        addedMembers.getColumns().add(nameColumn);

        TableColumn<Customer, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        actionColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button removeButton = new Button("Remove");
                    removeButton.setOnAction(event -> {
                        Customer member = getTableView().getItems().get(getIndex());
                        if (member.getId() == friend.getId()) {
                            return;
                        }
                        addedMembersList.remove(member);
                        membersToAddList.add(member);
                    });
                    setGraphic(removeButton);
                }
            }
        });
        addedMembers.getColumns().add(actionColumn);
        addedMembers.setItems(addedMembersList);
        grid.add(addedMembers, 0, 3);

        TableView<Customer> membersToAdd = new TableView<>();
        membersToAdd.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Customer, String> nameColumn2 = new TableColumn<>("Username");
        nameColumn2.setCellValueFactory(new PropertyValueFactory<>("username"));
        membersToAdd.getColumns().add(nameColumn2);

        TableColumn<Customer, String> actionColumn2 = new TableColumn<>("Action");
        actionColumn2.setCellValueFactory(new PropertyValueFactory<>("name"));
        actionColumn2.setStyle("-fx-alignment: BASELINE_CENTER;");
        actionColumn2.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button addButton = new Button("Add");
                    addButton.setOnAction(event -> {
                        Customer member = getTableView().getItems().get(getIndex());
                        addedMembersList.add(member);
                        membersToAddList.remove(member);
                    });
                    setGraphic(addButton);
                }
            }
        });
        membersToAdd.getColumns().add(actionColumn2);
        membersToAdd.setItems(membersToAddList);
        grid.add(membersToAdd, 1, 3);

        Button createGroupButton = new Button("Create Group");
        createGroupButton.setOnAction(e -> {
            if (groupNameField.getText().isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Group name cannot be empty!");
                alert.showAndWait();
                return;
            }

            if (addedMembersList.size() < 2) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("You must add at least 1 more member to create a group.");
                alert.showAndWait();
                return;
            }

            // CREATE_GROUP <userId> <groupName> END [list_of_member_ids] END
            // list_of_member_ids is a json list of member ids
            StringBuilder memberIds = new StringBuilder("[");
            for (Customer member : addedMembersList) {
                memberIds.append(member.getId()).append(",");
            }
            memberIds.deleteCharAt(memberIds.length() - 1);
            memberIds.append("]");
            SocketClient.getInstance().sendMessages("CREATE_GROUP " + SessionManager.getInstance().getCurrentUser().getId() + " " + groupNameField.getText() + " END " + memberIds + " END");
            createGroupStage.close();
        });
        grid.add(createGroupButton, 0, 4, 2, 1);

        Scene createGroupScene = new Scene(grid, 500, 600);
        createGroupStage.setScene(createGroupScene);
        createGroupStage.showAndWait();
    }


    public void onBlockError(int friendId) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to block user " + friends.stream().filter(f -> f.getId() == friendId).findFirst().orElse(new Customer()).getName());
            alert.showAndWait();
        });
    }

    public void onBlockSuccess(int friendId) {
        Platform.runLater(() -> {
            // show success message including friend's name
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("You have successfully blocked " + friends.stream().filter(f -> f.getId() == friendId).findFirst().orElse(new Customer()).getName());
            alert.showAndWait();

            friends.removeIf(f -> f.getId() == friendId);
            // displayFriends();
        });
    }

    public void onSpamResponse(boolean b) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Spam Report");
            alert.setHeaderText(b ? "User has been reported for spam." : "Failed to report user for spam.");
            alert.showAndWait();
        });
    }

    @FXML
    private void searchForUsers() {
        String prompt = userSearchField.getText();
        if (prompt.isBlank()) {
            return;
        }

        userListContainer.getChildren().removeAll();
        userListContainer.getChildren().add(loading);

        // SEARCH_USER <userId> <prompt>
        SocketClient.getInstance().sendMessages("SEARCH_USER " + SessionManager.getInstance().getCurrentUser().getId() + " " + prompt);
    }

    public void onUserSearch(boolean found, List<Customer> users) {
        Platform.runLater(() -> {
            userListContainer.getChildren().clear();
            if (!found) {
                userListContainer.getChildren().add(new Label("No users found."));
                return;
            }

            for (Customer user : users) {
                GridPane userCard = createUserCard(user, "FIND");
                userListContainer.getChildren().add(userCard);
            }
        });

    }

    private GridPane createUserCard(Customer user, String type) {
        GridPane userCard = new GridPane();
        userCard.setPrefHeight(70);

        ColumnConstraints colConst1 = new ColumnConstraints();
        colConst1.setFillWidth(true);
        colConst1.setHgrow(Priority.ALWAYS);
        colConst1.setPercentWidth(70.0);
        userCard.getColumnConstraints().add(colConst1);
        ColumnConstraints colConst2 = new ColumnConstraints();
        colConst2.setFillWidth(true);
        colConst2.setHgrow(Priority.ALWAYS);
        colConst2.setPercentWidth(30.0);
        userCard.getColumnConstraints().add(colConst2);

        RowConstraints rowConst1 = new RowConstraints();
        rowConst1.setPercentHeight(50.0);
        userCard.getRowConstraints().add(rowConst1);
        RowConstraints rowConst2 = new RowConstraints();
        rowConst2.setPercentHeight(50.0);
        userCard.getRowConstraints().add(rowConst2);

        userCard.setStyle("-fx-background-color: rgba(127, 81, 255, .8); -fx-background-radius: 8;");
        userCard.setPadding(new Insets(5, 5, 5, 15));

        Text name = new Text(user.getName());
        name.setFont(Font.font(null, FontWeight.BOLD, 14));
        name.setStyle("-fx-fill: white;");

        userCard.add(name, 0, 0);
        GridPane.setMargin(name, new Insets(0, 0, 2, 0));
        GridPane.setValignment(name, BOTTOM);

        Text statusText = new Text(user.getUsername());
        statusText.setFont(Font.font(null, 14));
        statusText.setStyle("-fx-fill: white;");

        userCard.add(statusText, 0, 1);
        GridPane.setMargin(statusText, new Insets(2, 0, 0, 0));
        GridPane.setValignment(statusText, TOP);

        if (type.equals("FIND")) {
            MenuButton actions = createActionsMenu(user);
            userCard.add(actions, 1, 0, 1, 2);
            GridPane.setValignment(actions, CENTER);
            GridPane.setHalignment(actions, HPos.CENTER);
        } else {
            Button addFriendButton = new Button("Accept");
            addFriendButton.setOnAction(e -> acceptInvitation(user));
            userCard.add(addFriendButton, 1, 0);
            GridPane.setValignment(addFriendButton, CENTER);
            GridPane.setHalignment(addFriendButton, HPos.CENTER);

            Button rejectButton = new Button("Reject");
            rejectButton.setOnAction(e -> rejectInvitation(user));
            userCard.add(rejectButton, 1, 1);
            GridPane.setValignment(rejectButton, CENTER);
            GridPane.setHalignment(rejectButton, HPos.CENTER);
        }

        return userCard;
    }

    private MenuButton createActionsMenu(Customer user) {
        MenuButton actions = new MenuButton("Actions");
        MenuItem m1 = new MenuItem("Chat");
        m1.setOnAction(e -> chatWithPerson(user));
//        MenuItem m2 = new MenuItem("New Group");
//        m2.setOnAction(e -> createNewGroup(user));
        MenuItem m3 = new MenuItem("Add Friend");
        m3.setOnAction(e -> addFriend(user));
        MenuItem m4 = new MenuItem("Report Spam");
        m4.setOnAction(e -> reportSpam(user));
        actions.getItems().addAll(m1, m3, m4);
        return actions;
    }

    private void addFriend(Customer user) {
        // ADD_FRIEND <userId> <friendId>
        SocketClient.getInstance().sendMessages("ADD_FRIEND " + SessionManager.getInstance().getCurrentUser().getId() + " " + user.getId());
    }

    public void onReceiveFriendRequestList(boolean success, List<Customer> invitationList) {
        Platform.runLater(() -> {
            invitationContainer.getChildren().clear();
            FilteredList<Customer> filteredFriendInvitations = new FilteredList<>(friendInvitations, p -> true);
            // Bind TextField to FilteredList
            friendRequestSearch.textProperty().addListener((obs, oldValue, newValue) -> {
                filteredFriendInvitations.setPredicate(friend -> friend.getName().toLowerCase().contains(newValue.toLowerCase()));
            });

            filteredFriendInvitations.addListener((ListChangeListener<Customer>) c -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        Platform.runLater(() -> {
                            for (Customer friend : c.getRemoved()) {
                                GridPane pane = friendInvitationMap.remove(friend.getId());
                                invitationContainer.getChildren().remove(pane);
                            }
                        });
                    }
                    if (c.wasAdded()) {
                        Platform.runLater(() -> {
                            for (Customer friend : c.getAddedSubList()) {
                                GridPane friendPane = createUserCard(friend, "INVITATION");
                                friendInvitationMap.put(friend.getId(), friendPane);
                                invitationContainer.getChildren().add(friendPane);
                            }
                        });
                    }
                }
            });

            invitationContainer.getChildren().remove(loading);
            friendInvitations.setAll(invitationList);
        });
    }

    private void rejectInvitation(Customer user) {
        // REJECT_INVITATION <userId> <friendId>
        SocketClient.getInstance().sendMessages("ANSWER_INVITATION REJECT " + SessionManager.getInstance().getCurrentUser().getId() + " " + user.getId());
    }

    public void onRejectStatus(boolean success, int friendId) {
        if (!success) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("ERROR Failed to reject friend request.");
                alert.setContentText("Please try again later.");
                alert.show();
            });
            return;
        }

        friendInvitations.removeIf(f -> f.getId() == friendId);
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_REQUEST " + userId);
    }

    private void acceptInvitation(Customer user) {
        // ACCEPT_INVITATION <userId> <friendId>
        SocketClient.getInstance().sendMessages("ANSWER_INVITATION ACCEPT " + SessionManager.getInstance().getCurrentUser().getId() + " " + user.getId());

    }

    public void onAcceptStatus(boolean success, Customer friend) {
        if (!success) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("ERROR Failed to accept friend request.");
                alert.setContentText("Please try again later.");
                alert.show();
            });
            return;
        }

        friendInvitations.removeIf(f -> f.getId() == friend.getId());
        friends.add(friend);

        int userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_LIST USER " + userId);
        SocketClient.getInstance().sendMessages("GET_FRIEND_REQUEST " + userId);
    }

    public void onReceiveNewFriendRequest(Customer friend) {
        System.out.println("Received new friend request from " + friend.getName());
        friendInvitations.add(friend);
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_REQUEST " + userId);

    }

    public void onNewOnlineUser(int userId) {
        Platform.runLater(() -> {
            friends.stream().filter(f -> f.getId() == userId).findFirst().ifPresent(f -> {
                // Change the Online status of the friend

                for (Node node : friendGridPaneMap.get(f.getId()).getChildren()) {
                    if (node instanceof TextFlow) {
                        TextFlow status = (TextFlow) node;
                        Text statusText = (Text) status.getChildren().get(1);
                        statusText.setText("Online");
                        statusText.setStyle("-fx-fill: #99FF66;");
                    }
                }
            });
        });
    }

    public void onOfflineUser(int userId) {
        Platform.runLater(() -> {
            friends.stream().filter(f -> f.getId() == userId).findFirst().ifPresent(f -> {
                // Change the Online status of the friend

                for (Node node : friendGridPaneMap.get(f.getId()).getChildren()) {
                    if (node instanceof TextFlow) {
                        TextFlow status = (TextFlow) node;
                        Text statusText = (Text) status.getChildren().get(1);
                        statusText.setText("Offline");
                        statusText.setStyle("-fx-fill: #DDDDDD;");
                    }
                }
            });
        });
    }

    public void onUnfriendFrom(int friendId) {
        friends.removeIf(f -> f.getId() == friendId);
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_LIST USER " + userId);
    }
}
