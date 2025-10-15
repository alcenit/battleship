module com.cenit.battleship {
 
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.base;
    
    opens com.cenit.battleship to javafx.fxml;
    opens com.cenit.battleship.view to javafx.fxml;
    opens com.cenit.battleship.controller to javafx.fxml;
    
    exports com.cenit.battleship;
    exports com.cenit.battleship.view;
    exports com.cenit.battleship.controller;
    requires com.google.gson;
}

