module org.apache.maven.archetypes.hotelflow {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.apache.maven.archetypes.hotelflow to javafx.fxml;
    exports org.apache.maven.archetypes.hotelflow;
}