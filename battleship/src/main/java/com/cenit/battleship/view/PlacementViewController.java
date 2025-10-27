package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.view.components.ShipRenderer;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.cenit.battleship.model.GameConfiguration;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import javafx.scene.image.ImageView;

import javafx.stage.Stage;

public class PlacementViewController implements Initializable {

    private static String gameMode;
    private static GameController gameController;
    private static String playerName;
    private String currentDifficulty = "NORMAL";

    private List<Ship> shipToPlacement;
    private List<Ship> placedShips = new ArrayList<>();
    private Ship selectShip;
    private Direction actualDirection = Direction.HORIZONTAL;
    private Object placementTimer;

    @FXML
    private GridPane boardPlayer;
    @FXML
    private VBox panelShips;
    @FXML
    private Button btnRotate;
    @FXML
    private Button btnAleatory;
    @FXML
    private Button btnBeging;
    @FXML
    private Label lblInstructions;
    @FXML
    private HBox mainContainer;
    @FXML
    private ComboBox<String> comboDifficulty;

    private Button[][] boardButtons;
    private final GameConfiguration config = GameConfiguration.getInstance();
    private Pane overlayLayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardButtons = new Button[config.getBoardSize()][config.getBoardSize()];

        initializeShips();
        initializeBoard();

        // Configurar overlay con mejor sincronización
        setupOverlay();

        // Usar Platform.runLater para asegurar que el layout esté listo
        Platform.runLater(() -> {
            addLayoutListeners();
            updateOverlayPosition();

            // Forzar una actualización después de que la escena esté completamente renderizada
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    Platform.runLater(() -> {
                        updateOverlayPosition();
                        updateShipGraphics();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        ConfigureEvents();
        updateInterface();
    }

    private void initializeShips() {
        shipToPlacement = new ArrayList<>();

        switch (gameMode != null ? gameMode : "Clásico") {
            case "Flota Especial":
                shipToPlacement.add(new Ship(ShipType.CARRIER));
                shipToPlacement.add(new Ship(ShipType.BATTLESHIP));
                shipToPlacement.add(new Ship(ShipType.CRUISER));
                shipToPlacement.add(new Ship(ShipType.DESTROYER));
                shipToPlacement.add(new Ship(ShipType.SUBMARINE));
                break;
            case "Táctico":
                shipToPlacement.add(new Ship(ShipType.CARRIER));
                shipToPlacement.add(new Ship(ShipType.BATTLESHIP));
                shipToPlacement.add(new Ship(ShipType.CRUISER));
                shipToPlacement.add(new Ship(ShipType.DESTROYER));
                shipToPlacement.add(new Ship(ShipType.SUBMARINE));
                break;
            default: // Clásico
                shipToPlacement.add(new Ship(ShipType.CARRIER));
                shipToPlacement.add(new Ship(ShipType.BATTLESHIP));
                shipToPlacement.add(new Ship(ShipType.CRUISER));
                shipToPlacement.add(new Ship(ShipType.DESTROYER));
                shipToPlacement.add(new Ship(ShipType.SUBMARINE));
        }

        selectShip = shipToPlacement.get(0);
    }

    private void initializeBoard() {
        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = new Button();
                button.setPrefSize(cellSize, cellSize);
                button.getStyleClass().add("casilla-vacia");

                final int x = i;
                final int y = j;

                button.setOnMouseClicked(e -> handleBoardClick(x, y));
                button.setOnMouseEntered(e -> highlightPosition(x, y));
                button.setOnMouseExited(e -> clearHighlight());

                boardPlayer.add(button, j, i);
                boardButtons[i][j] = button;
            }
        }
    }

    private void setupOverlay() {
        overlayLayer = new Pane();
        overlayLayer.setMouseTransparent(true);
        overlayLayer.setManaged(false);
        overlayLayer.setPickOnBounds(false);

        // Estilo de debug (puedes quitarlo en producción)
        overlayLayer.setStyle("-fx-background-color: rgba(255,0,0,0.1); -fx-border-color: red; -fx-border-width: 1;");

        // Encontrar el contenedor padre correcto
        Parent parent = boardPlayer.getParent();
        if (parent instanceof Pane) {
            Pane parentPane = (Pane) parent;
            if (!parentPane.getChildren().contains(overlayLayer)) {
                parentPane.getChildren().add(overlayLayer);
            }
        } else {
            // Fallback: usar el contenedor principal
            if (!mainContainer.getChildren().contains(overlayLayer)) {
                mainContainer.getChildren().add(overlayLayer);
            }
        }
    }

    private void addLayoutListeners() {
        // Listener para cambios en el layout del board
        boardPlayer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            updateOverlayPosition();
        });

        // Listener para cambios en la escena
        boardPlayer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldVal, newVal) -> updateOverlayPosition());
                newScene.heightProperty().addListener((o, oldVal, newVal) -> updateOverlayPosition());
            }
        });

        // Listener para cambios en el parent
        boardPlayer.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent instanceof Pane) {
                ((Pane) newParent).layoutBoundsProperty().addListener((o, oldVal, newVal) -> {
                    updateOverlayPosition();
                });
            }
        });
    }

    private void updateOverlayPosition() {
        if (overlayLayer == null || boardPlayer == null) {
            return;
        }

        try {
            // Obtener bounds del boardPlayer en las coordenadas del parent
            Bounds boardBounds = boardPlayer.getBoundsInParent();

            if (boardBounds.getWidth() > 0 && boardBounds.getHeight() > 0) {
                overlayLayer.setLayoutX(boardBounds.getMinX());
                overlayLayer.setLayoutY(boardBounds.getMinY());
                overlayLayer.setPrefSize(boardBounds.getWidth(), boardBounds.getHeight());

                System.out.println("? Overlay actualizado - Pos: (" + boardBounds.getMinX() + ", "
                        + boardBounds.getMinY() + ") Size: " + boardBounds.getWidth() + "x" + boardBounds.getHeight());
            }
        } catch (Exception e) {
            System.err.println("? Error actualizando overlay: " + e.getMessage());
        }
    }

    //EVENTOS DE BOTONES 
    private void ConfigureEvents() {
        btnRotate.setOnAction(e -> rotateShip());
        btnAleatory.setOnAction(e -> placeRandomShips());
        btnBeging.setOnAction(e -> StartGame());
    }

    // onAction button
    private void rotateShip() {
        actualDirection = (actualDirection == Direction.HORIZONTAL) ? Direction.VERTICAL : Direction.HORIZONTAL;
        // Actualizar el texto del botón
        btnRotate.setText(actualDirection == Direction.VERTICAL ? "Horizontal" : "Vertical");
        System.out.println("? Dirección cambiada a: " + actualDirection);
    }

    // onAction button
    private void placeRandomShips() {
        // Implementar colocación aleatoria de barcos
        System.out.println("? Colocando barcos aleatoriamente...");
    }

    private void StartGame() {
        try {
            // Validar que todos los barcos estén colocados
            if (placedShips.size() < shipToPlacement.size() + placedShips.size()) {
                System.out.println("? Error: No todos los barcos han sido colocados");
                return;
            }

            System.out.println("? Iniciando juego con " + placedShips.size() + " barcos colocados");
            App.changeView("com/cenit/battleship/view/GameView");
        } catch (IOException ex) {
            System.err.println("? Error al cambiar a GameView: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean canPlaceShip(int startX, int startY, Direction direction, int size) {
        Coordinate start = new Coordinate(startX, startY);

        // Calcular coordenadas potenciales
        List<Coordinate> coordinates = calculateShipCoordinates(startX, startY, direction, size);

        // Verificar límites del tablero
        for (Coordinate coord : coordinates) {
            if (coord.getX() < 0 || coord.getX() >= config.getBoardSize()
                    || coord.getY() < 0 || coord.getY() >= config.getBoardSize()) {
                System.out.println("? Error: Coordenada fuera del tablero: " + coord.aNotacion());
                return false;
            }
        }

        // Verificar superposición y proximidad con barcos existentes
        for (Ship placedShip : placedShips) {
            // Verificar superposición directa
            for (Coordinate placedCoord : placedShip.getSegments()) {
                for (Coordinate newCoord : coordinates) {
                    if (placedCoord.equals(newCoord)) {
                        System.out.println("? Superposición en: " + newCoord.aNotacion());
                        return false;
                    }
                }
            }

            // Verificar adyacencia (barcos no pueden tocarse)
            for (Coordinate placedCoord : placedShip.getSegments()) {
                for (Coordinate newCoord : coordinates) {
                    int diffX = Math.abs(placedCoord.getX() - newCoord.getX());
                    int diffY = Math.abs(placedCoord.getY() - newCoord.getY());
                    if (diffX <= 1 && diffY <= 1) {
                        System.out.println("? Barco muy cercano en: " + newCoord.aNotacion());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private List<Coordinate> calculateShipCoordinates(int startX, int startY, Direction direction, int size) {
        List<Coordinate> coordinates = new ArrayList<>();
        int dx = direction == Direction.HORIZONTAL ? 0 : 1;
        int dy = direction == Direction.HORIZONTAL ? 1 : 0;

        // Verificar primero que todo el barco quepa
        int endX = startX + (size - 1) * dx;
        int endY = startY + (size - 1) * dy;

        if (endX >= config.getBoardSize() || endY >= config.getBoardSize()) {
            System.out.println("❌ Barco se sale del tablero");
            return coordinates; // Lista vacía
        }

        // Si cabe, calcular todas las coordenadas
        for (int i = 0; i < size; i++) {
            int x = startX + i * dx;
            int y = startY + i * dy;
            coordinates.add(new Coordinate(x, y));
            System.out.println("  ? Segmento " + i + ": (" + x + "," + y + ") -> "
                    + new Coordinate(x, y).aNotacion());
        }

        return coordinates;
    }

    private boolean placeShip(int startX, int startY, Direction direction, Ship ship) {
        try {
            // Usar el nuevo método place de la clase Ship
            boolean success = ship.place(startX, startY, direction);

            if (success) {
                List<Coordinate> segments = ship.getSegments();
                Coordinate start = segments.get(0);
                Coordinate end = segments.get(segments.size() - 1);

                System.out.println("? " + ship.getType().getName() + " colocado exitosamente en "
                        + start.aNotacion() + " (" + direction + ")");
                System.out.println("? Barco " + ship.getType().getName() + " posicionado en "
                        + start.aNotacion() + " a " + end.aNotacion() + " (" + direction + ")");
                return true;
            }
        } catch (Exception e) {
            System.err.println("? Error al colocar barco: " + e.getMessage());
        }
        return false;
    }

    private void handleBoardClick(int x, int y) {
        if (selectShip == null) {
            return;
        }
        System.out.println("|||handleBoardClick\\n");
        System.out.println("? CALCULANDO COORDENADAS - Barco: " + selectShip.getType()
                + ", Start: " + getCoordinateName(x, y) + ", Dir: " + actualDirection + ", Size: " + selectShip.getType().getSize());

        try {
            // Verificar si se puede colocar el barco
            if (canPlaceShip(x, y, actualDirection, selectShip.getType().getSize())) {
                boolean placed = placeShip(x, y, actualDirection, selectShip);
                if (placed) {
                    placedShips.add(selectShip);
                    shipToPlacement.remove(selectShip);

                    if (!shipToPlacement.isEmpty()) {
                        selectShip = shipToPlacement.get(0);
                    } else {
                        selectShip = null;
                        btnBeging.setDisable(false);
                    }

                    updateInterface();
                    updateShipGraphics();
                }
            } else {
                System.out.println("? No se puede colocar el barco en " + getCoordinateName(x, y));
            }
        } catch (Exception e) {
            System.err.println("? Error al colocar barco: " + e.getMessage());
        }
    }

    private String getCoordinateName(int x, int y) {
        char column = (char) ('A' + x);
        int row = y + 1;
        return "" + column + row;
    }

    private void updateShipGraphics() {
        if (overlayLayer == null) {
            return;
        }
        System.out.println("|||| updateShipsGraphics/placementViewController\n");

        overlayLayer.getChildren().clear();

        int boardSize = config.getBoardSize();

        // Obtener las dimensiones reales del board
        Bounds boardBounds = boardPlayer.getBoundsInParent();
        double actualCellWidth = boardBounds.getWidth() / boardSize;
        double actualCellHeight = boardBounds.getHeight() / boardSize;

        System.out.println("? ACTUALIZANDO GRÁFICOS - Barcos: " + placedShips.size());

        for (Ship ship : placedShips) {
            if (!ship.getSegments().isEmpty()) {
                Coordinate start = ship.getSegments().get(0);

                // Calcular posición y tamaño basado en la orientación real
                double x = start.getY() * actualCellWidth;
                System.out.println("x: "+x);
                double y = start.getX() * actualCellHeight;
                System.out.println("y: "+y);

                // Determinar dimensiones del barco
                double width, height;
                if (ship.getDirection() == Direction.HORIZONTAL) {
                    width = ship.getType().getSize() * actualCellWidth;
                    height = actualCellHeight;
                } else {
                    width = actualCellWidth;
                    height = ship.getType().getSize() * actualCellHeight;
                }

                System.out.println("?? Dibujando: " + ship.getType() + " en " + getCoordinateName(start.getX(), start.getY()));

                // USAR  MÉTODO renderShip
                
                ImageView shipImage = ShipRenderer.renderShip(ship, (int) width, (int) height);
                
                if (shipImage != null) {
                    shipImage.setLayoutX(x);
                    shipImage.setLayoutY(y);
                    overlayLayer.getChildren().add(shipImage);

                    System.out.println("\n? POSICION FINAL - " + ship.getType() + " en "
                            + getCoordinateName(start.getX(), start.getY()) + " | Grid[fila=" + start.getX()
                            + ",col=" + start.getY() + "] | OverlayPos: (" + x + ", " + y + ") | Tamanio: "
                            + width + "x" + height);
                }
            }
        }
        System.out.println("|||||debugFinalPlacement");
        System.out.println("?Gráficos actualizados - Total overlays: " + overlayLayer.getChildren().size());
        debugFinalPlacement();
        
        System.out.println("|||| ? debugLayoutInfo");
        debugLayoutInfo();
    }

    /**
     * Valida toda la flota para asegurar colocación correcta
     *
     * @return
     */
    public boolean validateFleetPlacement() {
        System.out.println("? VALIDANDO FLOTA - Total barcos: " + placedShips.size());

        for (int i = 0; i < placedShips.size(); i++) {
            Ship ship1 = placedShips.get(i);

            // Verificar que el barco tenga todas sus coordenadas
            if (ship1.getSegments().size() != ship1.getType().getSize()) {
                System.out.println("❌ Barco " + ship1.getType().getName() + " no tiene todas sus coordenadas");
                return false;
            }

            // Verificar contra otros barcos
            for (int j = i + 1; j < placedShips.size(); j++) {
                Ship ship2 = placedShips.get(j);

                if (ship1.overlapsWith(ship2)) {
                    System.out.println("❌ Barcos " + ship1.getType().getName() + " y "
                            + ship2.getType().getName() + " se superponen");
                    return false;
                }

                if (ship1.isTooCloseTo(ship2)) {
                    System.out.println("❌ Barcos " + ship1.getType().getName() + " y "
                            + ship2.getType().getName() + " están demasiado cerca");
                    return false;
                }
            }
        }

        System.out.println("✅ Flota validada correctamente");
        return true;
    }
    //METODOS PAR DEBUGGEAR

    private void debugFinalPlacement() {
        System.out.println("\n? VERIFICACIÓN FINAL DE POSICIONAMIENTO:");
        System.out.println("? OverlayLayer:");
        System.out.println("   Local: " + overlayLayer.getLayoutBounds());
        System.out.println("   Parent: " + (overlayLayer.getParent() != null ? overlayLayer.getParent().getLayoutBounds() : "null"));
        System.out.println("   Scene: " + (overlayLayer.getScene() != null ? overlayLayer.getLocalToSceneTransform().transform(overlayLayer.getLayoutBounds()) : "null"));
        System.out.println("   Layout: (" + overlayLayer.getLayoutX() + ", " + overlayLayer.getLayoutY() + ")");
        System.out.println("   PrefSize: " + overlayLayer.getPrefWidth() + "x" + overlayLayer.getPrefHeight());
        System.out.println("   Children: " + overlayLayer.getChildren().size());

        for (Node child : overlayLayer.getChildren()) {
            if (child instanceof ImageView) {
                System.out.println("   ? Barco"+shipToPlacement.toString()+" - Layout: (" + child.getLayoutX() + ", " + child.getLayoutY() + ")");
                System.out.println("        Local: " + child.getLayoutBounds());
                System.out.println("        Parent: " + child.getParent().getLayoutBounds());
                System.out.println("        Scene: " + child.getLocalToSceneTransform().transform(child.getLayoutBounds()));
            }
        }

        System.out.println("? BoardPlayer:");
        System.out.println("   Local: " + boardPlayer.getLayoutBounds());
        System.out.println("   Scene: " + boardPlayer.getLocalToSceneTransform().transform(boardPlayer.getLayoutBounds()));
    }

    private void debugLayoutInfo() {
        System.out.println("\n? DEBUG LAYOUT COMPLETO:");
        System.out.println("=== BOARD PLAYER ===");
        System.out.println("  Local: " + boardPlayer.getLayoutBounds());
        System.out.println("  Parent: " + (boardPlayer.getParent() != null ? boardPlayer.getParent().getLayoutBounds() : "null"));
        System.out.println("  Scene: " + (boardPlayer.getScene() != null ? boardPlayer.getLocalToSceneTransform().transform(boardPlayer.getLayoutBounds()) : "null"));
        System.out.println("  Layout: (" + boardPlayer.getLayoutX() + ", " + boardPlayer.getLayoutY() + ")");
        System.out.println("  PrefSize: " + boardPlayer.getPrefWidth() + "x" + boardPlayer.getPrefHeight());

        if (overlayLayer != null) {
            System.out.println("=== OVERLAY LAYER ===");
            System.out.println("  Local: " + overlayLayer.getLayoutBounds());
            System.out.println("  Parent: " + (overlayLayer.getParent() != null ? overlayLayer.getParent().getLayoutBounds() : "null"));
            System.out.println("  Scene: " + (overlayLayer.getScene() != null ? overlayLayer.getLocalToSceneTransform().transform(overlayLayer.getLayoutBounds()) : "null"));
            System.out.println("  Layout: (" + overlayLayer.getLayoutX() + ", " + overlayLayer.getLayoutY() + ")");
            System.out.println("  PrefSize: " + overlayLayer.getPrefWidth() + "x" + overlayLayer.getPrefHeight());
        }

        if (boardPlayer.getParent() != null) {
            System.out.println("=== PARENT CONTAINER ===");
            System.out.println("  Children: " + ((Pane) boardPlayer.getParent()).getChildren().size());
            System.out.println("  Bounds: " + boardPlayer.getParent().getLayoutBounds());
        }

        System.out.println("=== CONFIG ===");
        System.out.println("  BoardSize: " + config.getBoardSize());
        System.out.println("  CellSize: " + config.getCellSize());
        System.out.println("  Calculated Size: " + (config.getBoardSize() * config.getCellSize()));
    }

    private void highlightPosition(int x, int y) {
        // Implementar highlight de posición (opcional)
    }

    private void clearHighlight() {
        // Implementar limpieza de highlight (opcional)
    }

    private void updateInterface() {
        // Actualizar la interfaz de usuario
        System.out.println("||||updateInterface?\\n BARCOS UNICOS - Total: " + shipToPlacement.size()
                + " | placedShips: " + placedShips.size() + " | shipToPlacement colocados: "
                + (shipToPlacement.size() - placedShips.size()));

        if (shipToPlacement.isEmpty()) {
            lblInstructions.setText("Todos los barcos colocados. Presiona 'Comenzar' para iniciar el juego.");
        } else {
            lblInstructions.setText("Coloca tu " + selectShip.getType() + " ("
                    + selectShip.getType().getSize() + " casillas) - " + actualDirection);
        }
    }

    private boolean prepareNextView(Stage currentStage, GameController gameController, String gameMode, String playerName) {
        try {
            // Verificar que el controlador esté listo
            if (!gameController.areFleetsReady()) {
                showAlert("Error de Configuración", "Las flotas no están listas. No se puede iniciar el juego.");
                return false;
            }

            // Cargar el FXML y obtener el controlador
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cenit/battleship/view/PlacementView.fxml"));
            Parent root = loader.load();

            PlacementViewController placementController = loader.getController();
            if (placementController == null) {
                showAlert("Error", "No se pudo cargar el controlador de colocación.");
                return false;
            }

            // Configurar usando métodos de instancia
            placementController.setGameController(gameController);
            placementController.setGameMode(gameMode);
            placementController.setPlayerName(playerName);
            placementController.setDifficulty(comboDifficulty.getValue()); // Pasar dificultad

            // Crear una nueva escena con el contenido cargado
            Scene placementScene = new Scene(root);

            // Asignar la nueva escena a la ventana actual
            currentStage.setScene(placementScene);
            currentStage.setTitle("Coloca tus barcos - Dificultad: " + comboDifficulty.getValue());

            // Opcional: Ajustar el tamaño de la ventana al nuevo contenido
            currentStage.sizeToScene();

            System.out.println("✅ Vista de colocación preparada y mostrada exitosamente");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error al preparar vista: " + e.getMessage());
            showAlert("Error", "No se pudo preparar la vista del juego: " + e.getMessage());
            return false;
        }
    }

    // Getters y Setters estáticos para la configuración
    public static void setGameMode(String mode) {
        gameMode = mode;
    }

    public static void setGameController(GameController controller) {
        gameController = controller;
    }

    public static void setPlayerName(String name) {
        playerName = name;
    }

    // MODIFICAR el setter para solo guardar la dificultad
    public void setDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
        // Solo log, sin UI
        System.out.println("? Dificultad establecida: " + difficulty);

        // Opcional: actualizar las instrucciones si quieres mostrar la dificultad
        if (lblInstructions != null) {
            lblInstructions.setText("Coloca tus barcos - Dificultad: " + difficulty);
        }
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
