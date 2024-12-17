package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserManagement {
    private static UserManagement instance;
    private final ProgressIndicator loading = new ProgressIndicator();
    ObservableList<Customer> accountList;
    FilteredList<Customer> filteredAccountList;
    SortedList<Customer> sortedAccountList;
    @FXML
    private HBox titleContainer;
    @FXML
    private TableView<Customer> accountTable;
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> searchBy;
    @FXML
    private ChoiceBox<String> statusFilter;

    public UserManagement() {
        instance = this;
    }

    public static UserManagement getInstance() {
        return instance;
    }
    
    @FXML
    public void initialize() {
        loading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        titleContainer.getChildren().add(loading);
        setupTable();
        SocketClient.getInstance().sendMessages("FETCH_ACCOUNT_LIST ALL");
        // Bind ChoiceBox and TextField to FilteredList

    }

    private void validateInput(Alert alert, String username, String name, String email) {
        if (username.isEmpty()) {
            alert.setContentText("Username field is required!");
        } else if (!username.matches("^(?=.{6,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$")) {
            alert.setContentText("Username must be 6-20 characters long and contain only letters, numbers and underscores.");
        }
        if (name.isEmpty()) {
            alert.setContentText("Name field is required!");
        }
        if (email.isEmpty()) {
            alert.setContentText("Email field is required!");
        } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            alert.setContentText("Invalid email address.");
        }
    }

    private void setupTable() {
        TableColumn<Customer, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        idColumn.setSortable(false);

        TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        usernameColumn.setSortable(true);

        TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setSortable(true);

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        emailColumn.setSortable(false);

        TableColumn<Customer, Date> birthColumn = new TableColumn<>("Birthday");
        birthColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        birthColumn.setSortable(false);
        birthColumn.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        birthColumn.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });

        TableColumn<Customer, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        genderColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        TableColumn<Customer, Boolean> statusColumn = new TableColumn<>("Status");
        statusColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isLock"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item ? "Locked" : "Active");
                }
            }
        });

        TableColumn<Customer, Timestamp> dateCreatedColumn = new TableColumn<>("Date Created");
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

        TableColumn<Customer, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        actionsColumn.setSortable(false);
        // Create a cell factory for the actions column with a MenuButton
        actionsColumn.setCellFactory(column -> {
            // Create a MenuButton for each customer row
            // Add actions to the MenuButton
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Create a MenuButton for each customer row
                        MenuButton menuButton = new MenuButton("Actions");

                        // Add actions to the MenuButton
                        MenuItem editItem = new MenuItem("Edit");
                        editItem.setOnAction(event -> editAccount(getTableRow().getItem()));

                        MenuItem deleteItem = new MenuItem("Delete");
                        deleteItem.setOnAction(event -> deleteAccount(getTableRow().getItem()));

                        MenuItem lockItem = new MenuItem(getTableRow().getItem().getIsLock() ? "Unlock" : "Lock");
                        lockItem.setOnAction(event -> SocketClient.getInstance().sendMessages("TOGGLE_ACCOUNT_STATUS " + getTableRow().getItem().getId()));

                        MenuItem changePasswordItem = new MenuItem("Change Password");
                        changePasswordItem.setOnAction(event -> changePassword(getTableRow().getItem()));

                        MenuItem resetPasswordItem = new MenuItem("Reset Password");
                        resetPasswordItem.setOnAction(event -> SocketClient.getInstance().sendMessages("RESET_PASSWORD NULL " + getTableRow().getItem().getEmail()));

                        MenuItem loginHistoryItem = new MenuItem("View Login History");
                        loginHistoryItem.setOnAction(event -> SocketClient.getInstance().sendMessages("LOGIN_HISTORY USER " + getTableRow().getItem().getId()));

                        MenuItem friendsItem = new MenuItem("View Friends");
                        friendsItem.setOnAction(event -> SocketClient.getInstance().sendMessages("GET_FRIEND_LIST ADMIN " + getTableRow().getItem().getId()));

                        menuButton.getItems().addAll(editItem, deleteItem, lockItem, changePasswordItem, resetPasswordItem, loginHistoryItem, friendsItem);

                        setGraphic(menuButton);
                    }
                }
            };
        });

        accountTable.getColumns().addAll(idColumn, usernameColumn, nameColumn, emailColumn, birthColumn, addressColumn, genderColumn, statusColumn, dateCreatedColumn, actionsColumn);
    }

    public void onFetchAccountList(List<Customer> accounts) {
        Platform.runLater(() -> {
            titleContainer.getChildren().remove(loading);
            accountList = FXCollections.observableArrayList(accounts);
            filteredAccountList = new FilteredList<>(accountList, p -> true);
            sortedAccountList = new SortedList<>(filteredAccountList);
            accountTable.setItems(sortedAccountList);
            sortedAccountList.comparatorProperty().bind(accountTable.comparatorProperty());

            searchBy.setItems(FXCollections.observableArrayList("Username", "Name"));
            searchBy.setValue("Username");
            statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Locked"));
            statusFilter.setValue("All");
            searchField.textProperty().addListener((observable, oldValue, newValue) -> updateSearchAndFilterSearch());
            searchBy.valueProperty().addListener((observable, oldValue, newValue) -> updateSearchAndFilterSearch());
            statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateSearchAndFilterSearch());
        });
    }

    private void updateSearchAndFilterSearch() {
        filteredAccountList.setPredicate(account -> {
            String searchText = this.searchField.getText().toLowerCase();
            String searchBy = this.searchBy.getValue();
            String status = this.statusFilter.getValue();

            boolean isMatchKeyword;
            boolean isMatchStatus = status.equals("All") || (status.equals("Active") && !account.getIsLock()) || (status.equals("Locked") && account.getIsLock());
            if (searchBy.equals("Username")) {
                isMatchKeyword = account.getUsername().toLowerCase().contains(searchText);
            } else {
                isMatchKeyword = account.getName().toLowerCase().contains(searchText);
            }

            return isMatchKeyword && isMatchStatus;
        });
    }

    /* ============================
              EDIT ACCOUNT
       ============================ */

    private void editAccount(Customer account) {
        Stage stage = new Stage();
        stage.initOwner(SceneController.primaryStage);
        stage.initModality(Modality.NONE);
        stage.setTitle("Edit Account");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Username:"), 0, 0);
        TextField username = new TextField(account.getUsername());
        grid.add(username, 1, 0);

        grid.add(new Label("Name:"), 0, 1);
        TextField name = new TextField(account.getName());
        grid.add(name, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        TextField email = new TextField(account.getEmail());
        grid.add(email, 1, 2);

        grid.add(new Label("Gender:"), 0, 3);
        ChoiceBox<String> gender = new ChoiceBox<>();
        gender.getItems().setAll(FXCollections.observableArrayList("Male", "Female"));
        gender.setValue(account.getSex() == null ? account.getSex() : "Male");
        grid.add(gender, 1, 3);

        grid.add(new Label("Address:"), 0, 4);
        String addressStr = account.getAddress() == null ? "" : account.getAddress();
        TextField address = new TextField(addressStr);
        grid.add(address, 1, 4);

        grid.add(new Label("Date of birth:"), 0, 5);
        DatePicker birthdate = new DatePicker();
        if (account.getBirthdate() != null) {
            birthdate.setValue(account.getBirthdate().toLocalDate());
        }
        grid.add(birthdate, 1, 5);

        grid.add(new Label("Role:"), 0, 6);
        CheckBox isAdmin = new CheckBox("Admin");
        isAdmin.setSelected(account.isAdmin());
        grid.add(isAdmin, 1, 6);

        Button submit = new Button("Update");
        submit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(null);
            validateInput(alert, username.getText(), name.getText(), email.getText());
            if (alert.getContentText() != null) {
                alert.showAndWait();
                return;
            }

            Customer newAccount = new Customer();
            newAccount.setId(account.getId());
            newAccount.setUsername(username.getText());
            newAccount.setPassword(account.getPassword());
            newAccount.setName(name.getText());
            newAccount.setEmail(email.getText());
            newAccount.setAddress(address.getText());
            newAccount.setCreateDate(account.getCreateDate());
            if (birthdate.getValue() != null) {
                newAccount.setBirthdate(java.sql.Date.valueOf(birthdate.getValue()));
            }
            newAccount.setSex(gender.getValue());
            newAccount.setAdmin(isAdmin.isSelected());

            SocketClient.getInstance().sendMessages("EDIT_ACCOUNT " + Util.serializeObject(newAccount));
            stage.close();
        });

        grid.add(submit, 0, 7, 2, 1);

        Scene scene = new Scene(grid, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    public void onEditAccount(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("OK")) {
                Customer updatedAccount = Util.deserializeObject(message.split(" ", 2)[1], new TypeReference<>() {
                });
                accountList.replaceAll(account -> {
                    if (account.getId() == updatedAccount.getId()) {
                        return updatedAccount;
                    }
                    return account;
                });
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Edit Account");
                alert.setContentText("Account updated successfully.");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Edit Account");
                alert.setContentText(message.split(" ", 2)[1]);
                alert.show();
            }
        });
    }

    /* ============================
              DELETE ACCOUNT
       ============================ */
    private void deleteAccount(Customer account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setContentText("Are you sure you want to delete account " + account.getUsername() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SocketClient.getInstance().sendMessages("DELETE_ACCOUNT " + account.getId());
            }
        });
    }

    public void onDeleteAccount(String message) {
        Platform.runLater(() -> {
            String[] parts = message.split(" ", 2);
            if (parts[0].equals("OK")) {
                // find in the list the account to delete
                accountList.removeIf(account -> account.getId() == Integer.parseInt(parts[1]));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Delete Account");
                alert.setContentText("Account deleted successfully.");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Delete Account");
                alert.setContentText("Failed to delete account.");
                alert.show();
            }
        });
    }

    /* ============================
           CHANGE PASSWORD
       ============================ */
    private void changePassword(Customer account) {
        Stage stage = new Stage();
        stage.initOwner(SceneController.primaryStage);
        stage.initModality(Modality.NONE);
        stage.setTitle("Change Password");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label notification = new Label();
        notification.setStyle("-fx-text-fill: red;");
        grid.add(notification, 0, 0, 2, 1);

        grid.add(new Label("New Password:"), 0, 1);
        PasswordField newPassword = new PasswordField();
        grid.add(newPassword, 1, 1);

        grid.add(new Label("Confirm Password:"), 0, 2);
        PasswordField checkPassword = new PasswordField();
        grid.add(checkPassword, 1, 2);

        Button submit = new Button("Change Password");
        submit.setOnAction(e -> {
            if (!newPassword.getText().matches("^(?=.*?[0-9])(?=.*?[A-Za-z]).{8,32}$")) {
                notification.setText("Password must be between 8 and 32 characters (A-Z, a-z, 0-9)");
                return;
            }

            if (!newPassword.getText().equals(checkPassword.getText())) {
                notification.setText("Passwords do not match.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(newPassword.getText(), BCrypt.gensalt(10));

            SocketClient.getInstance().sendMessages("CHANGE_PASSWORD " + account.getId() + " " + hashedPassword);
            stage.close();
        });

        grid.add(submit, 1, 3);

        Scene scene = new Scene(grid, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    public void onChangePassword(boolean success, int userID) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Change Password");
                alert.setHeaderText("Failed to change password for account #" + userID);
                alert.show();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Change Password");
            alert.setHeaderText("Password changed successfully for account #" + userID);
            alert.show();
        });
    }

    /* ============================
           RESET PASSWORD
       ============================ */
    public void onResetPassword() {

    }

    /* ============================
           LOCK/UNLOCK ACCOUNT
       ============================ */
    public void onToggleLockStatus(boolean success, int userID) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lock/Unlock Account");
                alert.setHeaderText("Failed to lock/unlock account #" + userID);
                alert.show();
                return;
            }

            accountList.replaceAll(account -> {
                if (account.getId() == userID) {
                    account.setIsLock(!account.getIsLock());
                }
                return account;
            });

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Lock/Unlock Account");
            alert.setHeaderText("Account #" + userID + " status updated successfully.");
            alert.show();
        });
    }

    /* ============================
              ADD NEW ACCOUNT
       ============================ */
    @FXML
    private void addNewAccountEvent() {
        // Create a new stage for filling new account details
        Stage stage = new Stage();
        stage.initOwner(SceneController.primaryStage);
        stage.initModality(Modality.NONE);
        stage.setTitle("Add New Account");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Username:"), 0, 0);
        TextField username = new TextField();
        grid.add(username, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        TextField name = new TextField();
        grid.add(name, 1, 1);
        grid.add(new Label("Password"), 0, 2);
        PasswordField password = new PasswordField();
        grid.add(password, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        TextField email = new TextField();
        grid.add(email, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        TextField address = new TextField();
        grid.add(address, 1, 4);
        grid.add(new Label("Date of birth:"), 0, 5);
        DatePicker birthdate = new DatePicker();
        grid.add(birthdate, 1, 5);

        grid.add(new Label("Gender:"), 0, 6);
        ChoiceBox<String> gender = new ChoiceBox<>();
        gender.getItems().setAll(FXCollections.observableArrayList("Male", "Female"));
        gender.setValue("Male");
        grid.add(gender, 1, 6);

        grid.add(new Label("Admin:"), 0, 7);
        CheckBox isAdmin = new CheckBox("Admin");
        grid.add(isAdmin, 1, 7);

        Button submit = new Button("Create");
        submit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(null);
            validateInput(alert, username.getText(), name.getText(), email.getText());
            if (alert.getContentText() != null) {
                alert.showAndWait();
                return;
            }

            Customer newAccount = new Customer();
            newAccount.setUsername(username.getText());
            newAccount.setName(name.getText());
            String hashedPassword = BCrypt.hashpw(password.getText(), BCrypt.gensalt(10));
            newAccount.setPassword(hashedPassword);
            newAccount.setEmail(email.getText());
            newAccount.setAddress(address.getText());
            if (birthdate.getValue() != null) {
                newAccount.setBirthdate(java.sql.Date.valueOf(birthdate.getValue()));
            }
            newAccount.setSex(gender.getValue());
            newAccount.setCreateDate(new Timestamp(System.currentTimeMillis()));
            newAccount.setIsLock(false);
            newAccount.setAdmin(isAdmin.isSelected());

            SocketClient.getInstance().sendMessages("ADD_ACCOUNT " + Util.serializeObject(newAccount));
        });
        grid.add(submit, 0, 8, 2, 1);

        Scene scene = new Scene(grid, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    public void onAddNewAccount(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("OK")) {
                accountList.add(Util.deserializeObject(message.split(" ", 2)[1], new TypeReference<>() {
                }));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Add New Account");
                alert.setContentText("Account added successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Add New Account");
                alert.setHeaderText(message.split(" ", 2)[0]);
                alert.setContentText(message.split(" ", 2)[1]);
                alert.showAndWait();
            }
        });
    }


    /* ============================
           FETCH LOGIN HISTORY
       ============================ */
    public void onLoginHistoryResponse(boolean status, String type, List<LoginTime> loginTimes) {
        Platform.runLater(() -> {
            if (!status) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login History");
                alert.setContentText("Count not fetch login history.");
                return;
            }

            if (type.equals("ALL")) {
                return;
            }

            Stage stage = new Stage();
            stage.initOwner(SceneController.primaryStage);
            stage.initModality(Modality.NONE);
            stage.setTitle("Login History");

            TableView<LoginTime> loginTable = new TableView<>();
            loginTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            loginTable.getColumns().clear();
            TableColumn<LoginTime, String> userIdColumn = new TableColumn<>("UserID");
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
            userIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
            loginTable.getColumns().add(userIdColumn);

            TableColumn<LoginTime, Timestamp> loginTimeColumn = new TableColumn<>("Login Time");
            loginTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            loginTimeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            loginTimeColumn.setCellFactory(column -> new TableCell<>() {
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
            loginTable.getColumns().add(loginTimeColumn);

            loginTable.setItems(FXCollections.observableArrayList(loginTimes));

            Scene scene = new Scene(loginTable, 300, 400);
            stage.setScene(scene);
            stage.show();
        });
    }

    /* ============================
           FETCH FRIENDS LIST
       ============================ */
    public void onReceiveFriendList(boolean success, int userID, List<Customer> friends) {
        Platform.runLater(() -> {
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User's Friend List");
                alert.setContentText("Count not fetch friends information.");
                return;
            }

            Stage stage = new Stage();
            stage.initOwner(SceneController.primaryStage);
            stage.initModality(Modality.NONE);
            stage.setTitle("Friends List - User #" + userID);

            TableView<Customer> friendsTable = new TableView<>();
            friendsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<Customer, Integer> userIdColumn = new TableColumn<>("UserID");
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            userIdColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
            friendsTable.getColumns().add(userIdColumn);

            TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            friendsTable.getColumns().add(usernameColumn);

            TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            friendsTable.getColumns().add(nameColumn);

            friendsTable.setItems(FXCollections.observableArrayList(friends));

            Scene scene = new Scene(friendsTable, 300, 400);
            stage.setScene(scene);
            stage.show();
        });
    }
}