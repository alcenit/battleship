package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.AnimationController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.controller.SoundController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.enums.ShotResult;
import com.cenit.battleship.services.StorageService;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    private boolean activeGame = true;

    // Tablero del jugador
    @FXML
    private GridPane playerBoard;
    @FXML
    private Label lblPlayerState;

    // Tablero de la CPU (para disparar)
    @FXML
    private GridPane CPUBoard;
    @FXML
    private Label lblCPUState;
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
    private VBox CPUInfoPanel;
    @FXML
    private VBox panelSkills;

    @FXML
    private MenuItem menuGuardar;
    @FXML
    private MenuItem menuGuardarComo;
    @FXML
    private MenuItem menuCargar;
    @FXML
    private MenuItem menuSalir;

    // Matrices de botones
    private Button[][] playerButtons = new Button[10][10];
    private Button[][] CPUButtons = new Button[10][10];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Si tenemos un GameController estático (desde carga), usarlo
        if (gameControllerStatic != null) {
            this.gameController = gameControllerStatic;
            gameControllerStatic = null; // Limpiar para futuras uses
        } else {
            // Si no, crear uno nuevo (juego normal)
            this.gameController = new GameController();
        }

        storageService = new StorageService();
        soundController = SoundController.getInstance();
        animationController = AnimationController.getInstance();

        initializeInterface();
        configureEvents();
        configurarMenu();
        startGame();

        soundController.startBackgroundMusic();
    }

    private void initializeInterface() {
        initializeBoard(playerBoard, playerButtons, false);
        initializeBoard(CPUBoard, CPUButtons, true);
        updateInformationPanels();
        updateSkills();
        updateSkillPoints();
    }

    private void initializeBoard(GridPane board, Button[][] buttons, boolean isClickable) {
        board.getChildren().clear();

        // Agregar labels de coordenadas
        addLabelsCoordinates(board);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = new Button();
                button.setPrefSize(35, 35);
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
    }

    private void configurarMenu() {
        menuGuardar.setOnAction(e -> guardarPartida());
        menuGuardarComo.setOnAction(e -> guardarPartidaComo());
        menuCargar.setOnAction(e -> cargarPartida());
        menuSalir.setOnAction(e -> exitToMainMenu());
    }

    /**
     * Método estático para establecer el GameController antes de cargar la vista
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
        updateBoardDisplay(playerButtons, false);
        updateBoardDisplay(CPUButtons, true);

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

    private void exitToMainMenu() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Salir al Menú Principal");
        confirmacion.setHeaderText("¿Salir al menú principal?");
        confirmacion.setContentText("El progreso no guardado se perderá.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    soundController.stopBackgroundMusic();
                    App.changeView("view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showMessage(String mensaje) {
        if (lblMessage != null) {
            lblMessage.setText(mensaje);
        }
        System.out.println("GameViewController: " + mensaje);
    }

    private void addLabelsCoordinates(GridPane board) {
        // Letras (A-J) en la parte superior
        for (int i = 0; i < 10; i++) {
            Label label = new Label(String.valueOf((char) ('A' + i)));
            label.getStyleClass().add("coordenada-label");
            board.add(label, i + 1, 0);
        }

        // Números (1-10) en el lado izquierdo
        for (int i = 0; i < 10; i++) {
            Label label = new Label(String.valueOf(i + 1));
            label.getStyleClass().add("coordenada-label");
            board.add(label, 0, i + 1);
        }
    }

    private void configureEvents() {
        btnPause.setOnAction(e -> pauseGame());
        btnReset.setOnAction(e -> resetGame());
        btnSkill.setOnAction(e -> useSkill());
    }

    private void startGame() {
        activeGame = true;
        updateTurnStatus();
        lblMessage.setText("¡Comienza la batalla! Tu turno.");
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
            updateFiredCell(CPUButtons[x][y], result);
            showMessage(result.getMessage());

            // Sonidos y animaciones
            if (result.isImpact()) {
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
                    updateFiredCell(playerButtons[lastShot.getX()][lastShot.getY()], result);
                }
                
                showMessage("CPU: " + result.getMessage());

                // Sonidos y animaciones
                if (result.isImpact()) {
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

        if (result.isImpact()) {
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

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Coordinate coord = new Coordinate(i, j);
                Cell cell = board.getCell(coord);
                Button button = buttons[i][j];

                button.getStyleClass().removeAll(
                    "casilla-agua", "casilla-barco",
                    "casilla-impacto", "casilla-fallo"
                );

                // Resetear texto
                button.setText("");

                if (cell.isRevealed()) {
                    if (cell.hasShip()) {
                        button.getStyleClass().add("casilla-impacto");
                        button.setText("💥");
                    } else {
                        button.getStyleClass().add("casilla-fallo");
                        button.setText("●");
                    }
                    button.setDisable(true);
                } else {
                    button.getStyleClass().add("casilla-agua");
                    if (showShips && cell.hasShip()) {
                        button.getStyleClass().add("casilla-barco");
                    }
                    button.setDisable(!isCPUBoard || !activeGame || !gameController.isPlayerTurn());
                }
            }
        }
    }

    private void highlightCPUcell(int x, int y) {
        if (!gameController.isPlayerTurn() || !activeGame) {
            return;
        }

        Button button = CPUButtons[x][y];
        if (!button.isDisable()) {
            button.getStyleClass().add("casilla-resaltada");
        }
    }

    private void clearCPUHighlight() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = CPUButtons[i][j];
                button.getStyleClass().remove("casilla-resaltada");
            }
        }
    }

    private void disableCPUboard(boolean disable) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                CPUButtons[i][j].setDisable(disable || !activeGame || !gameController.isPlayerTurn());
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
        updateFleetPanel(CPUInfoPanel, gameController.getCPUShips(), "Barcos Enemigos");
        
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
            int totalShips = gameController.getPlayerShips().size() + gameController.getCPUShips().size();
            int sunkShips = (int) (gameController.getPlayerShips().stream().filter(Ship::isSunk).count() + 
                                 gameController.getCPUShips().stream().filter(Ship::isSunk).count());
            
            double progress = totalShips > 0 ? (double) sunkShips / totalShips : 0.0;
            gameProgress.setProgress(progress);
        }
    }

    private void updateSkills() {
        panelSkills.getChildren().clear();

        Label lblTitle = new Label("Habilidades");
        lblTitle.getStyleClass().add("subtitle-label");
        panelSkills.getChildren().add(lblTitle);

        // Ejemplo de habilidades básicas
        Button btnSonar = new Button("Sonar (3 puntos)");
        btnSonar.setOnAction(e -> activarSonar());
        btnSonar.setDisable(!gameController.getPlayerSkills().canUseSkill(com.cenit.battleship.model.Skill.SONAR));
        
        Button btnRadar = new Button("Radar (5 puntos)");
        btnRadar.setOnAction(e -> activarRadar());
        btnRadar.setDisable(!gameController.getPlayerSkills().canUseSkill(com.cenit.battleship.model.Skill.RADAR));

        panelSkills.getChildren().addAll(btnSonar, btnRadar);
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
            App.changeView("view/MainView");
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
