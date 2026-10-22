package org.apache.maven.archetypes.hotelflow.Model;


public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private boolean active;
    private Role role;

    // Parametresiz yapıcı (Constructor)
    public User() {
        this.active = true;
    }

    // Parametreli yapıcı (Constructor)
    public User(Long userId, String username, String passwordHash, String fullName, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    // Class diyagramındaki metotlar
    public boolean authenticate(String password) {
        if (!this.active) {
            return false;
        }
        // Şimdilik düz eşleşme kontrolü (ileride BCrypt/Hashing eklenebilir)
        return this.passwordHash != null && this.passwordHash.equals(password);
    }

    public void logout() {
        // Oturum kapatma mantığı (UserSession üzerinden de yönetilir)
        System.out.println(this.username + " oturumu kapatıldı.");
    }

    // Getter ve Setter Metotları
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

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}