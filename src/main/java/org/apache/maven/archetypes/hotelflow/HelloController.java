package org.apache.maven.archetypes.hotelflow;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            statusLabel.setText("Lütfen kullanıcı adı ve şifre giriniz!");
            return;
        }

        // Geçici doğrulama testi (Veritabanı servisini bağlayana kadar)
        statusLabel.setStyle("-fx-text-fill: #2e7d32;");
        statusLabel.setText("Giriş yapılıyor: " + username);
    }
}