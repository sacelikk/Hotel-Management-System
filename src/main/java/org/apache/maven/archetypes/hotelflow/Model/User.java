package org.apache.maven.archetypes.hotelflow.Model;

public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private boolean active;
    private Role role;
    private Long hotelId;

    public User() {
        this.active = true;
    }

    public User(Long userId, String username, String passwordHash, String fullName, String email, Role role) {
        this(userId, username, passwordHash, fullName, email, role, null);
    }

    public User(Long userId, String username, String passwordHash, String fullName, String email, Role role, Long hotelId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.hotelId = hotelId;
        this.active = true;
    }

    public boolean authenticate(String password) {
        if (!this.active) {
            return false;
        }
        return this.passwordHash != null && this.passwordHash.equals(password);
    }

    public void logout() {
        System.out.println(this.username + " oturumu kapatıldı.");
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + " - " + role + ")";
    }
}
