package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label lblTotalBookings;
    @FXML private Label lblOccupancyRate;
    @FXML private Label lblActiveGuests;
    @FXML private Label lblEstimatedRevenue;

    @FXML private PieChart bookingStatusPieChart;
    @FXML private BarChart<String, Number> weeklyActivityBarChart;

    @FXML
    public void initialize() {
        // Varsayılan olarak son 7 gün ile gelecek 7 gün arası
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now().plusDays(7));

        updateAnalyticsView(startDatePicker.getValue(), endDatePicker.getValue());
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Dates", "Please select both start and end dates.");
            return;
        }

        if (end.isBefore(start)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Range", "End date cannot be earlier than start date.");
            return;
        }

        updateAnalyticsView(start, end);
    }

    private void updateAnalyticsView(LocalDate start, LocalDate end) {
        long daysInRange = ChronoUnit.DAYS.between(start, end);
        if (daysInRange <= 0) daysInRange = 1;

        // Seçilen tarih aralığına göre dinamik hesaplama (Tarih uzadıkça rezervasyon ve gelir artar)
        int simulatedBookings = (int) Math.max(3, (daysInRange * 1.8));
        int confirmed = (int) (simulatedBookings * 0.45);
        int checkedIn = (int) (simulatedBookings * 0.30);
        int completed = (int) (simulatedBookings * 0.15);
        int cancelled = Math.max(1, simulatedBookings - (confirmed + checkedIn + completed));

        int totalRooms = 30;
        int activeGuests = Math.min(totalRooms, checkedIn);
        double occupancyRate = ((double) activeGuests / totalRooms) * 100.0;

        // Ortalama oda gecelik ücreti ~€85 üzerinden gelir hesabı
        double estimatedRevenue = (confirmed + checkedIn + completed) * 85.0 * Math.min(3, Math.max(1, daysInRange / 5.0));

        // 1. Kartları Güncelle
        lblTotalBookings.setText(String.valueOf(simulatedBookings));
        lblOccupancyRate.setText(String.format("%.1f%%", occupancyRate));
        lblActiveGuests.setText(String.valueOf(activeGuests));
        lblEstimatedRevenue.setText(String.format("€%,.2f", estimatedRevenue));

        // 2. Pasta Grafiğini Güncelle
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Confirmed (" + confirmed + ")", confirmed),
                new PieChart.Data("Checked-In (" + checkedIn + ")", checkedIn),
                new PieChart.Data("Completed (" + completed + ")", completed),
                new PieChart.Data("Cancelled (" + cancelled + ")", cancelled)
        );
        bookingStatusPieChart.setData(pieData);

        // 3. Aktivite Sütun Grafiğini Güncelle
        weeklyActivityBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            // Tarih genişliğine göre dağıtılmış gerçekçi sayılar
            int dailyVolume = Math.max(1, (int) ((simulatedBookings / 7.0) + ((i % 3 == 0) ? 2 : -1)));
            series.getData().add(new XYChart.Data<>(days[i], dailyVolume));
        }

        weeklyActivityBarChart.getData().add(series);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}