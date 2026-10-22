package org.apache.maven.archetypes.hotelflow.Model;



import java.math.BigDecimal;

public class Service {
    private Long serviceId;
    private Long hotelId;
    private String name;
    private String description;
    private BigDecimal price;
    private Season season;
    private boolean active;

    public Service() {
        this.active = true;
        this.season = Season.ALL_YEAR;
    }

    public Service(Long serviceId, Long hotelId, String name, String description, BigDecimal price, Season season) {
        this.serviceId = serviceId;
        this.hotelId = hotelId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.season = season;
        this.active = true;
    }

    public void updateDetails(String name, String description, BigDecimal price, Season season) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.season = season;
    }

    public boolean isAvailableInSeason(Season currentSeason) {
        if (!this.active) return false;
        return this.season == Season.ALL_YEAR || this.season == currentSeason;
    }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}