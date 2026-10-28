package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.maven.archetypes.hotelflow.Model.Client;

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

    private final ObservableList<Client> masterClients = FXCollections.observableArrayList();
    private FilteredList<Client> filteredClients;

    @FXML
    public void initialize() {
        setupColumns();
        loadSampleClients();
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

    private void loadSampleClients() {
        Client c1 = new Client(101L, "Alexander Schmidt", "alex.schmidt@gmail.com", "+49 170 1234567", "Berlin, Germany");
        Client c2 = new Client(102L, "Elena Popova", "elena.popova@mail.bg", "+359 88 7654321", "Sofia, Bulgaria");
        Client c3 = new Client(103L, "Marcus Vance", "marcus.vance@yahoo.com", "+44 7911 123456", "London, UK");
        c3.markAsRisky("Late cancellation fee unpaid & noise complaints");

        Client c4 = new Client(104L, "Sophia Rossi", "sophia.rossi@outlook.it", "+39 06 6987123", "Rome, Italy");

        masterClients.addAll(c1, c2, c3, c4);
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
                    || client.getFullName().toLowerCase().contains(q)
                    || client.getEmail().toLowerCase().contains(q)
                    || client.getPhone().toLowerCase().contains(q);

            boolean matchesRisk = !onlyRisky || client.isRisky();

            return matchesSearch && matchesRisk;
        });

        updateStatus();
    }

    private void updateStatus() {
        statusLabel.setText("Total Clients: " + filteredClients.size() + " (Active in view)");
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
            selected.markAsRisky("Marked by front desk due to policy infraction");
            showAlert(Alert.AlertType.WARNING, "Blacklisted", selected.getFullName() + " has been flagged as RISKY.");
        }

        clientsTable.refresh();
        applyFilter();
    }

    @FXML
    private void handleNewClient() {
        TextInputDialog dialog = new TextInputDialog("John Doe");
        dialog.setTitle("Quick Add Client");
        dialog.setHeaderText("Register New Guest Profile");
        dialog.setContentText("Enter guest full name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                long newId = 100L + masterClients.size() + 1;
                Client newClient = new Client(newId, name.trim(), name.toLowerCase().replace(" ", ".") + "@example.com", "+00 000 000000", "Pending Address");
                masterClients.add(newClient);
                clientsTable.refresh();
                updateStatus();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Client registered with ID: " + newId);
            }
        });
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        chkOnlyRisky.setSelected(false);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}