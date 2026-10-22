package org.apache.maven.archetypes.hotelflow.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private Long bookingId;
    private String bookingNumber;
    private Long clientId;
    private Long roomId;
    private Long receptionistId;
    private BookingType bookingType;
    private CancellationType cancellationType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking() {
        this.status = BookingStatus.CONFIRMED;
        this.bookingType = BookingType.STANDARD;
        this.cancellationType = CancellationType.FREE;
        this.createdAt = LocalDateTime.now();
    }

    public Booking(Long bookingId, String bookingNumber, Long clientId, Long roomId,
                   Long receptionistId, BookingType bookingType, CancellationType cancellationType,
                   LocalDate startDate, LocalDate endDate) {
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.clientId = clientId;
        this.roomId = roomId;
        this.receptionistId = receptionistId;
        this.bookingType = bookingType;
        this.cancellationType = cancellationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    // FR-11: Çıkış tarihi giriş tarihinden sonra olmalıdır
    public boolean checkAvailability() {
        return this.startDate != null && this.endDate != null && this.endDate.isAfter(this.startDate);
    }

    // FR-10: Rezervasyon iptali
    public void cancel(CancellationType type) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationType = type;
    }

    // Getter ve Setter Metotları
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getReceptionistId() {
        return receptionistId;
    }

    public void setReceptionistId(Long receptionistId) {
        this.receptionistId = receptionistId;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public CancellationType getCancellationType() {
        return cancellationType;
    }

    public void setCancellationType(CancellationType cancellationType) {
        this.cancellationType = cancellationType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}