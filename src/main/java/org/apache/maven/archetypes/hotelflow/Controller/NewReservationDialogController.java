package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.maven.archetypes.hotelflow.Model.Booking;
import org.apache.maven.archetypes.hotelflow.Model.BookingType;
import org.apache.maven.archetypes.hotelflow.Model.CancellationType;
import org.apache.maven.archetypes.hotelflow.Model.Client;
import org.apache.maven.archetypes.hotelflow.Model.Room;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class NewReservationDialogController {

    @FXML private TextField clientIdField;
    @FXML private TextField roomIdField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private ComboBox<BookingType> bookingTypeCombo;
    @FXML private ComboBox<CancellationType> cancellationTypeCombo;
    @FXML private Label errorLabel;

    private List<Client> availableClients;
    private Booking createdBooking;
    private boolean saved = false;

    @FXML
    public void initialize() {
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        errorLabel.setVisible(false);

        bookingTypeCombo.getItems().setAll(BookingType.values());
        bookingTypeCombo.setValue(BookingType.STANDARD);

        cancellationTypeCombo.getItems().setAll(CancellationType.values());
        cancellationTypeCombo.setValue(CancellationType.FREE);

        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(LocalDate.now().plusDays(2));
    }

    public void setAvailableClients(List<Client> clients) {
        this.availableClients = clients;
    }

    private List<Client> getAvailableClients() {
        if (this.availableClients != null) {
            return this.availableClients;
        }
        return DataStore.getInstance().getClients();
    }

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);

        String clientText = clientIdField.getText() != null ? clientIdField.getText().trim() : "";
        String roomText = roomIdField.getText() != null ? roomIdField.getText().trim() : "";
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (clientText.isEmpty() || roomText.isEmpty() || checkIn == null || checkOut == null) {
            showError("Please fill in all required fields.");
            return;
        }

        Long clientId;
        Long roomId;
        try {
            clientId = Long.parseLong(clientText);
            roomId = Long.parseLong(roomText);
        } catch (NumberFormatException e) {
            showError("Client ID and Room ID must be numeric.");
            return;
        }

        // 1. Müşteri Sistemde Var mı Kontrolü (FR-09)
        Client matchedClient = getAvailableClients().stream()
                .filter(c -> c.getClientId() != null && c.getClientId().equals(clientId))
                .findFirst()
                .orElse(null);

        if (matchedClient == null) {
            showError("Client ID #" + clientId + " not found! Please register the client in the Clients tab first.");
            return;
        }

        // 2. Tarih Doğrulamaları (FR-11: check-out must be strictly after check-in)
        if (checkIn.isBefore(LocalDate.now())) {
            showError("Check-In date cannot be in the past.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showError("Check-Out date must be strictly after Check-In date.");
            return;
        }

        // 3. Oda Var mı Kontrolü (Opsiyonel oda doğrulaması)
        ObservableList<Room> knownRooms = DataStore.getInstance().getRooms();
        if (!knownRooms.isEmpty()) {
            boolean roomExists = knownRooms.stream()
                    .anyMatch(r -> r.getRoomId() != null && r.getRoomId().equals(roomId));
            if (!roomExists) {
                showError("Room ID #" + roomId + " does not exist! Please check the 'Rooms & Status' tab for valid room IDs.");
                return;
            }
        }

        // 4. Oda Çakışma Kontrolü (FR-11: Room Availability / Conflict Check)
        Booking conflict = DataStore.getInstance().findConflictingBooking(roomId, checkIn, checkOut, null);
        if (conflict != null) {
            showError("Room #" + roomId + " is not available! Conflicts with active Booking " +
                    conflict.getBookingNumber() + " (" + conflict.getStartDate() + " to " + conflict.getEndDate() + ").");
            return;
        }

        // 5. Riskli Müşteri Uyarısı (FR-12: Risky Client Notification)
        if (matchedClient.isRisky()) {
            Alert warningAlert = new Alert(Alert.AlertType.CONFIRMATION);
            warningAlert.setTitle("Warning: Risky Client (FR-12)");
            warningAlert.setHeaderText("Guest Flagged as Risky / Blacklisted");
            String reason = matchedClient.getRiskReason() != null ? matchedClient.getRiskReason() : "No reason provided";
            warningAlert.setContentText("Client: " + matchedClient.getFullName() +
                    "\nFlag Reason: " + reason +
                    "\n\nDo you want to proceed and finalize this reservation?");

            Optional<ButtonType> choice = warningAlert.showAndWait();
            if (choice.isEmpty() || choice.get() != ButtonType.OK) {
                showError("Reservation cancelled due to risky client policy.");
                return;
            }
        }

        BookingType bType = bookingTypeCombo.getValue();
        CancellationType cType = cancellationTypeCombo.getValue();

        this.createdBooking = new Booking(null, null, clientId, roomId, 4L, bType, cType, checkIn, checkOut);
        this.saved = true;

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        this.saved = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) clientIdField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public boolean isSaved() {
        return saved;
    }

    public Booking getCreatedBooking() {
        return createdBooking;
    }
}
