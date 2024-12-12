package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

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

    ObservableList<UserLoginTime> loginList;

    public void initialize() {
        SocketClient.getInstance().sendMessages("LOGIN_HISTORY ALL");
        setupLoginTable();
    }

    private void setupLoginTable() {
        TableColumn<UserLoginTime, Integer> customerIdColumn = new TableColumn<>("ID");
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerIdColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");

        TableColumn<UserLoginTime, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<UserLoginTime, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<UserLoginTime, Timestamp> timeColumn = new TableColumn<>("Time");
        timeColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
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

    public void onLoginHistoryReceived(boolean success, List<UserLoginTime> loginHistory) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("Failed to fetch login history");
                alert.showAndWait();
                return;
            }

            loginList = FXCollections.observableArrayList(loginHistory);
            loginTable.getItems().clear();
            loginTable.setItems(loginList);
        });
    }
}
