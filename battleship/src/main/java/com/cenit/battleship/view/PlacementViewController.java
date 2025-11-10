package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Board;
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
import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.view.components.BoardRenderer;
import java.util.Random;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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

    private List<Coordinate> currentHighlight = new ArrayList<>();
    private boolean isValidPlacement = false;

    private BoardRenderer boardRenderer;

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
    private Label lblMessage;
    @FXML
    private HBox mainContainer;
    @FXML
    private ComboBox<String> comboDifficulty;

    private Button[][] boardButtons;
    private GameConfiguration config;
    private Pane overlayLayer;
    private PlayerProfile currentProfile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Inicializar variables
        this.placedShips = new ArrayList<>(); // Asegurar que esté inicializada
        this.config = GameConfiguration.getInstance();
        this.boardRenderer = new BoardRenderer();
        
        // Crear o cargar el perfil del jugador
        initializePlayerProfile();

        //establecer el tamaño de lasceldas
        config.setCellSize(40);
        boardButtons = new Button[config.getBoardSize()][config.getBoardSize()];

        initializeShips();
        initializeBoard();

        // Configurar overlay con mejor sincronización
        setupOverlay();

        // Inicializar panel de barcos
        updateShipsPanel();

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
        setBattleShipBackground();
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
    try {
        System.out.println("🎯 Inicializando tablero con BoardRenderer...");
        boardRenderer.initializeBoard(boardPlayer, boardButtons, true, this::handleBoardClick);
        System.out.println("✅ Tablero inicializado correctamente");
    } catch (Exception e) {
        System.err.println("❌ Error inicializando tablero: " + e.getMessage());
        e.printStackTrace();
        // Fallback: inicialización manual
        initializeBoardManual();
    }
}

// Método de fallback por si BoardRenderer falla
private void initializeBoardManual() {
    int boardSize = config.getBoardSize();
    int cellSize = config.getCellSize();

    System.out.println("🔄 Usando inicialización manual - Tamaño: " + cellSize + "px");

    int totalSize = boardSize * cellSize;
    boardPlayer.setPrefSize(totalSize, totalSize);
    boardPlayer.setMinSize(totalSize, totalSize);
    boardPlayer.setMaxSize(totalSize, totalSize);

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

    System.out.println("✅ Tablero manual inicializado: " + totalSize + "x" + totalSize + "px");
}

    private void updateBoardVisuals() {
    try {
        Board tempBoard = createBoardFromPlacement();
        if (tempBoard != null) {
            boardRenderer.updateBoardDisplay(boardButtons, tempBoard, true, true);
            System.out.println("✅ Visualización del tablero actualizada");
        }
    } catch (Exception e) {
        System.err.println("❌ Error actualizando visualización: " + e.getMessage());
        // Continuar sin actualizar la visualización del BoardRenderer
    }
    
    
}
    /**
     * Limpia el aspecto visual del tablero
     */
    private void clearBoardVisuals() {
    try {
        if (boardRenderer != null) {
            // Usar BoardRenderer si está disponible
            Board tempBoard = new Board(config.getBoardSize());
            boardRenderer.updateBoardDisplay(boardButtons, tempBoard, true, true);
        } else {
            // Fallback manual
            int boardSize = config.getBoardSize();
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    Button button = boardButtons[i][j];
                    button.getStyleClass().removeAll("casilla-barco", "casilla-bloqueada");
                    button.getStyleClass().add("casilla-vacia");
                    button.setText("");
                }
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Error limpiando tablero: " + e.getMessage());
    }
}

//    private void initializeBoard() {
//        int boardSize = config.getBoardSize();
//        int cellSize = config.getCellSize(); // Esto es 40
//
//        System.out.println("? Tamaño de casilla establecido a: " + cellSize + "px");
//
//        // ✅ SOLUCIÓN: FORZAR TAMAÑO DEL GRIDPANE
//        int totalSize = boardSize * cellSize; // 15 * 40 = 600px
//        boardPlayer.setPrefSize(totalSize, totalSize);
//        boardPlayer.setMinSize(totalSize, totalSize);
//        boardPlayer.setMaxSize(totalSize, totalSize);
//
//        for (int i = 0; i < boardSize; i++) {
//            for (int j = 0; j < boardSize; j++) {
//                Button button = new Button();
//                button.setPrefSize(cellSize, cellSize); // 40x40
//                button.getStyleClass().add("casilla-vacia");
//
//                final int x = i;
//                final int y = j;
//
//                button.setOnMouseClicked(e -> handleBoardClick(x, y));
//                button.setOnMouseEntered(e -> highlightPosition(x, y));
//                button.setOnMouseExited(e -> clearHighlight());
//
//                boardPlayer.add(button, j, i);
//                boardButtons[i][j] = button;
//            }
//        }
//
//        System.out.println("✅ Tablero forzado a: " + totalSize + "x" + totalSize + "px");
//    }
    /**
     * Inicializa el perfil del jugador (versión simplificada sin
     * ProfileManager)
     */
    private void initializePlayerProfile() {
        try {
            // Obtener el nombre del jugador de la configuración
            playerName = config.getPlayerName();

            // Crear nuevo perfil
            currentProfile = new PlayerProfile(playerName);

            // Establecer valores iniciales
            //   currentProfile.setGamesPlayed(0);
            //   currentProfile.setGamesWon(0);
            currentProfile.setTotalScore(0);

            System.out.println("✅ Perfil del jugador inicializado: " + playerName);

        } catch (Exception e) {
            System.err.println("❌ Error al inicializar perfil del jugador: " + e.getMessage());
            // Fallback: crear perfil con nombre por defecto
            currentProfile = new PlayerProfile("Jugador");
            System.out.println("🔄 Usando perfil por defecto: Jugador");
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

    /**
     * Obtiene el GameController actual o crea uno nuevo
     */
    private GameController getGameController() {
        if (this.gameController == null) {
            System.out.println("🔄 GameController es null, creando uno nuevo...");
            this.gameController = createGameController();
        }
        return this.gameController;
    }

    /**
     * Crea un nuevo GameController con la configuración actual
     */
    private GameController createGameController() {
        try {
            System.out.println("🎮 Creando nuevo GameController...");

            // Validar que hay barcos colocados
            if (placedShips == null || placedShips.isEmpty()) {
                System.err.println("❌ ERROR: No hay barcos colocados");
                showMessage("Error: Debes colocar todos los barcos antes de iniciar");
                return null;
            }

            // Crear GameController con perfil y dificultad
            GameController newController = new GameController(currentProfile, config.getCpuDifficulty());

            // Crear tablero del jugador desde la colocación
            Board playerBoard = createBoardFromPlacement();
            if (playerBoard == null) {
                System.err.println("❌ ERROR: No se pudo crear el tablero del jugador");
                return null;
            }

            // Configurar el GameController
            newController.setPlayerBoard(playerBoard);
            newController.setPlayerShips(new ArrayList<>(placedShips));

            // Inicializar el juego
            newController.initializeGame();

            // Validar que se creó correctamente
            if (validateGameController(newController)) {
                System.out.println("✅ GameController creado exitosamente");
                return newController;
            } else {
                System.err.println("❌ GameController no válido después de la creación");
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO al crear GameController: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crea el tablero del jugador a partir de los barcos colocados
     */
    private Board createBoardFromPlacement() {
        try {
            System.out.println("🔄 Creando tablero desde " + placedShips.size() + " barcos colocados...");

            Board board = new Board(config.getBoardSize());

            if (placedShips.isEmpty()) {
                System.err.println("❌ No hay barcos colocados");
                return null;
            }

            // Colocar cada barco en el tablero
            for (Ship ship : placedShips) {
                if (ship == null) {
                    System.err.println("⚠️  Barco nulo encontrado en placedShips");
                    continue;
                }

                List<Coordinate> segments = ship.getSegments();
                if (segments == null || segments.isEmpty()) {
                    System.err.println("⚠️  Barco " + ship.getType().getName() + " sin segmentos");
                    continue;
                }

                try {
                    // Verificar que se puede colocar
                    if (board.canPlaceShip(segments)) {
                        board.placeShip(ship, segments);
                        System.out.println("✅ " + ship.getType().getName() + " colocado en el tablero");
                    } else {
                        System.err.println("❌ No se puede colocar " + ship.getType().getName());
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al colocar " + ship.getType().getName() + ": " + e.getMessage());
                    return null;
                }
            }

            System.out.println("✅ Tablero del jugador creado con " + placedShips.size() + " barcos");
            return board;

        } catch (Exception e) {
            System.err.println("❌ ERROR en createBoardFromPlacement(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida que el GameController esté correctamente configurado
     */
    private boolean validateGameController(GameController gameController) {
        if (gameController == null) {
            System.err.println("❌ GameController es null");
            return false;
        }

        if (gameController.getPlayerBoard() == null) {
            System.err.println("❌ PlayerBoard es null");
            return false;
        }

        if (gameController.getCpuBoard() == null) {
            System.err.println("❌ CpuBoard es null");
            return false;
        }

        if (gameController.getPlayerShips().isEmpty()) {
            System.err.println("❌ PlayerShips está vacío");
            return false;
        }

        if (gameController.getCpuShips().isEmpty()) {
            System.err.println("❌ CpuShips está vacío");
            return false;
        }

        System.out.println("✅ GameController validado correctamente");
        return true;
    }
    
    //EVENTOS DE BOTONES 
    private void ConfigureEvents() {
        btnRotate.setOnAction(e -> rotateShip());
        btnAleatory.setOnAction(e -> placeRandomShips());
        btnBeging.setOnAction(e -> startGame());
    }

    // onAction button
    private void rotateShip() {
        actualDirection = (actualDirection == Direction.HORIZONTAL) ? Direction.VERTICAL : Direction.HORIZONTAL;
        btnRotate.setText(actualDirection == Direction.VERTICAL ? "Horizontal" : "Vertical");

        // Recalcular highlight si hay una posición activa
        if (!currentHighlight.isEmpty() && selectShip != null) {
            Coordinate firstCoord = currentHighlight.get(0);
            highlightPosition(firstCoord.getX(), firstCoord.getY());
        }

        updateInstructions();
    }

    // onAction button
    private void placeRandomShips() {
    System.out.println("🎲 Colocando barcos aleatoriamente...");

    try {
        clearAllShips();
        List<Ship> shipsToPlace = new ArrayList<>(shipToPlacement);

        for (Ship ship : shipsToPlace) {
            placeSingleShipRandomly(ship);
        }

        updateInterface();
        updateShipGraphics();
        updateBoardVisuals(); // ✅ Actualizar BoardRenderer después de colocación aleatoria
        btnBeging.setDisable(shipToPlacement.isEmpty());

        System.out.println("✅ Colocación aleatoria completada");

    } catch (Exception e) {
        System.err.println("❌ Error en colocación aleatoria: " + e.getMessage());
    }
}
    

    /**
     * Método startGame 
     */
    private void startGame() {
        System.out.println("? Iniciando juego con " + placedShips.size() + " barcos colocados");

        try {
            // Obtener el GameController actual
            GameController gameController = getGameController();

            if (gameController == null) {
                System.err.println("❌ ERROR: GameController es null en startGame()");
                showMessage("Error: No se pudo inicializar el juego");
                return;
            }

            System.out.println("✅ GameController obtenido: " + (gameController != null));

            // Configurar el GameController antes de cambiar de vista
            GameViewController.setGameController(gameController);

            System.out.println("🔄 Cambiando a GameView...");
            App.changeView("/com/cenit/battleship/view/GameView.fxml");

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO en startGame(): " + e.getMessage());
            e.printStackTrace();
            showMessage("Error al iniciar el juego: " + e.getMessage());
        }
    }

    // ========== MÉTODOS PARA GESTIONAR placedShips ==========
    /**
     * Agrega un barco a la lista de barcos colocados
     */
    public void addPlacedShip(Ship ship) {
        if (placedShips == null) {
            placedShips = new ArrayList<>();
        }

        if (ship != null && !placedShips.contains(ship)) {
            placedShips.add(ship);
            System.out.println("✅ Barco agregado a placedShips: " + ship.getType().getName());
        }
    }

    /**
     * Elimina un barco de la lista de barcos colocados
     */
    public void removePlacedShip(Ship ship) {
        if (placedShips != null && ship != null) {
            placedShips.remove(ship);
            System.out.println("🗑️ Barco removido de placedShips: " + ship.getType().getName());
        }
    }

    /**
     * Obtiene la lista de barcos colocados
     */
    public List<Ship> getPlacedShips() {
        if (placedShips == null) {
            placedShips = new ArrayList<>();
        }
        return new ArrayList<>(placedShips);
    }

    /**
     * Verifica si todos los barcos necesarios han sido colocados
     */
    public boolean areAllShipsPlaced() {
        if (placedShips == null) {
            return false;
        }

        // Define cuántos barcos se necesitan (ajusta según tu juego)
        int requiredShips = 5; // Por ejemplo: Carrier, Battleship, Cruiser, Submarine, Destroyer

        boolean allPlaced = placedShips.size() >= requiredShips;
        System.out.println("📊 Barcos colocados: " + placedShips.size() + "/" + requiredShips);

        return allPlaced;
    }

    /**
     * Reinicia la colocación de barcos
     */
    public void resetPlacement() {
        if (placedShips != null) {
            placedShips.clear();
            System.out.println("🔄 Colocación de barcos reiniciada");
        }

        if (gameController != null) {
            gameController = null;
            System.out.println("🔄 GameController reiniciado");
        }
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

    

    /**
     * Limpia todos los barcos colocados
     */
    private void clearAllShips() {
        placedShips.clear();

        // Reiniciar la lista de barcos por colocar
        shipToPlacement.clear();
        initializeShips();

        // Limpiar gráficos
        if (overlayLayer != null) {
            overlayLayer.getChildren().clear();
        }

        // Limpiar el estado visual del tablero
        clearBoardVisuals();
    }

    
    

    /**
     * Coloca un solo barco en posición aleatoria
     */
    private void placeSingleShipRandomly(Ship ship) {
        Random random = new Random();
        int maxAttempts = 100; // Límite para evitar bucle infinito
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            // Generar posición y dirección aleatorias
            int x = random.nextInt(config.getBoardSize());
            int y = random.nextInt(config.getBoardSize());
            Direction direction = random.nextBoolean() ? Direction.HORIZONTAL : Direction.VERTICAL;

            System.out.println("? Intentando colocar " + ship.getType().getName()
                    + " en " + getCoordinateName(x, y) + " (" + direction + ")");

            // Verificar si se puede colocar
            if (canPlaceShip(x, y, direction, ship.getType().getSize())) {
                // Colocar el barco
                boolean placed = placeShip(x, y, direction, ship);
                if (placed) {
                    placedShips.add(ship);
                    shipToPlacement.remove(ship);

                    System.out.println("✅ " + ship.getType().getName()
                            + " colocado aleatoriamente en " + getCoordinateName(x, y));
                    return; // Salir del bucle si se colocó exitosamente
                }
            }
        }

        // Si no se pudo colocar después de muchos intentos
        System.err.println("❌ No se pudo colocar " + ship.getType().getName()
                + " después de " + maxAttempts + " intentos");
    }

    /**
     * Versión mejorada de canPlaceShip con mejor logging
     */
    private boolean canPlaceShip(int startX, int startY, Direction direction, int size) {
        // Calcular coordenadas
        List<Coordinate> coordinates = calculateShipCoordinates(startX, startY, direction, size);

        // Verificar que el barco quepa en el tablero
        if (coordinates.isEmpty()) {
            return false;
        }

        // Verificar límites
        for (Coordinate coord : coordinates) {
            if (coord.getX() < 0 || coord.getX() >= config.getBoardSize()
                    || coord.getY() < 0 || coord.getY() >= config.getBoardSize()) {
                return false;
            }
        }

        // Verificar superposición y proximidad
        for (Ship placedShip : placedShips) {
            for (Coordinate placedCoord : placedShip.getSegments()) {
                // Verificar superposición
                for (Coordinate newCoord : coordinates) {
                    if (placedCoord.equals(newCoord)) {
                        return false;
                    }
                }

                // Verificar adyacencia
                for (Coordinate newCoord : coordinates) {
                    int diffX = Math.abs(placedCoord.getX() - newCoord.getX());
                    int diffY = Math.abs(placedCoord.getY() - newCoord.getY());
                    if (diffX <= 1 && diffY <= 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Realiza una validación final antes de iniciar el juego
     */
    private boolean performFinalValidation(GameController gameController) {
        if (gameController == null) {
            return false;
        }

        // Verificar barcos del jugador
        if (gameController.getPlayerShips().isEmpty()) {
            System.err.println("❌ El jugador no tiene barcos");
            return false;
        }

        // Verificar barcos de la CPU
        if (gameController.getCpuShips().isEmpty()) {
            System.err.println("❌ La CPU no tiene barcos");
            return false;
        }

        // Verificar tableros
        if (gameController.getPlayerBoard() == null) {
            System.err.println("❌ Tablero del jugador es null");
            return false;
        }

        if (gameController.getCpuBoard() == null) {
            System.err.println("❌ Tablero de la CPU es null");
            return false;
        }

        System.out.println("✅ Validación final exitosa");
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
            showAlert("Selecciona un barco", "Por favor, selecciona un barco del panel primero.");
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

                    // Seleccionar automáticamente el siguiente barco
                    if (!shipToPlacement.isEmpty()) {
                        selectShip = shipToPlacement.get(0);
                    } else {
                        selectShip = null;
                    }

                    // Limpiar highlight después de colocar
                    clearHighlight();

                    updateInterface();
                    updateShipGraphics();
                    updateBoardVisuals();
                }
            } else {
                System.out.println("? No se puede colocar el barco en " + getCoordinateName(x, y));
                showAlert("Posición inválida", "No se puede colocar el barco aquí. Intenta en otra posición.");
            }
        } catch (Exception e) {
            System.err.println("? Error al colocar barco: " + e.getMessage());
            showAlert("Error", "Error al colocar el barco: " + e.getMessage());
        }
    }

    private String getCoordinateName(int x, int y) {
        char column = (char) ('A' + x);
        int row = y + 1;
        return "" + column + row;
    }

    private void updateShipGraphics() {
        System.out.println("=== 🔍 DEBUG DIMENSIONES --updateShipgraphics ===\n");
        System.out.println("Config CellSize: " + config.getCellSize());
        System.out.println("Board Bounds: " + boardPlayer.getBoundsInParent());
        System.out.println("Board PrefSize: " + boardPlayer.getPrefWidth() + "x" + boardPlayer.getPrefHeight());
        System.out.println("Calculated Cell: " + (boardPlayer.getBoundsInParent().getWidth() / config.getBoardSize()));
        System.out.println("===========================");

        if (overlayLayer == null) {
            return;
        }

        // ✅ USAR EL TAMAÑO CONFIGURADO, NO EL CALCULADO
        int cellSize = config.getCellSize(); // 40px
        double actualCellWidth = cellSize;
        double actualCellHeight = cellSize;

        System.out.println("✅ Usando cellSize configurado: " + cellSize + "px");

        for (Ship ship : placedShips) {
            if (!ship.getSegments().isEmpty()) {
                Coordinate start = ship.getSegments().get(0);

                // Calcular posición con tamaño FIJO
                double x = start.getY() * actualCellWidth;
                double y = start.getX() * actualCellHeight;

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

    //ESTILO DE BACKGROUND
    private void setBattleShipBackground() {
        // Fondo gradiente azul marino
        String backgroundStyle
                = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, "
                + "#0a2463 0%, #1e3c72 50%, #2a5298 100%);"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #1565c0;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 10;";

        mainContainer.setStyle(backgroundStyle);

        // Opcional: también poner fondo al panel de barcos
        if (panelShips != null) {
            panelShips.setStyle("-fx-background-color: rgba(255,255,255,0.1);"
                    + "-fx-background-radius: 8;"
                    + "-fx-border-color: rgba(255,255,255,0.3);"
                    + "-fx-border-width: 1;"
                    + "-fx-border-radius: 8;");
        }

    }

    /**
     * Actualiza el panel de barcos mostrando los que faltan colocar
     */
    private void updateShipsPanel() {
        if (panelShips == null) {
            return;
        }

        // Limpiar el panel (mantener solo el título)
        panelShips.getChildren().removeIf(node
                -> !(node instanceof Label && ((Label) node).getText().equals("Tu Flota")));

        System.out.println("? Actualizando panel de barcos - Faltan: " + shipToPlacement.size());

        // Crear elementos para cada barco que falta colocar
        for (Ship ship : shipToPlacement) {
            HBox shipItem = createShipItem(ship);
            panelShips.getChildren().add(shipItem);
        }

        // Si no hay barcos por colocar, mostrar mensaje
        if (shipToPlacement.isEmpty()) {
            Label allPlacedLabel = new Label("✅ Todos los barcos colocados");
            allPlacedLabel.getStyleClass().add("success-label");
            panelShips.getChildren().add(allPlacedLabel);
        }
    }

    /**
     * Crea un elemento de UI para representar un barco
     */
    private HBox createShipItem(Ship ship) {
        HBox shipContainer = new HBox(10);
        shipContainer.getStyleClass().add("ship-item");
        shipContainer.setAlignment(Pos.CENTER_LEFT);
        shipContainer.setPadding(new Insets(8, 12, 8, 12));

        // Crear icono visual del barco (usando texto o rectángulo)
        Pane shipIcon = createShipIcon(ship);

        // Información del barco
        VBox shipInfo = new VBox(2);
        shipInfo.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(ship.getType().getName());
        nameLabel.getStyleClass().add("ship-name");

        Label sizeLabel = new Label("Tamaño: " + ship.getType().getSize() + " casillas");
        sizeLabel.getStyleClass().add("ship-size");

        shipInfo.getChildren().addAll(nameLabel, sizeLabel);

        // Botón de selección rápida (opcional)
        Button selectButton = new Button("Seleccionar");
        selectButton.getStyleClass().add("select-ship-button");
        selectButton.setOnAction(e -> selectShipForPlacement(ship));

        // Hacer que todo el contenedor sea clickeable
        shipContainer.setOnMouseClicked(e -> selectShipForPlacement(ship));

        shipContainer.getChildren().addAll(shipIcon, shipInfo, selectButton);

        // Resaltar si es el barco actualmente seleccionado
        if (ship.equals(selectShip)) {
            shipContainer.getStyleClass().add("ship-item-selected");
        }

        return shipContainer;
    }

    /**
     * Crea un icono visual representando el barco
     */
    private Pane createShipIcon(Ship ship) {
        HBox iconContainer = new HBox(2);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getStyleClass().add("ship-icon");

        int shipSize = ship.getType().getSize();

        // Crear segmentos visuales del barco
        for (int i = 0; i < shipSize; i++) {
            Rectangle segment = new Rectangle(12, 20);
            segment.getStyleClass().add("ship-segment");

            // Color diferente según el tipo de barco
            switch (ship.getType()) {
                case CARRIER:
                    segment.setFill(Color.RED);
                    break;
                case BATTLESHIP:
                    segment.setFill(Color.BLUE);
                    break;
                case CRUISER:
                    segment.setFill(Color.GREEN);
                    break;
                case DESTROYER:
                    segment.setFill(Color.ORANGE);
                    break;
                case SUBMARINE:
                    segment.setFill(Color.PURPLE);
                    break;
                default:
                    segment.setFill(Color.GRAY);
            }

            iconContainer.getChildren().add(segment);
        }

        return iconContainer;
    }

    /**
     * Selecciona un barco para colocación manual
     */
    private void selectShipForPlacement(Ship ship) {
        if (shipToPlacement.contains(ship)) {
            selectShip = ship;
            System.out.println("? Barco seleccionado: " + ship.getType().getName());

            // Actualizar instrucciones
            updateInstructions();

            // Actualizar panel para reflejar selección
            updateShipsPanel();
        }
    }

    /**
     * Actualiza las instrucciones basado en el barco seleccionado
     */
    private void updateInstructions() {
        if (selectShip != null) {
            lblInstructions.setText("Selecciona: " + selectShip.getType().getName()
                    + " (" + selectShip.getType().getSize() + " casillas) - "
                    + actualDirection);
        } else if (shipToPlacement.isEmpty()) {
            lblInstructions.setText("✅ Todos los barcos colocados. Presiona 'Comenzar'.");
        } else {
            lblInstructions.setText("Selecciona un barco para colocar");
        }
    }
    //METODOS PARA DEBUGGEAR

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
                System.out.println("   ? Barco" + shipToPlacement.toString() + " - Layout: (" + child.getLayoutX() + ", " + child.getLayoutY() + ")");
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
        if (selectShip == null) {
            return;
        }

        try {
            // Limpiar highlight anterior
            clearHighlight();

            // Calcular las coordenadas que ocuparía el barco
            List<Coordinate> potentialCoordinates = calculateShipCoordinates(
                    x, y, actualDirection, selectShip.getType().getSize()
            );

            // Verificar si la posición es válida
            isValidPlacement = canPlaceShip(x, y, actualDirection, selectShip.getType().getSize());

            // Aplicar highlight a cada coordenada
            for (Coordinate coord : potentialCoordinates) {
                int coordX = coord.getX();
                int coordY = coord.getY();

                if (coordX >= 0 && coordX < config.getBoardSize()
                        && coordY >= 0 && coordY < config.getBoardSize()) {

                    Button button = boardButtons[coordX][coordY];

                    // Remover estilos anteriores
                    button.getStyleClass().removeAll("casilla-valida", "casilla-invalida");

                    // Aplicar nuevo estilo
                    if (isValidPlacement) {
                        button.getStyleClass().add("casilla-valida"); // Verde
                    } else {
                        button.getStyleClass().add("casilla-invalida"); // Rojo
                    }

                    currentHighlight.add(coord);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en highlight: " + e.getMessage());
        }
    }

    private void clearHighlight() {
        for (Coordinate coord : currentHighlight) {
            int x = coord.getX();
            int y = coord.getY();

            if (x >= 0 && x < config.getBoardSize()
                    && y >= 0 && y < config.getBoardSize()) {

                Button button = boardButtons[x][y];

                // Remover estilos de highlight
                button.getStyleClass().removeAll("casilla-valida", "casilla-invalida");

                // Restaurar estilo normal
                if (!button.getStyleClass().contains("casilla-vacia")) {
                    button.getStyleClass().add("casilla-vacia");
                }
            }
        }

        currentHighlight.clear();
        isValidPlacement = false;
    }

    // Actualizar la interfaz de usuario
    private void updateInterface() {
        System.out.println("||||updateInterface?\\n BARCOS UNICOS - Total: " + shipToPlacement.size()
                + " | placedShips: " + placedShips.size() + " | shipToPlacement colocados: "
                + (shipToPlacement.size() - placedShips.size()));

        // Actualizar panel de barcos
        updateShipsPanel();

        // Actualizar instrucciones
        updateInstructions();

        // Actualizar estado del botón comenzar
        btnBeging.setDisable(!shipToPlacement.isEmpty());
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

    private void showMessage(String message) {
        // Asegurarse de que se ejecuta en el hilo de JavaFX
        Platform.runLater(() -> {
            try {
                if (lblMessage != null) {
                    lblMessage.setText(message);

                    // Aplicar estilo según el tipo de mensaje
                    if (message.toLowerCase().contains("error")
                            || message.toLowerCase().contains("crítico")
                            || message.toLowerCase().contains("fallo")) {
                        // Mensaje de error - estilo rojo
                        lblMessage.getStyleClass().removeAll("mensaje-normal", "mensaje-especial", "mensaje-info");
                        lblMessage.getStyleClass().add("mensaje-error");
                    } else if (message.toLowerCase().contains("éxito")
                            || message.toLowerCase().contains("correcto")
                            || message.toLowerCase().contains("victoria")) {
                        // Mensaje de éxito - estilo verde
                        lblMessage.getStyleClass().removeAll("mensaje-normal", "mensaje-error", "mensaje-info");
                        lblMessage.getStyleClass().add("mensaje-especial");
                    } else {
                        // Mensaje normal - estilo por defecto
                        lblMessage.getStyleClass().removeAll("mensaje-error", "mensaje-especial", "mensaje-info");
                        lblMessage.getStyleClass().add("mensaje-normal");
                    }
                }

                // También imprimir en consola para debugging
                System.out.println("📢 PlacementView: " + message);

            } catch (Exception e) {
                System.err.println("❌ Error al mostrar mensaje: " + e.getMessage());
                // Fallback: imprimir en consola
                System.out.println("📢 [FALLBACK] " + message);
            }
        });
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
