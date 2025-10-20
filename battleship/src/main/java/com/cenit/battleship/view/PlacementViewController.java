package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.CPUController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
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

    private Button[][] boardButtons = new Button[10][10];
    
    
    
    

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                break;
            case "Táctico":
                // shipToPlacement.add(new Ship(ShipType.RADAR));
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
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                button.getStyleClass().add("casilla-vacia");

                final int x = i;
                final int y = j;

                button.setOnMouseClicked(e -> handleCheckboxClick(x, y));
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
    private void handleCheckboxClick(int x, int y) {
        if (selectShip == null) {
            return;
        }

        Coordinate coord = new Coordinate(x, y);

        if (gameController.canPlaceShip(selectShip, coord, actualDirection)) {
            // Colocar el barco
            boolean placed = gameController.placeShip(selectShip, coord, actualDirection);

            if (placed) {
                System.out.println("✅ " + selectShip.getType().getName()
                        + " colocado en " + coord.aNotacion());
                selectShip = getNextShip();
                updateInterface();
            }
        } else {
            System.out.println("❌ No se puede colocar " + selectShip.getType().getName()
                    + " en " + coord.aNotacion());
            // Mostrar feedback visual al usuario
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
        // Implementar resaltado según dificultad
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

    private void StartGame() {
        try {
            App.changeView("view/GameView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            HBox rowShip = new HBox(10);
            rowShip.getStyleClass().add("barco-item");

            Label name = new Label(ship.getType().getName());
            name.getStyleClass().add("barco-nombre");

            HBox representation = createShipRepresentation(ship);
            rowShip.getChildren().addAll(name, representation);
            panelShips.getChildren().add(rowShip);
        }
    }

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
}
