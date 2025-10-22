package com.cenit.battleship.view.components;

import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class ShipRenderer {
    
    /**
     * Crea una representación gráfica de un barco para el panel de barcos
     */
    public static HBox createShipDisplay(Ship ship) {
        HBox container = new HBox(5);
        container.getStyleClass().add("barco-item");
        
        Label nameLabel = new Label(ship.getType().getName());
        nameLabel.getStyleClass().add("barco-nombre");
        
        HBox shipGraphic = createShipGraphic(ship, false);
        shipGraphic.getStyleClass().add("barco-representacion");
        
        container.getChildren().addAll(nameLabel, shipGraphic);
        return container;
    }
    
    /**
     * Crea la representación gráfica del barco (para panel y tablero)
     */
    public static HBox createShipGraphic(Ship ship, boolean forBoard) {
        HBox shipContainer = new HBox(0);
        shipContainer.getStyleClass().add("barco-grafico");
        
        int size = ship.getType().getSize();
        int segmentSize = forBoard ? 40 : 25;
        
        for (int i = 0; i < size; i++) {
            StackPane segment = createShipSegment(ship, i, segmentSize, forBoard);
            shipContainer.getChildren().add(segment);
        }
        
        // Aplicar dirección si es para el tablero
        if (forBoard) {
            if (ship.getDirection() == Direction.VERTICAL) {
                shipContainer.getStyleClass().add("barco-vertical");
            } else {
                shipContainer.getStyleClass().add("barco-horizontal");
            }
        }
        
        return shipContainer;
    }
    
    /**
     * Crea un segmento individual del barco
     */
    private static StackPane createShipSegment(Ship ship, int segmentIndex, int size, boolean forBoard) {
        StackPane segment = new StackPane();
        segment.setPrefSize(size, size);
        
        // Fondo del segmento
        Rectangle background = new Rectangle(size, size);
        
        // Determinar el estado del segmento
        boolean isDamaged = false;
        if (ship.isPlaced() && segmentIndex < ship.getCoordinates().size()) {
            isDamaged = ship.isPartDamaged(ship.getCoordinates().get(segmentIndex));
        }
        
        // Configurar colores según el estado
        if (forBoard) {
            background.setFill(isDamaged ? Color.valueOf("#ff5252") : Color.valueOf("#4a6572"));
            background.setStroke(isDamaged ? Color.valueOf("#d32f2f") : Color.valueOf("#344955"));
        } else {
            background.setFill(isDamaged ? Color.valueOf("#dc3545") : Color.valueOf("#6c757d"));
            background.setStroke(Color.valueOf("#495057"));
        }
        
        background.setStrokeWidth(1);
        background.setArcWidth(3);
        background.setArcHeight(3);
        
        // Icono del barco (solo para el primer segmento o según el tipo)
        if (segmentIndex == 0) {
            Label icon = createShipIcon(ship.getType());
            segment.getChildren().addAll(background, icon);
        } else {
            segment.getChildren().add(background);
        }
        
        segment.getStyleClass().add("segmento-barco");
        if (isDamaged) {
            segment.getStyleClass().add("danado");
        } else {
            segment.getStyleClass().add("intacto");
        }
        
        return segment;
    }
    
    /**
     * Crea un icono para el barco según su tipo
     */
    private static Label createShipIcon(ShipType type) {
        Label icon = new Label();
        icon.getStyleClass().add("barco-icono");
        
        switch (type) {
            case CARRIER:
                icon.setText("🚢"); // Portaaviones
                break;
            case BATTLESHIP:
                icon.setText("⚓"); // Acorazado
                break;
            case CRUISER:
                icon.setText("⛴"); // Crucero
                break;
            case DESTROYER:
                icon.setText("🚤"); // Destructor
                break;
            case SUBMARINE:
                icon.setText("🔻"); // Submarino
                break;
            default:
                icon.setText("🚢");
        }
        
        return icon;
    }
    
    /**
     * Crea un overlay de barco para el tablero
     */
    public static StackPane createShipOverlay(Ship ship) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("barco-container");
        
        HBox shipGraphic = createShipGraphic(ship, true);
        overlay.getChildren().add(shipGraphic);
        
        return overlay;
    }
}