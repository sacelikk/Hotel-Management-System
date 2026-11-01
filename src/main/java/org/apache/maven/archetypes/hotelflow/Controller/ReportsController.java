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
import org.apache.maven.archetypes.hotelflow.Model.BookingStatus;
import org.apache.maven.archetypes.hotelflow.Service.ReportService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label lblTotalBookings;
    @FXML private Label lblOccupancyRate;
    @FXML private Label lblActiveGuests;
    @FXML private Label lblEstimatedRevenue;

    @FXML private PieChart bookingStatusPieChart;
    @FXML private BarChart<String, Number> weeklyActivityBarChart;

    private final ReportService reportService = new ReportService();

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
        ReportService.AnalyticsSummary summary = reportService.computeAnalytics(start, end);

        // 1. KPI Kartlarını Güncelle
        lblTotalBookings.setText(String.valueOf(summary.getTotalBookings()));
        lblOccupancyRate.setText(String.format("%.1f%%", summary.getOccupancyRate()));
        lblActiveGuests.setText(String.valueOf(summary.getActiveGuests()));
        lblEstimatedRevenue.setText(String.format("€%,.2f", summary.getTotalRevenue()));

        // 2. Pasta Grafiğini Güncelle (Booking Status Dağılımı)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<BookingStatus, Integer> statusMap = summary.getStatusDistribution();

        for (Map.Entry<BookingStatus, Integer> entry : statusMap.entrySet()) {
            if (entry.getValue() > 0) {
                String labelName = formatStatusLabel(entry.getKey());
                pieData.add(new PieChart.Data(labelName + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("No Bookings", 1));
        }
        bookingStatusPieChart.setData(pieData);

        // 3. Aktivite Sütun Grafiğini Güncelle (Haftanın günlerine göre dağılım)
        weeklyActivityBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");

        Map<DayOfWeek, Integer> dayMap = summary.getBookingsByDayOfWeek();
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DayOfWeek[] dayEnums = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (int i = 0; i < dayLabels.length; i++) {
            int count = dayMap.getOrDefault(dayEnums[i], 0);
            series.getData().add(new XYChart.Data<>(dayLabels[i], count));
        }

        weeklyActivityBarChart.getData().add(series);
    }

    private String formatStatusLabel(BookingStatus status) {
        switch (status) {
            case CONFIRMED: return "Confirmed";
            case CHECKED_IN: return "Checked-In";
            case COMPLETED: return "Completed";
            case CANCELLED: return "Cancelled";
            case PENDING: return "Pending";
            default: return status.name();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
