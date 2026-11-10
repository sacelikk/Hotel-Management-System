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
import org.apache.maven.archetypes.hotelflow.Util.UserSession;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HotelsController {

    @FXML private Label lblTotalHotels;
    @FXML private Label lblManagedHotels;
    @FXML private Label lblUnassignedHotels;
    @FXML private Label lblTotalHotelRooms;

    @FXML private TextField searchField;
    @FXML private Label statusMessageLabel;

    @FXML private TableView<Hotel> hotelsTable;
    @FXML private TableColumn<Hotel, Long> colHotelId;
    @FXML private TableColumn<Hotel, String> colHotelName;
    @FXML private TableColumn<Hotel, String> colAddress;
    @FXML private TableColumn<Hotel, String> colPhone;
    @FXML private TableColumn<Hotel, String> colEmail;
    @FXML private TableColumn<Hotel, String> colManager;
    @FXML private TableColumn<Hotel, String> colOwner;

    private final ObservableList<Hotel> masterHotels = DataStore.getInstance().getHotels();
    private FilteredList<Hotel> filteredHotels;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        updateKpisAndStatus();
    }

    private void setupTable() {
        colHotelId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHotelId()));
        colHotelName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        colManager.setCellValueFactory(data -> {
            Long mId = data.getValue().getManagerId();
            if (mId == null) {
                return new SimpleStringProperty("UNASSIGNED");
            }
            User manager = DataStore.getInstance().findUserById(mId);
            return new SimpleStringProperty(manager != null ? manager.getFullName() + " (#" + mId + ")" : "Manager #" + mId);
        });

        colManager.setCellFactory(col -> new TableCell<Hotel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("UNASSIGNED".equals(item)) {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colOwner.setCellValueFactory(data -> {
            Long oId = data.getValue().getOwnerId();
            if (oId == null) return new SimpleStringProperty("-");
            User owner = DataStore.getInstance().findUserById(oId);
            return new SimpleStringProperty(owner != null ? owner.getFullName() : "Owner #" + oId);
        });

        filteredHotels = new FilteredList<>(masterHotels, p -> true);
        hotelsTable.setItems(filteredHotels);
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal != null ? newVal.trim().toLowerCase() : "";
            filteredHotels.setPredicate(h -> {
                if (q.isEmpty()) return true;
                return (h.getName() != null && h.getName().toLowerCase().contains(q))
                        || (h.getAddress() != null && h.getAddress().toLowerCase().contains(q))
                        || (h.getEmail() != null && h.getEmail().toLowerCase().contains(q))
                        || String.valueOf(h.getHotelId()).contains(q);
            });
            hotelsTable.refresh();
            updateKpisAndStatus();
        });
    }

    private void updateKpisAndStatus() {
        int total = masterHotels.size();
        long managed = masterHotels.stream().filter(h -> h.getManagerId() != null).count();
        long unassigned = total - managed;
        int roomsCount = DataStore.getInstance().getRooms().size();

        if (lblTotalHotels != null) lblTotalHotels.setText(String.valueOf(total));
        if (lblManagedHotels != null) lblManagedHotels.setText(String.valueOf(managed));
        if (lblUnassignedHotels != null) lblUnassignedHotels.setText(String.valueOf(unassigned));
        if (lblTotalHotelRooms != null) lblTotalHotelRooms.setText(String.valueOf(roomsCount));

        if (statusMessageLabel != null) {
            statusMessageLabel.setText("Showing " + filteredHotels.size() + " hotel properties in directory.");
        }
    }

    @FXML
    private void handleNewHotel() {
        Dialog<Hotel> dialog = new Dialog<>();
        dialog.setTitle("Register New Hotel Property");
        dialog.setHeaderText("Owner Portal: Create and register a new hotel in the system (FR-04)");

        ButtonType saveBtn = new ButtonType("Add Hotel", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Royal Palace Varna Resort");

        TextField addressField = new TextField();
        addressField.setPromptText("e.g. 5 Sea Garden Lane, Varna");

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +359 52 789123");

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. royal@varna-hotels.bg");

        List<User> availableManagers = DataStore.getInstance().getUsers().stream()
                .filter(u -> u.getRole() == Role.MANAGER)
                .collect(Collectors.toList());

        ComboBox<User> managerCombo = new ComboBox<>();
        managerCombo.getItems().addAll(availableManagers);
        managerCombo.setPromptText("Optional: Assign initial manager");

        grid.add(new Label("Hotel Name *:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address *:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Phone *:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email *:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Assign Manager:"), 0, 4);
        grid.add(managerCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String name = nameField.getText() != null ? nameField.getText().trim() : "";
                String address = addressField.getText() != null ? addressField.getText().trim() : "";
                String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
                String email = emailField.getText() != null ? emailField.getText().trim() : "";

                if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Information", "Please complete all required fields.");
                    return null;
                }

                long maxId = masterHotels.stream().mapToLong(h -> h.getHotelId() != null ? h.getHotelId() : 0L).max().orElse(0L);
                long newHotelId = maxId + 1;

                User current = UserSession.getInstance().getCurrentUser();
                Long ownerId = (current != null && current.getRole() == Role.OWNER) ? current.getUserId() : 2L;

                Long managerId = managerCombo.getValue() != null ? managerCombo.getValue().getUserId() : null;

                Hotel hotel = new Hotel(newHotelId, ownerId, managerId, name, address, phone, email);
                if (managerId != null) {
                    managerCombo.getValue().setHotelId(newHotelId);
                }
                return hotel;
            }
            return null;
        });

        Optional<Hotel> result = dialog.showAndWait();
        result.ifPresent(hotel -> {
            DataStore.getInstance().addHotel(hotel);
            hotelsTable.refresh();
            updateKpisAndStatus();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Hotel '" + hotel.getName() + "' created successfully!");
        });
    }

    @FXML
    private void handleAssignManager() {
        Hotel selected = hotelsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a hotel from the table first.");
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Assign Hotel Manager (FR-05)");
        dialog.setHeaderText("Assign a designated General Manager for: " + selected.getName());

        ButtonType assignBtn = new ButtonType("Save Assignment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        List<User> managers = DataStore.getInstance().getUsers().stream()
                .filter(u -> u.getRole() == Role.MANAGER)
                .collect(Collectors.toList());

        ComboBox<User> managerCombo = new ComboBox<>();
        managerCombo.getItems().addAll(managers);

        if (selected.getManagerId() != null) {
            User currentMgr = DataStore.getInstance().findUserById(selected.getManagerId());
            if (currentMgr != null) managerCombo.setValue(currentMgr);
        } else if (!managers.isEmpty()) {
            managerCombo.setValue(managers.get(0));
        }

        grid.add(new Label("Hotel:"), 0, 0);
        grid.add(new Label(selected.getName() + " (#" + selected.getHotelId() + ")"), 1, 0);
        grid.add(new Label("Select Manager *:"), 0, 1);
        grid.add(managerCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == assignBtn) {
                User chosen = managerCombo.getValue();
                if (chosen == null) {
                    showAlert(Alert.AlertType.ERROR, "Selection Required", "Please select a manager.");
                    return false;
                }

                return DataStore.getInstance().assignManagerToHotel(selected.getHotelId(), chosen.getUserId());
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(ok -> {
            if (ok) {
                hotelsTable.refresh();
                updateKpisAndStatus();
                showAlert(Alert.AlertType.INFORMATION, "Manager Assigned",
                        "Manager successfully assigned to " + selected.getName() + "!");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        hotelsTable.refresh();
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
