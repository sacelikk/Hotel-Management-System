package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.maven.archetypes.hotelflow.HelloApplication;
import org.apache.maven.archetypes.hotelflow.Model.Role;
import org.apache.maven.archetypes.hotelflow.Model.User;
import org.apache.maven.archetypes.hotelflow.Util.UserSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HelloController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private ImageView bgImageView;

    // Geçici test kullanıcı havuzu (Giriş: kullanıcıAdı / Şifre: 123)
    private final Map<String, User> mockUsers = new HashMap<>();

    @FXML
    public void initialize() {
        // Günün tarihini otomatik formatlayıp sağ paneldeki rozete basar
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        dateLabel.setText(LocalDate.now().format(formatter));

        // Test kullanıcıları
        mockUsers.put("admin", new User(1L, "admin", "123", "System Administrator", "admin@hotelflow.com", Role.ADMINISTRATOR));
        mockUsers.put("owner", new User(2L, "owner", "123", "Hotel Owner", "owner@hotelflow.com", Role.OWNER));
        mockUsers.put("manager", new User(3L, "manager", "123", "Hotel Manager", "manager@hotelflow.com", Role.MANAGER));
        mockUsers.put("receptionist", new User(4L, "receptionist", "123", "Front Desk Receptionist", "receptionist@hotelflow.com", Role.RECEPTIONIST));
    }

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = mockUsers.get(username.toLowerCase());

        if (user != null && user.authenticate(password)) {
            UserSession.getInstance().login(user);//login time
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
            statusLabel.setText("Login successful! Redirecting...");

            try {
                // Dashboard ekranına geçiş yap
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("dashboard-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setTitle("HotelFlow - Dashboard (" + user.getRole() + ")");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Could not load dashboard view.");
            }
        } else {
            showError("Invalid username or password.");
        }
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
        statusLabel.setText(message);
    }
}