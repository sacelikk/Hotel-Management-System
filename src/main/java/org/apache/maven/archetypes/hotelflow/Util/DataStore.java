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
    private final ObservableList<Service> services = FXCollections.observableArrayList();
    private final ObservableList<ServiceUsage> serviceUsages = FXCollections.observableArrayList();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObservableList<RoomCategory> roomCategories = FXCollections.observableArrayList();

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
        services.clear();
        serviceUsages.clear();
        users.clear();
        hotels.clear();
        roomCategories.clear();
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
        roomCategories.addAll(standardCat, deluxeCat, suiteCat);

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

        // 6. Ortak Sezonluk Ek Hizmetler (FR-13)
        Service s1 = new Service(1L, 1L, "Thermal Spa & Massage", "Full body thermal spa and hot stone relaxation massage", new java.math.BigDecimal("45.00"), Season.ALL_YEAR);
        Service s2 = new Service(2L, 1L, "Outdoor Infinity Pool & Bar", "Access to heated outdoor infinity pool and cocktails", new java.math.BigDecimal("25.00"), Season.SUMMER);
        Service s3 = new Service(3L, 1L, "Ski Gear & Mountain Shuttle", "Full ski/snowboard equipment and mountain slope shuttle", new java.math.BigDecimal("60.00"), Season.WINTER);
        Service s4 = new Service(4L, 1L, "Buffet Breakfast Premium", "Continental & English open buffet with fresh organic beverages", new java.math.BigDecimal("18.00"), Season.ALL_YEAR);
        Service s5 = new Service(5L, 1L, "Airport VIP Transfer", "Private luxury two-way airport transfer service", new java.math.BigDecimal("50.00"), Season.ALL_YEAR);
        Service s6 = new Service(6L, 1L, "Autumn Forest Guided Tour", "Mountain guided hiking, photography and local wine tasting", new java.math.BigDecimal("35.00"), Season.AUTUMN);
        services.addAll(s1, s2, s3, s4, s5, s6);

        // 7. Ortak Hizmet Kullanımları
        ServiceUsage u1 = new ServiceUsage(1L, 1L, 101L, 2, new java.math.BigDecimal("45.00")); // Alex Schmidt -> 2x Spa
        ServiceUsage u2 = new ServiceUsage(2L, 4L, 104L, 3, new java.math.BigDecimal("18.00")); // Sophia Rossi -> 3x Breakfast
        ServiceUsage u3 = new ServiceUsage(3L, 5L, 102L, 1, new java.math.BigDecimal("50.00")); // Elena Popova -> 1x VIP Transfer
        serviceUsages.addAll(u1, u2, u3);

        // 8. Sistem Kullanıcıları (Admin, Owner, Manager, Receptionist)
        User uAdmin = new User(1L, "admin", "123", "System Administrator", "admin@hotelflow.com", Role.ADMINISTRATOR);
        User uOwner = new User(2L, "owner", "123", "Alexander Vance (Owner)", "owner@hotelflow.com", Role.OWNER);
        User uManager = new User(3L, "manager", "123", "Viktor Stoyanov (Manager)", "manager@hotelflow.com", Role.MANAGER, 1L);
        User uReceptionist = new User(4L, "receptionist", "123", "Maria Petrova (Front Desk)", "receptionist@hotelflow.com", Role.RECEPTIONIST, 1L);
        users.addAll(uAdmin, uOwner, uManager, uReceptionist);

        // 9. Sistem Otelleri (FR-04, FR-05)
        Hotel h1 = new Hotel(1L, 2L, 3L, "Grand Varna Palace", "14 Primorski Blvd, Varna", "+359 52 123456", "grand@hotelflow.bg");
        Hotel h2 = new Hotel(2L, 2L, null, "Black Sea Luxury Resort", "Golden Sands Resort, Varna", "+359 52 654321", "resort@hotelflow.bg");
        hotels.addAll(h1, h2);
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

    // ==========================================
    // FR-13: Seasonal Additional Services & Usages
    // ==========================================

    public ObservableList<Service> getServices() {
        return services;
    }

    public ObservableList<ServiceUsage> getServiceUsages() {
        return serviceUsages;
    }

    public Service findServiceById(Long id) {
        if (id == null) return null;
        return services.stream()
                .filter(s -> s.getServiceId() != null && s.getServiceId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addService(Service service) {
        if (service != null) {
            this.services.add(service);
        }
    }

    public void addServiceUsage(ServiceUsage usage) {
        if (usage != null) {
            this.serviceUsages.add(usage);
        }
    }

    /**
     * FR-13 & Business Rule: Records a guest's service usage, ensuring the service is available in the current season.
     * Returns the created ServiceUsage, or null if service is out of season and override is false.
     */
    public ServiceUsage recordServiceUsage(Long serviceId, Long clientId, int quantity, boolean allowOutOfSeason) {
        if (serviceId == null || clientId == null || quantity <= 0) {
            return null;
        }

        Service service = findServiceById(serviceId);
        Client client = findClientById(clientId);
        if (service == null || client == null || !service.isActive()) {
            return null;
        }

        if (!allowOutOfSeason && !service.isAvailableNow()) {
            return null; // Business rule: Seasonal services shall only be provided within their configured availability periods
        }

        long maxId = serviceUsages.stream()
                .mapToLong(u -> u.getUsageId() != null ? u.getUsageId() : 0L)
                .max()
                .orElse(0L);
        long newId = maxId + 1;

        ServiceUsage usage = new ServiceUsage(newId, serviceId, clientId, quantity, service.getPrice());
        serviceUsages.add(usage);
        return usage;
    }

    // ==========================================
    // FR-03, FR-04, FR-05, FR-06: Users, Hotels, Categories
    // ==========================================

    public ObservableList<User> getUsers() {
        return users;
    }

    public ObservableList<Hotel> getHotels() {
        return hotels;
    }

    public ObservableList<RoomCategory> getRoomCategories() {
        return roomCategories;
    }

    public User findUserById(Long id) {
        if (id == null) return null;
        return users.stream()
                .filter(u -> u.getUserId() != null && u.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User findUserByUsername(String username) {
        if (username == null) return null;
        return users.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst()
                .orElse(null);
    }

    public Hotel findHotelById(Long id) {
        if (id == null) return null;
        return hotels.stream()
                .filter(h -> h.getHotelId() != null && h.getHotelId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public RoomCategory findRoomCategoryById(int id) {
        return roomCategories.stream()
                .filter(rc -> rc.getRoomCategoryId() == id)
                .findFirst()
                .orElse(null);
    }

    public void addUser(User user) {
        if (user != null) {
            this.users.add(user);
        }
    }

    public void addHotel(Hotel hotel) {
        if (hotel != null) {
            this.hotels.add(hotel);
        }
    }

    public boolean assignManagerToHotel(Long hotelId, Long managerId) {
        Hotel hotel = findHotelById(hotelId);
        if (hotel == null) return false;
        hotel.setManagerId(managerId);

        if (managerId != null) {
            User manager = findUserById(managerId);
            if (manager != null) {
                manager.setHotelId(hotelId);
            }
        }
        return true;
    }

    public boolean updateRoomCategoryPrice(int categoryId, double newPrice) {
        RoomCategory cat = findRoomCategoryById(categoryId);
        if (cat == null || newPrice < 0) return false;

        cat.updatePrice(newPrice);
        for (Room r : rooms) {
            if (r.getRoomCategory() != null && r.getRoomCategory().getRoomCategoryId() == categoryId) {
                r.getRoomCategory().updatePrice(newPrice);
            }
        }
        return true;
    }
}
