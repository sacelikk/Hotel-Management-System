package org.apache.maven.archetypes.hotelflow;

import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Service.ReportService;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FR-16, FR-20: ReportService and Analytics Layer Tests")
class ReportServiceTest {

    private DataStore dataStore;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        dataStore.resetToDefault();
        reportService = new ReportService(dataStore);
    }

    @Test
    @DisplayName("Analytics correctly computes total bookings and active guests from DataStore")
    void testComputeAnalyticsBasicCounts() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusDays(7);

        ReportService.AnalyticsSummary summary = reportService.computeAnalytics(start, end);

        // DataStore has 3 initial bookings (b1, b2, b3)
        assertEquals(3, summary.getTotalBookings(), "Should match the 3 sample bookings");

        // 1 of the initial bookings is CHECKED_IN (b3) and has a CheckIn record
        assertEquals(1, summary.getActiveGuests(), "Should match 1 active checked-in guest");

        // Status counts: b1 Confirmed, b2 Confirmed, b3 Checked-in
        assertEquals(2, summary.getConfirmedCount());
        assertEquals(1, summary.getCheckedInCount());
        assertEquals(0, summary.getCancelledCount());
    }

    @Test
    @DisplayName("Occupancy rate is accurately derived from active guests divided by total hotel rooms")
    void testOccupancyRateCalculation() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusDays(7);

        ReportService.AnalyticsSummary summary = reportService.computeAnalytics(start, end);

        int totalRooms = dataStore.getRooms().size();
        assertEquals(7, totalRooms, "DataStore has 7 sample rooms");

        double expectedOccupancy = (1.0 / 7.0) * 100.0;
        assertEquals(expectedOccupancy, summary.getOccupancyRate(), 0.01,
                "Occupancy rate should equal active guests / total rooms * 100");
    }

    @Test
    @DisplayName("Revenue is calculated from stay duration and room category base price, excluding cancelled stays")
    void testRevenueCalculationExcludingCancelled() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusDays(7);

        ReportService.AnalyticsSummary initialSummary = reportService.computeAnalytics(start, end);
        double initialRevenue = initialSummary.getTotalRevenue();
        assertTrue(initialRevenue > 0, "Initial revenue should be positive");

        // Cancel booking 1 (b1: Room 201 Standard, 3 nights * €85 = €255)
        Booking b1 = dataStore.findBookingById(1L);
        assertNotNull(b1);
        b1.setStatus(BookingStatus.CANCELLED);

        ReportService.AnalyticsSummary afterCancelSummary = reportService.computeAnalytics(start, end);
        double reducedRevenue = afterCancelSummary.getTotalRevenue();

        assertEquals(1, afterCancelSummary.getCancelledCount());
        assertTrue(reducedRevenue < initialRevenue, "Revenue must decrease when a booking is cancelled");
        assertEquals(initialRevenue - 255.0, reducedRevenue, 0.01,
                "Cancelled booking (3 nights * €85 = €255) must be deducted from estimated revenue");
    }

    @Test
    @DisplayName("Bookings are accurately grouped by DayOfWeek")
    void testBookingsByDayOfWeekGrouping() {
        LocalDate start = LocalDate.now().minusDays(14);
        LocalDate end = LocalDate.now().plusDays(14);

        ReportService.AnalyticsSummary summary = reportService.computeAnalytics(start, end);
        assertNotNull(summary.getBookingsByDayOfWeek());

        int sumOfDays = summary.getBookingsByDayOfWeek().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(summary.getTotalBookings(), sumOfDays,
                "Sum of bookings across all days of the week should equal total bookings");
    }

    @Test
    @DisplayName("FR-20: Room Occupancy Report generates structured content")
    void testGenerateRoomOccupancyReport() {
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = LocalDate.now().plusDays(3);

        Report report = reportService.generateRoomOccupancyReport(start, end);
        assertNotNull(report);
        assertNotNull(report.getTitle());
        assertTrue(report.getTitle().contains("Room Occupancy Report"));
        assertTrue(report.getContent().contains("Current Occupancy Rate"));
        assertTrue(report.getContent().contains("Total Hotel Rooms"));
    }

    @Test
    @DisplayName("FR-16: Client Report generates structured client history")
    void testGenerateClientReport() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().plusDays(30);

        Report report = reportService.generateClientReport(start, end);
        assertNotNull(report);
        assertTrue(report.getTitle().contains("Client History Report"));
        assertTrue(report.getContent().contains("Alexander Schmidt"));
        assertTrue(report.getContent().contains("Marcus Vance"));
        assertTrue(report.getContent().contains("Risky Status: YES"));
    }
}
