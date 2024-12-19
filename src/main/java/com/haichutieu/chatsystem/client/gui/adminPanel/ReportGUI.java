package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.bus.AdminController;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.SpamList;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import javafx.application.Platform;
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

public class ReportGUI {
    private static ReportGUI instance;
    @FXML
    TableView<UserLoginTime> loginTable;
    @FXML
    TableView<SpamList> spamTable;
    @FXML
    TableView<Customer> newUserTable;
    @FXML
    DatePicker firstDate;
    @FXML
    DatePicker secondDate;
    @FXML
    TextField spamSearchField;
    @FXML
    TextField newUserSearchField;
    @FXML
    ChoiceBox<String> spamFilter;
    @FXML
    ChoiceBox<String> newAccountFilter;
    private ObservableList<UserLoginTime> loginList;
    private ObservableList<SpamList> spamList;
    private FilteredList<SpamList> spamListFiltered;
    private SortedList<SpamList> spamListSorted;
    private ObservableList<Customer> newAccountList;
    private FilteredList<Customer> newAccountListFiltered;
    private SortedList<Customer> newAccountListSorted;

    public ReportGUI() {
        instance = this;
    }

    public static ReportGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        AdminController.fetchSpamList(null, null);
        SocketClient.getInstance().sendMessages("LOGIN_HISTORY ALL 50");
        SocketClient.getInstance().sendMessages("FETCH_NEW_ACCOUNTS 20");
        setupLoginTable();
        setupSpamTable();
        setupNewAccountTable();
    }

    private void setupLoginTable() {
        TableColumn<UserLoginTime, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<UserLoginTime, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<UserLoginTime, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<UserLoginTime, Timestamp> timeColumn = new TableColumn<>("Time");
        timeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });

        loginTable.getColumns().addAll(customerIdColumn, usernameColumn, nameColumn, timeColumn);
    }

    private void setupSpamTable() {
        TableColumn<SpamList, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        usernameColumn.setSortable(true);

        TableColumn<SpamList, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        emailColumn.setSortable(false);

        TableColumn<SpamList, String> reportedColumn = new TableColumn<>("User Reported");
        reportedColumn.setCellValueFactory(new PropertyValueFactory<>("userReported"));
        reportedColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        reportedColumn.setSortable(false);

        TableColumn<SpamList, Timestamp> timeColumn = new TableColumn<>("Time");
        timeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        timeColumn.setSortable(true);
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setCellFactory(column -> new TableCell<>() {
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

        TableColumn<SpamList, Boolean> lockAccountColumn = new TableColumn<>("Action");
        lockAccountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        lockAccountColumn.setCellValueFactory(new PropertyValueFactory<>("getIsLocked"));
        lockAccountColumn.setCellFactory(column -> {
            // Create a MenuButton for each customer row
            // Add actions to the MenuButton
            return new TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Create a MenuButton for each customer row
                        MenuButton menuButton = new MenuButton("Actions");

                        // Add actions to the MenuButton
                        MenuItem lockItem = new MenuItem("Lock Account");
                        lockItem.setOnAction(event -> {
                            SpamList spam = getTableView().getItems().get(getIndex());
                            SocketClient.getInstance().sendMessages("LOCK_ACCOUNT " + spam.getPersonID());
                        });

                        MenuItem deleteItem = new MenuItem("Remove Report");
                        deleteItem.setOnAction(event -> {
                            SpamList spam = getTableView().getItems().get(getIndex());
                            SocketClient.getInstance().sendMessages("DELETE_SPAM " + spam.getCustomerID() + " " + spam.getPersonID());
                        });

                        menuButton.getItems().addAll(lockItem, deleteItem);
                        setGraphic(menuButton);
                    }
                }
            };
        });

        UserAndFriendGUI.getInstance().setupDatePickerRange(firstDate, secondDate);

        spamTable.getColumns().addAll(usernameColumn, emailColumn, reportedColumn, timeColumn, lockAccountColumn);

        spamFilter.getItems().addAll("Username", "Email");
        spamFilter.setValue("Username");
    }

    private void setupNewAccountTable() {
        TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, Timestamp> timeColumn = new TableColumn<>("Created Date");
        timeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        timeColumn.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });

        newUserTable.getColumns().addAll(usernameColumn, nameColumn, emailColumn, timeColumn);

        newAccountFilter.getItems().addAll("Name", "Email");
        newAccountFilter.setValue("Name");
    }

    public void onLoginHistoryReceived(boolean success, List<UserLoginTime> loginHistory) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to fetch login history");
                alert.show();
                return;
            }

            loginList = FXCollections.observableArrayList(loginHistory);
            loginTable.getItems().clear();
            loginTable.setItems(loginList);
        });
    }

    public void onNewAccountReceived(boolean success, List<Customer> accounts) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to fetch new accounts list.");
                alert.show();
                return;
            }

            this.newAccountList = FXCollections.observableArrayList(accounts);
            newAccountListFiltered = new FilteredList<>(this.newAccountList, p -> true);
            newAccountListSorted = new SortedList<>(newAccountListFiltered);
            newUserTable.setItems(newAccountListSorted);
            newAccountListSorted.comparatorProperty().bind(newUserTable.comparatorProperty());

            newUserSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterNewAccountList());
            newAccountFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterNewAccountList());
        });
    }

    private void filterNewAccountList() {
        newAccountListFiltered.setPredicate(account -> {
            if (newUserSearchField.getText() == null || newUserSearchField.getText().isEmpty()) {
                return true;
            }

            String lowerCaseFilter = newUserSearchField.getText().toLowerCase();
            boolean isMatchKeyword;
            if (newAccountFilter.getValue().equals("Name")) {
                isMatchKeyword = account.getName().toLowerCase().contains(lowerCaseFilter);
            } else {
                isMatchKeyword = account.getEmail().toLowerCase().contains(lowerCaseFilter);
            }

            return isMatchKeyword;
        });
    }

    public void onLockStatusResponse(boolean success, int id) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to lock account.");
                alert.show();
                return;
            }

            // Delete the locked account from the spam list
            spamList.removeIf(spam -> spam.getPersonID() == id);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Account #" + id + " locked successfully.");
            alert.show();
        });
    }

    public void onDeleteSpam(int customerID, int personID) {
        spamList.removeIf(spam -> spam.getCustomerID() == customerID && spam.getPersonID() == personID);
    }

    public void onSpamListReceived(boolean success, List<SpamList> spamList) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to fetch spam list.");
                alert.show();
                return;
            }

            this.spamList = FXCollections.observableArrayList(spamList);
            spamListFiltered = new FilteredList<>(this.spamList, p -> true);
            spamListSorted = new SortedList<>(spamListFiltered);
            spamTable.setItems(spamListSorted);
            spamListSorted.comparatorProperty().bind(spamTable.comparatorProperty());

            spamSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterSpamList());
            spamFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterSpamList());
            firstDate.valueProperty().addListener((observable, oldValue, newValue) -> filterSpamList());
            secondDate.valueProperty().addListener((observable, oldValue, newValue) -> filterSpamList());
        });
    }

    private void filterSpamList() {
        spamListFiltered.setPredicate(spam -> {
            if (spamSearchField.getText() == null || spamSearchField.getText().isEmpty()) {
                return true;
            }

            String lowerCaseFilter = spamSearchField.getText().toLowerCase();
            boolean isMatchKeyword;
            if (spamFilter.getValue().equals("Username")) {
                isMatchKeyword = spam.getUsername().toLowerCase().contains(lowerCaseFilter);
            } else {
                isMatchKeyword = spam.getEmail().toLowerCase().contains(lowerCaseFilter);
            }

            return isMatchKeyword;
        });
    }

    public void onSubmitSpamDate() {
        if (firstDate.getValue() == null || secondDate.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Filter Date Range");
            alert.setHeaderText("Please select both from and to date.");
            alert.show();
            return;
        }
        spamSearchField.clear();
        spamFilter.setValue("Username");
        AdminController.fetchSpamList(Timestamp.valueOf(firstDate.getValue().atStartOfDay()), Timestamp.valueOf(secondDate.getValue().atStartOfDay()));
    }

    public void onResetSpamDate() {
        spamSearchField.clear();
        firstDate.setValue(null);
        secondDate.setValue(null);
        spamFilter.setValue("Username");
        AdminController.fetchSpamList(null, null);
    }
}
