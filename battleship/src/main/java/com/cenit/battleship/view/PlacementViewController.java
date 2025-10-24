package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.CPUController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Difficulty;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.cenit.battleship.model.GameConfiguration;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlacementViewController implements Initializable {

    private static String gameMode;
    private static GameController gameController;
    private static String playerName;
    private String currentDifficulty = "NORMAL"; // Dificultad por defecto

    // Variables para elementos UI de dificultad (debes agregarlos en el FXML)
    @FXML
    private Label lblDifficulty;
    @FXML
    private Label lblDifficultyInfo;
    @FXML
    private Button btnAutoPlace;

    private List<Ship> shipToPlacement;
    private List<Ship> placedShips = new ArrayList<>();
    private Ship selectShip;
    private Direction actualDirection = Direction.HORIZONTAL;
    private Object placementTimer; // Placeholder para futura implementación

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

    // Usa los valores de la configuración para inicializar tus variables
    private Button[][] boardButtons;

    // Instancia de la configuración
    private final GameConfiguration config = GameConfiguration.getInstance();

    private Pane overlayLayer; // Capa para los overlays de barcos

    // METODOS DE INICIALIZACION
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardButtons = new Button[config.getBoardSize()][config.getBoardSize()];

        initializeDifficultyDisplay();
        initializeShips();
        initializeBoard();

        // Configurar overlay PRIMERO
        setupSimpleOverlay();

        // Luego agregar listeners y debug
        Platform.runLater(() -> {
            addLayoutListeners();
            debugLayoutInfo();

            // Forzar una actualización después de un breve delay
            new Thread(() -> {
                try {
                    Thread.sleep(200); // Dar más tiempo para que el layout se estabilice
                    Platform.runLater(() -> {
                        updateOverlayPosition();
                        debugLayoutInfo(); // Verificar que esté correcto
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        ConfigureEvents();
        updateInterface();
    }

    private void initializeDifficultyDisplay() {
        // Solo si los elementos están inyectados desde FXML
        if (lblDifficulty != null) {
            updateDifficultyDisplay();
        }
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
        // valores de la configuración en lugar de números fijos
        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = new Button();

                // El tamaño de la casilla es dinámico
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

    private void ConfigureEvents() {
        btnRotate.setOnAction(e -> rotateShip());
        btnAleatory.setOnAction(e -> placeRandomShips());
        btnBeging.setOnAction(e -> StartGame());
    }

    private void StartGame() {
        try {
            App.changeView("com/cenit/battleship/view/GameView");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ========== SISTEMA DE OVERLAY SIMPLE ==========
    private void setupSimpleOverlay() {
        try {
            // Crear overlay layer como Pane normal
            overlayLayer = new Pane();
            overlayLayer.setMouseTransparent(true);
            overlayLayer.getStyleClass().add("overlay-layer");
            overlayLayer.setManaged(false);

            // Estilo de debug VISIBLE
            overlayLayer.setStyle("-fx-background-color: rgba(255,0,0,0.2); -fx-border-color: blue; -fx-border-width: 2;");

            // Obtener el contenedor padre
            Parent parent = boardPlayer.getParent();
            if (parent instanceof Pane) {
                Pane parentPane = (Pane) parent;

                // Agregar el overlay AL FINAL
                if (!parentPane.getChildren().contains(overlayLayer)) {
                    parentPane.getChildren().add(overlayLayer);
                }

                // Esperar a que el layout esté listo
                Platform.runLater(() -> {
                    try {
                        // Obtener bounds del boardPlayer en el PARENT
                        Bounds parentBounds = boardPlayer.getBoundsInParent();

                        System.out.println("🎯 CONFIGURANDO OVERLAY - BoardParent: " + parentBounds);

                        if (parentBounds.getWidth() <= 0 || parentBounds.getHeight() <= 0) {
                            // Forzar layout
                            boardPlayer.applyCss();
                            boardPlayer.layout();
                            parentBounds = boardPlayer.getBoundsInParent();
                        }

                        // Configurar overlayLayer con posición y tamaño ABSOLUTOS
                        overlayLayer.setLayoutX(parentBounds.getMinX());
                        overlayLayer.setLayoutY(parentBounds.getMinY());
                        overlayLayer.setPrefSize(parentBounds.getWidth(), parentBounds.getHeight());

                        // CRÍTICO: También establecer min y max size
                        overlayLayer.setMinSize(parentBounds.getWidth(), parentBounds.getHeight());
                        overlayLayer.setMaxSize(parentBounds.getWidth(), parentBounds.getHeight());

                        System.out.println("✅ OVERLAY CONFIGURADO - Pos: (" + parentBounds.getMinX() + ", "
                                + parentBounds.getMinY() + ") Size: " + parentBounds.getWidth() + "x" + parentBounds.getHeight());

                        // Verificar que se aplicaron las dimensiones
                        Bounds overlayBounds = overlayLayer.getBoundsInLocal();
                        System.out.println("📏 Overlay Local después: " + overlayBounds);

                    } catch (Exception e) {
                        System.err.println("❌ Error en setup overlay: " + e.getMessage());
                        setupManualOverlay();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("❌ Error inicializando overlay: " + e.getMessage());
            setupManualOverlay();
        }
    }

// Método de respaldo manual
    private void setupManualOverlay() {
        System.out.println("🔄 Usando configuración manual overlay");

        // Calcular tamaño manual basado en configuración
        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize();
        double totalSize = boardSize * cellSize;

        // Usar la misma posición que boardPlayer
        overlayLayer.setLayoutX(boardPlayer.getLayoutX());
        overlayLayer.setLayoutY(boardPlayer.getLayoutY());
        overlayLayer.setPrefSize(totalSize, totalSize);
        overlayLayer.setMinSize(totalSize, totalSize);
        overlayLayer.setMaxSize(totalSize, totalSize);

        overlayLayer.setStyle("-fx-background-color: rgba(0,255,0,0.2); -fx-border-color: green; -fx-border-width: 2;");

        System.out.println("📐 Overlay Manual - Pos: (" + boardPlayer.getLayoutX() + ", "
                + boardPlayer.getLayoutY() + ") Size: " + totalSize + "x" + totalSize);
    }

    // Método de respaldo si falla la configuración automática
    private void setupFallbackOverlay() {
        System.out.println("🔄 Usando configuración fallback para overlay");

        // Usar valores calculados basados en la configuración
        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize();
        double totalSize = boardSize * cellSize;

        // Posicionar en la misma posición que el boardPlayer
        overlayLayer.setLayoutX(boardPlayer.getLayoutX());
        overlayLayer.setLayoutY(boardPlayer.getLayoutY());
        overlayLayer.setPrefSize(totalSize, totalSize);
        overlayLayer.setStyle("-fx-background-color: rgba(0,255,0,0.1); -fx-border-color: green; -fx-border-width: 2;");

        System.out.println("   Fallback - Pos: (" + boardPlayer.getLayoutX() + ", " + boardPlayer.getLayoutY() + ")");
        System.out.println("   Fallback - Size: " + totalSize + "x" + totalSize);
    }

// Listener para cambios de layout - CRÍTICO
    private void addLayoutListeners() {
        // Escuchar cambios en los bounds del boardPlayer
        boardPlayer.boundsInParentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.getWidth() > 0 && newVal.getHeight() > 0) {
                Platform.runLater(() -> {
                    updateOverlayPosition();
                });
            }
        });

        // Escuchar cambios en la escena
        boardPlayer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Cuando la escena esté disponible, actualizar overlay
                Platform.runLater(() -> {
                    updateOverlayPosition();
                });
            }
        });
    }

    private void updateOverlayPosition() {
        if (overlayLayer == null) {
            return;
        }

        try {
            Bounds parentBounds = boardPlayer.getBoundsInParent();

            if (parentBounds.getWidth() > 0 && parentBounds.getHeight() > 0) {
                overlayLayer.setLayoutX(parentBounds.getMinX());
                overlayLayer.setLayoutY(parentBounds.getMinY());
                overlayLayer.setPrefSize(parentBounds.getWidth(), parentBounds.getHeight());

                System.out.println("🔄 Overlay actualizado - Pos: ("
                        + parentBounds.getMinX() + ", " + parentBounds.getMinY()
                        + ") Size: " + parentBounds.getWidth() + "x" + parentBounds.getHeight());
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando overlay: " + e.getMessage());
        }
    }

    private void drawShipOnBoard(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();

        System.out.println("🎨 DIBUJANDO - Ship: " + ship.getType().getName()
                + ", Dir: " + ship.getDirection()
                + ", Coords: " + coordinates);

        // Primero limpiar overlay anterior de este barco
        clearShipOverlay(ship);

        // Actualizar estilos de las celdas
        updateCellStyles(ship);

        // Agregar overlay del barco
        addShipOverlay(ship);
    }

    private void updateCellStyles(Ship ship) {
        for (Coordinate coord : ship.getCoordinates()) {
            Button button = boardButtons[coord.getX()][coord.getY()];
            button.getStyleClass().remove("casilla-vacia");
            if (ship.isPartDamaged(coord)) {
                button.getStyleClass().add("casilla-barco-danado");
            } else {
                button.getStyleClass().add("casilla-barco");
            }
            button.setGraphic(null); // No usar gráficos en botones
        }
    }

    private void addShipOverlay(Ship ship) {
        if (overlayLayer == null) {
            System.err.println("❌ OverlayLayer es null");
            return;
        }

        try {
            List<Coordinate> coordinates = ship.getCoordinates();
            if (coordinates.isEmpty()) {
                System.err.println("❌ Barco sin coordenadas: " + ship.getType().getName());
                return;
            }

            Coordinate firstCoord = coordinates.get(0);
            double cellSize = config.getCellSize();

            // CORRECCIÓN CRÍTICA: Calcular posición basada en el GridPane real
            // El GridPane organiza: GridPane.add(node, columnIndex, rowIndex)
            // Por lo tanto: x = columna * cellSize, y = fila * cellSize
            double x = firstCoord.getY() * cellSize; // COLUMNA → X
            double y = firstCoord.getX() * cellSize; // FILA → Y

            int shipSize = ship.getType().getSize();
            boolean isVertical = ship.getDirection() == Direction.VERTICAL;

            double width = isVertical ? cellSize : cellSize * shipSize;
            double height = isVertical ? cellSize * shipSize : cellSize;

            // Crear el overlay del barco
            StackPane shipOverlay = createShipOverlayWithProperPositioning(ship, width, height);
            String overlayId = "ship-" + ship.getType().name() + "-" + System.identityHashCode(ship);
            shipOverlay.setId(overlayId);

            // POSICIÓN ABSOLUTA dentro del overlayLayer
            shipOverlay.setLayoutX(x);
            shipOverlay.setLayoutY(y);
            shipOverlay.setPrefSize(width, height);

            // Asegurar que el barco sea visible y esté bien posicionado
            shipOverlay.setVisible(true);
            shipOverlay.setManaged(true);

            System.out.println("🎯 POSICIÓN FINAL - " + ship.getType().getName()
                    + " en " + firstCoord.aNotacion()
                    + " | Grid[fila=" + firstCoord.getX() + ",col=" + firstCoord.getY() + "]"
                    + " | OverlayPos: (" + x + ", " + y + ")"
                    + " | Tamaño: " + width + "x" + height);

            overlayLayer.getChildren().add(shipOverlay);

            // Forzar actualización visual
            Platform.runLater(() -> {
                shipOverlay.applyCss();
                shipOverlay.layout();
            });

        } catch (Exception e) {
            System.err.println("❌ Error crítico en addShipOverlay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private StackPane createShipOverlayWithProperPositioning(Ship ship, double width, double height) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("barco-overlay");

        // Usar el renderizador real de barcos
        Label shipIcon = createShipIcon(ship.getType(), (int) width, (int) height,
                ship.getDirection() == Direction.VERTICAL);

        // Configurar el icono para que ocupe todo el espacio
        shipIcon.setPrefSize(width, height);
        shipIcon.setMaxSize(width, height);

        overlay.getChildren().add(shipIcon);
        overlay.setPrefSize(width, height);

        // Estilo de debug mínimo (solo borde)
        overlay.setStyle("-fx-border-color: rgba(255,0,0,0.5); -fx-border-width: 1;");

        return overlay;
    }

    private Label createShipIcon(ShipType type, int width, int height, boolean isVertical) {
        Label icon = new Label();
        icon.setPrefSize(width, height);
        icon.setMaxSize(width, height);
        icon.getStyleClass().add("barco-icono");
        icon.setMouseTransparent(true);

        ImageView imageView = null;

        try {
            // Cargar imágenes directamente desde resources
            switch (type) {
                case CARRIER:
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/carrierTop.jpg")));
                    break;
                case BATTLESHIP:
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/battleshipTop1.png")));
                    break;
                case CRUISER:
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/cruiserTop.jpg")));
                    break;
                case DESTROYER:
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/destroyerTop.jpg")));
                    break;
                case SUBMARINE:
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/com/cenit/battleship/images/submarineTop.jpg")));
                    break;
                default:
                    // Fallback: usar texto
                    icon.setText(type.getName().substring(0, 3));
                    icon.setStyle("-fx-background-color: #4a6572; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                    return icon;
            }

            if (imageView != null) {
                // Para barcos verticales, rotar la imagen
                if (isVertical) {
                    imageView.setRotate(90);
                }

                // Configurar tamaño de imagen
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setMouseTransparent(true);

                icon.setGraphic(imageView);
                icon.setText("");

                System.out.println("🖼️ Icono creado - " + type.getName() + " "
                        + (isVertical ? "VERTICAL" : "HORIZONTAL")
                        + " " + width + "x" + height);
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando imagen para " + type + ": " + e.getMessage());
            // Fallback a texto
            icon.setText(type.getName().substring(0, 3));
            icon.setStyle("-fx-background-color: #4a6572; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
        }

        return icon;
    }

// Método fallback si falla el renderizador principal
    private void addSimpleShipOverlay(Ship ship) {
        try {
            List<Coordinate> coordinates = ship.getCoordinates();
            if (coordinates.isEmpty()) {
                return;
            }

            Coordinate firstCoord = coordinates.get(0);
            double cellSize = config.getCellSize();

            double x = firstCoord.getY() * cellSize;
            double y = firstCoord.getX() * cellSize;

            int shipSize = ship.getType().getSize();
            boolean isVertical = ship.getDirection() == Direction.VERTICAL;

            double width = isVertical ? cellSize : cellSize * shipSize;
            double height = isVertical ? cellSize * shipSize : cellSize;

            // Crear un overlay simple con color y texto
            StackPane simpleOverlay = createSimpleShipVisual(ship, width, height);
            simpleOverlay.setLayoutX(x);
            simpleOverlay.setLayoutY(y);
            simpleOverlay.setPrefSize(width, height);

            overlayLayer.getChildren().add(simpleOverlay);

        } catch (Exception e) {
            System.err.println("❌ Error en fallback simple: " + e.getMessage());
        }
    }

    private StackPane createSimpleShipVisual(Ship ship, double width, double height) {
        StackPane overlay = new StackPane();

        // Fondo de color según el tipo de barco
        Rectangle background = new Rectangle(width, height);
        background.setFill(getDebugColorForShip(ship.getType()));
        background.setOpacity(0.7);

        // Texto con el nombre del barco
        Label label = new Label(ship.getType().getName());
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
        label.setAlignment(Pos.CENTER);

        overlay.getChildren().addAll(background, label);
        overlay.setAlignment(Pos.CENTER);

        return overlay;
    }

// Método auxiliar para crear overlays con debug visual
    private StackPane createShipOverlayWithDebug(Ship ship, double width, double height) {
        StackPane overlay = new StackPane();

        // Fondo semitransparente para debug
        Rectangle background = new Rectangle(width, height);
        background.setFill(getDebugColorForShip(ship.getType()));
        background.setOpacity(0.7);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(2);

        // Etiqueta con el nombre del barco
        Label label = new Label(ship.getType().getName());
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

        overlay.getChildren().addAll(background, label);
        overlay.setAlignment(Pos.CENTER);

        return overlay;
    }

    private Color getDebugColorForShip(ShipType type) {
        switch (type) {
            case CARRIER:
                return Color.RED;
            case BATTLESHIP:
                return Color.BLUE;
            case CRUISER:
                return Color.GREEN;
            case DESTROYER:
                return Color.ORANGE;
            case SUBMARINE:
                return Color.PURPLE;
            default:
                return Color.BLACK;
        }
    }

    private void clearShipOverlay(Ship ship) {
        if (overlayLayer == null) {
            return;
        }

        String overlayId = "ship-overlay-" + ship.getType().name() + "-" + System.identityHashCode(ship);
        int removed = 0;

        // Buscar y remover todos los overlays de este barco
        for (int i = overlayLayer.getChildren().size() - 1; i >= 0; i--) {
            Node node = overlayLayer.getChildren().get(i);
            if (node.getId() != null && node.getId().contains("ship-overlay-" + ship.getType().name())) {
                overlayLayer.getChildren().remove(i);
                removed++;
            }
        }

        System.out.println("🗑️ Limpiados " + removed + " overlays para " + ship.getType().getName());
    }

    private void updateBoardGraphics() {
    Platform.runLater(() -> {
        try {
            List<Ship> allShips = getAllPlacedShips();
            System.out.println("🎨 ACTUALIZANDO GRÁFICOS - Barcos: " + allShips.size());

            // Limpiar overlays existentes
            if (overlayLayer != null) {
                overlayLayer.getChildren().clear();
                System.out.println("🧹 Overlays limpiados");
            }

            // Limpiar estilos del tablero
            clearBoardStyles();

            // Dibujar cada barco
            for (Ship ship : allShips) {
                if (ship.isPlaced() && !ship.getCoordinates().isEmpty()) {
                    System.out.println("➡️ Dibujando: " + ship.getType().getName() + 
                                     " en " + ship.getCoordinates().get(0).aNotacion());
                    addShipOverlay(ship);
                }
            }

            // Forzar actualización visual
            if (overlayLayer != null) {
                overlayLayer.applyCss();
                overlayLayer.layout();
                System.out.println("✅ Gráficos actualizados - Total overlays: " + 
                                 overlayLayer.getChildren().size());
                
                // VERIFICACIÓN FINAL
                verifyFinalPositioning();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error en updateBoardGraphics: " + e.getMessage());
        }
    });
}

    private void clearBoardStyles() {
        for (int i = 0; i < config.getBoardSize(); i++) {
            for (int j = 0; j < config.getBoardSize(); j++) {
                Button button = boardButtons[i][j];
                button.getStyleClass().removeAll("casilla-barco", "casilla-barco-danado");
                button.getStyleClass().add("casilla-vacia");
                button.setGraphic(null);
            }
        }
    }

    // También actualiza clearHighlight para no interferir con overlays
    private void clearHighlight() {
        for (int i = 0; i < config.getBoardSize(); i++) {
            for (int j = 0; j < config.getBoardSize(); j++) {
                Button button = boardButtons[i][j];
                button.getStyleClass().remove("casilla-resaltada");
                button.setStyle("");
            }
        }
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========
    public static void setGameMode(String mode) {
        gameMode = mode;
    }

    public static void setGameController(GameController controller) {
        gameController = controller;
    }

    public static void setPlayerName(String name) {
        playerName = name;
    }

    /**
     * Valida y normaliza la dificultad recibida
     */
    private String validateAndNormalizeDifficulty(Object difficulty) {
        if (difficulty == null) {
            return "NORMAL";
        }

        String normalized;
        if (difficulty instanceof String) {
            normalized = ((String) difficulty).trim().toUpperCase();
        } else if (difficulty instanceof Difficulty) {
            normalized = ((Difficulty) difficulty).name();
        } else if (difficulty instanceof CPUController.Difficulty) {
            normalized = ((CPUController.Difficulty) difficulty).name();
        } else {
            normalized = "NORMAL";
        }

        // Validar que sea un valor de Difficulty válido
        try {
            Difficulty.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Valor de dificultad no válido: " + normalized + ", usando NORMAL");
            return "NORMAL";
        }
    }

    /**
     * Establece la dificultad del juego (método de instancia)
     */
    public void setDifficulty(Object difficulty) {
        this.currentDifficulty = validateAndNormalizeDifficulty(difficulty);

        updateDifficultyDisplay();
        applyDifficultyVisuals();
        System.out.println("🎯 Dificultad establecida: " + this.currentDifficulty);
    }

    // Sobrecargas para compatibilidad
    public void setDifficulty(String difficulty) {
        setDifficulty((Object) difficulty);
    }

    public void setDifficulty(Difficulty difficulty) {
        setDifficulty((Object) difficulty);
    }

    // ========== MÉTODOS DE DIFICULTAD (IMPLEMENTACIÓN BÁSICA) ==========
    private void updateDifficultyDisplay() {
        if (lblDifficulty != null) {
            String displayText = getDifficultyDisplayText();
            lblDifficulty.setText("Dificultad: " + displayText);
            applyDifficultyStyle(lblDifficulty);
        }

        if (lblDifficultyInfo != null) {
            lblDifficultyInfo.setText(getDifficultyDescription());
        }
    }

    private String getDifficultyDisplayText() {
        switch (currentDifficulty) {
            case "EASY":
                return "Principiante 🟢";
            case "NORMAL":
                return "Normal 🟡";
            case "HARD":
                return "Avanzado 🟠";
            case "EXPERT":
                return "Experto 🔴";
            default:
                return "Normal 🟡";
        }
    }

    private String getDifficultyDescription() {
        switch (currentDifficulty) {
            case "EASY":
                return "CPU: Disparos aleatorios. Ideal para aprender.";
            case "NORMAL":
                return "CPU: Estrategia básica. Buen equilibrio.";
            case "HARD":
                return "CPU: Búsqueda inteligente. Desafiante.";
            case "EXPERT":
                return "CPU: Algoritmo avanzado. ¡Extremadamente difícil!";
            default:
                return "Dificultad estándar.";
        }
    }

    private void applyDifficultyStyle(Label label) {
        if (label == null) {
            return;
        }

        label.getStyleClass().removeAll(
                "difficulty-easy", "difficulty-normal",
                "difficulty-hard", "difficulty-expert"
        );

        switch (currentDifficulty) {
            case "EASY":
                label.getStyleClass().add("difficulty-easy");
                break;
            case "NORMAL":
                label.getStyleClass().add("difficulty-normal");
                break;
            case "HARD":
                label.getStyleClass().add("difficulty-hard");
                break;
            case "EXPERT":
                label.getStyleClass().add("difficulty-expert");
                break;
        }
    }

    private void applyDifficultyVisuals() {
        switch (currentDifficulty) {
            case "EASY":
                showPlacementHints(true);
                enableAutoPlacement(true);
                break;
            case "NORMAL":
                showPlacementHints(true);
                enableAutoPlacement(false);
                break;
            case "HARD":
                showPlacementHints(false);
                enableAutoPlacement(false);
                break;
            case "EXPERT":
                showPlacementHints(false);
                enableAutoPlacement(false);
                applyExpertRestrictions();
                break;
        }
        updatePlacementInstructions();
    }

    private void showPlacementHints(boolean show) {
        // Implementar lógica de ayudas visuales
        System.out.println(show ? "💡 Ayudas activadas" : "🚫 Ayudas desactivadas");
    }

    private void enableAutoPlacement(boolean enable) {
        if (btnAutoPlace != null) {
            btnAutoPlace.setVisible(enable);
        }
    }

    private void applyExpertRestrictions() {
        System.out.println("💀 Aplicando restricciones de modo experto");
    }

    private void updatePlacementInstructions() {
        if (lblInstructions == null) {
            return;
        }
        lblInstructions.setText(getPlacementInstructions());
    }

    private String getPlacementInstructions() {
        String base = "Coloca tus barcos en el tablero. ";
        switch (currentDifficulty) {
            case "EASY":
                return base + "¡No te preocupes! La CPU será generosa.";
            case "NORMAL":
                return base + "Coloca estratégicamente para tener ventaja.";
            case "HARD":
                return base + "Cada posición cuenta. La CPU es inteligente.";
            case "EXPERT":
                return base + "¡Precaución! La CPU aprenderá de tus patrones.";
            default:
                return base;
        }
    }

    // ========== MÉTODOS DE CONSULTA ==========
    public String getDifficulty() {
        return currentDifficulty;
    }

    public CPUController.Difficulty getDifficultyEnum() {
        try {
            return CPUController.Difficulty.valueOf(currentDifficulty);
        } catch (IllegalArgumentException e) {
            return CPUController.Difficulty.NORMAL;
        }
    }

    public boolean isExpertDifficulty() {
        return "EXPERT".equals(currentDifficulty);
    }

    public boolean isBeginnerDifficulty() {
        return "EASY".equals(currentDifficulty);
    }

    // ========== MÉTODOS EXISTENTES DEL JUEGO ==========
    private void handleBoardClick(int x, int y) {
        if (selectShip == null) {
            return;
        }

        Coordinate coord = new Coordinate(x, y);

        if (gameController.canPlaceShip(selectShip, coord, actualDirection)) {
            boolean placed = gameController.placeShip(selectShip, coord, actualDirection);

            if (placed) {
                System.out.println("✅ " + selectShip.getType().getName() + " colocado en " + coord.aNotacion());

                // SOLO si el barco no está ya en la lista, agregarlo
                if (!placedShips.contains(selectShip)) {
                    placedShips.add(selectShip);
                }

                // Actualizar gráficos del tablero
                updateBoardGraphics();

                // Obtener el siguiente barco DE LA LISTA ORIGINAL
                selectShip = getNextShip();
                updateInterface();
            }
        } else {
            System.out.println("❌ No se puede colocar " + selectShip.getType().getName() + " en " + coord.aNotacion());
            showTemporaryMessage("❌ No se puede colocar aquí", 2000);
        }
    }

    public boolean validatePlacementWithDifficulty(Ship ship, Coordinate coord, Direction direction) {
        boolean isValid = gameController.canPlaceShip(ship, coord, direction);

        if (isValid && isExpertDifficulty()) {
            isValid = validateExpertPlacement(ship, coord, direction);
        }

        return isValid;
    }

    private boolean validateExpertPlacement(Ship ship, Coordinate coord, Direction direction) {
        // Validaciones adicionales para modo experto
        if (isTooManyOnEdge(coord, direction)) {
            showExpertWarning("Demasiados barcos en los bordes. La CPU puede detectar este patrón.");
            return false;
        }
        return true;
    }

    private boolean isTooManyOnEdge(Coordinate coord, Direction direction) {
        // Lógica simplificada - implementar según necesidades
        return (coord.getX() == 0 || coord.getX() == config.getBoardSize() || coord.getY() == 0 || coord.getY() == config.getBoardSize());
    }

    private void showExpertWarning(String message) {
        if (isExpertDifficulty()) {
            showTemporaryMessage("⚠️ " + message, 3000);
        }
    }

    private void showTemporaryMessage(String message, int duration) {
        // Implementación básica - mejorar con UI
        System.out.println("💬 " + message);
    }

    // pinta la celda del barco
    private void highlightPosition(int x, int y) {
        // --- PASO 1: LIMPIAR SIEMPRE LA PREVISUALIZACIÓN ANTERIOR ---
        clearHighlight();

        if (selectShip == null) {
            return;
        }

        Coordinate coord = new Coordinate(x, y);

        // --- PASO 2: AHORA SÍ, PINTAR LA NUEVA PREVISUALIZACIÓN ---
        if (gameController.canPlaceShip(selectShip, coord, actualDirection)) {
            showShipPreview(selectShip, coord, actualDirection);
        } else {
            // Si no se puede colocar, resalta solo la casilla actual en rojo
            if (x >= 0 && x < config.getBoardSize() && y >= 0 && y < config.getBoardSize()) {
                boardButtons[x][y].getStyleClass().add("casilla-resaltada");
                // Usamos un estilo inline para el rojo, que tiene prioridad sobre el CSS
                boardButtons[x][y].setStyle("-fx-background-color: #ffebee; -fx-border-color: #f44336;");
            }
        }
    }

    private void rotateShip() {
        actualDirection = (actualDirection == Direction.HORIZONTAL)
                ? Direction.VERTICAL : Direction.HORIZONTAL;
        btnRotate.setText(actualDirection == Direction.HORIZONTAL ? "Horizontal" : "Vertical");
    }

    private void placeRandomShips() {
        // TODO: Implementar colocación aleatoria
        selectShip = null;
        updateInterface();
    }

    private Ship getNextShip() {
        return shipToPlacement.isEmpty() ? null : shipToPlacement.remove(0);
    }

    private void updateInterface() {
        if (selectShip != null) {
            lblInstructions.setText("Coloca: " + selectShip.getType().getName());
            btnBeging.setDisable(true);
        } else {
            lblInstructions.setText("¡Todos los barcos colocados! Listo para comenzar.");
            btnBeging.setDisable(false);
        }
        updateShipsPanel();
    }

    private void updateShipsPanel() {
        panelShips.getChildren().clear();

        Label title = new Label("Barcos por colocar:");
        title.getStyleClass().add("subtitle-label");
        panelShips.getChildren().add(title);

        for (Ship ship : shipToPlacement) {
            HBox shipDisplay = ShipRenderer.createShipDisplay(ship);
            panelShips.getChildren().add(shipDisplay);
        }

        // Agregar sección de barcos colocados
        if (!shipToPlacement.contains(selectShip) && selectShip != null) {
            Label placedTitle = new Label("Barcos colocados:");
            placedTitle.getStyleClass().add("subtitle-label");
            panelShips.getChildren().add(placedTitle);

            // Mostrar barcos ya colocados
            for (Ship placedShip : getAlreadyPlacedShips()) {
                HBox shipDisplay = ShipRenderer.createShipDisplay(placedShip);
                panelShips.getChildren().add(shipDisplay);
            }
        }
    }

    // label con nombres de barcos de la flota
    private HBox createShipRepresentation(Ship ship) {
        HBox container = new HBox(2);
        int size = ship.getType().getSize();
        for (int i = 0; i < size; i++) {
            Label segment = new Label();
            segment.setPrefSize(20, 20);
            segment.getStyleClass().add("segmento-barco");
            container.getChildren().add(segment);
        }
        return container;
    }

    // RENDERIZACION DE BARCOS
    private List<Ship> getAlreadyPlacedShips() {
        List<Ship> placed = new ArrayList<>();
        for (Ship ship : shipToPlacement) {
            if (ship.isPlaced() && ship != selectShip) {
                placed.add(ship);
            }
        }
        return placed;
    }

    // muestra las casillas donde se posiciona el barco
    private void showShipPreview(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = calculateShipCoordinates(ship, start, direction);

        for (Coordinate coord : coordinates) {
            int x = coord.getX();
            int y = coord.getY();

            if (x >= 0 && x < config.getBoardSize() && y >= 0 && y < config.getBoardSize()) {
                boardButtons[x][y].getStyleClass().add("casilla-resaltada");
                boardButtons[x][y].setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50;");
            }
        }
    }

    private List<Coordinate> calculateShipCoordinates(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = new ArrayList<>();
        int size = ship.getType().getSize();

        System.out.println("🧮 CALCULANDO COORDENADAS - Barco: " + ship.getType().getName()
                + ", Start: " + start.aNotacion() + ", Dir: " + direction + ", Size: " + size);

        int dx = 0, dy = 0;
        if (direction == Direction.HORIZONTAL) {
            dy = 1; // Horizontal: misma fila, columnas diferentes
            System.out.println("  → Dirección: HORIZONTAL (dy=" + dy + ")");
        } else {
            dx = 1; // Vertical: misma columna, filas diferentes
            System.out.println("  → Dirección: VERTICAL (dx=" + dx + ")");
        }

        for (int i = 0; i < size; i++) {
            int x = start.getX() + (dx * i);
            int y = start.getY() + (dy * i);
            coordinates.add(new Coordinate(x, y));
            System.out.println("  → Segmento " + i + ": (" + x + "," + y + ")");
        }

        return coordinates;
    }

    /**
     * Encuentra la primera coordenada del barco (la más arriba-izquierda)
     */
    private Coordinate findFirstCoordinate(List<Coordinate> coordinates, Direction direction) {
        if (coordinates.isEmpty()) {
            return null;
        }

        // Para ambos casos, tomamos la primera coordenada de la lista
        // ya que calculateShipCoordinates ya las ordena correctamente
        return coordinates.get(0);
    }

    private List<Ship> getAllPlacedShips() {
        // Usar un Set para evitar duplicados
        Set<Ship> uniqueShips = new HashSet<>();

        // Agregar barcos de placedShips
        uniqueShips.addAll(placedShips);

        // Agregar barcos de shipToPlacement que ya estén colocados
        for (Ship ship : shipToPlacement) {
            if (ship.isPlaced()) {
                uniqueShips.add(ship);
            }
        }

        System.out.println("🔍 BARCOS ÚNICOS - Total: " + uniqueShips.size()
                + " | placedShips: " + placedShips.size()
                + " | shipToPlacement colocados: " + shipToPlacement.stream().filter(Ship::isPlaced).count());

        return new ArrayList<>(uniqueShips);
    }

    private void verifyOverlayState() {
        Platform.runLater(() -> {
            if (overlayLayer == null) {
                System.err.println("❌ OverlayLayer es NULL");
                return;
            }

            Bounds local = overlayLayer.getBoundsInLocal();
            Bounds parent = overlayLayer.getBoundsInParent();
            Bounds scene = overlayLayer.localToScene(overlayLayer.getBoundsInLocal());

            System.out.println("🔍 VERIFICACIÓN OVERLAY:");
            System.out.println("   Local: " + local);
            System.out.println("   Parent: " + parent);
            System.out.println("   Scene: " + scene);
            System.out.println("   Children: " + overlayLayer.getChildren().size());
            System.out.println("   Visible: " + overlayLayer.isVisible());
            System.out.println("   Managed: " + overlayLayer.isManaged());

            // Verificar cada barco
            for (Node child : overlayLayer.getChildren()) {
                if (child instanceof StackPane) {
                    StackPane shipOverlay = (StackPane) child;
                    System.out.println("   🚢 Barco - Pos: (" + shipOverlay.getLayoutX() + ", "
                            + shipOverlay.getLayoutY() + ") Size: "
                            + shipOverlay.getWidth() + "x" + shipOverlay.getHeight());
                }
            }
        });
    }

    private void debugLayoutInfo() {
        Platform.runLater(() -> {
            System.out.println("\n🔍 DEBUG LAYOUT COMPLETO:");

            // Información del BoardPlayer
            System.out.println("=== BOARD PLAYER ===");
            System.out.println("  Local: " + boardPlayer.getBoundsInLocal());
            System.out.println("  Parent: " + boardPlayer.getBoundsInParent());
            System.out.println("  Scene: " + boardPlayer.localToScene(boardPlayer.getBoundsInLocal()));
            System.out.println("  Layout: (" + boardPlayer.getLayoutX() + ", " + boardPlayer.getLayoutY() + ")");
            System.out.println("  PrefSize: " + boardPlayer.getPrefWidth() + "x" + boardPlayer.getPrefHeight());

            // Información del OverlayLayer
            if (overlayLayer != null) {
                System.out.println("=== OVERLAY LAYER ===");
                System.out.println("  Local: " + overlayLayer.getBoundsInLocal());
                System.out.println("  Parent: " + overlayLayer.getBoundsInParent());
                System.out.println("  Scene: " + overlayLayer.localToScene(overlayLayer.getBoundsInLocal()));
                System.out.println("  Layout: (" + overlayLayer.getLayoutX() + ", " + overlayLayer.getLayoutY() + ")");
                System.out.println("  PrefSize: " + overlayLayer.getPrefWidth() + "x" + overlayLayer.getPrefHeight());
            }

            // Información del Parent
            Parent parent = boardPlayer.getParent();
            if (parent instanceof Pane) {
                Pane parentPane = (Pane) parent;
                System.out.println("=== PARENT CONTAINER ===");
                System.out.println("  Children: " + parentPane.getChildren().size());
                System.out.println("  Bounds: " + parentPane.getBoundsInLocal());
            }

            System.out.println("=== CONFIG ===");
            System.out.println("  BoardSize: " + config.getBoardSize());
            System.out.println("  CellSize: " + config.getCellSize());
            System.out.println("  Calculated Size: " + (config.getBoardSize() * config.getCellSize()));
        });
    }

    private void debugShipPosition(Ship ship, double x, double y) {
        Platform.runLater(() -> {
            try {
                // Verificar la celda correspondiente en el GridPane
                int row = ship.getCoordinates().get(0).getX();
                int col = ship.getCoordinates().get(0).getY();

                Node cell = getCellAt(row, col);
                if (cell != null) {
                    Bounds cellBounds = cell.getBoundsInParent();
                    Bounds cellScene = cell.localToScene(cell.getBoundsInLocal());

                    System.out.println("🔍 DEBUG POSICIÓN CELDA - " + ship.getType().getName());
                    System.out.println("   Celda [" + row + "," + col + "] Local: " + cellBounds);
                    System.out.println("   Celda [" + row + "," + col + "] Scene: " + cellScene);
                    System.out.println("   Overlay calculado: (" + x + ", " + y + ")");

                    // Verificar diferencia
                    double diffX = Math.abs(x - cellBounds.getMinX());
                    double diffY = Math.abs(y - cellBounds.getMinY());
                    System.out.println("   Diferencia: X=" + diffX + ", Y=" + diffY);
                }
            } catch (Exception e) {
                System.err.println("❌ Error en debug de posición: " + e.getMessage());
            }
        });
    }

// Método auxiliar para obtener celda del GridPane
    private Node getCellAt(int row, int col) {
        for (Node node : boardPlayer.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);

            if (rowIndex != null && colIndex != null
                    && rowIndex == row && colIndex == col) {
                return node;
            }
        }
        return null;
    }

    private void verifyFinalPositioning() {
        Platform.runLater(() -> {
            System.out.println("\n🎯 VERIFICACIÓN FINAL DE POSICIONAMIENTO:");

            if (overlayLayer == null) {
                System.out.println("❌ OverlayLayer es null");
                return;
            }

            // Verificar overlayLayer
            Bounds overlayLocal = overlayLayer.getBoundsInLocal();
            Bounds overlayParent = overlayLayer.getBoundsInParent();
            Bounds overlayScene = overlayLayer.localToScene(overlayLayer.getBoundsInLocal());

            System.out.println("📐 OverlayLayer:");
            System.out.println("   Local: " + overlayLocal);
            System.out.println("   Parent: " + overlayParent);
            System.out.println("   Scene: " + overlayScene);
            System.out.println("   Layout: (" + overlayLayer.getLayoutX() + ", " + overlayLayer.getLayoutY() + ")");
            System.out.println("   PrefSize: " + overlayLayer.getPrefWidth() + "x" + overlayLayer.getPrefHeight());
            System.out.println("   Children: " + overlayLayer.getChildren().size());

            // Verificar cada barco individual
            for (Node child : overlayLayer.getChildren()) {
                if (child instanceof StackPane) {
                    StackPane shipOverlay = (StackPane) child;
                    Bounds shipLocal = shipOverlay.getBoundsInLocal();
                    Bounds shipParent = shipOverlay.getBoundsInParent();
                    Bounds shipScene = shipOverlay.localToScene(shipOverlay.getBoundsInLocal());

                    System.out.println("   🚢 Barco - Layout: (" + shipOverlay.getLayoutX() + ", "
                            + shipOverlay.getLayoutY() + ")");
                    System.out.println("        Local: " + shipLocal);
                    System.out.println("        Parent: " + shipParent);
                    System.out.println("        Scene: " + shipScene);
                }
            }

            // Verificar boardPlayer para comparar
            Bounds boardLocal = boardPlayer.getBoundsInLocal();
            Bounds boardScene = boardPlayer.localToScene(boardPlayer.getBoundsInLocal());
            System.out.println("📊 BoardPlayer:");
            System.out.println("   Local: " + boardLocal);
            System.out.println("   Scene: " + boardScene);
        });
    }
}
