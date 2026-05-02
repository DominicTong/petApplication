module com.petapp {
    requires javafx.controls;
    requires java.sql;

    // Allow JavaFX to reflectively access our Application subclass
    opens com.petapp    to javafx.graphics;
    opens com.petapp.ui to javafx.graphics;

    exports com.petapp;
    exports com.petapp.ui;
    exports com.petapp.model;
}
