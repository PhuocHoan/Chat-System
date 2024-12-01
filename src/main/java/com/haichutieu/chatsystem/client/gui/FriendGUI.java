package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.server.bus.FriendsController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.server.dto.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.scene.input.MouseEvent;


import static javafx.geometry.VPos.*;

public class FriendGUI {

    @FXML
    private GridPane screen;

    @FXML
    private VBox friendContainer;

    @FXML
    private TextField friendSearchField;

    private FriendsController friendsController;

//    private Map<Long, GridPane> friendCards = new HashMap<>();
    private ObservableList<Customer> friends;
    private FilteredList<Customer> filteredFriends;

    @FXML
    public void initialize() {
        screen.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        screen.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

        if (friendsController == null) {
            friendsController = new FriendsController();
            friends = FXCollections.observableArrayList(friendsController.fetchFriendList(1));
            for (Customer friend : friends) {
                System.out.println(friend.getName());
            }

//            displayFriends();

            filteredFriends = new FilteredList<>(friends, p -> true);



            displayFriends();
        }

    }

    @FXML
    public void switchToProfileTab() {

    }

    @FXML
    public void switchToChatTab(MouseEvent event) {
        SceneController.setScene("chat");
    }

    private void setupFriendSearch() {
        friendSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredFriends.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return customer.getName().toLowerCase().contains(newValue.toLowerCase());
            });
            displayFriends();
        });
    }

    private void displayFriends() {
        friendContainer.getChildren().clear();

        for (Customer customer : filteredFriends) {
            GridPane friendPane = createFriendCard(customer);
            friendContainer.getChildren().add(friendPane);
        }
    }

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
        friendCard.add(name, 0, 0);
        GridPane.setMargin(name, new Insets(0, 0, 2, 0));
        GridPane.setValignment(name, BOTTOM);

        Text status = new Text("Status: Online");
        status.setFont(Font.font(14));
        friendCard.add(status, 0, 1);
        GridPane.setMargin(status, new Insets(2, 0, 0, 0));
        GridPane.setValignment(status, TOP);

        MenuButton actions = new MenuButton("Actions");
        MenuItem m1 = new MenuItem("Chat");
        MenuItem m2 = new MenuItem("New Group");
        MenuItem m3 = new MenuItem("Unfriend");
        m3.setOnAction(e -> unfriendFriend(friend, friendCard));
        MenuItem m4 = new MenuItem("Block");
        m4.setOnAction(e -> blockFriend(friend, friendCard));

        actions.getItems().addAll(m1, m2, m3, m4);

        friendCard.add(actions, 1, 0, 1, 2);
        GridPane.setValignment(actions, CENTER);
        GridPane.setHalignment(actions, HPos.CENTER);

        return friendCard;
    }

    private void blockFriend(Customer friend, GridPane friendCard) {
        friends.remove(friend);
        friendContainer.getChildren().remove(friendCard);
        //friendsController.blockFriend(friend);
    }


    private void unfriendFriend(Customer friend, GridPane friendCard) {
        friends.remove(friend);
        friendContainer.getChildren().remove(friendCard);
        //friendsController.removeFriend(friend);
    }
}
