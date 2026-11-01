package org.apache.maven.archetypes.hotelflow.Model;

public class Room {
    private Long roomId;
    private Long hotelId;
    private RoomCategory roomCategory;
    private String roomNumber;
    private int floor;
    private RoomStatus roomStatus;

    public Room() {
        this.roomStatus = RoomStatus.AVAILABLE;
    }

    public Room(Long roomId, Long hotelId, String roomNumber, int floor, RoomStatus roomStatus) {
        this.roomId = roomId;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.roomStatus = roomStatus != null ? roomStatus : RoomStatus.AVAILABLE;
    }

    public Room(Long roomId, Long hotelId, RoomCategory roomCategory, String roomNumber, int floor, RoomStatus roomStatus) {
        this(roomId, hotelId, roomNumber, floor, roomStatus);
        this.roomCategory = roomCategory;
    }

    // Odanın müsait olup olmadığını döner (FR-11)
    public boolean isAvailable() {
        return this.roomStatus == RoomStatus.AVAILABLE;
    }

    // Odanın durumunu günceller
    public void updateStatus(RoomStatus newStatus) {
        this.roomStatus = newStatus;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public RoomCategory getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(RoomCategory roomCategory) {
        this.roomCategory = roomCategory;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    @Override
    public String toString() {
        return "Room #" + (roomNumber != null ? roomNumber : roomId) + " (Floor " + floor + ")";
    }
}
