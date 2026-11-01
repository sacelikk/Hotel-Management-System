package org.apache.maven.archetypes.hotelflow.Service;

import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final DataStore dataStore;

    public ReportService() {
        this(DataStore.getInstance());
    }

    public ReportService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * DTO containing aggregated metrics for dashboard reports and charts.
     */
    public static class AnalyticsSummary {
        private final int totalBookings;
        private final int confirmedCount;
        private final int checkedInCount;
        private final int completedCount;
        private final int cancelledCount;
        private final int pendingCount;
        private final int activeGuests;
        private final int totalRooms;
        private final double occupancyRate;
        private final double totalRevenue;
        private final Map<DayOfWeek, Integer> bookingsByDayOfWeek;
        private final Map<BookingStatus, Integer> statusDistribution;

        public AnalyticsSummary(int totalBookings, int confirmedCount, int checkedInCount,
                                int completedCount, int cancelledCount, int pendingCount,
                                int activeGuests, int totalRooms, double occupancyRate,
                                double totalRevenue, Map<DayOfWeek, Integer> bookingsByDayOfWeek,
                                Map<BookingStatus, Integer> statusDistribution) {
            this.totalBookings = totalBookings;
            this.confirmedCount = confirmedCount;
            this.checkedInCount = checkedInCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
            this.pendingCount = pendingCount;
            this.activeGuests = activeGuests;
            this.totalRooms = totalRooms;
            this.occupancyRate = occupancyRate;
            this.totalRevenue = totalRevenue;
            this.bookingsByDayOfWeek = bookingsByDayOfWeek;
            this.statusDistribution = statusDistribution;
        }

        public int getTotalBookings() { return totalBookings; }
        public int getConfirmedCount() { return confirmedCount; }
        public int getCheckedInCount() { return checkedInCount; }
        public int getCompletedCount() { return completedCount; }
        public int getCancelledCount() { return cancelledCount; }
        public int getPendingCount() { return pendingCount; }
        public int getActiveGuests() { return activeGuests; }
        public int getTotalRooms() { return totalRooms; }
        public double getOccupancyRate() { return occupancyRate; }
        public double getTotalRevenue() { return totalRevenue; }
        public Map<DayOfWeek, Integer> getBookingsByDayOfWeek() { return bookingsByDayOfWeek; }
        public Map<BookingStatus, Integer> getStatusDistribution() { return statusDistribution; }
    }

    /**
     * Generates aggregated analytics for the given date range based on actual DataStore entities.
     */
    public AnalyticsSummary computeAnalytics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now().plusDays(7);

        final LocalDate rangeStart = startDate;
        final LocalDate rangeEnd = endDate;

        // 1. Filter bookings overlapping with [rangeStart, rangeEnd]
        List<Booking> overlappingBookings = dataStore.getBookings().stream()
                .filter(b -> b.getStartDate() != null && b.getEndDate() != null)
                .filter(b -> !b.getStartDate().isAfter(rangeEnd) && !b.getEndDate().isBefore(rangeStart))
                .collect(Collectors.toList());

        int totalBookings = overlappingBookings.size();

        Map<BookingStatus, Integer> statusMap = new HashMap<>();
        for (BookingStatus s : BookingStatus.values()) {
            statusMap.put(s, 0);
        }

        Map<DayOfWeek, Integer> dayMap = new LinkedHashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            dayMap.put(d, 0);
        }

        double revenue = 0.0;

        for (Booking b : overlappingBookings) {
            BookingStatus status = b.getStatus() != null ? b.getStatus() : BookingStatus.CONFIRMED;
            statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);

            if (b.getStartDate() != null) {
                DayOfWeek dow = b.getStartDate().getDayOfWeek();
                dayMap.put(dow, dayMap.getOrDefault(dow, 0) + 1);
            }

            // Calculate revenue for non-cancelled reservations
            if (status != BookingStatus.CANCELLED) {
                double nightlyRate = getNightlyRateForBooking(b);
                long nights = ChronoUnit.DAYS.between(b.getStartDate(), b.getEndDate());
                if (nights <= 0) nights = 1;
                revenue += (nights * nightlyRate);
            }
        }

        // 2. Active Guests from CheckIn records
        int activeGuests = (int) dataStore.getCheckIns().stream()
                .filter(c -> c.getStatus() == CheckInStatus.CHECKED_IN)
                .count();

        // 3. Occupancy calculation
        int totalRooms = dataStore.getRooms().size();
        if (totalRooms == 0) totalRooms = 30; // fallback to hotel standard capacity
        double occupancyRate = Math.min(100.0, ((double) activeGuests / totalRooms) * 100.0);

        return new AnalyticsSummary(
                totalBookings,
                statusMap.getOrDefault(BookingStatus.CONFIRMED, 0),
                statusMap.getOrDefault(BookingStatus.CHECKED_IN, 0),
                statusMap.getOrDefault(BookingStatus.COMPLETED, 0),
                statusMap.getOrDefault(BookingStatus.CANCELLED, 0),
                statusMap.getOrDefault(BookingStatus.PENDING, 0),
                activeGuests,
                totalRooms,
                occupancyRate,
                revenue,
                dayMap,
                statusMap
        );
    }

    private double getNightlyRateForBooking(Booking booking) {
        if (booking.getRoomId() != null) {
            Room room = dataStore.findRoomById(booking.getRoomId());
            if (room != null && room.getRoomCategory() != null && room.getRoomCategory().getBasePrice() > 0) {
                return room.getRoomCategory().getBasePrice();
            }
        }

        if (booking.getBookingType() == BookingType.VIP) {
            return 140.0;
        } else if (booking.getBookingType() == BookingType.CORPORATE) {
            return 110.0;
        }
        return 85.0; // Standard room default
    }

    /**
     * FR-20: Room Occupancy Report generator
     */
    public Report generateRoomOccupancyReport(LocalDate start, LocalDate end) {
        AnalyticsSummary summary = computeAnalytics(start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("=== ROOM OCCUPANCY REPORT ===\n");
        sb.append("Period: ").append(start).append(" to ").append(end).append("\n");
        sb.append("Total Hotel Rooms: ").append(summary.getTotalRooms()).append("\n");
        sb.append("Active In-House Guests: ").append(summary.getActiveGuests()).append("\n");
        sb.append(String.format("Current Occupancy Rate: %.1f%%\n", summary.getOccupancyRate()));
        sb.append("Total Active/Confirmed Bookings in Period: ")
                .append(summary.getConfirmedCount() + summary.getCheckedInCount()).append("\n");

        return new Report(System.currentTimeMillis(), "Room Occupancy Report (" + start + " - " + end + ")", sb.toString());
    }

    /**
     * FR-16: Client Report generator
     */
    public Report generateClientReport(LocalDate start, LocalDate end) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CLIENT & GUEST REPORT ===\n");
        sb.append("Period: ").append(start).append(" to ").append(end).append("\n\n");

        for (Client c : dataStore.getClients()) {
            sb.append("Client #").append(c.getClientId()).append(": ").append(c.getFullName()).append("\n");
            sb.append("  Email: ").append(c.getEmail()).append(" | Phone: ").append(c.getPhone()).append("\n");
            sb.append("  Risky Status: ").append(c.isRisky() ? "YES (" + c.getRiskReason() + ")" : "NO (Clean)").append("\n");

            long clientBookings = dataStore.getBookings().stream()
                    .filter(b -> b.getClientId() != null && b.getClientId().equals(c.getClientId()))
                    .count();
            sb.append("  Total Bookings: ").append(clientBookings).append("\n\n");
        }

        return new Report(System.currentTimeMillis(), "Client History Report", sb.toString());
    }
}
