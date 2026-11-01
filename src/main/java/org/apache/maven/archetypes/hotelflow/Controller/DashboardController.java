package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.maven.archetypes.hotelflow.HelloApplication;
import org.apache.maven.archetypes.hotelflow.Model.Role;
import org.apache.maven.archetypes.hotelflow.Model.User;
import org.apache.maven.archetypes.hotelflow.Util.UserSession;

import java.io.IOException;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleBadge;
    @FXML private Label welcomeHeaderLabel;
    @FXML private StackPane contentArea;

    // Menü Butonları
    @FXML private Button btnManageOwners;
    @FXML private Button btnHotels;
    @FXML private Button btnAssignManagers;
    @FXML private Button btnReceptionists;
    @FXML private Button btnServices;
    @FXML private Button btnReservations;
    @FXML private Button btnCheckIn;
    @FXML private Button btnClients;
    @FXML private Button btnReports;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        userNameLabel.setText(currentUser.getFullName());
        userRoleBadge.setText(currentUser.getRole().name());
        welcomeHeaderLabel.setText("Welcome back, " + currentUser.getFullName() + "!");

        // Rol yetkilerine göre butonları filtrele (FR-02)
        applyRolePermissions(currentUser.getRole());

        // Buton tıklama eylemlerini bağla
        setupNavigationActions(currentUser.getRole());
    }

    private void setupNavigationActions(Role role) {
        if (btnReservations != null) {
            btnReservations.setOnAction(e -> loadView("reservations-view.fxml"));
        }

        if (btnCheckIn != null) {
            btnCheckIn.setOnAction(e -> loadView("checkin-view.fxml"));
        }

        // Müşteriler butonu bağlantısı:
        if (btnClients != null) {
            btnClients.setOnAction(e -> loadView("clients-view.fxml"));
        }

        if (role == Role.RECEPTIONIST) {
            loadView("reservations-view.fxml");
        }
    }
    @FXML
    private void handleReportsClick() {
        loadView("reports-view.fxml");
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Görünüm yüklenemedi: " + fxmlFile);
        }
    }

    private void applyRolePermissions(Role role) {
        hideAllMenuButtons();

        switch (role) {
            case ADMINISTRATOR:
                setButtonVisible(btnManageOwners, true);
                break;
            case OWNER:
                setButtonVisible(btnHotels, true);
                setButtonVisible(btnAssignManagers, true);
                setButtonVisible(btnReports, true);
                break;
            case MANAGER:
                setButtonVisible(btnReceptionists, true);
                setButtonVisible(btnServices, true);
                setButtonVisible(btnReports, true);
                break;
            case RECEPTIONIST:
                setButtonVisible(btnReservations, true);
                setButtonVisible(btnCheckIn, true);
                setButtonVisible(btnClients, true);
                setButtonVisible(btnReports, true);
                break;
        }
    }

    private void hideAllMenuButtons() {
        setButtonVisible(btnManageOwners, false);
        setButtonVisible(btnHotels, false);
        setButtonVisible(btnAssignManagers, false);
        setButtonVisible(btnReceptionists, false);
        setButtonVisible(btnServices, false);
        setButtonVisible(btnReservations, false);
        setButtonVisible(btnCheckIn, false);
        setButtonVisible(btnClients, false);
        setButtonVisible(btnReports, false);
    }

    private void setButtonVisible(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().logout();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("HotelFlow - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}