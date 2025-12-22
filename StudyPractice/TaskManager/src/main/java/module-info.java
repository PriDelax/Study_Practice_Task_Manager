module com.example.taskmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;

    opens com.example.taskmanager to javafx.fxml, com.google.gson;
    exports com.example.taskmanager;
}