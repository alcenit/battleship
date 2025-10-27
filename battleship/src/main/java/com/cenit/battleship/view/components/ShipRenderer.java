package com.cenit.battleship.view.components;

import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class ShipRenderer {

    // UNA SOLA IMAGEN POR TIPO DE BARCO
    private static final Image CARRIER_IMAGE = loadImage("/com/cenit/battleship/images/carrierTop.png");
    private static final Image BATTLESHIP_IMAGE = loadImage("/com/cenit/battleship/images/battleshipTop.png");
    private static final Image CRUISER_IMAGE = loadImage("/com/cenit/battleship/images/cruiserTop.jpg");
    private static final Image DESTROYER_IMAGE = loadImage("/com/cenit/battleship/images/destroyerTop.jpg");
    private static final Image SUBMARINE_IMAGE = loadImage("/com/cenit/battleship/images/submarineTop.jpg");

    private static Image loadImage(String path) {
        try {
            return new Image(ShipRenderer.class.getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("❌ No se pudo cargar imagen: " + path);
            return null;
        }
    }

    /**
     * Renderiza un barco con UNA SOLA IMAGEN + rotación si es necesario
     */
    /**
 * Método RENDER SHIP para PlacementViewController - VERSIÓN CORREGIDA
 * Maneja correctamente la rotación y dimensiones de barcos verticales
 */
  /**
public static ImageView renderShip(Ship ship, int width, int height) {
    System.out.println("renderShip/ShipRenderer\n");
    try {
        Image image = getShipImage(ship.getType());
        
        if (image == null) {
            return createFallbackShip(ship, width, height);
        }

        ImageView imageView = new ImageView(image);
        
        // ✅ MEJORA: Determinar dimensiones basado en dirección
        if (ship.getDirection() == Direction.VERTICAL) {
            // Para vertical: intercambiar lógicamente dimensiones
            imageView.setRotate(90);
            imageView.setFitWidth(height);  // El alto recibido se usa como ancho
            imageView.setFitHeight(width);  // El ancho recibido se usa como alto
        } else {
            // Para horizontal: dimensiones normales
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        
        // ✅ CORRECCIÓN: Mantener proporciones
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        System.out.println("✅ Barco renderizado MEJORADO: " + ship.getType() + 
                         " | Dirección: " + ship.getDirection() +
                         " | Dimensiones solicitadas: " + width + "x" + height +
                         " | Dimensiones aplicadas: " + imageView.getFitWidth() + "x" + imageView.getFitHeight() +
                         " | Rotación: " + imageView.getRotate() + "°");

        return imageView;

    } catch (Exception e) {
        System.err.println("❌ Error renderizando barco: " + e.getMessage());
        return createFallbackShip(ship, width, height);
    }
}
*/
//versioncorregida
public static ImageView renderShip(Ship ship, int width, int height) {
    try {
        Image image = getShipImage(ship.getType());
        if (image == null) return createFallbackShip(ship, width, height);

        ImageView imageView = new ImageView(image);
        
        if (ship.getDirection() == Direction.VERTICAL) {
            // ✅ PARA VERTICAL: Intercambiar dimensiones + rotar
            imageView.setRotate(90);
            imageView.setFitWidth(height);   // Alto como ancho
            imageView.setFitHeight(width);   // Ancho como alto
            
            // ✅ CORRECCIÓN: Centrado manual después de rotación
            imageView.setTranslateX((width - height) / 2.0);
            imageView.setTranslateY((height - width) / 2.0);
                  
            } else {
            // ✅ PARA HORIZONTAL: Dimensiones normales
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        
        // ✅ CRÍTICO: NO mantener proporciones para control exacto
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        

        System.out.println("✅ Barco CORREGIDO: " + ship.getType() + 
                         " | Dimensiones finales: " + imageView.getFitWidth() + "x" + imageView.getFitHeight());

        return imageView;

    } catch (Exception e) {
        return createFallbackShip(ship, width, height);
    }
}


    

    /**
     * Obtiene la imagen para el tipo de barco (UNA por tipo)
     */
    private static Image getShipImage(ShipType type) {
        switch (type) {
            case CARRIER:
                return CARRIER_IMAGE;
            case BATTLESHIP:
                return BATTLESHIP_IMAGE;
            case CRUISER:
                return CRUISER_IMAGE;
            case DESTROYER:
                return DESTROYER_IMAGE;
            case SUBMARINE:
                return SUBMARINE_IMAGE;
            default:
                return null;
        }
    }

    /**
     * Fallback visual cuando no hay imágenes
     */
    private static ImageView createFallbackShip(Ship ship, int width, int height) {
        Rectangle rect = new Rectangle(width, height);
        
        // Asignar colores distintos por tipo para debug
        Color fillColor = getShipColor(ship.getType());
        Color strokeColor = fillColor.darker();
        
        rect.setFill(fillColor);
        rect.setStroke(strokeColor);
        rect.setStrokeWidth(2);
        
        // Convertir a ImageView
        ImageView fallback = new ImageView(rect.snapshot(null, null));
        fallback.setFitWidth(width);
        fallback.setFitHeight(height);
        
        System.out.println("?? Fallback creado para " + ship.getType() + 
                         " | Color: " + fillColor + " | Tamaño: " + width + "x" + height);
        
        return fallback;
    }
    
    /**
 * Versión CORREGIDA del renderizado de barcos
 */
public static ImageView renderShipCorrected(Ship ship, double width, double height, boolean isVertical) {
    try {
        Image image = getShipImage(ship.getType());
        
        if (image == null) {
            return createFallbackShip(ship, (int)width, (int)height);
        }

        ImageView imageView = new ImageView(image);
        
        // ✅ CORRECCIÓN: Configurar dimensiones según orientación
        if (isVertical) {
            // Para VERTICAL: la imagen se rota 90° pero mantiene dimensiones originales
            imageView.setRotate(90);
            imageView.setFitWidth(width);   // Mantener el ancho del overlay
            imageView.setFitHeight(height); // Mantener el alto del overlay
            
            // ✅ CORRECCIÓN CRÍTICA: Ajustar traslación para centrar después de rotación
            imageView.setTranslateX((height - width) / 2);
            imageView.setTranslateY((width - height) / 2);
        } else {
            // Para HORIZONTAL: dimensiones normales
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        System.out.println("✅ Barco CORREGIDO: " + ship.getType() + 
                         " | Vertical: " + isVertical +
                         " | Dimensiones: " + width + "x" + height +
                         " | Rotación: " + imageView.getRotate() + "°");

        return imageView;

    } catch (Exception e) {
        System.err.println("❌ Error renderizando barco: " + e.getMessage());
        return createFallbackShip(ship, (int)width, (int)height);
    }
}

    /**
     * Colores distintos para cada tipo de barco (solo para fallback)
     */
    private static Color getShipColor(ShipType type) {
        switch (type) {
            case CARRIER: return Color.RED;
            case BATTLESHIP: return Color.BLUE;
            case CRUISER: return Color.GREEN;
            case DESTROYER: return Color.ORANGE;
            case SUBMARINE: return Color.PURPLE;
            default: return Color.GRAY;
        }
    }

    /**
     * Método auxiliar para crear overlay (opcional)
     */private StackPane createShipOverlayForBoard(Ship ship, double cellWidth, double cellHeight) {
    StackPane overlay = new StackPane();
    
    int shipSize = ship.getType().getSize();
    boolean isVertical = (ship.getDirection() == Direction.VERTICAL);
    
    double width = isVertical ? cellWidth : cellWidth * shipSize;
    double height = isVertical ? cellHeight * shipSize : cellHeight;
    
    overlay.setPrefSize(width, height);
    
    // Usar método existente sin parámetros extra
    ImageView shipImage = ShipRenderer.renderShip(ship, (int)width, (int)height);
    overlay.getChildren().add(shipImage);
    
    return overlay;
}
    
    public static ImageView createShipOverlay(Ship ship, double cellWidth, double cellHeight) {
        int shipSize = ship.getType().getSize();
        boolean isVertical = ship.getDirection() == Direction.VERTICAL;
        
        double width = isVertical ? cellWidth : cellWidth * shipSize;
        double height = isVertical ? cellHeight * shipSize : cellHeight;
        
        return renderShip(ship, (int) width, (int) height);
    }
    
    /**
 * Método específico para renderizar barcos en el tablero de juego
 * con manejo mejorado de dimensiones y orientación
 */
public static ImageView renderShipForBoard(Ship ship, double cellWidth, double cellHeight) {
    int shipSize = ship.getType().getSize();
    boolean isVertical = ship.getDirection() == Direction.VERTICAL;
    
    // Calcular dimensiones reales basadas en celdas
    double width = isVertical ? cellWidth : cellWidth * shipSize;
    double height = isVertical ? cellHeight * shipSize : cellHeight;
    
    ImageView shipImage = renderShip(ship, (int) width, (int) height);
    
    // Asegurar que se muestre correctamente
    shipImage.setFitWidth(width);
    shipImage.setFitHeight(height);
    shipImage.setPreserveRatio(false);
    
    System.out.println("?? Barco en tablero: " + ship.getType() + 
                     " | Dirección: " + ship.getDirection() +
                     " | Celdas: " + shipSize +
                     " | Tamaño render: " + width + "x" + height);
    
    return shipImage;
}

/**
 * Método para debug detallado de transformaciones
 */
public static void debugShipTransformations(Ship ship, double requestedWidth, double requestedHeight) {
    System.out.println("=== DEBUG TRANSFORMACIONES ===");
    System.out.println("Barco: " + ship.getType() + " - " + ship.getDirection());
    System.out.println("Tamaño solicitado: " + requestedWidth + "x" + requestedHeight);
    
    boolean isVertical = ship.getDirection() == Direction.VERTICAL;
    double finalWidth = isVertical ? requestedHeight : requestedWidth;
    double finalHeight = isVertical ? requestedWidth : requestedHeight;
    
    System.out.println("Tamaño después de rotación: " + finalWidth + "x" + finalHeight);
    System.out.println("Rotación aplicada: " + (isVertical ? "90°" : "0°"));
    System.out.println("=============================");
    
}

}