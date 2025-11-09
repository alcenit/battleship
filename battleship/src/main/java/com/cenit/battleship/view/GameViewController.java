package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.AnimationController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.controller.SoundController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.enums.ShotResult;
import com.cenit.battleship.model.GameConfiguration;
import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.services.StorageService;
import com.cenit.battleship.view.components.ShipRenderer;
import static com.cenit.battleship.view.components.ShipRenderer.renderShipCorrected;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameViewController implements Initializable {

    private static GameController gameControllerStatic;

    private GameController gameController;
    private StorageService storageService;
    public SoundController soundController;
    private AnimationController animationController;
    private GameConfiguration config;

    // Variables existentes
    private List<Ship> placedShips; // Esta variable debería existir ya

    private PlayerProfile currentProfile;

    private boolean activeGame = true;

    // Tablero del jugador
    @FXML
    private GridPane playerBoard;
    @FXML
    private Label lblPlayerState;

    // Tablero de la CPU (para disparar)
    @FXML
    private GridPane cpuBoard;
    @FXML
    private Label lblCpuState;
    @FXML
    private Label lblSkillpoints;

    // Controles del juego
    @FXML
    private Label lblTurn;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSkill;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnReset;
    @FXML
    private ProgressBar gameProgress;

    // Paneles de información
    @FXML
    private VBox playerInfoPanel;
    @FXML
    private VBox cpuInfoPanel;
    @FXML
    private VBox skillsPanel;

    @FXML
    private MenuItem saveMenu;
    @FXML
    private MenuItem saveAsMenu;
    @FXML
    private MenuItem loadMenu;
    @FXML
    private MenuItem exitMenu;

    // Matrices de botones - ahora dinámicas según el tamaño del tablero
    private Button[][] playerBoardButtons;
    private Button[][] cpuBoardButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: GameViewController - Iniciando inicialización");

        try {
            // Inicializar configuración
            this.config = GameConfiguration.getInstance();

            // Inicializar matrices de botones con el tamaño correcto
            int boardSize = config.getBoardSize();
            playerBoardButtons = new Button[boardSize][boardSize];
            cpuBoardButtons = new Button[boardSize][boardSize];

            debugInitialization();

            // --- VERIFICACIÓN DEL GameController ---
            if (gameControllerStatic != null) {
                this.gameController = gameControllerStatic;
                gameControllerStatic = null;
                System.out.println("✅ GameController establecido desde variable estática");
            } else {
                // Si no hay GameController, mostrar error
                System.err.println("❌ ERROR: GameController estático es null - No se puede iniciar el juego");
                showErrorAndReturnToMainMenu("Error: No se pudo cargar el juego. Vuelve a iniciar desde el menú principal.");
                return;
            }

            // Verificar que el GameController sea válido
            if (this.gameController == null) {
                System.err.println("❌ ERROR CRÍTICO: GameController es null después de la inicialización");
                showErrorAndReturnToMainMenu("Error crítico: No se pudo inicializar el controlador del juego");
                return;
            }

            // Inicializar servicios
            storageService = new StorageService();
            soundController = SoundController.getInstance();
            animationController = AnimationController.getInstance();

            // Inicializar componentes de la UI
            System.out.println("🔄 Inicializando interfaz de usuario...");
            initializeInterface();
            configureEvents();
            configurarMenu();

            // Programar inicio del juego en el hilo de JavaFX
            Platform.runLater(() -> {
                try {
                    System.out.println("🎮 Iniciando juego en JavaFX Thread...");
                    startGameView(); // Cambié el nombre para evitar confusión

                    // Iniciar música de fondo
                    if (soundController != null) {
                        soundController.startBackgroundMusic();
                    }

                    System.out.println("✅ GameView completamente inicializada y lista");

                } catch (Exception e) {
                    System.err.println("❌ ERROR durante el inicio del juego: " + e.getMessage());
                    e.printStackTrace();
                    showErrorAndReturnToMainMenu("Error durante el inicio del juego: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ ERROR durante la inicialización: " + e.getMessage());
            e.printStackTrace();
            showErrorAndReturnToMainMenu("Error fatal durante la inicialización: " + e.getMessage());
        }
    }

    /**
     * Método específico de GameViewController para iniciar la vista de juego
     */
    private void startGameView() {
        System.out.println("🎯 Iniciando vista de juego...");

        try {
            // Verificar que tenemos un GameController válido
            if (gameController == null) {
                throw new IllegalStateException("GameController no está inicializado");
            }

            // Actualizar el estado del turno
            updateTurnStatus();

            // Actualizar los tableros
            updateBoardDisplay(playerBoardButtons, false);
            updateBoardDisplay(cpuBoardButtons, true);

            // Actualizar paneles de información
            updateInformationPanels();
            updateSkills();
            updateSkillPoints();

            // Configurar mensaje inicial
            if (lblMessage != null) {
                if (gameController.isPlayerTurn()) {
                    lblMessage.setText("¡Comienza la batalla! Tu turno.");
                } else {
                    lblMessage.setText("¡Comienza la batalla! Turno de la CPU.");
                    // Si es turno de la CPU, ejecutar su turno después de un delay
                    runCPUTurn();
                }
            }

            // Activar el juego
            activeGame = true;

            System.out.println("✅ Vista de juego iniciada correctamente");
            System.out.println("   - Turno actual: " + (gameController.isPlayerTurn() ? "Jugador" : "CPU"));
            System.out.println("   - Barcos jugador: " + gameController.getPlayerShips().size());
            System.out.println("   - Barcos CPU: " + gameController.getCpuShips().size());

        } catch (Exception e) {
            System.err.println("❌ ERROR en startGameView: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al iniciar la vista de juego", e);
        }
    }

    /**
     * Método de debug para la inicialización
     */
    private void debugInitialization() {
        System.out.println("=== DEBUG INITIALIZATION ===");
        System.out.println("GameController: " + (gameController != null ? "OK" : "NULL"));
        System.out.println("Player Board: " + (playerBoard != null ? "OK" : "NULL"));
        System.out.println("CPU Board: " + (cpuBoard != null ? "OK" : "NULL"));
        System.out.println("Board Size: " + config.getBoardSize());
        System.out.println("Player Buttons: " + (playerBoardButtons != null ? playerBoardButtons.length + "x" + playerBoardButtons[0].length : "NULL"));
        System.out.println("CPU Buttons: " + (cpuBoardButtons != null ? cpuBoardButtons.length + "x" + cpuBoardButtons[0].length : "NULL"));
        System.out.println("============================");
    }

    private void initializeInterface() {
        System.out.println("DEBUG: Entrando en initializeInterface()");

        initializeBoard(playerBoard, playerBoardButtons, false);
        initializeBoard(cpuBoard, cpuBoardButtons, true);
        updateInformationPanels();
        updateSkills();
        updateSkillPoints();
        System.out.println("DEBUG: Saliendo de initializeInterface()");
    }

    private void initializeBoard(GridPane board, Button[][] buttons, boolean isClickable) {
        System.out.println("DEBUG: Entrando en initializeBoard()");

        board.getChildren().clear();

        int boardSize = config.getBoardSize();
        int cellSize = config.getCellSize(); // Usar 40x40 desde configuración

        // Agregar labels de coordenadas
        addLabelsCoordinates(board, boardSize);

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = new Button();
                button.setPrefSize(cellSize, cellSize); // Usar tamaño de configuración
                button.getStyleClass().add("casilla-agua");

                final int x = i;
                final int y = j;

                if (isClickable) {
                    button.setOnMouseClicked(e -> handlePlayerShot(x, y));
                    button.setOnMouseEntered(e -> highlightCPUcell(x, y));
                    button.setOnMouseExited(e -> clearCPUHighlight());
                }

                // Posicionar en grid (offset por labels)
                board.add(button, j + 1, i + 1);
                buttons[i][j] = button;
            }
        }

        updateBoardDisplay(buttons, isClickable);
        System.out.println("DEBUG: Saliendo de initializeBoard()");
    }

    private void addLabelsCoordinates(GridPane board, int boardSize) {
        // Letras (A-J) en la parte superior
        for (int i = 0; i < boardSize; i++) {
            Label label = new Label(String.valueOf((char) ('A' + i)));
            label.getStyleClass().add("coordenada-label");
            board.add(label, i + 1, 0);
        }

        // Números (1-10) en el lado izquierdo
        for (int i = 0; i < boardSize; i++) {
            Label label = new Label(String.valueOf(i + 1));
            label.getStyleClass().add("coordenada-label");
            board.add(label, 0, i + 1);
        }
    }

    private void configurarMenu() {
        saveMenu.setOnAction(e -> guardarPartida());
        saveAsMenu.setOnAction(e -> guardarPartidaComo());
        loadMenu.setOnAction(e -> cargarPartida());
        exitMenu.setOnAction(e -> exitToMainMenu());
    }

    /**
     * Método estático para establecer el GameController antes de cargar la
     * vista
     *
     * @param controller
     */
    public static void setGameController(GameController controller) {
        gameControllerStatic = controller;
    }

    private void guardarPartida() {
        if (storageService.saveGameAuto(gameController)) {
            showMessage("Partida guardada automáticamente");
            soundController.playButtonClick();
        } else {
            showMessage("Error al guardar la partida");
            soundController.playError();
        }
    }

    private void guardarPartidaComo() {
        TextInputDialog dialog = new TextInputDialog("mi_partida");
        dialog.setTitle("Guardar Partida");
        dialog.setHeaderText("Guardar partida como:");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (storageService.saveGame(gameController, nombre)) {
                showMessage("Partida guardada como: " + nombre);
                soundController.playButtonClick();
            } else {
                showMessage("Error al guardar la partida");
                soundController.playError();
            }
        });
    }

    private void cargarPartida() {
        try {
            // Mostrar diálogo de selección de partidas guardadas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GuardadosView.fxml"));
            Parent root = loader.load();

            SavedViewController controller = loader.getController();
            controller.setSaveGameListener(new SavedViewController.SaveGameListener() {
                @Override
                public void onPartidaCargada(String nombreArchivo) {
                    cargarPartidaSeleccionada(nombreArchivo);
                }

                @Override
                public void onDialogoCerrado() {
                    System.out.println("Diálogo de carga cerrado");
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Cargar Partida Guardada");
            stage.setScene(new Scene(root, 800, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(App.getPrimaryStage());
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error al cargar el diálogo de partidas guardadas");
            soundController.playError();
        }
    }

    private void cargarPartidaSeleccionada(String nombreArchivo) {
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Cargar Partida");
            confirmacion.setHeaderText("¿Cargar partida: " + nombreArchivo + "?");
            confirmacion.setContentText("El progreso actual se perderá si no está guardado.");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    realizarCargaPartida(nombreArchivo);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error al preparar la carga de partida");
            soundController.playError();
        }
    }

    private void realizarCargaPartida(String nombreArchivo) {
        try {
            GameController nuevoGameController = storageService.loadGame(nombreArchivo);

            if (nuevoGameController != null) {
                this.gameController = nuevoGameController;
                resetInterface();
                showMessage("Partida cargada exitosamente: " + nombreArchivo);
                soundController.playButtonClick();
            } else {
                showMessage("Error: No se pudo cargar la partida seleccionada");
                soundController.playError();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error crítico al cargar la partida: " + ex.getMessage());
            soundController.playError();
        }
    }

    private void resetInterface() {
        activeGame = false;

        // Actualizar ambos tableros
        updateBoardDisplay(playerBoardButtons, false);
        updateBoardDisplay(cpuBoardButtons, true);

        // Actualizar paneles de información
        updateInformationPanels();

        // Actualizar habilidades
        updateSkills();
        updateSkillPoints();

        // Actualizar estado del turno
        updateTurnStatus();

        // Actualizar mensaje según el estado del juego
        GamePhase gamePhase = gameController.getGamePhase();
        if (gamePhase == GamePhase.PLAYER_WIN || gamePhase == GamePhase.CPU_WIN) {
            if (gameController.playerWin()) {
                showMessage("¡Partida cargada - Victoria previa!");
            } else {
                showMessage("¡Partida cargada - Derrota previa!");
            }
        } else {
            if (gameController.isPlayerTurn()) {
                showMessage("Partida cargada - Tu turno");
                disableCPUboard(false);
            } else {
                showMessage("Partida cargada - Turno de la CPU");
                disableCPUboard(true);
                runCPUTurn();
            }
        }

        // Reactivar el juego si no ha terminado
        activeGame = !gameController.isGameOver();
    }

    private void updateSkillPoints() {
        if (lblSkillpoints != null) {
            int puntos = gameController.getPlayerSkills().getSkillPoints();
            lblSkillpoints.setText("Puntos Habilidad: " + puntos);
        }
    }

    private void showErrorAndReturnToMainMenu(String errorMessage) {
        System.err.println("💥 ERROR GRAVE: " + errorMessage);

        // Mostrar alerta de error
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Inicialización");
                alert.setHeaderText("No se pudo cargar el juego");
                alert.setContentText(errorMessage + "\n\nLa aplicación volverá al menú principal.");

                // Mostrar mensaje de error en la UI
                showErrorMessage("Error crítico: " + errorMessage);

                // Esperar a que el usuario confirme
                alert.showAndWait();

                // Detener música si está reproduciéndose
                if (soundController != null) {
                    soundController.stopBackgroundMusic();
                }

                // Volver al menú principal
                App.changeView("view/MainView");

            } catch (Exception ex) {
                System.err.println("❌ ERROR al mostrar diálogo de error: " + ex.getMessage());
                ex.printStackTrace();
                // Fallback directo al menú principal
                try {
                    App.changeView("view/MainView");
                } catch (Exception fatalEx) {
                    System.err.println("💥 ERROR FATAL: No se pudo volver al menú principal");
                    fatalEx.printStackTrace();
                }
            }
        });
    }

    private void exitToMainMenu() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Salir al Menú Principal");
        confirmacion.setHeaderText("¿Salir al menú principal?");
        confirmacion.setContentText("El progreso no guardado se perderá.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    soundController.stopBackgroundMusic();
                    App.changeView("com/cenit/battleship/view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void configureEvents() {
        btnPause.setOnAction(e -> pauseGame());
        btnReset.setOnAction(e -> resetGame());
        btnSkill.setOnAction(e -> useSkill());
    }

    /**
     * Crea un overlay de barco específico para el tablero
     */
    /**
     * Crea un overlay de barco específico para el tablero - VERSIÓN CORREGIDA
     */
    private StackPane createShipOverlayForBoard(Ship ship, double cellWidth, double cellHeight) {
        StackPane overlay = new StackPane();

        if (ship == null || ship.getType() == null) {
            System.err.println("ERROR: Ship o ship type es null");
            return overlay;
        }

        int shipSize = ship.getType().getSize();
        boolean isVertical = (ship.getDirection() == Direction.VERTICAL);

        double width, height;

        // ✅ CORRECCIÓN: Dimensiones lógicas correctas
        if (isVertical) {
            // VERTICAL: ancho = 1 celda, alto = tamaño * celda
            width = cellWidth;                    // 1 celda de ancho
            height = cellHeight * shipSize;       // tamaño * altura de celda
        } else {
            // HORIZONTAL: ancho = tamaño * celda, alto = 1 celda  
            width = cellWidth * shipSize;         // tamaño * ancho de celda
            height = cellHeight;                  // 1 celda de alto
        }

        overlay.setPrefSize(width, height);
        overlay.setMinSize(width, height);
        overlay.setMaxSize(width, height);

        try {
            // ✅ CORRECCIÓN: Usar dimensiones CORRECTAS para el renderizado
            ImageView shipImage = renderShipCorrected(ship, width, height, isVertical);
            overlay.getChildren().add(shipImage);

            System.out.println("✅ Overlay creado: " + ship.getType()
                    + " | Dirección: " + ship.getDirection()
                    + " | Tamaño overlay: " + width + "x" + height);
        } catch (Exception e) {
            System.err.println("ERROR al crear imagen del barco: " + e.getMessage());
            e.printStackTrace();
        }

        return overlay;
    }

    private void handlePlayerShot(int x, int y) {
        if (!activeGame || !gameController.isPlayerTurn() || gameController.isGameOver()) {
            return;
        }

        Coordinate coord = new Coordinate(x, y);

        try {
            // Verificar si ya se disparó aquí
            if (!gameController.getCpuBoard().canShootAt(coord)) {
                showMessage("¡Ya disparaste en esta posición!");
                soundController.playError();
                return;
            }

            ShotResult result = gameController.processPlayerShot(coord);
            updateFiredCell(cpuBoardButtons[x][y], result);
            showMessage(result.getMessage());

            // Sonidos y animaciones
            if (result.isHit()) {
                soundController.playExplosion();
                if (animationController != null) {
                    animationController.playExplosionAnimation(coord, result);
                }
            } else {
                soundController.playWaterSplash();
                if (animationController != null) {
                    animationController.playWaterSplashAnimation(coord, result);
                }
            }

            if (result.isSunk()) {
                Ship sunkShip = gameController.getLastSunkShipCPU();
                if (sunkShip != null) {
                    showSpecialMessage("¡HUNDISTE UN " + sunkShip.getType().getName() + "!");
                    soundController.playShipSinking();
                    if (animationController != null) {
                        animationController.playSinkingAnimation(sunkShip);
                    }
                }
            }

            // Actualizar interfaz
            updateInformationPanels();
            updateSkillPoints();

            if (gameController.isGameOver()) {
                endGame(true);
                return;
            }

            // Cambiar turno si no impactó
            if (!result.allowsAnotherTurn()) {
                gameController.setPlayerTurn(false);
                updateTurnStatus();
                runCPUTurn();
            }

        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
            soundController.playError();
        }
    }

    private void runCPUTurn() {
        if (!activeGame || gameController.isGameOver()) {
            return;
        }

        lblMessage.setText("Turno de la CPU...");
        disableCPUboard(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            try {
                ShotResult result = gameController.processCPUShot();
                Coordinate lastShot = gameController.getLastShotCPU();

                if (lastShot != null) {
                    updateFiredCell(playerBoardButtons[lastShot.getX()][lastShot.getY()], result);
                }

                showMessage("CPU: " + result.getMessage());

                // Sonidos y animaciones
                if (result.isHit()) {
                    soundController.playExplosion();
                    if (animationController != null && lastShot != null) {
                        animationController.playExplosionAnimation(lastShot, result);
                    }
                } else {
                    soundController.playWaterSplash();
                    if (animationController != null && lastShot != null) {
                        animationController.playWaterSplashAnimation(lastShot, result);
                    }
                }

                if (result.isSunk()) {
                    Ship sunkShip = gameController.getLastSunkShipPlayer();
                    if (sunkShip != null) {
                        showSpecialMessage("¡LA CPU HUNDIÓ TU " + sunkShip.getType().getName() + "!");
                        soundController.playShipSinking();
                        if (animationController != null) {
                            animationController.playSinkingAnimation(sunkShip);
                        }
                    }
                }

                // Actualizar interfaz
                updateInformationPanels();

                if (gameController.isGameOver()) {
                    endGame(false);
                } else if (!result.allowsAnotherTurn()) {
                    gameController.setPlayerTurn(true);
                    updateTurnStatus();
                    disableCPUboard(false);
                } else {
                    // CPU sigue disparando
                    runCPUTurn();
                }
            } catch (Exception ex) {
                showMessage("Error en turno de CPU: " + ex.getMessage());
                soundController.playError();
            }
        });
        pause.play();
    }

    private void updateFiredCell(Button button, ShotResult result) {
        button.getStyleClass().removeAll("casilla-agua", "casilla-resaltada");

        if (result.isHit()) {
            button.getStyleClass().add("casilla-impacto");
            button.setText("💥");
        } else {
            button.getStyleClass().add("casilla-fallo");
            button.setText("●");
        }

        button.setDisable(true);
    }

    private void updateBoardDisplay(Button[][] buttons, boolean isCPUBoard) {
        Board board = isCPUBoard ? gameController.getCpuBoard() : gameController.getPlayerBoard();
        boolean showShips = !isCPUBoard; // Mostrar barcos solo en tablero del jugador
        int boardSize = config.getBoardSize();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Coordinate coord = new Coordinate(i, j);
                Cell cell = board.getCell(coord);
                Button button = buttons[i][j];

                if (cell == null) {
                    continue;
                }

                // Limpiar todas las clases de estilo
                button.getStyleClass().removeAll(
                        "casilla-agua", "casilla-barco",
                        "casilla-impacto", "casilla-fallo", "casilla-hundido"
                );

                // Resetear texto y gráfico
                button.setText("");
                button.setGraphic(null);

                // Obtener el estado actual de la celda
                CellState state = cell.getState();

                // Aplicar estilos según el estado
                switch (state) {
                    case WATER:
                        button.getStyleClass().add("casilla-agua");
                        if (showShips && cell.hasShip()) {
                            button.getStyleClass().add("casilla-barco");
                            // Opcional: agregar gráfico del barco si está en la primera casilla
                            if (cell.hasShip() && isFirstSegment(cell.getShip(), coord)) {
                                StackPane shipOverlay = createShipOverlayForBoard(cell.getShip(), button.getPrefWidth(), button.getPrefHeight());
                                button.setGraphic(shipOverlay);
                            }
                        }
                        break;

                    case SHIP:
                        if (showShips) {
                            button.getStyleClass().add("casilla-barco");
                            // Gráfico del barco para la primera casilla
                            if (cell.hasShip() && isFirstSegment(cell.getShip(), coord)) {
                                StackPane shipOverlay = createShipOverlayForBoard(cell.getShip(), button.getPrefWidth(), button.getPrefHeight());
                                button.setGraphic(shipOverlay);
                            }
                        } else {
                            button.getStyleClass().add("casilla-agua");
                        }
                        break;

                    case IMPACT:
                        button.getStyleClass().add("casilla-impacto");
                        button.setText("💥");
                        break;

                    case MISS:
                        button.getStyleClass().add("casilla-fallo");
                        button.setText("●");
                        break;

                    case SUNK_SHIP:
                        button.getStyleClass().add("casilla-hundido");
                        button.setText("💀");
                        // También mostrar el barco hundido completo
                        if (cell.hasShip() && isFirstSegment(cell.getShip(), coord)) {
                            StackPane shipOverlay = createShipOverlayForBoard(cell.getShip(), button.getPrefWidth(), button.getPrefHeight());
                            button.setGraphic(shipOverlay);
                        }
                        break;
                }

                // Configurar disponibilidad del botón
                if (isCPUBoard) {
                    // En tablero de CPU: solo habilitado si es turno del jugador y la celda no ha sido disparada
                    boolean isEnabled = activeGame && gameController.isPlayerTurn() && !cell.hasBeenShot();
                    button.setDisable(!isEnabled);
                } else {
                    // En tablero del jugador: siempre deshabilitado (solo para visualización)
                    button.setDisable(true);
                }

                // Tooltip informativo
                setupCellTooltip(button, cell, showShips);
            }
        }
    }

    /**
     * Verifica si esta coordenada es el primer segmento del barco
     */
    private boolean isFirstSegment(Ship ship, Coordinate coord) {
        if (ship == null || ship.getSegments().isEmpty()) {
            return false;
        }
        return ship.getSegments().get(0).equals(coord);
    }

    /**
     * Tooltip mejorado - no revela información del enemigo
     */
    private void setupCellTooltip(Button button, Cell cell, boolean showShips) {
        String tooltipText = getCellTooltipText(cell, showShips);
        if (!tooltipText.isEmpty()) {
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setShowDelay(javafx.util.Duration.millis(500));
            Tooltip.install(button, tooltip);
        }
    }

    private String getCellTooltipText(Cell cell, boolean showShips) {
        StringBuilder sb = new StringBuilder();

        // Coordenada siempre visible
        if (cell.getCoordinate() != null) {
            sb.append("Coordenada: ").append(cell.getCoordinate().aNotacion());
        }

        // Información adicional según el estado y visibilidad
        if (cell.hasBeenShot()) {
            sb.append("\nEstado: ");
            if (cell.isHit()) {
                sb.append("Impacto");
                if (cell.hasShip() && showShips) {
                    Ship ship = cell.getShip();
                    sb.append(" - ").append(ship.getType().getName());
                    if (ship.isSunk()) {
                        sb.append(" (HUNDIDO)");
                    } else {
                        sb.append(" (").append(ship.getIntactPartsCount())
                                .append("/").append(ship.getSize()).append(" intactos)");
                    }
                }
            } else {
                sb.append("Disparo fallido");
            }
        } else if (showShips && cell.hasShip()) {
            // Solo mostrar información de barcos en tablero propio
            sb.append("\nBarco: ").append(cell.getShip().getType().getName());
        }

        return sb.toString();
    }

    private void highlightCPUcell(int x, int y) {
        if (!gameController.isPlayerTurn() || !activeGame) {
            return;
        }

        Button button = cpuBoardButtons[x][y];
        if (!button.isDisable()) {
            button.getStyleClass().add("casilla-resaltada");
        }
    }

    private void clearCPUHighlight() {
        int boardSize = config.getBoardSize();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = cpuBoardButtons[i][j];
                button.getStyleClass().remove("casilla-resaltada");
            }
        }
    }

    private void disableCPUboard(boolean disable) {
        int boardSize = config.getBoardSize();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cpuBoardButtons[i][j].setDisable(disable || !activeGame || !gameController.isPlayerTurn());
            }
        }
    }

    private void updateTurnStatus() {
        if (gameController.isPlayerTurn()) {
            lblTurn.setText("TU TURNO");
            lblTurn.getStyleClass().removeAll("turno-cpu", "turno-jugador");
            lblTurn.getStyleClass().add("turno-jugador");
        } else {
            lblTurn.setText("TURNO CPU");
            lblTurn.getStyleClass().removeAll("turno-cpu", "turno-jugador");
            lblTurn.getStyleClass().add("turno-cpu");
        }
    }

    private void updateInformationPanels() {
        // Panel del jugador
        updateFleetPanel(playerInfoPanel, gameController.getPlayerShips(), "Tus Barcos");

        // Panel de la CPU  
        updateFleetPanel(cpuInfoPanel, gameController.getCpuShips(), "Barcos Enemigos");

        // Actualizar progreso del juego
        updateGameProgress();
    }

    private void updateFleetPanel(VBox panel, List<Ship> ships, String title) {
        panel.getChildren().clear();

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("subtitle-label");
        panel.getChildren().add(lblTitle);

        for (Ship ship : ships) {
            HBox infoShip = new HBox(10);
            infoShip.getStyleClass().add("barco-info");

            Label name = new Label(ship.getType().getName());
            ProgressBar health = new ProgressBar();
            health.setProgress(ship.getIntegrityPercentage());

            // Color según estado
            if (ship.isSunk()) {
                health.getStyleClass().add("barco-hundido");
                name.getStyleClass().add("barco-hundido");
                name.setText(ship.getType().getName() + " 💀");
            } else if (ship.getImpactsReceived() > 0) {
                health.getStyleClass().add("barco-danado");
                name.setText(ship.getType().getName() + " (" + ship.getImpactsReceived() + "/" + ship.getSize() + ")");
            } else {
                name.setText(ship.getType().getName() + " (" + ship.getSize() + ")");
            }

            infoShip.getChildren().addAll(name, health);
            panel.getChildren().add(infoShip);
        }
    }

    private void updateGameProgress() {
        if (gameProgress != null) {
            int totalShips = gameController.getPlayerShips().size() + gameController.getCpuShips().size();
            int sunkShips = (int) (gameController.getPlayerShips().stream().filter(Ship::isSunk).count()
                    + gameController.getCpuShips().stream().filter(Ship::isSunk).count());

            double progress = totalShips > 0 ? (double) sunkShips / totalShips : 0.0;
            gameProgress.setProgress(progress);
        }
    }

    private void updateSkills() {
        skillsPanel.getChildren().clear();

        Label lblTitle = new Label("Habilidades");
        lblTitle.getStyleClass().add("subtitle-label");
        skillsPanel.getChildren().add(lblTitle);

        // Ejemplo de habilidades básicas
        Button btnSonar = new Button("Sonar (3 puntos)");
        btnSonar.setOnAction(e -> activarSonar());
        btnSonar.setDisable(!gameController.getPlayerSkills().canUseSkill(com.cenit.battleship.model.Skill.SONAR));

        Button btnRadar = new Button("Radar (5 puntos)");
        btnRadar.setOnAction(e -> activarRadar());
        btnRadar.setDisable(!gameController.getPlayerSkills().canUseSkill(com.cenit.battleship.model.Skill.RADAR));

        skillsPanel.getChildren().addAll(btnSonar, btnRadar);
    }

    private void activarSonar() {
        showMessage("Habilidad Sonar activada! Selecciona una posición para escanear.");
        // TODO: Implementar lógica de selección de coordenada para sonar
    }

    private void activarRadar() {
        showMessage("Habilidad Radar activada! Escaneando barcos enemigos...");
        // TODO: Implementar lógica de radar
    }

    private void useSkill() {
        showMessage("Selecciona una habilidad del panel lateral");
    }

    private void pauseGame() {
        activeGame = !activeGame;
        disableCPUboard(!activeGame);

        if (!activeGame) {
            lblMessage.setText("JUEGO EN PAUSA");
            btnPause.setText("Reanudar");
            soundController.pauseBackgroundMusic();
        } else {
            updateTurnStatus();
            btnPause.setText("Pausa");
            soundController.startBackgroundMusic();
        }
    }

    private void resetGame() {
        try {
            soundController.stopBackgroundMusic();
            App.changeView("/com/cenit/battleship/view/MainView.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void endGame(boolean playerWin) {
        activeGame = false;
        disableCPUboard(true);

        if (playerWin) {
            showSpecialMessage("¡VICTORIA! Has hundido toda la flota enemiga.");
            soundController.playVictory();
        } else {
            showSpecialMessage("¡DERROTA! La CPU ha hundido todos tus barcos.");
            soundController.playDefeat();
        }

        showDialogEndGame(playerWin);
    }

    /**
     * Muestra un mensaje en la interfaz de usuario
     *
     * @param message El mensaje a mostrar
     */
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
                System.out.println("📢 GameView: " + message);

            } catch (Exception e) {
                System.err.println("❌ Error al mostrar mensaje: " + e.getMessage());
                // Fallback: imprimir en consola
                System.out.println("📢 [FALLBACK] " + message);
            }
        });
    }

    /**
     * Muestra un mensaje temporal que se desvanece después de un tiempo
     *
     * @param message El mensaje a mostrar
     * @param durationSeconds Duración en segundos
     */
    private void showTemporaryMessage(String message, int durationSeconds) {
        showMessage(message);

        PauseTransition pause = new PauseTransition(Duration.seconds(durationSeconds));
        pause.setOnFinished(e -> {
            // Restaurar mensaje normal después del tiempo
            if (lblMessage != null && lblMessage.getText().equals(message)) {
                lblMessage.setText("¡Buena suerte, Almirante!");
                lblMessage.getStyleClass().removeAll("mensaje-error", "mensaje-especial", "mensaje-info");
                lblMessage.getStyleClass().add("mensaje-normal");
            }
        });
        pause.play();
    }

    /**
     * Muestra un mensaje de error específico
     *
     * @param errorMessage El mensaje de error
     */
    private void showErrorMessage(String errorMessage) {
        showMessage("❌ " + errorMessage);
    }

    /**
     * Muestra un mensaje de éxito
     *
     * @param successMessage El mensaje de éxito
     */
    private void showSuccessMessage(String successMessage) {
        showMessage("✅ " + successMessage);
    }

    /**
     * Muestra un mensaje informativo
     *
     * @param infoMessage El mensaje informativo
     */
    private void showInfoMessage(String infoMessage) {
        showMessage("ℹ️ " + infoMessage);
    }

    private void showDialogEndGame(boolean victoria) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin del Juego");
        alert.setHeaderText(victoria ? "¡Felicidades! Has Ganado" : "Juego Terminado");
        alert.setContentText(victoria
                ? "Has demostrado ser un excelente almirante."
                : "La flota enemiga ha prevalecido. Mejor suerte la próxima vez.");

        ButtonType btnRevancha = new ButtonType("Revancha");
        ButtonType btnMenu = new ButtonType("Menú Principal");

        alert.getButtonTypes().setAll(btnRevancha, btnMenu);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == btnRevancha) {
                resetGame();
            } else {
                try {
                    soundController.stopBackgroundMusic();
                    App.changeView("view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showSpecialMessage(String mensaje) {
        lblMessage.getStyleClass().add("mensaje-especial");
        lblMessage.setText(mensaje);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblMessage.getStyleClass().remove("mensaje-especial"));
        pause.play();
    }

}
