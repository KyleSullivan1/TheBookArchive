module testingjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    opens TheBookArchive to javafx.fxml;
    exports TheBookArchive;

    //requires java.desktop;
    requires java.sql;
}
