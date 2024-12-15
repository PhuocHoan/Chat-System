package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.bus.AdminController;
import com.haichutieu.chatsystem.dto.Conversation;
import com.haichutieu.chatsystem.dto.MemberConversation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class ChatGroup {

    private static ChatGroup instance;
    private final ObservableList<Conversation> groupList = FXCollections.observableArrayList();
    private final ObservableList<MemberConversation> memberList = FXCollections.observableArrayList();

    @FXML
    private TableView<Conversation> groupTable;

    @FXML
    private TableView<MemberConversation> membersTable;

    @FXML
    private VBox screen;

    @FXML
    private TextField searchGroup;

    public ChatGroup() {
        instance = this;
    }

    public static ChatGroup getInstance() {
        return instance;
    }
    
    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        screen.setOnMouseClicked(event -> screen.requestFocus());

        setupTableGroup();
        setupTableMember();

        // search group name
        FilteredList<Conversation> filterGroupList = new FilteredList<>(groupList, p -> true);
        searchGroup.textProperty().addListener((observable, oldValue, newValue) -> filterGroupList.setPredicate(group -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Filter by the group name
            String lowerCaseFilter = newValue.toLowerCase();
            return group.getName().toLowerCase().contains(lowerCaseFilter);
        }));
        filterGroupList.addListener((ListChangeListener<Conversation>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Conversation removedItem : change.getRemoved()) {
                        removeItemFromGroupList(removedItem);
                    }
                }
                if (change.wasAdded()) {
                    for (Conversation addedItem : change.getAddedSubList()) {
                        addItemToGroupList(addedItem);
                    }
                }
            }
        });

        AdminController.fetchGroupList();
    }

    private void setupTableGroup() {
        TableColumn<Conversation, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        idColumn.setSortable(false);

        TableColumn<Conversation, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setSortable(true);

        TableColumn<Conversation, Timestamp> dateCreatedColumn = new TableColumn<>("Date Created");
        dateCreatedColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        dateCreatedColumn.setSortable(true);
        dateCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        dateCreatedColumn.setCellFactory(column -> new TableCell<>() {
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

        groupTable.setRowFactory(groupTable -> {
            TableRow<Conversation> row = new TableRow<>();

            row.setOnMouseClicked(event -> AdminController.fetchMemberList(row.getItem().getId()));

            return row;
        });

        groupTable.getColumns().setAll(idColumn, nameColumn, dateCreatedColumn);
    }

    private void setupTableMember() {
        TableColumn<MemberConversation, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        idColumn.setSortable(false);

        TableColumn<MemberConversation, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setSortable(true);

        TableColumn<MemberConversation, Boolean> isAdminColumn = new TableColumn<>("Admin");
        isAdminColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        nameColumn.setSortable(true);

        membersTable.getColumns().setAll(idColumn, nameColumn, isAdminColumn);
    }

    private void addItemToGroupList(Conversation item) {
        Platform.runLater(() -> {
            groupTable.getItems().add(item);
        });
    }

    private void removeItemFromGroupList(Conversation item) {
        Platform.runLater(() -> {
            groupTable.getItems().remove(item);
        });
    }

    public void onFetchGroupList(List<Conversation> groupListReceive) {
        groupList.setAll(groupListReceive);
    }

    public void onFetchMemberList(List<MemberConversation> memberListReceive) {
        memberList.setAll(memberListReceive);
        Platform.runLater(() -> {
            membersTable.setItems(memberList);
        });
    }
}
