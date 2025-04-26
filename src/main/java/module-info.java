module com.example.livraison {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;


    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;

    requires java.sql;

    requires javafx.media;

    opens com.example.livraison to javafx.fxml;
    opens com.example.livraison.Controllers to javafx.fxml;

    exports com.example.livraison;
    exports com.example.livraison.Controllers;
}




