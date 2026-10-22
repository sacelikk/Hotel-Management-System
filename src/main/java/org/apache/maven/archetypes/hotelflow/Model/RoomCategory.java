package org.apache.maven.archetypes.hotelflow.Model;

public class RoomCategory {
    private int RoomCategoryId;
    private int HotelId;
    private String Description;
    private String name;
    private double basePrice;
    private int capacity;
    public void updateprice(){

    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getRoomCategoryId() {
        return RoomCategoryId;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getHotelId() {
        return HotelId;
    }

    public String getDescription() {
        return Description;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomCategoryId(int roomCategoryId) {
        RoomCategoryId = roomCategoryId;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setHotelId(int hotelId) {
        HotelId = hotelId;
    }

}
