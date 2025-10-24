package com.cenit.battleship.view.components;

import com.cenit.battleship.model.GameConfiguration;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class ShipRenderer {

    // imágenes de iconos barcos
    private static final Image CARRIER_IMAGE = new Image(ShipRenderer.class.getResourceAsStream("/com/cenit/battleship/images/carrierTop.jpg"));
    private static final Image BATTLESHIP_IMAGE = new Image(ShipRenderer.class.getResourceAsStream("/com/cenit/battleship/images/battleshipTop1.png"));
    private static final Image CRUISER_IMAGE = new Image(ShipRenderer.class.getResourceAsStream("/com/cenit/battleship/images/cruiserTop.jpg"));
    private static final Image DESTROYER_IMAGE = new Image(ShipRenderer.class.getResourceAsStream("/com/cenit/battleship/images/destroyerTop.jpg"));
    private static final Image SUBMARINE_IMAGE = new Image(ShipRenderer.class.getResourceAsStream("/com/cenit/battleship/images/submarineTop.jpg"));

    // instancia de la configuración
    private static final GameConfiguration config = GameConfiguration.getInstance();

    // En ShipRenderer - método createShipOverlay mejorado
    public static StackPane createShipOverlay(Ship ship) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("barco-overlay");

        int cellSize = 40; // Tamaño consistente
        int shipSize = ship.getType().getSize();

        boolean isVertical = ship.getDirection() == Direction.VERTICAL;
        int width = isVertical ? cellSize : cellSize * shipSize;
        int height = isVertical ? cellSize * shipSize : cellSize;

        overlay.setPrefSize(width, height);

        // Crear el icono del barco
        Label icon = createShipIcon(ship.getType(), width, height, isVertical);

        // Asegurar que el icono ocupe todo el espacio
        icon.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        icon.setPrefSize(width, height);

        overlay.getChildren().add(icon);

        // DEBUG: Solo borde para ver posición, no fondo que oculte la imagen
        overlay.setStyle("-fx-border-color: rgba(255,0,0,0.5); -fx-border-width: 1;");

        return overlay;
    }

// Método createShipIcon mejorado
    private static Label createShipIcon(ShipType type, int width, int height, boolean isVertical) {
        Label icon = new Label();
        icon.setPrefSize(width, height);
        icon.setMaxSize(width, height);
        icon.getStyleClass().add("barco-icono");
        icon.setMouseTransparent(true);

        ImageView imageView = null;

        try {
            switch (type) {
                case CARRIER:
                    imageView = new ImageView(CARRIER_IMAGE);
                    break;
                case BATTLESHIP:
                    imageView = new ImageView(BATTLESHIP_IMAGE);
                    break;
                case CRUISER:
                    imageView = new ImageView(CRUISER_IMAGE);
                    break;
                case DESTROYER:
                    imageView = new ImageView(DESTROYER_IMAGE);
                    break;
                case SUBMARINE:
                    imageView = new ImageView(SUBMARINE_IMAGE);
                    break;
                default:
                    // Fallback: usar emoji si no hay imagen
                    icon.setText("🚢");
                    return icon;
            }

            if (imageView != null) {
                // Para barcos verticales, rotamos la imagen 90 grados
                if (isVertical) {
                    imageView.setRotate(90);
                }

                // Configurar tamaño de la imagen
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setMouseTransparent(true);

                icon.setGraphic(imageView);
                icon.setText("");

                System.out.println("🖼️ Imagen cargada para " + type.getName()
                        + " - Tamaño: " + width + "x" + height);
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando imagen para " + type + ": " + e.getMessage());
            // Fallback a texto
            icon.setText(type.getName().substring(0, 3)); // Primeras 3 letras
            icon.setStyle("-fx-background-color: #4a6572; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        return icon;
    }

    /**
     * Crea una representación gráfica de un barco para el panel de barcos
     */
    public static HBox createShipDisplay(Ship ship) {
        HBox container = new HBox(5);
        container.getStyleClass().add("barco-item");

        Label nameLabel = new Label(ship.getType().getName());
        nameLabel.getStyleClass().add("barco-nombre");

        StackPane shipGraphic = createShipGraphic(ship, false);
        shipGraphic.getStyleClass().add("barco-representacion");

        container.getChildren().addAll(nameLabel, shipGraphic);
        return container;
    }

    /**
     * Crea la representación gráfica del barco (para panel y tablero)
     */
    public static StackPane createShipGraphic(Ship ship, boolean forBoard) {
        int size = ship.getType().getSize();
        int segmentSize = forBoard ? 40 : 25;

        // Para barcos verticales, intercambiamos ancho y alto
        boolean isVertical = forBoard && ship.getDirection() == Direction.VERTICAL;
        int totalWidth = isVertical ? segmentSize : segmentSize * size;
        int totalHeight = isVertical ? segmentSize * size : segmentSize;

        // Contenedor principal donde se superponen los segmentos y el icono
        StackPane shipContainer = new StackPane();
        shipContainer.setPrefSize(totalWidth, totalHeight);
        shipContainer.getStyleClass().add("barco-grafico");

        // Crear contenedor de segmentos - declarar como Pane
        Pane segmentsContainer;

        if (isVertical) {
            segmentsContainer = new VBox(0);
        } else {
            segmentsContainer = new HBox(0);
        }
        segmentsContainer.setPrefSize(totalWidth, totalHeight);

        for (int i = 0; i < size; i++) {
            StackPane segment = createShipSegment(ship, i, segmentSize, forBoard);
            segmentsContainer.getChildren().add(segment);
        }

        // Crear icono que cubre todo el barco
        Label icon = createShipIcon(ship.getType(), totalWidth, totalHeight, isVertical);

        // Añadir primero los segmentos y luego el icono (para que quede encima)
        shipContainer.getChildren().addAll(segmentsContainer, icon);

        // Aplicar clases CSS para dirección
        if (forBoard) {
            if (isVertical) {
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

        segment.getChildren().add(background);

        segment.getStyleClass().add("segmento-barco");
        if (isDamaged) {
            segment.getStyleClass().add("danado");
        } else {
            segment.getStyleClass().add("intacto");
        }

        return segment;
    }

    /**
     * Método alternativo para crear solo los segmentos (sin icono) Útil para
     * cuando quieres mostrar el estado de daño
     */
    public static HBox createShipSegmentsOnly(Ship ship, boolean forBoard) {
        HBox segmentsContainer = new HBox(0);
        int size = ship.getType().getSize();
        int segmentSize = forBoard ? 40 : 25;

        for (int i = 0; i < size; i++) {
            StackPane segment = createShipSegment(ship, i, segmentSize, forBoard);
            segmentsContainer.getChildren().add(segment);
        }

        return segmentsContainer;
    }
}
