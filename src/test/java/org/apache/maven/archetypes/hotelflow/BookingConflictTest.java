package org.apache.maven.archetypes.hotelflow;

import org.apache.maven.archetypes.hotelflow.Model.Booking;
import org.apache.maven.archetypes.hotelflow.Model.BookingStatus;
import org.apache.maven.archetypes.hotelflow.Model.BookingType;
import org.apache.maven.archetypes.hotelflow.Model.CancellationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FR-11: Room Conflict and Date Availability Tests")
class BookingConflictTest {

    private Booking existingBooking;
    private final Long roomId = 201L;
    private final LocalDate baseStart = LocalDate.of(2026, 6, 10);
    private final LocalDate baseEnd = LocalDate.of(2026, 6, 15);

    @BeforeEach
    void setUp() {
        existingBooking = new Booking(
                1L,
                "BK-TEST-001",
                101L,
                roomId,
                4L,
                BookingType.STANDARD,
                CancellationType.FREE,
                baseStart,
                baseEnd
        );
        existingBooking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Check availability should return false when check-out is before or equal to check-in")
    void testInvalidDateRange() {
        Booking invalidBooking1 = new Booking();
        invalidBooking1.setStartDate(LocalDate.of(2026, 6, 10));
        invalidBooking1.setEndDate(LocalDate.of(2026, 6, 10)); // Same day
        assertFalse(invalidBooking1.checkAvailability(), "Same day check-in and check-out should be invalid");

        Booking invalidBooking2 = new Booking();
        invalidBooking2.setStartDate(LocalDate.of(2026, 6, 12));
        invalidBooking2.setEndDate(LocalDate.of(2026, 6, 10)); // Check-out before check-in
        assertFalse(invalidBooking2.checkAvailability(), "Check-out before check-in should be invalid");

        Booking validBooking = new Booking();
        validBooking.setStartDate(LocalDate.of(2026, 6, 10));
        validBooking.setEndDate(LocalDate.of(2026, 6, 12));
        assertTrue(validBooking.checkAvailability(), "Check-out strictly after check-in should be valid");
    }

    @Test
    @DisplayName("Back-to-back bookings (check-out day equals next check-in day) should NOT conflict")
    void testBackToBackBookingsDoNotConflict() {
        // Preceding booking: ends on 2026-06-10 (existing starts on 2026-06-10)
        LocalDate beforeStart = LocalDate.of(2026, 6, 5);
        LocalDate beforeEnd = LocalDate.of(2026, 6, 10);
        assertFalse(existingBooking.conflictsWith(roomId, beforeStart, beforeEnd),
                "Check-out day of first guest can be check-in day of second guest");

        // Succeeding booking: starts on 2026-06-15 (existing ends on 2026-06-15)
        LocalDate afterStart = LocalDate.of(2026, 6, 15);
        LocalDate afterEnd = LocalDate.of(2026, 6, 20);
        assertFalse(existingBooking.conflictsWith(roomId, afterStart, afterEnd),
                "Check-in day of next guest can be check-out day of existing guest");
    }

    @Test
    @DisplayName("Overlapping date ranges for the same room should conflict")
    void testOverlappingBookingsConflict() {
        // 1. Exact same dates [10, 15)
        assertTrue(existingBooking.conflictsWith(roomId, baseStart, baseEnd),
                "Exact matching dates must conflict");

        // 2. Starts before, ends inside [8, 12)
        assertTrue(existingBooking.conflictsWith(roomId, LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 12)),
                "Overlap starting before and ending during existing booking must conflict");

        // 3. Starts inside, ends after [12, 18)
        assertTrue(existingBooking.conflictsWith(roomId, LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 18)),
                "Overlap starting during and ending after existing booking must conflict");

        // 4. Completely inside [11, 14)
        assertTrue(existingBooking.conflictsWith(roomId, LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 14)),
                "Booking completely inside existing booking must conflict");

        // 5. Completely enclosing existing [8, 20)
        assertTrue(existingBooking.conflictsWith(roomId, LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 20)),
                "Booking enclosing existing booking must conflict");
    }

    @Test
    @DisplayName("Different room on the same dates should NOT conflict")
    void testDifferentRoomDoesNotConflict() {
        Long differentRoomId = 202L;
        assertFalse(existingBooking.conflictsWith(differentRoomId, baseStart, baseEnd),
                "Bookings in different rooms on the same dates must not conflict");
    }

    @Test
    @DisplayName("Cancelled reservation must NOT cause a conflict (Business Rules)")
    void testCancelledBookingDoesNotConflict() {
        existingBooking.setStatus(BookingStatus.CANCELLED);
        assertFalse(existingBooking.conflictsWith(roomId, baseStart, baseEnd),
                "Cancelled bookings should not occupy room capacity or cause conflict");
    }
}
