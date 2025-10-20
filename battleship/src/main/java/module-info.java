module com.cenit.battleship {
 
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.base;
    requires com.google.gson;
    
    opens com.cenit.battleship to javafx.fxml;
    opens com.cenit.battleship.view to javafx.fxml;
    opens com.cenit.battleship.controller to javafx.fxml;
    opens com.cenit.battleship.styles;
    opens com.cenit.battleship.model to com.google.gson;
    
   

    
    exports com.cenit.battleship;
    exports com.cenit.battleship.view;
    exports com.cenit.battleship.controller;
    exports com.cenit.battleship.model.enums to com.google.gson;
}
