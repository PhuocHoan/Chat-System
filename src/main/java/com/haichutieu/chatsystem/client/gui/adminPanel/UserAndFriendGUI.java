package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.bus.AdminController;
import com.haichutieu.chatsystem.dto.FriendCount;
import com.haichutieu.chatsystem.dto.OnlineUserCount;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserAndFriendGUI {
    private static UserAndFriendGUI instance;
    private final ObservableList<OnlineUserCount> onlineUserList = FXCollections.observableArrayList();
    private ObservableList<FriendCount> friendCountList = FXCollections.observableArrayList();
    FilteredList<FriendCount> filteredFriendCountList;

    @FXML
    TableView<FriendCount> friendCountTable;
    @FXML
    TextField friendCountSearch;
    @FXML
    TextField friendUserSearch;
    @FXML
    ChoiceBox<String> friendCountFilter;
    @FXML
    private TextField onlineUserCount;

    @FXML
    private ChoiceBox<String> onlineUserCountFilter;

    @FXML
    private TextField onlineUserSearch;

    @FXML
    private TableView<OnlineUserCount> onlineUserTable;

    @FXML
    private DatePicker fromDate;

    @FXML
    private DatePicker toDate;

    public UserAndFriendGUI() {
        instance = this;
    }

    public static UserAndFriendGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        SocketClient.getInstance().sendMessages("FRIEND_COUNT null");
        setupFriendCountTable();

        // online users
        setupOnlineUserTable();
        AdminController.fetchOnlineUserCountList();
    }

    private void setupFriendCountTable() {
        TableColumn<FriendCount, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        customerIdColumn.setSortable(false);

        TableColumn<FriendCount, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<FriendCount, Timestamp> createdDateColumn = new TableColumn<>("Date Created");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdDateColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        createdDateColumn.setCellFactory(column -> new TableCell<>() {
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
        });

        TableColumn<FriendCount, Long> friendCountColumn = new TableColumn<>("Direct Friends");
        friendCountColumn.setCellValueFactory(new PropertyValueFactory<>("friendCount"));
        friendCountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<FriendCount, Long> friendOfFriendCountColumn = new TableColumn<>("Friends of Friends");
        friendOfFriendCountColumn.setCellValueFactory(new PropertyValueFactory<>("friendOfFriendsCount"));
        friendOfFriendCountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        friendCountTable.getColumns().setAll(customerIdColumn, usernameColumn, createdDateColumn, friendCountColumn, friendOfFriendCountColumn);

        friendCountFilter.getItems().addAll("Equals", "Greater Than", "Less Than");
        friendCountFilter.setValue("Equals");
    }

    public void onFriendCountTable(List<FriendCount> friendCounts) {
        friendCountList = FXCollections.observableArrayList(friendCounts);
        filteredFriendCountList = new FilteredList<>(friendCountList, p -> true);
        SortedList<FriendCount> sortedFriendCountList = new SortedList<>(filteredFriendCountList);
        friendCountTable.setItems(sortedFriendCountList);
        sortedFriendCountList.comparatorProperty().bind(friendCountTable.comparatorProperty());

        friendCountSearch.textProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
        friendCountFilter.valueProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
        friendUserSearch.textProperty().addListener((observable, oldValue, newValue) -> friendCountFilter());
    }

    private void friendCountFilter() {
        filteredFriendCountList.setPredicate(friendCount -> {
            boolean isKeywordMatch = true;
            boolean isCountMatch = true;

            if (friendCountSearch.getText() == null || friendCountSearch.getText().isEmpty()) {
                if (friendUserSearch.getText() == null || friendUserSearch.getText().isEmpty()) {
                    return true;
                }

                isKeywordMatch = friendCount.getUsername().toLowerCase().contains(friendUserSearch.getText().toLowerCase());
            } else {
                long value = Long.parseLong(friendCountSearch.getText());
                isCountMatch = switch (friendCountFilter.getValue()) {
                    case "Equals" -> (friendCount.getFriendCount() == value);
                    case "Greater Than" -> (friendCount.getFriendCount() > value);
                    case "Less Than" -> (friendCount.getFriendCount() < value);
                    default -> true;
                };

                if (!(friendUserSearch.getText() == null || friendUserSearch.getText().isEmpty())) {
                    isKeywordMatch = friendCount.getUsername().toLowerCase().contains(friendUserSearch.getText().toLowerCase());
                }
            }

            return isKeywordMatch && isCountMatch;
        });
    }

    private void setupOnlineUserTable() {
        TableColumn<OnlineUserCount, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        customerIdColumn.setSortable(false);

        TableColumn<OnlineUserCount, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<OnlineUserCount, Timestamp> createdDateColumn = new TableColumn<>("Date Created");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdDateColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        createdDateColumn.setCellFactory(column -> new TableCell<>() {
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
        });

        TableColumn<OnlineUserCount, Long> loginTimesColumn = new TableColumn<>("App Usage"); // number of times user login
        loginTimesColumn.setCellValueFactory(new PropertyValueFactory<>("loginTimes"));
        loginTimesColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<OnlineUserCount, Long> numberPeopleChatWithColumn = new TableColumn<>("People Chat With"); // number of people user chat with
        numberPeopleChatWithColumn.setCellValueFactory(new PropertyValueFactory<>("numberPeopleChatWith"));
        numberPeopleChatWithColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<OnlineUserCount, Long> numberGroupChatWithColumn = new TableColumn<>("Groups Chat With"); // number of groups user chat with
        numberGroupChatWithColumn.setCellValueFactory(new PropertyValueFactory<>("numberGroupChatWith"));
        numberGroupChatWithColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        setupDatePickerRange(fromDate, toDate);

        onlineUserTable.getColumns().setAll(customerIdColumn, nameColumn, createdDateColumn, loginTimesColumn, numberPeopleChatWithColumn, numberGroupChatWithColumn);

        FilteredList<OnlineUserCount> filteredOnlineUserList = new FilteredList<>(onlineUserList, p -> true);
        filteredOnlineUserList.addListener((ListChangeListener<OnlineUserCount>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (OnlineUserCount removedItem : change.getRemoved()) {
                        removeItemFromUserOnlineTable(removedItem);
                    }
                }
                if (change.wasAdded()) {
                    for (OnlineUserCount addedItem : change.getAddedSubList()) {
                        addItemToUserOnlineTable(addedItem);
                    }
                }
            }
        });
        onlineUserCountFilter.getItems().addAll("Equals", "Greater Than", "Less Than");
        onlineUserCountFilter.setValue("Equals");
        onlineUserCount.textProperty().addListener((observable, oldValue, newValue) -> onlineUserCountFilter(filteredOnlineUserList));
        onlineUserCountFilter.valueProperty().addListener((observable, oldValue, newValue) -> onlineUserCountFilter(filteredOnlineUserList));
        onlineUserSearch.textProperty().addListener((observable, oldValue, newValue) -> onlineUserCountFilter(filteredOnlineUserList));
    }

    public void setupDatePickerRange(DatePicker fromDate, DatePicker toDate) {
        toDate.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (fromDate.getValue() != null && item.isBefore(fromDate.getValue())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                        if (fromDate.getValue() != null) {
                            long p = ChronoUnit.DAYS.between(fromDate.getValue(), item);
                            setTooltip(new Tooltip("Filter in " + p + " days range"));
                        }
                    }
                };
            }
        });
    }

    public void onOnlineUserTable(List<OnlineUserCount> onlineUserCount) {
        onlineUserList.setAll(onlineUserCount);
    }

    private void onlineUserCountFilter(FilteredList<OnlineUserCount> filteredOnlineUserCountList) {
        filteredOnlineUserCountList.setPredicate(onlineUser -> {
            boolean isKeywordMatch = true;
            if (!(onlineUserSearch.getText() == null || onlineUserSearch.getText().isEmpty())) {
                isKeywordMatch = onlineUser.getName().toLowerCase().contains(onlineUserSearch.getText().toLowerCase());
            }

            boolean isCountMatch = true;
            if (!(onlineUserCount.getText() == null || onlineUserCount.getText().isEmpty())) {
                long value = Long.parseLong(onlineUserCount.getText());
                isCountMatch = switch (onlineUserCountFilter.getValue()) {
                    case "Equals" -> (onlineUser.getLoginTimes() == value);
                    case "Greater Than" -> (onlineUser.getLoginTimes() > value);
                    case "Less Than" -> (onlineUser.getLoginTimes() < value);
                    default -> throw new IllegalStateException("Unexpected value: " + onlineUserCountFilter.getValue());
                };
            }

            return isKeywordMatch && isCountMatch;
        });
    }

    private void addItemToUserOnlineTable(OnlineUserCount item) {
        Platform.runLater(() -> onlineUserTable.getItems().add(item));
    }

    private void removeItemFromUserOnlineTable(OnlineUserCount item) {
        Platform.runLater(() -> onlineUserTable.getItems().remove(item));
    }

    @FXML
    void resetAllOnlineUser() {
        onlineUserSearch.clear();
        fromDate.setValue(null);
        toDate.setValue(null);
        onlineUserCount.clear();
        onlineUserCountFilter.setValue("Equals");
        AdminController.fetchOnlineUserCountList();
    }

    @FXML
    void submitDateRange() {
        if (fromDate.getValue() == null || toDate.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Filter Date Range");
            alert.setHeaderText("Please select both from and to date");
            alert.showAndWait();
            return;
        }
        onlineUserSearch.clear();
        onlineUserCount.clear();
        onlineUserCountFilter.setValue("Equals");
        AdminController.fetchOnlineUserCountList(Timestamp.valueOf(fromDate.getValue().atStartOfDay()), Timestamp.valueOf(toDate.getValue().atStartOfDay()));
    }
}
