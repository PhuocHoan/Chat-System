package com.haichutieu.chatsystem.client.adminPanel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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

    @FXML
    void handleUserButtonAction(ActionEvent event) {
        TabLoader tabLoader = new TabLoader();

        userBtn.setId("btn-chosen");
        groupBtn.setId("btn-default");
        friendBtn.setId("btn-default");
        reportBtn.setId("btn-default");
        statisticBtn.setId("btn-default");

        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("userManagement"),1,0 );
    }

    @FXML
    void handleGroupButtonAction(ActionEvent event) {
        TabLoader tabLoader = new TabLoader();

        groupBtn.setId("btn-chosen");
        userBtn.setId("btn-default");
        friendBtn.setId("btn-default");
        reportBtn.setId("btn-default");
        statisticBtn.setId("btn-default");

        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("chatGroup"),1,0 );
    }

    @FXML
    void handleFriendButtonAction(ActionEvent event) {
        TabLoader tabLoader = new TabLoader();

        friendBtn.setId("btn-chosen");
        userBtn.setId("btn-default");
        groupBtn.setId("btn-default");
        reportBtn.setId("btn-default");
        statisticBtn.setId("btn-default");

        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("friend"),1,0 );
    }

    @FXML
    void handleReportLogButtonAction(ActionEvent event) {
        TabLoader tabLoader = new TabLoader();

        reportBtn.setId("btn-chosen");
        userBtn.setId("btn-default");
        groupBtn.setId("btn-default");
        friendBtn.setId("btn-default");
        statisticBtn.setId("btn-default");

        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("report"),1,0 );
    }

    @FXML
    void handleStatisticButtonAction(ActionEvent event) {
        TabLoader tabLoader = new TabLoader();

        statisticBtn.setId("btn-chosen");
        userBtn.setId("btn-default");
        groupBtn.setId("btn-default");
        friendBtn.setId("btn-default");
        reportBtn.setId("btn-default");

        tabPane.getChildren().removeLast();
        tabPane.add(tabLoader.getPane("statistics"),1,0 );
    }
}
