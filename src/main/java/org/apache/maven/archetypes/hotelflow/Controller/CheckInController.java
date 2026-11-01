package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.maven.archetypes.hotelflow.Model.Booking;
import org.apache.maven.archetypes.hotelflow.Model.BookingStatus;
import org.apache.maven.archetypes.hotelflow.Model.CheckIn;
import org.apache.maven.archetypes.hotelflow.Model.CheckInStatus;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckInController {

    @FXML private TextField searchField;
    @FXML private TableView<CheckIn> checkInTable;
    @FXML private TableColumn<CheckIn, Long> colCheckInId;
    @FXML private TableColumn<CheckIn, Long> colBookingId;
    @FXML private TableColumn<CheckIn, String> colRoomNumber;
    @FXML private TableColumn<CheckIn, String> colCheckInTime;
    @FXML private TableColumn<CheckIn, String> colCheckOutTime;
    @FXML private TableColumn<CheckIn, CheckInStatus> colStatus;
    @FXML private Label statusMessageLabel;

    private final ObservableList<CheckIn> masterList = DataStore.getInstance().getCheckIns();
    private FilteredList<CheckIn> filteredList;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {
        setupColumns();
        loadFrontDeskData();
        setupSearch();
    }

    private void setupColumns() {
        colCheckInId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckInId()));
        colBookingId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBookingId()));
        colRoomNumber.setCellValueFactory(data -> {
            Long bId = data.getValue().getBookingId();
            Booking booking = DataStore.getInstance().findBookingById(bId);
            return new SimpleStringProperty(booking != null ? "Room #" + booking.getRoomId() : "Room #" + (200 + bId));
        });

        colCheckInTime.setCellValueFactory(data -> {
            LocalDateTime time = data.getValue().getActualCheckInTime();
            return new SimpleStringProperty(time != null ? time.format(timeFormatter) : "-");
        });

        colCheckOutTime.setCellValueFactory(data -> {
            LocalDateTime time = data.getValue().getActualCheckOutTime();
            return new SimpleStringProperty(time != null ? time.format(timeFormatter) : "Still Staying");
        });

        colStatus.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));

        colStatus.setCellFactory(col -> new TableCell<CheckIn, CheckInStatus>() {
            @Override
            protected void updateItem(CheckInStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    if (item == CheckInStatus.CHECKED_IN) {
                        setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    } else if (item == CheckInStatus.CHECKED_OUT) {
                        setStyle("-fx-text-fill: #64748b; -fx-font-weight: normal;");
                    } else {
                        setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadFrontDeskData() {
        filteredList = new FilteredList<>(masterList, p -> true);
        checkInTable.setItems(filteredList);
        updateStatus("Displaying " + filteredList.size() + " front desk records.");
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredList.setPredicate(item -> query.isEmpty()
                    || String.valueOf(item.getBookingId()).contains(query)
                    || String.valueOf(item.getCheckInId()).contains(query));
            updateStatus("Found " + filteredList.size() + " matches.");
        });
    }

    @FXML
    private void handleRegisterArrival() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Register Guest Arrival");
        dialog.setHeaderText("Front Desk Check-In");
        dialog.setContentText("Enter Booking ID (e.g., 1, 2, 4):");

        dialog.showAndWait().ifPresent(input -> {
            String trimmed = input.trim();
            if (trimmed.isEmpty()) return;

            Long bookingId;
            try {
                bookingId = Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Booking ID must be a numeric value.");
                return;
            }

            Booking matchedBooking = DataStore.getInstance().findBookingById(bookingId);
            if (matchedBooking == null) {
                showAlert(Alert.AlertType.ERROR, "Not Found",
                        "Booking ID #" + bookingId + " not found in reservations system!");
                return;
            }

            if (matchedBooking.getStatus() == BookingStatus.CANCELLED) {
                showAlert(Alert.AlertType.ERROR, "Reservation Cancelled",
                        "Cannot check-in: Booking #" + bookingId + " is CANCELLED.");
                return;
            }

            CheckIn arrival = DataStore.getInstance().performCheckIn(bookingId);
            if (arrival == null) {
                showAlert(Alert.AlertType.WARNING, "Already Active",
                        "Booking #" + bookingId + " is already active in the hotel.");
                return;
            }

            searchField.clear();
            filteredList.setPredicate(p -> true);
            checkInTable.refresh();

            updateStatus("Arrival registered: Room key assigned for Booking #" + bookingId + " (" + matchedBooking.getBookingNumber() + ")");
        });
    }

    @FXML
    private void handleCompleteCheckOut() {
        CheckIn selected = checkInTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an active stay record to check-out.");
            return;
        }

        if (selected.getStatus() == CheckInStatus.CHECKED_OUT) {
            showAlert(Alert.AlertType.INFORMATION, "Already Completed", "This client has already checked out.");
            return;
        }

        boolean ok = DataStore.getInstance().performCheckOut(selected.getCheckInId());
        if (ok) {
            checkInTable.refresh();
            updateStatus("Check-Out completed for Booking #" + selected.getBookingId() + ". Room marked for cleaning/available.");
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filteredList.setPredicate(p -> true);
        checkInTable.setItems(filteredList);
        checkInTable.refresh();
        updateStatus("Records refreshed. Total records: " + masterList.size());
    }

    private void updateStatus(String msg) {
        statusMessageLabel.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
