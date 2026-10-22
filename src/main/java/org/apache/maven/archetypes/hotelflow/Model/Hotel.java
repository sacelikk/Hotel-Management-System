package org.apache.maven.archetypes.hotelflow.Model;

import org.apache.maven.archetypes.hotelflow.Model.Room;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private Long hotelId;
    private Long ownerId;
    private Long managerId;
    private String name;
    private String address;
    private String phone;
    private String email;

    // Otelde bulunan odaların referans listesi
    private List<Room> rooms;

    public Hotel() {
        this.rooms = new ArrayList<>();
    }

    public Hotel(Long hotelId, Long ownerId, Long managerId, String name, String address, String phone, String email) {
        this.hotelId = hotelId;
        this.ownerId = ownerId;
        this.managerId = managerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.rooms = new ArrayList<>();
    }

    // Class diyagramındaki metotlar
    public void addRoom(Room room) {
        if (room != null) {
            this.rooms.add(room);
        }
    }

    public void updateDetails(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Report getOccupancyReport() {
        // İleride ReportService üzerinden doldurulacak
        return null;
    }

    // Getter ve Setter metotları
    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
