module com.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.desktop;

    opens com.gui to javafx.fxml;
    exports com.gui;
}
