package com.haichutieu.chatsystem.client.gui.adminPanel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class AdminPanelController {
    @FXML
    private GridPane tabPane;

    @FXML
    private Button userBtn;

    @FXML
    private Button groupBtn;

    @FXML
    private Button friendBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private Button statisticBtn;

    TabLoader tabLoader = new TabLoader();

    private void setSelectedTab(Button button) {
        userBtn.setId("btn-default");
        groupBtn.setId("btn-default");
        friendBtn.setId("btn-default");
        reportBtn.setId("btn-default");
        statisticBtn.setId("btn-default");
        button.setId("btn-chosen");
    }

    @FXML
    void handleUserButtonAction(ActionEvent event) {
        setSelectedTab(userBtn);
        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("userManagement"), 1, 0);
    }

    @FXML
    void handleGroupButtonAction(ActionEvent event) {
        setSelectedTab(groupBtn);
        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("chatGroup"), 1, 0);
    }

    @FXML
    void handleFriendButtonAction(ActionEvent event) {
        setSelectedTab(friendBtn);
        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("friend"), 1, 0);
    }

    @FXML
    void handleReportLogButtonAction(ActionEvent event) {
        setSelectedTab(reportBtn);
        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("report"), 1, 0);
    }

    @FXML
    void handleStatisticButtonAction(ActionEvent event) {
        setSelectedTab(statisticBtn);
        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("statistics"), 1, 0);
    }
}
