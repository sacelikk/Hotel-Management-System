package org.apache.maven.archetypes.hotelflow.Model;

import java.time.LocalDateTime;

public class CheckIn {
    private Long checkInId;
    private Long bookingId;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
    private CheckInStatus status;

    public CheckIn() {
        this.status = CheckInStatus.CHECKED_IN;
    }

    public CheckIn(Long checkInId, Long bookingId, LocalDateTime actualCheckInTime, LocalDateTime actualCheckOutTime, CheckInStatus status) {
        this.checkInId = checkInId;
        this.bookingId = bookingId;
        this.actualCheckInTime = actualCheckInTime;
        this.actualCheckOutTime = actualCheckOutTime;
        this.status = status;
    }

    // İşlem metotları
    public void registerCheckIn() {
        this.actualCheckInTime = LocalDateTime.now();
        this.status = CheckInStatus.CHECKED_IN;
    }

    public void registerCheckOut() {
        this.actualCheckOutTime = LocalDateTime.now();
        this.status = CheckInStatus.CHECKED_OUT;
    }

    // Getter & Setter Metotları
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

    public LocalDateTime getActualCheckInTime() {
        return actualCheckInTime;
    }

    public void setActualCheckInTime(LocalDateTime actualCheckInTime) {
        this.actualCheckInTime = actualCheckInTime;
    }

    public LocalDateTime getActualCheckOutTime() {
        return actualCheckOutTime;
    }

    public void setActualCheckOutTime(LocalDateTime actualCheckOutTime) {
        this.actualCheckOutTime = actualCheckOutTime;
    }

    public CheckInStatus getStatus() {
        return status;
    }

    public void setStatus(CheckInStatus status) {
        this.status = status;
    }
}