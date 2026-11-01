package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.maven.archetypes.hotelflow.Model.Booking;
import org.apache.maven.archetypes.hotelflow.Model.BookingStatus;
import org.apache.maven.archetypes.hotelflow.Model.BookingType;
import org.apache.maven.archetypes.hotelflow.Model.CancellationType;
import org.apache.maven.archetypes.hotelflow.Model.CheckIn;
import org.apache.maven.archetypes.hotelflow.Model.CheckInStatus;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> colBookingNumber;
    @FXML private TableColumn<Booking, Long> colClientId;
    @FXML private TableColumn<Booking, Long> colRoomId;
    @FXML private TableColumn<Booking, LocalDate> colStartDate;
    @FXML private TableColumn<Booking, LocalDate> colEndDate;
    @FXML private TableColumn<Booking, BookingType> colType;
    @FXML private TableColumn<Booking, CancellationType> colCancellation;
    @FXML private TableColumn<Booking, BookingStatus> colStatus;
    @FXML private Label tableStatusLabel;

    private final ObservableList<Booking> masterBookingList = DataStore.getInstance().getBookings();
    private FilteredList<Booking> filteredBookingList;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadBookingData();
        setupFilters();
    }

    private void setupTableColumns() {
        colBookingNumber.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingNumber()));

        colClientId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getClientId()));

        colRoomId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRoomId()));

        colStartDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStartDate()));

        colEndDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getEndDate()));

        colType.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBookingType()));

        colCancellation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCancellationType()));

        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus()));

        colStatus.setCellFactory(col -> new TableCell<Booking, BookingStatus>() {
            @Override
            protected void updateItem(BookingStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case CONFIRMED:
                            setStyle("-fx-text-fill: #0369a1; -fx-font-weight: bold;");
                            break;
                        case CHECKED_IN:
                            setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                            break;
                        case CANCELLED:
                            setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                            break;
                        case COMPLETED:
                            setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #475569;");
                            break;
                    }
                }
            }
        });
    }

    private void loadBookingData() {
        filteredBookingList = new FilteredList<>(masterBookingList, p -> true);
        bookingsTable.setItems(filteredBookingList);
        updateStatusLabel();
    }

    private void setupFilters() {
        statusFilterCombo.getItems().addAll("ALL", "CONFIRMED", "CHECKED_IN", "CANCELLED", "PENDING", "COMPLETED");
        statusFilterCombo.setValue("ALL");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void applyFilter() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();

        filteredBookingList.setPredicate(booking -> {
            boolean matchesSearch = searchText.isEmpty()
                    || (booking.getBookingNumber() != null && booking.getBookingNumber().toLowerCase().contains(searchText))
                    || String.valueOf(booking.getClientId()).contains(searchText);

            boolean matchesStatus = statusFilter == null
                    || statusFilter.equals("ALL")
                    || booking.getStatus().name().equalsIgnoreCase(statusFilter);

            return matchesSearch && matchesStatus;
        });

        updateStatusLabel();
    }

    private void updateStatusLabel() {
        tableStatusLabel.setText("Total Bookings: " + filteredBookingList.size());
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        statusFilterCombo.setValue("ALL");
        filteredBookingList.setPredicate(p -> true);
        bookingsTable.refresh();
        updateStatusLabel();
    }

    @FXML
    private void handleCancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a booking to cancel.");
            return;
        }

        if (selected.getStatus() == BookingStatus.CANCELLED) {
            showAlert(Alert.AlertType.INFORMATION, "Already Cancelled", "This booking is already cancelled.");
            return;
        }

        selected.cancel(CancellationType.FREE);

        // Varsa ilgili aktif check-in kaydını çıkış yapılmış olarak sonlandır
        DataStore.getInstance().getCheckIns().stream()
                .filter(c -> c.getBookingId() != null && c.getBookingId().equals(selected.getBookingId())
                        && c.getStatus() == CheckInStatus.CHECKED_IN)
                .findFirst()
                .ifPresent(c -> {
                    c.setStatus(CheckInStatus.CHECKED_OUT);
                    c.setActualCheckOutTime(LocalDateTime.now());
                });

        DataStore.getInstance().syncRoomStatuses();
        bookingsTable.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Booking " + selected.getBookingNumber() + " has been cancelled.");
    }

    @FXML
    private void handleCheckInSelected() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a booking for check-in.");
            return;
        }

        if (selected.getStatus() == BookingStatus.CANCELLED) {
            showAlert(Alert.AlertType.ERROR, "Cannot Check-In", "Cannot check-in: Booking " + selected.getBookingNumber() + " is CANCELLED.");
            return;
        }

        if (selected.getStatus() == BookingStatus.CHECKED_IN) {
            showAlert(Alert.AlertType.INFORMATION, "Already Checked-In", "Client is already checked-in.");
            return;
        }

        CheckIn checkIn = DataStore.getInstance().performCheckIn(selected.getBookingId());

        if (checkIn != null) {
            bookingsTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Check-In Success",
                    "Arrival registered for Booking " + selected.getBookingNumber() + "!\n" +
                    "Check-In Record #" + checkIn.getCheckInId() + " has been created on Check-In Desk.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Already Active",
                    "Booking " + selected.getBookingNumber() + " is already active in the hotel.");
        }
    }

    @FXML
    private void handleNewReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    org.apache.maven.archetypes.hotelflow.HelloApplication.class.getResource("new-reservation-dialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            NewReservationDialogController controller = loader.getController();
            controller.setAvailableClients(DataStore.getInstance().getClients());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Reservation - HotelFlow");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(bookingsTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                Booking newBooking = controller.getCreatedBooking();
                long newId = masterBookingList.size() + 1;
                String bookingNumber = String.format("BK-2026-%03d", newId);

                newBooking.setBookingId(newId);
                newBooking.setBookingNumber(bookingNumber);
                newBooking.setStatus(BookingStatus.CONFIRMED);

                masterBookingList.add(newBooking);
                DataStore.getInstance().syncRoomStatuses();
                filteredBookingList.setPredicate(p -> true);
                bookingsTable.refresh();
                updateStatusLabel();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation " + bookingNumber + " created successfully!");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open reservation dialog: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
