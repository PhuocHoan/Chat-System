package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.FriendCount;
import com.haichutieu.chatsystem.dto.FriendList;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class FriendGUI {
    private static FriendGUI instance;

    public FriendGUI() {
        instance = this;
    }

    public static FriendGUI getInstance() {
        return instance;
    }

    @FXML
    TextField friendCountSearch;
    @FXML
    TextField friendUserSearch;
    @FXML
    ChoiceBox<String> friendCountFilter;
    @FXML
    TableView<FriendCount> friendCountTable;

    private ObservableList<FriendCount> friendCounts;
    private FilteredList<FriendCount> filteredFriendCountList;
    private SortedList<FriendCount> sortedFriendCountList;

    public void initialize() {
        SocketClient.getInstance().sendMessages("FRIEND_COUNT null");
        setupFriendCountTable();
    }

    private void setupFriendCountTable() {
        TableColumn<FriendCount, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<FriendCount, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<FriendCount, Timestamp> createdColumn = new TableColumn<>("Date Created");
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        createdColumn.setCellFactory(column -> {
            return new TableCell<FriendCount, Timestamp>() {
                private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                @Override
                protected void updateItem(Timestamp item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(sdf.format(item));
                    }
                }
            };
        });

        TableColumn<FriendCount, Long> friendCountColumn = new TableColumn<>("Direct Friends");
        friendCountColumn.setCellValueFactory(new PropertyValueFactory<>("friendCount"));
        friendCountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<FriendCount, Long> friendOfFriendCountColumn = new TableColumn<>("Friends of Friends");
        friendOfFriendCountColumn.setCellValueFactory(new PropertyValueFactory<>("friendOfFriendsCount"));
        friendOfFriendCountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        friendCountTable.getColumns().addAll(customerIdColumn, usernameColumn, createdColumn, friendCountColumn, friendOfFriendCountColumn);
    }

    public void onFriendCountTable(List<FriendCount> friendCounts) {
        this.friendCounts = FXCollections.observableArrayList(friendCounts);
        filteredFriendCountList = new FilteredList<>(this.friendCounts, p -> true);
        sortedFriendCountList = new SortedList<>(filteredFriendCountList);
        friendCountTable.setItems(sortedFriendCountList);
        sortedFriendCountList.comparatorProperty().bind(friendCountTable.comparatorProperty());

        friendCountFilter.getItems().addAll("Equals", "Greater Than", "Less Than");
        friendCountFilter.setValue("Equals");
        friendCountSearch.textProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
        friendCountFilter.valueProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
        friendUserSearch.textProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
    }

    private void friendCountFilter() {
        filteredFriendCountList.setPredicate(friendCount -> {
            boolean isKeywordMatch;
            if (friendUserSearch.getText() == null || friendUserSearch.getText().isEmpty()) {
                isKeywordMatch = true;
            } else {
                isKeywordMatch = friendCount.getUsername().toLowerCase().contains(friendUserSearch.getText().toLowerCase());
            }

            boolean isCountMatch = true;
            if ((friendCountSearch.getText() == null || friendCountSearch.getText().isEmpty())) {
                isCountMatch = true;
            } else {
                long value = Long.parseLong(friendCountSearch.getText());
                switch (friendCountFilter.getValue()) {
                    case "Equals":
                        isCountMatch = (friendCount.getFriendCount() == value);
                        break;
                    case "Greater Than":
                        isCountMatch = (friendCount.getFriendCount() > value);
                        break;
                    case "Less Than":
                        isCountMatch = (friendCount.getFriendCount() < value);
                        break;
                }
            }

            return isCountMatch && isKeywordMatch;
        });
    }
}
