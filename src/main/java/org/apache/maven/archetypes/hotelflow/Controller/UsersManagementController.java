package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.maven.archetypes.hotelflow.Model.Hotel;
import org.apache.maven.archetypes.hotelflow.Model.Role;
import org.apache.maven.archetypes.hotelflow.Model.User;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.util.Optional;

public class UsersManagementController {

    @FXML private Label lblHeaderTitle;
    @FXML private Label lblHeaderSubtitle;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblRoleUsers;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private Label statusMessageLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, Role> colRole;
    @FXML private TableColumn<User, String> colHotel;
    @FXML private TableColumn<User, String> colStatus;

    private final ObservableList<User> masterUsers = DataStore.getInstance().getUsers();
    private FilteredList<User> filteredUsers;
    private Role targetRole = null;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        updateKpisAndStatus();
    }

    public void setRoleFilter(Role role) {
        this.targetRole = role;
        if (role != null) {
            if (role == Role.OWNER) {
                lblHeaderTitle.setText("Owner Account Management (FR-03)");
                lblHeaderSubtitle.setText("Administrator Portal: Create, monitor, and manage hotel owner credentials.");
                roleFilterCombo.setValue("OWNER");
            } else if (role == Role.RECEPTIONIST) {
                lblHeaderTitle.setText("Receptionist Staff Management (FR-06)");
                lblHeaderSubtitle.setText("Manager Portal: Register front-desk receptionists assigned to your hotel.");
                roleFilterCombo.setValue("RECEPTIONIST");
            } else if (role == Role.MANAGER) {
                lblHeaderTitle.setText("Manager Account Directory (FR-05)");
                lblHeaderSubtitle.setText("Owner Portal: Review registered hotel managers and their assigned locations.");
                roleFilterCombo.setValue("MANAGER");
            }
        }
        applyFilters();
    }

    private void setupTable() {
        colUserId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getUserId()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colFullName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRole()));

        colRole.setCellFactory(col -> new TableCell<User, Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case ADMINISTRATOR:
                            setStyle("-fx-text-fill: #9333ea; -fx-font-weight: bold;");
                            break;
                        case OWNER:
                            setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                            break;
                        case MANAGER:
                            setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                            break;
                        case RECEPTIONIST:
                            setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #475569;");
                            break;
                    }
                }
            }
        });

        colHotel.setCellValueFactory(data -> {
            Long hId = data.getValue().getHotelId();
            if (hId == null) {
                return new SimpleStringProperty("Global / All Hotels");
            }
            Hotel h = DataStore.getInstance().findHotelById(hId);
            return new SimpleStringProperty(h != null ? h.getName() + " (#" + hId + ")" : "Hotel #" + hId);
        });

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "ACTIVE" : "DISABLED"));
        colStatus.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        filteredUsers = new FilteredList<>(masterUsers, p -> true);
        usersTable.setItems(filteredUsers);
    }

    private void setupFilters() {
        roleFilterCombo.getItems().addAll("ALL Roles", "ADMINISTRATOR", "OWNER", "MANAGER", "RECEPTIONIST");
        roleFilterCombo.setValue("ALL Roles");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String roleStr = roleFilterCombo.getValue();

        filteredUsers.setPredicate(u -> {
            boolean matchSearch = q.isEmpty()
                    || (u.getUsername() != null && u.getUsername().toLowerCase().contains(q))
                    || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                    || String.valueOf(u.getUserId()).contains(q);

            boolean matchRole = roleStr == null || roleStr.equals("ALL Roles")
                    || (u.getRole() != null && u.getRole().name().equalsIgnoreCase(roleStr));

            return matchSearch && matchRole;
        });

        usersTable.refresh();
        updateKpisAndStatus();
    }

    private void updateKpisAndStatus() {
        int total = masterUsers.size();
        long active = masterUsers.stream().filter(User::isActive).count();
        long targetCount = filteredUsers.size();

        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(total));
        if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(active));
        if (lblRoleUsers != null) lblRoleUsers.setText(String.valueOf(targetCount));

        if (statusMessageLabel != null) {
            statusMessageLabel.setText("Showing " + filteredUsers.size() + " accounts in current view.");
        }
    }

    @FXML
    private void handleNewUser() {
        Dialog<User> dialog = new Dialog<>();
        String roleName = targetRole != null ? targetRole.name() : "User";
        dialog.setTitle("Create New " + roleName + " Account");
        dialog.setHeaderText("Register new credentials for role-based authorization");

        ButtonType createBtn = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("e.g. maria.petrova");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Initial password (e.g. 123)");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("e.g. Maria Petrova");

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. maria@varna-hotels.bg");

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().setAll(Role.values());
        roleCombo.setValue(targetRole != null ? targetRole : Role.RECEPTIONIST);

        ComboBox<Hotel> hotelCombo = new ComboBox<>();
        hotelCombo.getItems().addAll(DataStore.getInstance().getHotels());
        if (!hotelCombo.getItems().isEmpty()) hotelCombo.setValue(hotelCombo.getItems().get(0));

        grid.add(new Label("Username *:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password *:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name *:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email *:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Role *:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Assigned Hotel:"), 0, 5);
        grid.add(hotelCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                String uName = usernameField.getText() != null ? usernameField.getText().trim() : "";
                String pwd = passwordField.getText() != null ? passwordField.getText().trim() : "";
                String fName = fullNameField.getText() != null ? fullNameField.getText().trim() : "";
                String email = emailField.getText() != null ? emailField.getText().trim() : "";

                if (uName.isEmpty() || pwd.isEmpty() || fName.isEmpty() || email.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please complete all required fields.");
                    return null;
                }

                // Check duplicate username
                if (DataStore.getInstance().findUserByUsername(uName) != null) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Username", "Username '" + uName + "' is already taken.");
                    return null;
                }

                long maxId = masterUsers.stream().mapToLong(u -> u.getUserId() != null ? u.getUserId() : 0L).max().orElse(0L);
                Long hId = hotelCombo.getValue() != null ? hotelCombo.getValue().getHotelId() : null;

                return new User(maxId + 1, uName, pwd, fName, email, roleCombo.getValue(), hId);
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(newUser -> {
            DataStore.getInstance().addUser(newUser);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Account Created",
                    "User '" + newUser.getUsername() + "' (" + newUser.getRole() + ") registered successfully!\n" +
                    "They can now log in using password '" + newUser.getPasswordHash() + "'.");
        });
    }

    @FXML
    private void handleToggleActive() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a user account first.");
            return;
        }

        selected.setActive(!selected.isActive());
        usersTable.refresh();
        updateKpisAndStatus();
        showAlert(Alert.AlertType.INFORMATION, "Status Changed",
                "User '" + selected.getUsername() + "' is now " + (selected.isActive() ? "ACTIVE" : "DISABLED") + ".");
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        roleFilterCombo.setValue(targetRole != null ? targetRole.name() : "ALL Roles");
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        applyFilters();
        usersTable.refresh();
        updateKpisAndStatus();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
