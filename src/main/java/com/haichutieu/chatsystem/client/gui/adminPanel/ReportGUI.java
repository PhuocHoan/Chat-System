package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.SpamList;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Filter;

public class ReportGUI {
    private static ReportGUI instance;

    public ReportGUI() {
        instance = this;
    }

    public static ReportGUI getInstance() {
        return instance;
    }

    @FXML
    TableView<UserLoginTime> loginTable;
    @FXML
    TableView<SpamList> spamTable;

    @FXML
    DatePicker firstDate;
    @FXML
    DatePicker secondDate;
    @FXML
    TextField spamSearchField;
    @FXML
    ChoiceBox<String> spamFilter;

    ObservableList<UserLoginTime> loginList;
    ObservableList<SpamList> spamList;
    FilteredList<SpamList> spamListFiltered;
    SortedList<SpamList> spamListSorted;

    public void initialize() {
        SocketClient.getInstance().sendMessages("SPAM_LIST ALL");
        SocketClient.getInstance().sendMessages("LOGIN_HISTORY ALL");
        setupLoginTable();
        setupSpamTable();
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
        timeColumn.setCellFactory(column -> {
            return new TableCell<>() {
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
            };
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
        timeColumn.setCellFactory(column -> {
            return new TableCell<>() {
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

        TableColumn<SpamList, Boolean> lockAccountColumn = new TableColumn<>("Action");
        lockAccountColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        lockAccountColumn.setCellValueFactory(new PropertyValueFactory<>("isLocked"));
        lockAccountColumn.setCellFactory(column -> {
            return new TableCell<>() {
                private final Button lockButton = new Button("Lock");

                {
                    lockButton.setOnAction(event -> {
                        SpamList spam = getTableView().getItems().get(getIndex());
                        SocketClient.getInstance().sendMessages("LOCK_ACCOUNT " + spam.getPersonID());
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(lockButton);
                    }
                }
            };
        });


        spamTable.getColumns().addAll(usernameColumn, emailColumn, reportedColumn, timeColumn, lockAccountColumn);

        spamFilter.getItems().addAll("Username", "Email");
        spamFilter.setValue("Username");
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
            boolean isMatchKeyword = true;
            if (spamFilter.getValue().equals("Username")) {
                isMatchKeyword = spam.getUsername().toLowerCase().contains(lowerCaseFilter);
            } else {
                isMatchKeyword = spam.getEmail().toLowerCase().contains(lowerCaseFilter);
            }

            boolean isMatchDate = true;
            if (firstDate.getValue() != null || secondDate.getValue() != null) {
                Timestamp first = firstDate.getValue() == null ? new Timestamp(0) : Timestamp.valueOf(firstDate.getValue().atStartOfDay());
                Timestamp second = secondDate.getValue() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.valueOf(secondDate.getValue().atStartOfDay());
                isMatchDate = spam.getTime().after(first) && spam.getTime().before(second);
            }

            return isMatchKeyword && isMatchDate;
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
}
