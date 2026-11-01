package org.apache.maven.archetypes.hotelflow;

import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataStore Synchronization and Availability Tests")
class DataStoreSyncTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        dataStore.resetToDefault();
    }

    @Test
    @DisplayName("DataStore sample clients and bookings are populated correctly")
    void testSampleData() {
        assertNotNull(dataStore.getClients());
        assertFalse(dataStore.getClients().isEmpty());

        assertNotNull(dataStore.getBookings());
        assertFalse(dataStore.getBookings().isEmpty());

        assertNotNull(dataStore.getRooms());
        assertFalse(dataStore.getRooms().isEmpty());
    }

    @Test
    @DisplayName("Adding a new client reflects immediately in DataStore and findClientById")
    void testClientAdditionAndLookup() {
        long newClientId = 999L;
        Client newClient = new Client(newClientId, "Marie Curie", "marie.curie@science.fr", "+33 1 23456789", "Paris, France");

        dataStore.addClient(newClient);

        Client fetched = dataStore.findClientById(newClientId);
        assertNotNull(fetched, "Client should be retrievable by ID");
        assertEquals("Marie Curie", fetched.getFullName());
        assertEquals("marie.curie@science.fr", fetched.getEmail());
        assertFalse(fetched.isRisky());
    }

    @Test
    @DisplayName("Toggling risky status on client in DataStore persists and records reason")
    void testClientRiskStatusUpdate() {
        Client client = dataStore.findClientById(101L);
        assertNotNull(client);
        assertFalse(client.isRisky(), "Initially client 101 is not risky");

        client.markAsRisky("Repeated late payments");
        assertTrue(client.isRisky());
        assertEquals("Repeated late payments", client.getRiskReason());

        // Clear risk
        client.setRisky(false);
        client.setRiskReason(null);
        assertFalse(client.isRisky());
        assertNull(client.getRiskReason());
    }

    @Test
    @DisplayName("DataStore detects room conflicts with existing active bookings")
    void testDataStoreRoomAvailability() {
        // Room 201 has Booking 1 from today to today + 3
        LocalDate today = LocalDate.now();
        LocalDate todayPlus3 = today.plusDays(3);

        assertFalse(dataStore.isRoomAvailable(201L, today, todayPlus3),
                "Room 201 should NOT be available on its exact active booking dates");

        Booking conflict = dataStore.findConflictingBooking(201L, today.plusDays(1), today.plusDays(2), null);
        assertNotNull(conflict, "Conflict must be returned");
        assertEquals("BK-2026-001", conflict.getBookingNumber());

        // Room 201 should be available starting from todayPlus3 (checkout day of Booking 1)
        assertTrue(dataStore.isRoomAvailable(201L, todayPlus3, todayPlus3.plusDays(2)),
                "Room 201 should be available after check-out date of previous booking");
    }

    @Test
    @DisplayName("Cancelled booking in DataStore does not block room availability")
    void testCancelledBookingRoomAvailability() {
        Booking b1 = dataStore.findBookingById(1L);
        assertNotNull(b1);
        assertEquals(201L, b1.getRoomId());

        LocalDate start = b1.getStartDate();
        LocalDate end = b1.getEndDate();

        // Cancel the booking
        b1.setStatus(BookingStatus.CANCELLED);

        assertTrue(dataStore.isRoomAvailable(201L, start, end),
                "After cancellation, room 201 should be immediately available on those dates");
    }

    @Test
    @DisplayName("Updating a booking ignores itself when checking conflicts")
    void testSelfExclusionInConflictCheck() {
        Booking b1 = dataStore.findBookingById(1L);
        assertNotNull(b1);

        // When checking for b1 itself (excludeBookingId = 1L), it shouldn't conflict with itself
        Booking conflict = dataStore.findConflictingBooking(b1.getRoomId(), b1.getStartDate(), b1.getEndDate(), b1.getBookingId());
        assertNull(conflict, "Booking should not conflict with itself when excluded");
    }

    @Test
    @DisplayName("FR-15: performCheckIn creates CheckIn entity and synchronizes with reservations and check-in desk")
    void testPerformCheckInSync() {
        Booking b2 = dataStore.findBookingById(2L);
        assertNotNull(b2);
        assertEquals(BookingStatus.CONFIRMED, b2.getStatus());

        int initialCheckInCount = dataStore.getCheckIns().size();

        // Perform Check-In
        CheckIn arrival = dataStore.performCheckIn(2L);
        assertNotNull(arrival, "CheckIn record should be successfully created");
        assertEquals(2L, arrival.getBookingId());
        assertEquals(CheckInStatus.CHECKED_IN, arrival.getStatus());
        assertEquals(BookingStatus.CHECKED_IN, b2.getStatus(), "Booking status must be updated to CHECKED_IN");
        assertEquals(initialCheckInCount + 1, dataStore.getCheckIns().size(), "CheckIns list must contain new record");

        // Attempting to check-in the same booking again should fail
        CheckIn duplicateArrival = dataStore.performCheckIn(2L);
        assertNull(duplicateArrival, "Duplicate check-in for already active guest should return null");

        // Perform Check-Out
        boolean checkedOut = dataStore.performCheckOut(arrival.getCheckInId());
        assertTrue(checkedOut, "Check-out should succeed");
        assertEquals(CheckInStatus.CHECKED_OUT, arrival.getStatus());
        assertEquals(BookingStatus.COMPLETED, b2.getStatus(), "Booking status must be updated to COMPLETED");
    }

    @Test
    @DisplayName("Room 301 changes to OCCUPIED when booked and reverts to AVAILABLE when cancelled")
    void testRoom301BookingAndStatusSync() {
        Room r301 = dataStore.findRoomById(301L);
        assertNotNull(r301, "Room 301 must exist in inventory");
        assertEquals(RoomStatus.AVAILABLE, r301.getRoomStatus(), "Room 301 should initially be AVAILABLE");

        // Book Room 301
        Booking booking301 = new Booking(10L, "BK-2026-301", 101L, 301L, 4L,
                BookingType.STANDARD, CancellationType.FREE,
                LocalDate.now(), LocalDate.now().plusDays(3));
        booking301.setStatus(BookingStatus.CONFIRMED);
        dataStore.addBooking(booking301);

        // Room 301 status must automatically switch to OCCUPIED
        assertEquals(RoomStatus.OCCUPIED, r301.getRoomStatus(),
                "Room 301 status must immediately update to OCCUPIED after booking");
        assertFalse(dataStore.isRoomAvailable(301L, LocalDate.now(), LocalDate.now().plusDays(3)),
                "Room 301 must not be available during booked dates");

        // Cancel the booking
        booking301.setStatus(BookingStatus.CANCELLED);
        dataStore.syncRoomStatuses();

        // Room 301 must automatically revert to AVAILABLE
        assertEquals(RoomStatus.AVAILABLE, r301.getRoomStatus(),
                "Room 301 status must revert to AVAILABLE after booking cancellation");
        assertTrue(dataStore.isRoomAvailable(301L, LocalDate.now(), LocalDate.now().plusDays(3)),
                "Room 301 must be available after cancellation");
    }

    @Test
    @DisplayName("syncRoomStatuses preserves MAINTENANCE status when rooms are out of service")
    void testMaintenanceRoomPreservedDuringSync() {
        Room r302 = dataStore.findRoomById(302L);
        assertNotNull(r302);
        r302.updateStatus(RoomStatus.MAINTENANCE);

        dataStore.syncRoomStatuses();
        assertEquals(RoomStatus.MAINTENANCE, r302.getRoomStatus(),
                "Room under maintenance should not be overwritten by syncRoomStatuses");
    }
}
