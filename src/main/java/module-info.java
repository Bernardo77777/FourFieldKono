module com.fourfield {
    requires javafx.controls;
    requires javafx.fxml;
    exports com.fourfield;
    exports com.fourfield.model;
    exports com.fourfield.view;
    exports com.fourfield.network;
    opens com.fourfield to javafx.fxml;
}
