package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.maven.archetypes.hotelflow.Model.Client;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.util.Optional;

public class ClientsController {

    @FXML private TextField searchField;
    @FXML private CheckBox chkOnlyRisky;
    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Long> colClientId;
    @FXML private TableColumn<Client, String> colFullName;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colPhone;
    @FXML private TableColumn<Client, String> colAddress;
    @FXML private TableColumn<Client, Boolean> colRisky;
    @FXML private Label statusLabel;

    private final ObservableList<Client> masterClients = DataStore.getInstance().getClients();
    private FilteredList<Client> filteredClients;

    @FXML
    public void initialize() {
        setupColumns();
        loadClientsData();
        setupFiltering();
    }

    private void setupColumns() {
        colClientId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getClientId()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        colRisky.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isRisky()));

        // Risk sütunu rozet renklendirmesi
        colRisky.setCellFactory(col -> new TableCell<Client, Boolean>() {
            @Override
            protected void updateItem(Boolean isRisky, boolean empty) {
                super.updateItem(isRisky, empty);
                if (empty || isRisky == null) {
                    setText(null);
                    setStyle("");
                } else if (isRisky) {
                    setText("RISKY (Blacklist)");
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else {
                    setText("Clean / Verified");
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: normal;");
                }
            }
        });
    }

    private void loadClientsData() {
        filteredClients = new FilteredList<>(masterClients, p -> true);
        clientsTable.setItems(filteredClients);
        updateStatus();
    }

    private void setupFiltering() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        chkOnlyRisky.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        boolean onlyRisky = chkOnlyRisky.isSelected();

        filteredClients.setPredicate(client -> {
            boolean matchesSearch = q.isEmpty()
                    || (client.getFullName() != null && client.getFullName().toLowerCase().contains(q))
                    || (client.getEmail() != null && client.getEmail().toLowerCase().contains(q))
                    || (client.getPhone() != null && client.getPhone().toLowerCase().contains(q))
                    || (client.getClientId() != null && String.valueOf(client.getClientId()).contains(q));

            boolean matchesRisk = !onlyRisky || client.isRisky();

            return matchesSearch && matchesRisk;
        });

        updateStatus();
    }

    private void updateStatus() {
        statusLabel.setText("Total Clients: " + filteredClients.size() + " (Active in view)");
    }

    @FXML
    private void handleNewClient() {
        showClientFormDialog(null);
    }

    @FXML
    private void handleEditClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a client from the table to edit.");
            return;
        }
        showClientFormDialog(selected);
    }

    private void showClientFormDialog(Client existingClient) {
        boolean isEdit = existingClient != null;

        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Client Profile" : "Register New Client");
        dialog.setHeaderText(isEdit
                ? "Update Client Details #" + existingClient.getClientId() + " (FR-07)"
                : "Fill in the complete guest details to create a new profile (FR-07)");

        ButtonType saveButtonType = new ButtonType(isEdit ? "Save Changes" : "Register Client", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 20, 24));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Michael Brown");
        nameField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. michael.brown@gmail.com");

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +90 532 123 4567");

        TextField addressField = new TextField();
        addressField.setPromptText("e.g. Istanbul, Turkey");

        CheckBox chkRisky = new CheckBox("Flag as Risky / Blacklisted");
        chkRisky.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");

        TextField riskReasonField = new TextField();
        riskReasonField.setPromptText("Specify risk reason (e.g. unpaid fees, noise complaints)");
        riskReasonField.setDisable(true);

        chkRisky.selectedProperty().addListener((obs, oldV, isSelected) -> {
            riskReasonField.setDisable(!isSelected);
            if (!isSelected) {
                riskReasonField.clear();
            }
        });

        if (isEdit) {
            nameField.setText(existingClient.getFullName() != null ? existingClient.getFullName() : "");
            emailField.setText(existingClient.getEmail() != null ? existingClient.getEmail() : "");
            phoneField.setText(existingClient.getPhone() != null ? existingClient.getPhone() : "");
            addressField.setText(existingClient.getAddress() != null ? existingClient.getAddress() : "");
            chkRisky.setSelected(existingClient.isRisky());
            if (existingClient.isRisky()) {
                riskReasonField.setDisable(false);
                riskReasonField.setText(existingClient.getRiskReason() != null ? existingClient.getRiskReason() : "");
            }
        }

        grid.add(new Label("Full Name *:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Email Address *:"), 0, 1);
        grid.add(emailField, 1, 1);

        grid.add(new Label("Phone Number *:"), 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(new Label("Address / City:"), 0, 3);
        grid.add(addressField, 1, 3);

        grid.add(chkRisky, 1, 4);

        grid.add(new Label("Risk Reason:"), 0, 5);
        grid.add(riskReasonField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText() != null ? nameField.getText().trim() : "";
                String email = emailField.getText() != null ? emailField.getText().trim() : "";
                String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
                String address = addressField.getText() != null ? addressField.getText().trim() : "";

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Required Fields",
                            "Full Name, Email Address, and Phone Number are required fields.");
                    return null;
                }

                if (address.isEmpty()) {
                    address = "Not specified";
                }

                if (isEdit) {
                    existingClient.setFullName(name);
                    existingClient.setEmail(email);
                    existingClient.setPhone(phone);
                    existingClient.setAddress(address);
                    existingClient.setRisky(chkRisky.isSelected());
                    existingClient.setRiskReason(chkRisky.isSelected()
                            ? (riskReasonField.getText() != null && !riskReasonField.getText().trim().isEmpty() ? riskReasonField.getText().trim() : "Flagged by staff")
                            : null);
                    return existingClient;
                } else {
                    long maxId = masterClients.stream()
                            .mapToLong(c -> c.getClientId() != null ? c.getClientId() : 0L)
                            .max()
                            .orElse(100L);
                    long newId = Math.max(100L, maxId) + 1;

                    Client newClient = new Client(newId, name, email, phone, address);
                    if (chkRisky.isSelected()) {
                        String reason = riskReasonField.getText() != null && !riskReasonField.getText().trim().isEmpty()
                                ? riskReasonField.getText().trim()
                                : "Flagged during registration";
                        newClient.markAsRisky(reason);
                    }
                    return newClient;
                }
            }
            return null;
        });

        Optional<Client> result = dialog.showAndWait();
        result.ifPresent(client -> {
            if (!isEdit) {
                masterClients.add(client);
                showAlert(Alert.AlertType.INFORMATION, "Registration Complete",
                        "Client #" + client.getClientId() + " (" + client.getFullName() + ") registered successfully!");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Client Updated",
                        "Client #" + client.getClientId() + " (" + client.getFullName() + ") has been updated successfully!");
            }
            clientsTable.refresh();
            applyFilter();
            updateStatus();
        });
    }

    @FXML
    private void handleToggleRiskStatus() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Client", "Please select a client from the table first.");
            return;
        }

        if (selected.isRisky()) {
            selected.setRisky(false);
            selected.setRiskReason(null);
            showAlert(Alert.AlertType.INFORMATION, "Status Cleared", selected.getFullName() + " is now marked as clean.");
        } else {
            TextInputDialog reasonDialog = new TextInputDialog("Late cancellation fee unpaid / noise complaint");
            reasonDialog.setTitle("Flag Risky Client");
            reasonDialog.setHeaderText("Specify Reason for Blacklisting");
            reasonDialog.setContentText("Reason:");
            reasonDialog.showAndWait().ifPresentOrElse(reason -> {
                String r = reason.trim().isEmpty() ? "Marked by front desk" : reason.trim();
                selected.markAsRisky(r);
                showAlert(Alert.AlertType.WARNING, "Blacklisted", selected.getFullName() + " has been flagged as RISKY.");
            }, () -> {
                selected.markAsRisky("Marked by front desk due to policy infraction");
                showAlert(Alert.AlertType.WARNING, "Blacklisted", selected.getFullName() + " has been flagged as RISKY.");
            });
        }

        clientsTable.refresh();
        applyFilter();
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        chkOnlyRisky.setSelected(false);
        applyFilter();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
