package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.time.LocalDate;
import java.util.Optional;

public class RoomsController {

    @FXML private Label lblTotalRooms;
    @FXML private Label lblAvailableRooms;
    @FXML private Label lblOccupiedRooms;
    @FXML private Label lblMaintenanceRooms;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> floorFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNumber;
    @FXML private TableColumn<Room, String> colFloor;
    @FXML private TableColumn<Room, String> colCategory;
    @FXML private TableColumn<Room, String> colCapacity;
    @FXML private TableColumn<Room, String> colPrice;
    @FXML private TableColumn<Room, RoomStatus> colStatus;
    @FXML private TableColumn<Room, String> colCurrentGuest;

    @FXML private Label statusMessageLabel;

    private final ObservableList<Room> masterRooms = DataStore.getInstance().getRooms();
    private FilteredList<Room> filteredRooms;

    @FXML
    public void initialize() {
        DataStore.getInstance().syncRoomStatuses();
        setupColumns();
        loadRoomsData();
        setupFilters();
        updateKpiCounts();
    }

    private void setupColumns() {
        colRoomNumber.setCellValueFactory(data ->
                new SimpleStringProperty("Room #" + (data.getValue().getRoomNumber() != null ? data.getValue().getRoomNumber() : data.getValue().getRoomId())));

        colFloor.setCellValueFactory(data ->
                new SimpleStringProperty("Floor " + data.getValue().getFloor()));

        colCategory.setCellValueFactory(data -> {
            RoomCategory cat = data.getValue().getRoomCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "Standard");
        });

        colCapacity.setCellValueFactory(data -> {
            RoomCategory cat = data.getValue().getRoomCategory();
            int cap = cat != null ? cat.getCapacity() : 2;
            return new SimpleStringProperty(cap + " Guests");
        });

        colPrice.setCellValueFactory(data -> {
            RoomCategory cat = data.getValue().getRoomCategory();
            double price = cat != null ? cat.getBasePrice() : 85.0;
            return new SimpleStringProperty(String.format("€%.2f / night", price));
        });

        colStatus.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRoomStatus()));

        colStatus.setCellFactory(col -> new TableCell<Room, RoomStatus>() {
            @Override
            protected void updateItem(RoomStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case AVAILABLE:
                            setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                            break;
                        case OCCUPIED:
                            setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                            break;
                        case MAINTENANCE:
                            setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #475569;");
                            break;
                    }
                }
            }
        });

        colCurrentGuest.setCellValueFactory(data -> new SimpleStringProperty(getOccupancyDetail(data.getValue())));
    }

    private String getOccupancyDetail(Room room) {
        if (room == null || room.getRoomId() == null) return "-";
        if (room.getRoomStatus() == RoomStatus.MAINTENANCE) {
            return "Under Maintenance / Out of Service";
        }

        Booking active = DataStore.getInstance().getBookings().stream()
                .filter(b -> b.getRoomId() != null &&
                        (b.getRoomId().equals(room.getRoomId()) ||
                         (room.getRoomNumber() != null && room.getRoomNumber().equals(String.valueOf(b.getRoomId())))))
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CONFIRMED)
                .findFirst()
                .orElse(null);

        if (active != null) {
            Client client = DataStore.getInstance().findClientById(active.getClientId());
            String name = client != null ? client.getFullName() : "Client #" + active.getClientId();
            String statusNote = active.getStatus() == BookingStatus.CHECKED_IN ? "Checked-In" : "Booked / Reserved";
            return name + " (" + statusNote + " | " + active.getBookingNumber() + " | " + active.getStartDate() + " to " + active.getEndDate() + ")";
        }

        return "Vacant - Ready for Booking";
    }

    private void loadRoomsData() {
        filteredRooms = new FilteredList<>(masterRooms, p -> true);
        roomsTable.setItems(filteredRooms);
        updateStatusMessage();
    }

    private void setupFilters() {
        floorFilterCombo.getItems().addAll("ALL Floors", "Floor 1", "Floor 2", "Floor 3", "Floor 4");
        floorFilterCombo.setValue("ALL Floors");

        categoryFilterCombo.getItems().addAll("ALL Categories", "Standard", "Deluxe", "Suite");
        categoryFilterCombo.setValue("ALL Categories");

        statusFilterCombo.getItems().addAll("ALL Statuses", "AVAILABLE", "OCCUPIED", "MAINTENANCE");
        statusFilterCombo.setValue("ALL Statuses");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        floorFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        DataStore.getInstance().syncRoomStatuses();
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String floor = floorFilterCombo.getValue();
        String category = categoryFilterCombo.getValue();
        String status = statusFilterCombo.getValue();

        filteredRooms.setPredicate(room -> {
            boolean matchesSearch = q.isEmpty()
                    || (room.getRoomNumber() != null && room.getRoomNumber().toLowerCase().contains(q))
                    || String.valueOf(room.getRoomId()).contains(q)
                    || (room.getRoomCategory() != null && room.getRoomCategory().getName().toLowerCase().contains(q));

            boolean matchesFloor = floor == null || floor.equals("ALL Floors")
                    || ("Floor " + room.getFloor()).equalsIgnoreCase(floor);

            boolean matchesCategory = category == null || category.equals("ALL Categories")
                    || (room.getRoomCategory() != null && room.getRoomCategory().getName().equalsIgnoreCase(category));

            boolean matchesStatus = status == null || status.equals("ALL Statuses")
                    || (room.getRoomStatus() != null && room.getRoomStatus().name().equalsIgnoreCase(status));

            return matchesSearch && matchesFloor && matchesCategory && matchesStatus;
        });

        roomsTable.refresh();
        updateStatusMessage();
        updateKpiCounts();
    }

    private void updateKpiCounts() {
        int total = masterRooms.size();
        long available = masterRooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.AVAILABLE).count();
        long occupied = masterRooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.OCCUPIED).count();
        long maintenance = masterRooms.stream().filter(r -> r.getRoomStatus() == RoomStatus.MAINTENANCE).count();

        lblTotalRooms.setText(String.valueOf(total));
        lblAvailableRooms.setText(String.valueOf(available));
        lblOccupiedRooms.setText(String.valueOf(occupied));
        lblMaintenanceRooms.setText(String.valueOf(maintenance));
    }

    private void updateStatusMessage() {
        statusMessageLabel.setText("Showing " + filteredRooms.size() + " of " + masterRooms.size() + " hotel rooms.");
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        floorFilterCombo.setValue("ALL Floors");
        categoryFilterCombo.setValue("ALL Categories");
        statusFilterCombo.setValue("ALL Statuses");
        applyFilters();
    }

    @FXML
    private void handleToggleMaintenance() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a room from the table first.");
            return;
        }

        if (selected.getRoomStatus() == RoomStatus.OCCUPIED) {
            showAlert(Alert.AlertType.ERROR, "Room Occupied",
                    "Room #" + selected.getRoomNumber() + " is currently occupied by a staying guest. Cannot put into maintenance.");
            return;
        }

        if (selected.getRoomStatus() == RoomStatus.AVAILABLE) {
            selected.updateStatus(RoomStatus.MAINTENANCE);
            showAlert(Alert.AlertType.INFORMATION, "Status Changed", "Room #" + selected.getRoomNumber() + " is now marked UNDER MAINTENANCE.");
        } else {
            selected.updateStatus(RoomStatus.AVAILABLE);
            showAlert(Alert.AlertType.INFORMATION, "Status Changed", "Room #" + selected.getRoomNumber() + " is now marked AVAILABLE (Clean/Ready).");
        }

        roomsTable.refresh();
        updateKpiCounts();
        applyFilters();
    }

    @FXML
    private void handleNewRoom() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add New Hotel Room");
        dialog.setHeaderText("Register a new room into the hotel inventory (FR-08)");

        ButtonType saveButtonType = new ButtonType("Add Room", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        TextField roomNumField = new TextField();
        roomNumField.setPromptText("e.g. 205");

        TextField floorField = new TextField();
        floorField.setPromptText("e.g. 2");

        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("Standard (€85)", "Deluxe (€130)", "Suite (€220)");
        catCombo.setValue("Standard (€85)");

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumField, 1, 0);
        grid.add(new Label("Floor:"), 0, 1);
        grid.add(floorField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(catCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String num = roomNumField.getText().trim();
                    int floor = Integer.parseInt(floorField.getText().trim());
                    long id = Long.parseLong(num);

                    // Duplicate ID check
                    if (DataStore.getInstance().findRoomById(id) != null) {
                        showAlert(Alert.AlertType.ERROR, "Duplicate", "Room #" + num + " already exists!");
                        return null;
                    }

                    RoomCategory cat;
                    if (catCombo.getValue().startsWith("Deluxe")) {
                        cat = new RoomCategory(2, 1, "Deluxe", "Spacious room with balcony", 130.0, 3);
                    } else if (catCombo.getValue().startsWith("Suite")) {
                        cat = new RoomCategory(3, 1, "Suite", "Luxury suite with panoramic view", 220.0, 4);
                    } else {
                        cat = new RoomCategory(1, 1, "Standard", "Comfortable standard room", 85.0, 2);
                    }

                    return new Room(id, 1L, cat, num, floor, RoomStatus.AVAILABLE);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numeric room number and floor.");
                    return null;
                }
            }
            return null;
        });

        Optional<Room> result = dialog.showAndWait();
        result.ifPresent(newRoom -> {
            masterRooms.add(newRoom);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Room Added", "Room #" + newRoom.getRoomNumber() + " registered successfully!");
        });
    }

    @FXML
    private void handleRefresh() {
        DataStore.getInstance().syncRoomStatuses();
        searchField.clear();
        applyFilters();
        roomsTable.refresh();
        updateKpiCounts();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
