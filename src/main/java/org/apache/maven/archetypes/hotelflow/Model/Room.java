package org.apache.maven.archetypes.hotelflow.Model;

public class Room {
    private int RoomId;
    private int HotelId;
    private RoomCategory roomCategory;
    private String roomNumber;
    private int floor;
    private RoomStatus roomStatus;

    // Odanın müsait olup olmadığını döner (FR-11)
    public boolean isAvailable() {
        return this.roomStatus == RoomStatus.AVAILABLE;
    }

    // Odanın durumunu günceller
    public void updateStatus(RoomStatus newStatus) {
        this.roomStatus = newStatus;
    }
}
