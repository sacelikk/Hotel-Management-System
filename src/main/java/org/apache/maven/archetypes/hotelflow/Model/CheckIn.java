package org.apache.maven.archetypes.hotelflow.Model;


import java.time.LocalDateTime;

public class CheckIn {
    private Long checkInId;
    private Long bookingId;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private CheckInStatus status;
    private String formContent;

    public CheckIn() {
        this.status = CheckInStatus.ACTIVE;
        this.checkInDate = LocalDateTime.now();
    }

    public CheckIn(Long checkInId, Long bookingId, String formContent) {
        this.checkInId = checkInId;
        this.bookingId = bookingId;
        this.formContent = formContent;
        this.checkInDate = LocalDateTime.now();
        this.status = CheckInStatus.ACTIVE;
    }

    // Class diyagramındaki iş metotları
    public void registerCheckIn() {
        this.checkInDate = LocalDateTime.now();
        this.status = CheckInStatus.ACTIVE;
    }

    public void registerCheckOut() {
        this.checkOutDate = LocalDateTime.now();
        this.status = CheckInStatus.CHECKED_OUT;
    }

    // Getter ve Setter metotları
    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDateTime checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDateTime getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDateTime checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public CheckInStatus getStatus() {
        return status;
    }

    public void setStatus(CheckInStatus status) {
        this.status = status;
    }

    public String getFormContent() {
        return formContent;
    }

    public void setFormContent(String formContent) {
        this.formContent = formContent;
    }
}