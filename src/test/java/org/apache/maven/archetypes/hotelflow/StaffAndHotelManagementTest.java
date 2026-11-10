package org.apache.maven.archetypes.hotelflow;

import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FR-03, FR-04, FR-05, FR-06: Staff Hierarchy, Hotels & Room Pricing Tests")
class StaffAndHotelManagementTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        dataStore.resetToDefault();
    }

    @Test
    @DisplayName("FR-03: Administrator creates Owner account, credentials persist and authenticate")
    void testOwnerAccountCreation() {
        User newOwner = new User(10L, "elena.owner", "pass123", "Elena Petrova", "elena@varna-hotels.bg", Role.OWNER);
        dataStore.addUser(newOwner);

        User fetched = dataStore.findUserByUsername("elena.owner");
        assertNotNull(fetched, "New owner should be retrievable by username");
        assertEquals("Elena Petrova", fetched.getFullName());
        assertEquals(Role.OWNER, fetched.getRole());
        assertTrue(fetched.authenticate("pass123"), "Owner must authenticate with valid password");
        assertFalse(fetched.authenticate("wrong_pass"), "Owner authentication must fail with incorrect password");

        // Deactivated user cannot authenticate
        fetched.setActive(false);
        assertFalse(fetched.authenticate("pass123"), "Disabled account must not be able to authenticate");
    }

    @Test
    @DisplayName("FR-04: Owner registers a new Hotel property into inventory")
    void testHotelCreation() {
        int initialHotelCount = dataStore.getHotels().size();

        Hotel newHotel = new Hotel(10L, 2L, null, "Black Sea Boutique Resort", "Beach Promenade 8, Varna", "+359 52 888999", "boutique@varna.bg");
        dataStore.addHotel(newHotel);

        assertEquals(initialHotelCount + 1, dataStore.getHotels().size());
        Hotel fetched = dataStore.findHotelById(10L);
        assertNotNull(fetched);
        assertEquals("Black Sea Boutique Resort", fetched.getName());
        assertEquals(2L, fetched.getOwnerId());
        assertNull(fetched.getManagerId(), "Initially unassigned manager");
    }

    @Test
    @DisplayName("FR-05: Owner assigns a General Manager to an operating hotel")
    void testAssignManagerToHotel() {
        // Manager ID 3 exists in sample data (Viktor Stoyanov)
        User manager = dataStore.findUserById(3L);
        assertNotNull(manager);
        assertEquals(Role.MANAGER, manager.getRole());

        // Hotel 2 initially has managerId = null
        Hotel hotel2 = dataStore.findHotelById(2L);
        assertNotNull(hotel2);
        assertNull(hotel2.getManagerId());

        boolean success = dataStore.assignManagerToHotel(2L, 3L);
        assertTrue(success, "Manager assignment should succeed");

        assertEquals(3L, hotel2.getManagerId(), "Hotel managerId must be updated");
        assertEquals(2L, manager.getHotelId(), "Manager's assigned hotelId must be updated");
    }

    @Test
    @DisplayName("FR-06: Manager registers Receptionist staff accounts assigned to their hotel")
    void testReceptionistRegistrationForHotel() {
        Long hotelId = 1L;
        User receptionist = new User(50L, "georgi.frontdesk", "rec123", "Georgi Ivanov", "georgi@grandvarna.bg", Role.RECEPTIONIST, hotelId);
        dataStore.addUser(receptionist);

        User fetched = dataStore.findUserByUsername("georgi.frontdesk");
        assertNotNull(fetched);
        assertEquals(Role.RECEPTIONIST, fetched.getRole());
        assertEquals(hotelId, fetched.getHotelId());
        assertTrue(fetched.authenticate("rec123"));
    }

    @Test
    @DisplayName("Manager updates room category base price, dynamically reflecting across all rooms")
    void testRoomCategoryPriceUpdate() {
        RoomCategory standardCat = dataStore.findRoomCategoryById(1);
        assertNotNull(standardCat);
        assertEquals(85.0, standardCat.getBasePrice(), 0.01);

        // Room 201 is standard
        Room r201 = dataStore.findRoomById(201L);
        assertNotNull(r201);
        assertEquals(85.0, r201.getRoomCategory().getBasePrice(), 0.01);

        // Manager updates base price to €95.00
        boolean updated = dataStore.updateRoomCategoryPrice(1, 95.0);
        assertTrue(updated);

        assertEquals(95.0, standardCat.getBasePrice(), 0.01);
        assertEquals(95.0, r201.getRoomCategory().getBasePrice(), 0.01,
                "Room 201 must dynamically reflect updated category nightly rate");

        // Negative price should be rejected
        assertFalse(dataStore.updateRoomCategoryPrice(1, -20.0), "Negative prices must be rejected");
    }
}
