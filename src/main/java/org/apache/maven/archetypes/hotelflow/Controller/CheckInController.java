package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.maven.archetypes.hotelflow.Model.CheckIn;
import org.apache.maven.archetypes.hotelflow.Model.CheckInStatus;

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

    private final ObservableList<CheckIn> masterList = FXCollections.observableArrayList();
    private FilteredList<CheckIn> filteredList;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {
        setupColumns();
        loadMockStays();
        setupSearch();
    }

    private void setupColumns() {
        colCheckInId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckInId()));
        colBookingId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBookingId()));
        colRoomNumber.setCellValueFactory(data -> new SimpleStringProperty("Room #" + (200 + data.getValue().getBookingId())));

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

    private void loadMockStays() {
        CheckIn c1 = new CheckIn(1L, 1L, LocalDateTime.now().minusDays(1), null, CheckInStatus.CHECKED_IN);
        CheckIn c2 = new CheckIn(2L, 2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(4), CheckInStatus.CHECKED_OUT);
        CheckIn c3 = new CheckIn(3L, 3L, LocalDateTime.now().minusHours(3), null, CheckInStatus.CHECKED_IN);

        masterList.addAll(c1, c2, c3);
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
        long newId = masterList.size() + 1;
        CheckIn arrival = new CheckIn(newId, 100L + newId, LocalDateTime.now(), null, CheckInStatus.CHECKED_IN);
        masterList.add(arrival);
        checkInTable.refresh();
        updateStatus("New arrival registered: Room Key assigned for Booking #" + arrival.getBookingId());
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

        selected.setActualCheckOutTime(LocalDateTime.now());
        selected.setStatus(CheckInStatus.CHECKED_OUT);
        checkInTable.refresh();
        updateStatus("Check-Out completed for Booking #" + selected.getBookingId() + ". Room marked for cleaning/available.");
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        checkInTable.refresh();
        updateStatus("Records refreshed.");
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