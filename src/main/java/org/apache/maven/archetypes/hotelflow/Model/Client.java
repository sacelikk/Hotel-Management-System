package org.apache.maven.archetypes.hotelflow.Model;

public class Client {
    private Long clientId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private boolean isRisky;
    private String riskReason;

    // Parametresiz yapıcı (Default Constructor)
    public Client() {
        this.isRisky = false;
    }

    // 5 Parametreli yapıcı (Controller'daki örnek verilerin kullandığı)
    public Client(Long clientId, String fullName, String email, String phone, String address) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isRisky = false;
    }

    // Parametreli ve parametresiz markAsRisky metodları
    public void markAsRisky() {
        this.isRisky = true;
        this.riskReason = "Marked as risky by staff";
    }

    public void markAsRisky(String reason) {
        this.isRisky = true;
        this.riskReason = reason;
    }

    // Getter ve Setter Metotları
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isRisky() {
        return isRisky;
    }

    public void setRisky(boolean risky) {
        isRisky = risky;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }
}