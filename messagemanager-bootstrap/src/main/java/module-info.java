module messagemanager.bootstrap {
    opens at.conapi.messagemanager.bootstrap to javafx.fxml, javafx.graphics, org.update4j;

    requires org.update4j;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;

    // see https://bugs.openjdk.org/browse/JDK-8268657
}
