module org.apache.maven.archetypes.hotelflow {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.apache.maven.archetypes.hotelflow to javafx.fxml;
    exports org.apache.maven.archetypes.hotelflow;
    exports org.apache.maven.archetypes.hotelflow.Controller;
    opens org.apache.maven.archetypes.hotelflow.Model to javafx.base, javafx.fxml;
    opens org.apache.maven.archetypes.hotelflow.Controller to javafx.fxml;
}