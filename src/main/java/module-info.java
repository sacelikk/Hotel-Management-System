module org.apache.maven.archetypes.hotelflow {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.apache.maven.archetypes.hotelflow to javafx.fxml;
    exports org.apache.maven.archetypes.hotelflow;
    exports org.apache.maven.archetypes.hotelflow.Controller;
    exports org.apache.maven.archetypes.hotelflow.Model;
    exports org.apache.maven.archetypes.hotelflow.Util;
    exports org.apache.maven.archetypes.hotelflow.Service;

    opens org.apache.maven.archetypes.hotelflow.Model to javafx.base, javafx.fxml;
    opens org.apache.maven.archetypes.hotelflow.Controller to javafx.fxml;
    opens org.apache.maven.archetypes.hotelflow.Util to javafx.base;
    opens org.apache.maven.archetypes.hotelflow.Service to javafx.base;
}
