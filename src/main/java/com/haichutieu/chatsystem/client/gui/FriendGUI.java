package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;
import java.util.Set;

import static javafx.geometry.VPos.*;

public class FriendGUI {

    private static FriendGUI instance;
    private final ProgressIndicator friendListLoading = new ProgressIndicator();
    @FXML
    private GridPane screen;
    @FXML
    private VBox friendContainer;
    @FXML
    private VBox userListContainer;
    @FXML
    private TextField friendSearchField;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private TextField userSearchField;
    @FXML
    private Button userSearchButton;

    private ObservableList<Customer> friends = null;
    private FilteredList<Customer> filteredFriends;
    private Set<Integer> onlineFriendIDs;

    public FriendGUI() {
        instance = this;
    }

    public static FriendGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // Fetch for initial friend list
        friendListLoading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        friendContainer.getChildren().add(friendListLoading);
        long userId = SessionManager.getInstance().getCurrentUser().getId();
        SocketClient.getInstance().sendMessages("GET_FRIEND_LIST USER " + userId);
    }

    @FXML
    public void switchToProfileTab() {

    }

    @FXML
    public void switchToChatTab() {
        SceneController.setScene("chat");
    }

    public void onReceiveFriendList(List<Customer> friendList, Set<Integer> onlineFriends) {
        Platform.runLater(() -> {
            if (friendList.isEmpty()) {
                friendContainer.getChildren().remove(friendListLoading);
                friendContainer.getChildren().add(new Label("You have no friends yet."));
                return;
            }
            friends = FXCollections.observableArrayList(friendList);
            filteredFriends = new FilteredList<>(friends, p -> true);
            onlineFriendIDs = onlineFriends;
            // Initialize ChoiceBox
            statusFilter.setItems(FXCollections.observableArrayList("All", "Online", "Offline"));
            statusFilter.setValue("All");
            // Bind ChoiceBox and TextField to FilteredList
            friendSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateFriendSearchAndFilter());
            statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateFriendSearchAndFilter());
            displayFriends();
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
            friends.addFirst(friend);
            displayFriends();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Request");
            alert.setHeaderText(friend.getName() + " accepted your friend request!");
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
            displayFriends();
        });
    }

    private void updateFriendSearchAndFilter() {
        filteredFriends.setPredicate(friend -> {
            String searchText = friendSearchField.getText().toLowerCase();
            String status = statusFilter.getValue();

            boolean matchesSearch = friend.getName().toLowerCase().contains(searchText);
            boolean matchesStatus = switch (status) {
                case "Online" -> onlineFriendIDs.contains(friend.getId());
                case "Offline" -> !onlineFriendIDs.contains(friend.getId());
                default -> true;
            };

            return matchesSearch && matchesStatus;
        });

        displayFriends();
    }

    private void displayFriends() {
        friendContainer.getChildren().clear();

        for (Customer customer : filteredFriends) {
            GridPane friendPane = createFriendCard(customer, onlineFriendIDs.contains(customer.getId()));
            friendContainer.getChildren().add(friendPane);
        }
    }

    private GridPane createFriendCard(Customer friend, boolean isOnline) {
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
//        m1.setOnAction(e -> switchToChatTab(e));
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
        ObservableList<Customer> addedMembersList = FXCollections.observableArrayList();
        addedMembersList.add(friend);
        ListView<String> addedMembers = new ListView<>();
        addedMembers.getItems().add(friend.getName());
        grid.add(addedMembers, 0, 3, 2, 1);
        Button addMemberButton = new Button("Remove");
        grid.add(addMemberButton, 1, 4);

        Label membersToAdd = new Label("Add new members");
        grid.add(membersToAdd, 0, 5);
        ObservableList<Customer> membersToAddList = FXCollections.observableArrayList();
        addedMembersList.add(friend);
//        ListView<String> membersToAdd = new ListView<String>();
//        addedMembers.getItems().add(friend.getName());
//        grid.add(addedMembers, 0, 3, 2, 1);
//        Button addMemberButton = new Button("Remove");
//        grid.add(addMemberButton, 1, 4);


        Scene createGroupScene = new Scene(grid, 500, 500);
        createGroupStage.setScene(createGroupScene);
        createGroupStage.showAndWait();

        // Get the data from the stage

        // Call the conversationController to create a new group

        // Switch the scene to the chat tab
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
            onlineFriendIDs.remove(friendId);
            displayFriends();
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
        userListContainer.getChildren().add(friendListLoading);

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
                GridPane userCard = createUserCard(user);
                userListContainer.getChildren().add(userCard);
            }
        });

    }

    private GridPane createUserCard(Customer user) {
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

        MenuButton actions = new MenuButton("Actions");
        MenuItem m1 = new MenuItem("Chat");
//        m1.setOnAction(e -> switchToChatTab(e));
        MenuItem m2 = new MenuItem("New Group");
        m2.setOnAction(e -> createNewGroup(user));
        MenuItem m3 = new MenuItem("Add Friend");
        m3.setOnAction(e -> addFriend(user));
        MenuItem m4 = new MenuItem("Report Spam");
        m4.setOnAction(e -> reportSpam(user));

        actions.getItems().addAll(m1, m2, m3, m4);

        userCard.add(actions, 1, 0, 1, 2);
        GridPane.setValignment(actions, CENTER);
        GridPane.setHalignment(actions, HPos.CENTER);

        return userCard;
    }

    private void addFriend(Customer user) {
        // ADD_FRIEND <userId> <friendId>
        SocketClient.getInstance().sendMessages("ADD_FRIEND " + SessionManager.getInstance().getCurrentUser().getId() + " " + user.getId());
    }
}
