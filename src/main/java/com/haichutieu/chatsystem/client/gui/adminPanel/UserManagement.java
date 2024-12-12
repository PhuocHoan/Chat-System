package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.util.Util;
import jakarta.persistence.Table;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

    public UserManagement() {
        instance = this;
    }

    public static UserManagement getInstance() {
        return instance;
    }

    @FXML
    private HBox titleContainer;

    @FXML
    private TableView<Customer> accountTable;

    @FXML
    private Button addNewAccount;

    @FXML
    private TextField searchField;

    private final ProgressIndicator loading = new ProgressIndicator();
    ObservableList<Customer> accountList;
    FilteredList<Customer> filteredAccountList;

    public void initialize() {
        loading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        titleContainer.getChildren().add(loading);
        setupTable();
        SocketClient.getInstance().sendMessages("FETCH_ACCOUNT_LIST ALL");
        // Bind ChoiceBox and TextField to FilteredList
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
        });
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
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            alert.setContentText("Invalid email address.");
        }
    }


    private void setupTable() {
        TableColumn<Customer, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");

        TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, Date> birthColumn = new TableColumn<>("Birthday");
        birthColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        birthColumn.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        birthColumn.setCellFactory(column -> {
            return new TableCell<Customer, Date>() {
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
            };
        });

        TableColumn<Customer, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

        TableColumn<Customer, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        genderColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");

        TableColumn<Customer, Boolean> statusColumn = new TableColumn<>("Status");
        statusColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isLock"));
        statusColumn.setCellFactory(column -> {
            return new TableCell<Customer, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item ? "Locked" : "Active");
                    }
                }
            };
        });

        TableColumn<Customer, Timestamp> dateCreatedColumn = new TableColumn<>("Date Created");
        dateCreatedColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
        dateCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        dateCreatedColumn.setCellFactory(column -> {
            return new TableCell<Customer, Timestamp>() {
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

        TableColumn<Customer, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        // Create a cell factory for the actions column with a MenuButton
        actionsColumn.setCellFactory(column -> {
            TableCell<Customer, String> cell = new TableCell<>() {
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

                        MenuItem lockItem = new MenuItem(getTableRow().getItem().isIsLock() ? "Unlock" : "Lock");
                        lockItem.setOnAction(event -> toggleLockStatus(getTableRow().getItem()));

                        MenuItem changePasswordItem = new MenuItem("Change Password");
                        changePasswordItem.setOnAction(event -> changePassword(getTableRow().getItem()));

                        MenuItem loginHistoryItem = new MenuItem("View Login History");
                        loginHistoryItem.setOnAction(event -> {
                            SocketClient.getInstance().sendMessages("LOGIN_HISTORY USER " + getTableRow().getItem().getId());
                        });

                        MenuItem friendsItem = new MenuItem("View Friends");
                        friendsItem.setOnAction(event -> {
                            SocketClient.getInstance().sendMessages("GET_FRIEND_LIST ADMIN " + getTableRow().getItem().getId());
                        });

                        menuButton.getItems().addAll(editItem, deleteItem, lockItem, changePasswordItem, loginHistoryItem, friendsItem);

                        setGraphic(menuButton);
                    }
                }
            };
            return cell;
        });

        accountTable.getColumns().addAll(idColumn, usernameColumn, nameColumn, emailColumn, birthColumn, addressColumn, genderColumn, statusColumn, dateCreatedColumn, actionsColumn);
    }

    public void onFetchAccountList(List<Customer> accounts) {
        Platform.runLater(() -> {
            titleContainer.getChildren().remove(loading);
            accountList = FXCollections.observableArrayList(accounts);
            filteredAccountList = new FilteredList<>(accountList, p -> true);
            accountTable.setItems(filteredAccountList);
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
        ChoiceBox<String> gender = new ChoiceBox<String>();
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
            if (birthdate.getValue() != null)
            {
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
                Customer updatedAccount = Util.deserializeObject(message.split(" ", 2)[1], Customer.class);
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

    private void changePassword(Customer account) {

    }

    private void toggleLockStatus(Customer account) {

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
        ChoiceBox<String> gender = new ChoiceBox<String>();
        gender.getItems().setAll(FXCollections.observableArrayList("Male", "Female"));
        gender.setValue("Male");
        grid.add(gender, 1, 6);

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
            newAccount.setAdmin(false);

            SocketClient.getInstance().sendMessages("ADD_ACCOUNT " + Util.serializeObject(newAccount));
        });
        grid.add(submit, 0, 7, 2, 1);

        Scene scene = new Scene(grid, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    public void onAddNewAccount(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("OK")) {
                accountList.add(Util.deserializeObject(message.split(" ", 2)[1], Customer.class));
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
            userIdColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");
            loginTable.getColumns().add(userIdColumn);

            TableColumn<LoginTime, Timestamp> loginTimeColumn = new TableColumn<>("Login Time");
            loginTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            loginTimeColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
            loginTimeColumn.setCellFactory(column -> {
                return new TableCell<LoginTime, Timestamp>() {
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
            stage.setTitle("Friends List - User #"+ userID);

            TableView<Customer> friendsTable = new TableView<>();
            friendsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<Customer, Integer> userIdColumn = new TableColumn<>("UserID");
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
            userIdColumn.setStyle( "-fx-alignment: BASELINE_CENTER;");
            friendsTable.getColumns().add(userIdColumn);

            TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
            friendsTable.getColumns().add(usernameColumn);

            TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
            friendsTable.getColumns().add(nameColumn);

            friendsTable.setItems(FXCollections.observableArrayList(friends));

            Scene scene = new Scene(friendsTable, 300, 400);
            stage.setScene(scene);
            stage.show();
        });
    }
}
