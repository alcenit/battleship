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

    //  Usa los valores de la configuración para inicializar tus variables
    private  Button[][] boardButtons; 

    //instancia de la configuración
    private final GameConfiguration config = GameConfiguration.getInstance();

    //METODOS DE INICIALIZACION
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardButtons= new Button[config.getBoardSize()][config.getBoardSize()];
        // Inicializar elementos de dificultad si existen
        initializeDifficultyDisplay();
        initializeShips();
        initializeBoard();
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
                // shipToPlacement.add(new Ship(ShipType.CARRIER_ANCHO));
                shipToPlacement.add(new Ship(ShipType.CARRIER));
                shipToPlacement.add(new Ship(ShipType.BATTLESHIP));
                shipToPlacement.add(new Ship(ShipType.CRUISER));
                shipToPlacement.add(new Ship(ShipType.DESTROYER));
                shipToPlacement.add(new Ship(ShipType.SUBMARINE));
                break;
            case "Táctico":
                // shipToPlacement.add(new Ship(ShipType.RADAR));
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

    //falta actualizar este metodo con boardssize
    private void initializeBoard() {
        //  valores de la configuración en lugar de números fijos
        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = new Button();

                //  El tamaño de la casilla  es dinámico
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
     *
     * @param difficulty
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
    //chequeo de colocacion de barco en tablero
    private void handleBoardClick(int x, int y) {
        if (selectShip == null) {
            return;
        }

        Coordinate coord = new Coordinate(x, y);

        if (gameController.canPlaceShip(selectShip, coord, actualDirection)) {
            boolean placed = gameController.placeShip(selectShip, coord, actualDirection);

            if (placed) {
                System.out.println("✅ " + selectShip.getType().getName() + " colocado en " + coord.aNotacion());

                // Guarda una referencia al barco que se acaba de colocar
                placedShips.add(selectShip);

                // Actualizar gráficos del tablero
                updateBoardGraphics();

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
        return (coord.getX() == 0 || coord.getX() == 9 || coord.getY() == 0 || coord.getY() == 9);
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

    private void highlightPosition(int x, int y) {
        if (selectShip == null) {
            return;
        }

        clearHighlight();
        Coordinate coord = new Coordinate(x, y);

        if (gameController.canPlaceShip(selectShip, coord, actualDirection)) {
            // Mostrar preview del barco
            showShipPreview(selectShip, coord, actualDirection);
        } else {
            // Resaltar en rojo si no se puede colocar
            boardButtons[x][y].getStyleClass().add("casilla-resaltada");
            boardButtons[x][y].setStyle("-fx-background-color: #ffebee; -fx-border-color: #f44336;");
        }
    }

    private void clearHighlight() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardButtons[i][j].getStyleClass().remove("casilla-resaltada");
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

    //RENDERIZACION DE BARCOS
    private List<Ship> getAlreadyPlacedShips() {
        List<Ship> placed = new ArrayList<>();
        for (Ship ship : shipToPlacement) {
            if (ship.isPlaced() && ship != selectShip) {
                placed.add(ship);
            }
        }
        return placed;
    }

    private void showShipPreview(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = calculateShipCoordinates(ship, start, direction);

        for (Coordinate coord : coordinates) {
            int x = coord.getX();
            int y = coord.getY();

            if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                boardButtons[x][y].getStyleClass().add("casilla-resaltada");
                boardButtons[x][y].setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50;");
            }
        }
    }

    private List<Coordinate> calculateShipCoordinates(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = new ArrayList<>();
        int size = ship.getType().getSize();

        for (int i = 0; i < size; i++) {
            int x = direction == Direction.HORIZONTAL ? start.getX() : start.getX() + i;
            int y = direction == Direction.HORIZONTAL ? start.getY() + i : start.getY();
            coordinates.add(new Coordinate(x, y));
        }

        return coordinates;
    }

    private void updateBoardGraphics() {
        // Limpiar tablero
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = boardButtons[i][j];
                button.getStyleClass().removeAll("casilla-barco", "casilla-barco-danado");
                button.getStyleClass().add("casilla-vacia");
                button.setGraphic(null);
            }
        }

        // Dibujar todos los barcos colocados
        for (Ship ship : getAllPlacedShips()) {
            if (ship.isPlaced()) {
                drawShipOnBoard(ship);
            }
        }
    }

    private void drawShipOnBoard(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();

        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate coord = coordinates.get(i);
            Button button = boardButtons[coord.getX()][coord.getY()];

            // Actualizar estilo de la casilla
            button.getStyleClass().remove("casilla-vacia");
            if (ship.isPartDamaged(coord)) {
                button.getStyleClass().add("casilla-barco-danado");
            } else {
                button.getStyleClass().add("casilla-barco");
            }

            // Solo agregar gráfico en la primera casilla del barco
            if (i == 0) {
                StackPane shipOverlay = ShipRenderer.createShipOverlay(ship);
                button.setGraphic(shipOverlay);
            }
        }
    }

    private List<Ship> getAllPlacedShips() {
        // Simplemente devuelve la lista de barcos que ya han sido colocados.
        return placedShips;
    }
}
