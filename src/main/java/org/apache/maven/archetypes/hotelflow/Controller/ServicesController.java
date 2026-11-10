package org.apache.maven.archetypes.hotelflow.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.maven.archetypes.hotelflow.Model.Client;
import org.apache.maven.archetypes.hotelflow.Model.Season;
import org.apache.maven.archetypes.hotelflow.Model.Service;
import org.apache.maven.archetypes.hotelflow.Model.ServiceUsage;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;

import java.math.BigDecimal;
import java.util.Optional;

public class ServicesController {

    // KPI Labels
    @FXML private Label lblTotalServices;
    @FXML private Label lblInSeasonServices;
    @FXML private Label lblTotalUsages;
    @FXML private Label lblServiceRevenue;
    @FXML private Label lblCurrentSeasonBadge;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> seasonFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label statusMessageLabel;

    // Tab 1: Services Catalog
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, Long> colServiceId;
    @FXML private TableColumn<Service, String> colServiceName;
    @FXML private TableColumn<Service, String> colDescription;
    @FXML private TableColumn<Service, String> colSeason;
    @FXML private TableColumn<Service, String> colPrice;
    @FXML private TableColumn<Service, String> colAvailability;
    @FXML private TableColumn<Service, String> colActive;

    // Tab 2: Guest Usages
    @FXML private TableView<ServiceUsage> usagesTable;
    @FXML private TableColumn<ServiceUsage, Long> colUsageId;
    @FXML private TableColumn<ServiceUsage, String> colUsageDate;
    @FXML private TableColumn<ServiceUsage, String> colClient;
    @FXML private TableColumn<ServiceUsage, String> colUsageServiceName;
    @FXML private TableColumn<ServiceUsage, Integer> colQuantity;
    @FXML private TableColumn<ServiceUsage, String> colUsageUnitPrice;
    @FXML private TableColumn<ServiceUsage, String> colUsageTotalPrice;

    private final ObservableList<Service> masterServices = DataStore.getInstance().getServices();
    private final ObservableList<ServiceUsage> masterUsages = DataStore.getInstance().getServiceUsages();
    private FilteredList<Service> filteredServices;
    private FilteredList<ServiceUsage> filteredUsages;

    @FXML
    public void initialize() {
        setupKPIs();
        setupServicesTable();
        setupUsagesTable();
        setupFilters();
        updateKpisAndStatus();
    }

    private void setupKPIs() {
        Season current = Season.getCurrentSeason();
        if (lblCurrentSeasonBadge != null) {
            lblCurrentSeasonBadge.setText("Active Season: " + current.name() + " (" + current.getDisplayName() + ")");
        }
    }

    private void setupServicesTable() {
        colServiceId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getServiceId()));
        colServiceName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        colSeason.setCellValueFactory(data -> {
            Season s = data.getValue().getSeason();
            return new SimpleStringProperty(s != null ? s.getDisplayName() : "All Year");
        });

        colPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedPrice()));

        colAvailability.setCellValueFactory(data -> {
            boolean avail = data.getValue().isAvailableNow();
            return new SimpleStringProperty(avail ? "AVAILABLE NOW" : "OUT OF SEASON");
        });

        colAvailability.setCellFactory(col -> new TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("AVAILABLE NOW".equals(item)) {
                        setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colActive.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "ACTIVE" : "INACTIVE"));
        colActive.setCellFactory(col -> new TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                }
            }
        });

        filteredServices = new FilteredList<>(masterServices, p -> true);
        servicesTable.setItems(filteredServices);
    }

    private void setupUsagesTable() {
        colUsageId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getUsageId()));
        colUsageDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));

        colClient.setCellValueFactory(data -> {
            Client c = DataStore.getInstance().findClientById(data.getValue().getClientId());
            if (c != null) {
                return new SimpleStringProperty(c.getFullName() + " (#" + c.getClientId() + ")");
            }
            return new SimpleStringProperty("Client #" + data.getValue().getClientId());
        });

        colUsageServiceName.setCellValueFactory(data -> {
            Service s = DataStore.getInstance().findServiceById(data.getValue().getServiceId());
            return new SimpleStringProperty(s != null ? s.getName() : "Service #" + data.getValue().getServiceId());
        });

        colQuantity.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));

        colUsageUnitPrice.setCellValueFactory(data -> {
            Service s = DataStore.getInstance().findServiceById(data.getValue().getServiceId());
            return new SimpleStringProperty(s != null ? s.getFormattedPrice() : "€0.00");
        });

        colUsageTotalPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTotalPrice()));

        filteredUsages = new FilteredList<>(masterUsages, p -> true);
        usagesTable.setItems(filteredUsages);
    }

    private void setupFilters() {
        seasonFilterCombo.getItems().addAll("ALL Seasons", "SPRING", "SUMMER", "AUTUMN", "WINTER", "ALL_YEAR");
        seasonFilterCombo.setValue("ALL Seasons");

        statusFilterCombo.getItems().addAll("ALL Statuses", "Active Only", "Inactive Only", "In-Season Only");
        statusFilterCombo.setValue("ALL Statuses");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        seasonFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String season = seasonFilterCombo.getValue();
        String status = statusFilterCombo.getValue();

        // 1. Filter Services
        filteredServices.setPredicate(s -> {
            boolean matchSearch = q.isEmpty()
                    || (s.getName() != null && s.getName().toLowerCase().contains(q))
                    || (s.getDescription() != null && s.getDescription().toLowerCase().contains(q))
                    || String.valueOf(s.getServiceId()).contains(q);

            boolean matchSeason = season == null || season.equals("ALL Seasons")
                    || (s.getSeason() != null && s.getSeason().name().equalsIgnoreCase(season));

            boolean matchStatus = true;
            if ("Active Only".equals(status)) matchStatus = s.isActive();
            else if ("Inactive Only".equals(status)) matchStatus = !s.isActive();
            else if ("In-Season Only".equals(status)) matchStatus = s.isAvailableNow();

            return matchSearch && matchSeason && matchStatus;
        });

        // 2. Filter Usages
        filteredUsages.setPredicate(u -> {
            if (q.isEmpty()) return true;
            Client c = DataStore.getInstance().findClientById(u.getClientId());
            Service s = DataStore.getInstance().findServiceById(u.getServiceId());
            String cName = c != null ? c.getFullName().toLowerCase() : "";
            String sName = s != null ? s.getName().toLowerCase() : "";
            return cName.contains(q) || sName.contains(q) || String.valueOf(u.getUsageId()).contains(q);
        });

        servicesTable.refresh();
        usagesTable.refresh();
        updateKpisAndStatus();
    }

    private void updateKpisAndStatus() {
        int totalServices = masterServices.size();
        long inSeason = masterServices.stream().filter(Service::isAvailableNow).count();
        int totalUsages = masterUsages.size();

        BigDecimal revenue = masterUsages.stream()
                .map(ServiceUsage::getTotalPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lblTotalServices != null) lblTotalServices.setText(String.valueOf(totalServices));
        if (lblInSeasonServices != null) lblInSeasonServices.setText(String.valueOf(inSeason));
        if (lblTotalUsages != null) lblTotalUsages.setText(String.valueOf(totalUsages));
        if (lblServiceRevenue != null) lblServiceRevenue.setText(String.format("€%.2f", revenue));

        if (statusMessageLabel != null) {
            statusMessageLabel.setText("Showing " + filteredServices.size() + " services and " + filteredUsages.size() + " recorded usages.");
        }
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        seasonFilterCombo.setValue("ALL Seasons");
        statusFilterCombo.setValue("ALL Statuses");
        applyFilters();
    }

    @FXML
    private void handleToggleActive() {
        Service selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a service from the table first.");
            return;
        }

        selected.setActive(!selected.isActive());
        servicesTable.refresh();
        updateKpisAndStatus();
        showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                "Service '" + selected.getName() + "' is now " + (selected.isActive() ? "ACTIVE" : "INACTIVE") + ".");
    }

    @FXML
    private void handleNewService() {
        Dialog<Service> dialog = new Dialog<>();
        dialog.setTitle("Add Seasonal Hotel Service");
        dialog.setHeaderText("Configure a new seasonal hotel service & pricing (FR-13)");

        ButtonType saveBtn = new ButtonType("Add Service", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Roof-top Jacuzzi & Wine");

        TextField descField = new TextField();
        descField.setPromptText("e.g. Evening relaxation session with skyline view");

        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 40.00");

        ComboBox<Season> seasonCombo = new ComboBox<>();
        seasonCombo.getItems().setAll(Season.values());
        seasonCombo.setValue(Season.ALL_YEAR);

        grid.add(new Label("Service Name *:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Unit Price (€) *:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Seasonal Availability *:"), 0, 3);
        grid.add(seasonCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String name = nameField.getText() != null ? nameField.getText().trim() : "";
                String desc = descField.getText() != null ? descField.getText().trim() : "";
                String priceStr = priceField.getText() != null ? priceField.getText().trim() : "";

                if (name.isEmpty() || priceStr.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Information", "Service name and price are required.");
                    return null;
                }

                BigDecimal price;
                try {
                    price = new BigDecimal(priceStr);
                    if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be a valid positive number.");
                    return null;
                }

                long maxId = masterServices.stream().mapToLong(s -> s.getServiceId() != null ? s.getServiceId() : 0L).max().orElse(0L);
                return new Service(maxId + 1, 1L, name, desc, price, seasonCombo.getValue());
            }
            return null;
        });

        Optional<Service> result = dialog.showAndWait();
        result.ifPresent(service -> {
            masterServices.add(service);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Service '" + service.getName() + "' created successfully!");
        });
    }

    @FXML
    private void handleRecordUsage() {
        Dialog<ServiceUsage> dialog = new Dialog<>();
        dialog.setTitle("Record Guest Service Usage");
        dialog.setHeaderText("Log an additional service consumed by a registered guest (FR-13)");

        ButtonType saveBtn = new ButtonType("Record Usage", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        ComboBox<Client> clientCombo = new ComboBox<>();
        clientCombo.getItems().addAll(DataStore.getInstance().getClients());
        if (!clientCombo.getItems().isEmpty()) clientCombo.setValue(clientCombo.getItems().get(0));

        ComboBox<Service> serviceCombo = new ComboBox<>();
        serviceCombo.getItems().addAll(DataStore.getInstance().getServices());
        if (!serviceCombo.getItems().isEmpty()) serviceCombo.setValue(serviceCombo.getItems().get(0));

        Spinner<Integer> qtySpinner = new Spinner<>(1, 20, 1);
        qtySpinner.setEditable(true);

        Label totalLabel = new Label("€0.00");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d; -fx-font-size: 14px;");

        Runnable calcTotal = () -> {
            Service s = serviceCombo.getValue();
            int qty = qtySpinner.getValue() != null ? qtySpinner.getValue() : 1;
            if (s != null && s.getPrice() != null) {
                BigDecimal tot = s.getPrice().multiply(BigDecimal.valueOf(qty));
                totalLabel.setText(String.format("€%.2f", tot));
            } else {
                totalLabel.setText("€0.00");
            }
        };

        serviceCombo.valueProperty().addListener((obs, o, n) -> calcTotal.run());
        qtySpinner.valueProperty().addListener((obs, o, n) -> calcTotal.run());
        calcTotal.run();

        grid.add(new Label("Select Guest *:"), 0, 0);
        grid.add(clientCombo, 1, 0);
        grid.add(new Label("Select Service *:"), 0, 1);
        grid.add(serviceCombo, 1, 1);
        grid.add(new Label("Quantity *:"), 0, 2);
        grid.add(qtySpinner, 1, 2);
        grid.add(new Label("Calculated Total:"), 0, 3);
        grid.add(totalLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Client client = clientCombo.getValue();
                Service service = serviceCombo.getValue();
                int qty = qtySpinner.getValue();

                if (client == null || service == null) {
                    showAlert(Alert.AlertType.ERROR, "Selection Required", "Please select both a client and a service.");
                    return null;
                }

                // FR-13 Business Rule: Check seasonal availability
                if (!service.isAvailableNow()) {
                    Alert warn = new Alert(Alert.AlertType.CONFIRMATION);
                    warn.setTitle("Seasonal Availability Notice (FR-13)");
                    warn.setHeaderText("Service Is Currently Out of Season!");
                    warn.setContentText("The selected service '" + service.getName() + "' is configured for " +
                            service.getSeason().getDisplayName() + ", but the current season is " +
                            Season.getCurrentSeason().getDisplayName() + ".\n\n" +
                            "Business rule dictates seasonal services should be consumed during their season.\n" +
                            "Do you want to authorize this exception and proceed with the charge?");

                    Optional<ButtonType> choice = warn.showAndWait();
                    if (choice.isEmpty() || choice.get() != ButtonType.OK) {
                        return null;
                    }
                }

                long maxId = masterUsages.stream().mapToLong(u -> u.getUsageId() != null ? u.getUsageId() : 0L).max().orElse(0L);
                return new ServiceUsage(maxId + 1, service.getServiceId(), client.getClientId(), qty, service.getPrice());
            }
            return null;
        });

        Optional<ServiceUsage> result = dialog.showAndWait();
        result.ifPresent(usage -> {
            masterUsages.add(usage);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Usage Recorded",
                    "Service usage #" + usage.getUsageId() + " successfully recorded and billed (" + usage.getFormattedTotalPrice() + ")!");
        });
    }

    @FXML
    private void handleDeleteUsage() {
        ServiceUsage selected = usagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a usage record from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Service Usage Record #" + selected.getUsageId());
        confirm.setContentText("Are you sure you want to cancel and remove this recorded charge (" + selected.getFormattedTotalPrice() + ")?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            masterUsages.remove(selected);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Usage record removed.");
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        applyFilters();
        updateKpisAndStatus();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
