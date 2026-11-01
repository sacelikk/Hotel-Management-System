package org.apache.maven.archetypes.hotelflow.Util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.maven.archetypes.hotelflow.Model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DataStore {

    private static DataStore instance;

    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private final ObservableList<CheckIn> checkIns = FXCollections.observableArrayList();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();

    private DataStore() {
        initSampleData();
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public synchronized void resetToDefault() {
        bookings.clear();
        checkIns.clear();
        clients.clear();
        rooms.clear();
        initSampleData();
    }

    private void initSampleData() {
        // 1. Ortak Müşteriler
        Client c1 = new Client(101L, "Alexander Schmidt", "alex.schmidt@gmail.com", "+49 170 1234567", "Berlin, Germany");
        Client c2 = new Client(102L, "Elena Popova", "elena.popova@mail.bg", "+359 88 7654321", "Sofia, Bulgaria");
        Client c3 = new Client(103L, "Marcus Vance", "marcus.vance@yahoo.com", "+44 7911 123456", "London, UK");
        c3.markAsRisky("Late cancellation fee unpaid & noise complaints");
        Client c4 = new Client(104L, "Sophia Rossi", "sophia.rossi@outlook.it", "+39 06 6987123", "Rome, Italy");
        clients.addAll(c1, c2, c3, c4);

        // 2. Ortak Oda Kategorileri ve Odalar
        RoomCategory standardCat = new RoomCategory(1, 1, "Standard", "Comfortable standard room", 85.0, 2);
        RoomCategory deluxeCat = new RoomCategory(2, 1, "Deluxe", "Spacious room with balcony", 130.0, 3);
        RoomCategory suiteCat = new RoomCategory(3, 1, "Suite", "Luxury suite with panoramic view", 220.0, 4);

        Room r201 = new Room(201L, 1L, standardCat, "201", 2, RoomStatus.AVAILABLE);
        Room r202 = new Room(202L, 1L, standardCat, "202", 2, RoomStatus.AVAILABLE);
        Room r203 = new Room(203L, 1L, standardCat, "203", 2, RoomStatus.AVAILABLE);
        Room r204 = new Room(204L, 1L, deluxeCat, "204", 2, RoomStatus.AVAILABLE);
        Room r301 = new Room(301L, 1L, deluxeCat, "301", 3, RoomStatus.AVAILABLE);
        Room r302 = new Room(302L, 1L, deluxeCat, "302", 3, RoomStatus.AVAILABLE);
        Room r305 = new Room(305L, 1L, suiteCat, "305", 3, RoomStatus.AVAILABLE);
        rooms.addAll(r201, r202, r203, r204, r301, r302, r305);

        // 3. Ortak Rezervasyonlar
        Booking b1 = new Booking(1L, "BK-2026-001", 101L, 201L, 4L, BookingType.STANDARD, CancellationType.FREE, LocalDate.now(), LocalDate.now().plusDays(3));
        b1.setStatus(BookingStatus.CONFIRMED);

        Booking b2 = new Booking(2L, "BK-2026-002", 102L, 204L, 4L, BookingType.VIP, CancellationType.NON_REFUNDABLE, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        b2.setStatus(BookingStatus.CONFIRMED);

        Booking b3 = new Booking(3L, "BK-2026-003", 104L, 305L, 4L, BookingType.CORPORATE, CancellationType.PARTIAL_REFUND, LocalDate.now().minusDays(2), LocalDate.now().plusDays(1));
        b3.setStatus(BookingStatus.CHECKED_IN);
        bookings.addAll(b1, b2, b3);

        // 4. Ortak Fiili Check-In Kaydı
        CheckIn chk1 = new CheckIn(1L, 3L, LocalDateTime.now().minusDays(2), null, CheckInStatus.CHECKED_IN);
        checkIns.add(chk1);

        // 5. Odaların doluluk durumlarını rezervasyonlara göre senkronize et
        syncRoomStatuses();
    }

    public ObservableList<Booking> getBookings() {
        return bookings;
    }

    public ObservableList<CheckIn> getCheckIns() {
        return checkIns;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public Booking findBookingById(Long id) {
        if (id == null) return null;
        return bookings.stream()
                .filter(b -> b.getBookingId() != null && b.getBookingId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Client findClientById(Long id) {
        if (id == null) return null;
        return clients.stream()
                .filter(c -> c.getClientId() != null && c.getClientId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Room findRoomById(Long id) {
        if (id == null) return null;
        return rooms.stream()
                .filter(r -> r.getRoomId() != null && r.getRoomId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addClient(Client client) {
        if (client != null) {
            this.clients.add(client);
        }
    }

    public void addBooking(Booking booking) {
        if (booking != null) {
            this.bookings.add(booking);
            syncRoomStatuses();
        }
    }

    public void addCheckIn(CheckIn checkIn) {
        if (checkIn != null) {
            this.checkIns.add(checkIn);
            syncRoomStatuses();
        }
    }

    /**
     * Tüm odaların AVAILABLE ve OCCUPIED durumlarını aktif rezervasyonlara göre anlık günceller.
     */
    public synchronized void syncRoomStatuses() {
        for (Room room : rooms) {
            if (room.getRoomStatus() == RoomStatus.MAINTENANCE) {
                continue; // Bakımdaki odaya dokunma
            }

            boolean isOccupied = bookings.stream()
                    .filter(b -> b.getRoomId() != null &&
                            (b.getRoomId().equals(room.getRoomId()) ||
                             (room.getRoomNumber() != null && room.getRoomNumber().equals(String.valueOf(b.getRoomId())))))
                    .anyMatch(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CONFIRMED);

            if (isOccupied) {
                room.updateStatus(RoomStatus.OCCUPIED);
            } else {
                room.updateStatus(RoomStatus.AVAILABLE);
            }
        }
    }

    /**
     * FR-15: Rezervasyonu fiili olarak otele giriş (Check-In) yaptırır ve CheckIn tablosuna ekler.
     */
    public CheckIn performCheckIn(Long bookingId) {
        if (bookingId == null) return null;
        Booking booking = findBookingById(bookingId);
        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            return null;
        }

        boolean alreadyActive = checkIns.stream()
                .anyMatch(c -> c.getBookingId() != null && c.getBookingId().equals(bookingId)
                        && c.getStatus() == CheckInStatus.CHECKED_IN);
        if (alreadyActive) {
            return null;
        }

        booking.setStatus(BookingStatus.CHECKED_IN);

        long maxId = checkIns.stream()
                .mapToLong(c -> c.getCheckInId() != null ? c.getCheckInId() : 0L)
                .max()
                .orElse(0L);
        long newId = maxId + 1;

        CheckIn checkIn = new CheckIn(newId, bookingId, LocalDateTime.now(), null, CheckInStatus.CHECKED_IN);
        checkIns.add(checkIn);
        syncRoomStatuses();
        return checkIn;
    }

    /**
     * FR-15: Fiili çıkış (Check-Out) işlemini tamamlar ve rezervasyon durumunu COMPLETED yapar.
     */
    public boolean performCheckOut(Long checkInId) {
        if (checkInId == null) return false;
        CheckIn checkIn = checkIns.stream()
                .filter(c -> c.getCheckInId() != null && c.getCheckInId().equals(checkInId))
                .findFirst()
                .orElse(null);
        if (checkIn == null || checkIn.getStatus() == CheckInStatus.CHECKED_OUT) {
            return false;
        }

        checkIn.setActualCheckOutTime(LocalDateTime.now());
        checkIn.setStatus(CheckInStatus.CHECKED_OUT);

        Booking booking = findBookingById(checkIn.getBookingId());
        if (booking != null) {
            booking.setStatus(BookingStatus.COMPLETED);
        }
        syncRoomStatuses();
        return true;
    }

    // FR-11: Oda Çakışma Kontrolü (Conflict Check Algorithm)
    public Booking findConflictingBooking(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        if (roomId == null || checkIn == null || checkOut == null) {
            return null;
        }
        if (!checkOut.isAfter(checkIn)) {
            return null;
        }
        for (Booking b : bookings) {
            if (excludeBookingId != null && b.getBookingId() != null && b.getBookingId().equals(excludeBookingId)) {
                continue;
            }
            if (b.conflictsWith(roomId, checkIn, checkOut)) {
                return b;
            }
        }
        return null;
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailable(roomId, checkIn, checkOut, null);
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        if (roomId == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }
        return findConflictingBooking(roomId, checkIn, checkOut, excludeBookingId) == null;
    }
}
