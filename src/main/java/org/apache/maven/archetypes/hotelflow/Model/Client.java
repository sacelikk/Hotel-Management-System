package org.apache.maven.archetypes.hotelflow.Model;

public class Client {
    private long ClientId;
    private String FullName;
    private String email;
    private String phone;
    private String identityNumber;
    private boolean isRisky=false;
    public void markAsRisky(){
        isRisky=true;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setClientId(long clientId) {
        ClientId = clientId;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public void setRisky(boolean risky) {
        isRisky = risky;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public long getClientId() {
        return ClientId;
    }

    public String getFullName() {
        return FullName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

}
