package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.maven.archetypes.hotelflow.Model.Booking;
import org.apache.maven.archetypes.hotelflow.Model.BookingType;
import org.apache.maven.archetypes.hotelflow.Model.CancellationType;

import java.time.LocalDate;

public class NewReservationDialogController {

    @FXML private TextField clientIdField;
    @FXML private TextField roomIdField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private ComboBox<BookingType> bookingTypeCombo;
    @FXML private ComboBox<CancellationType> cancellationTypeCombo;
    @FXML private Label errorLabel;

    private Booking createdBooking;
    private boolean saved = false;

    @FXML
    public void initialize() {
        bookingTypeCombo.getItems().setAll(BookingType.values());
        bookingTypeCombo.setValue(BookingType.STANDARD);

        cancellationTypeCombo.getItems().setAll(CancellationType.values());
        cancellationTypeCombo.setValue(CancellationType.FREE);

        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(LocalDate.now().plusDays(2));
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

        // Check-Out > Check-In kuralı doğrulaması
        if (!checkOut.isAfter(checkIn)) {
            showError("Check-Out date must be strictly after Check-In date.");
            return;
        }

        BookingType bType = bookingTypeCombo.getValue();
        CancellationType cType = cancellationTypeCombo.getValue();

        // Geçici ID ve numara ile yeni Booking üretimi
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