package org.apache.maven.archetypes.hotelflow.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ServiceUsage {
    private Long usageId;
    private Long serviceId;
    private Long clientId;
    private LocalDateTime usageDate;
    private int quantity;
    private BigDecimal totalPrice;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public ServiceUsage() {
        this.usageDate = LocalDateTime.now();
        this.quantity = 1;
    }

    public ServiceUsage(Long usageId, Long serviceId, Long clientId, int quantity, BigDecimal unitPrice) {
        this.usageId = usageId;
        this.serviceId = serviceId;
        this.clientId = clientId;
        this.quantity = quantity;
        this.usageDate = LocalDateTime.now();
        this.totalPrice = calculateTotal(unitPrice);
    }

    public ServiceUsage(Long usageId, Long serviceId, Long clientId, int quantity, BigDecimal unitPrice, LocalDateTime date) {
        this.usageId = usageId;
        this.serviceId = serviceId;
        this.clientId = clientId;
        this.quantity = quantity;
        this.usageDate = date != null ? date : LocalDateTime.now();
        this.totalPrice = calculateTotal(unitPrice);
    }

    public BigDecimal calculateTotal(BigDecimal unitPrice) {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getFormattedTotalPrice() {
        if (totalPrice == null) return "€0.00";
        return String.format(Locale.US, "€%.2f", totalPrice);
    }

    public String getFormattedDate() {
        if (usageDate == null) return "-";
        return usageDate.format(FORMATTER);
    }

    public Long getUsageId() { return usageId; }
    public void setUsageId(Long usageId) { this.usageId = usageId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public LocalDateTime getUsageDate() { return usageDate; }
    public void setUsageDate(LocalDateTime usageDate) { this.usageDate = usageDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
