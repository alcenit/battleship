/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.AnimationController;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.controller.SoundController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.ShotResult;
import com.cenit.battleship.sevices.StorageService;
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
        
        soundController.startMusicBackground();

     
        
        
    }
   

    private void initializeInterface() {
        initializeBoard(playerBoard, playerButtons, false);
        initializeBoard(CPUBoard, CPUButtons, true);
        updateInformationPanels();
        updateSkills();
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
        
        updateBoardDisplay(buttons, isClickable ? 
            gameController.getCPUBoard() : gameController.getPlayerBoard());
    }
    
    
    private void configurarMenu() {
        menuGuardar.setOnAction(e -> guardarPartida());
        menuGuardarComo.setOnAction(e -> guardarPartidaComo());
        menuCargar.setOnAction(e -> cargarPartida());
        menuSalir.setOnAction(e -> exitToMainMenu());
    }

/**
     * Método estático para establecer el GameController antes de cargar la vista
     * Esto se usa cuando cargamos una partida desde el menú principal
     */
public static void setGameController(GameController controller) {
        gameControllerStatic = controller;
    }

    private void guardarPartida() {
        if (storageService.guardarPartidaAutomatico(gameController)) {
            showMessage("Partida guardada automáticamente");
        } else {
            showMessage("Error al guardar la partida");
        }
    }

    private void guardarPartidaComo() {
        TextInputDialog dialog = new TextInputDialog("mi_partida");
        dialog.setTitle("Guardar Partida");
        dialog.setHeaderText("Guardar partida como:");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (storageService.guardarPartida(gameController, nombre)) {
                showMessage("Partida guardada como: " + nombre);
            } else {
                showMessage("Error al guardar la partida");
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
                    // Cargar la partida seleccionada
                    cargarPartidaSeleccionada(nombreArchivo);
                }
                
                @Override
                public void onDialogoCerrado() {
                    // El usuario cerró el diálogo sin seleccionar
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
        }
    }

    private void cargarPartidaSeleccionada(String nombreArchivo) {
        try {
            // Mostrar confirmación antes de cargar
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
        }
    }

    private void realizarCargaPartida(String nombreArchivo) {
        try {
            // Cargar la partida desde el archivo
            GameController nuevoGameController = storageService.loadGame(nombreArchivo);
            
            if (nuevoGameController != null) {
                // Reemplazar el controlador actual
                this.gameController = nuevoGameController;
                
                // Reinicializar la interfaz con el nuevo estado
                resetInterface();
                
                showMessage("Partida cargada exitosamente: " + nombreArchivo);
                soundController.playClickBoton();
                
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
        // Detener cualquier animación en curso
        activeGame = false;
        
        // Actualizar ambos tableros
        updateBoardDisplay(playerButtons, gameController.getPlayerBoard());
        updateBoardDisplay(CPUButtons, gameController.getCPUBoard());
        
        // Actualizar paneles de información
        updateInformationPanels();
        
        // Actualizar habilidades
        updateSkills();
        
        // Actualizar estado del turno
        updateTurnStatus();
        
        // Actualizar mensaje según el estado del juego
        if (gameController.isGameFinished()) {
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
                // Opcional: ejecutar turno de la CPU automáticamente
                runCPUTurn();
            }
        }
        
        // Reactivar el juego si no ha terminado
        activeGame = !gameController.isGameFinished();
        
        // Actualizar puntos de habilidad en la UI
        updateSkillPoints();
    }

    private void updateSkillPoints() {
        int puntos = gameController.getPlayerSkills().getSkillPoints();
        // Asumiendo que tienes un Label para mostrar puntos
        if (lblSkillpoints != null) {
            lblSkillpoints.setText("Puntos: " + puntos);
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
                    App.changeView("view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // Método auxiliar para mostrar mensajes
    private void showMessage(String mensaje) {
        if (lblMessage != null) {
            lblMessage.setText(mensaje);
        }
        System.out.println("JuegoViewController: " + mensaje);
    }

    // ... resto de métodos existentes ...

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
        if (!activeGame || !gameController.isPlayerTurn()) return;
        
        Coordinate coord = new Coordinate(x, y);
        
        try {
            ShotResult result = gameController.playerShoots(coord);
            updateFiredCell(CPUButtons[x][y], result);
            showMessage(result.message());
            
            if (result.sunk()) {
                ShowSpecialMessage("¡Hundiste un " + 
                    gameController.getLastSunkShipCPU().getType().getName() + "!");
            }
            
            if (gameController.isGameFinished()) {
                endGame(true);
                return;
            }
            
            // Cambiar turno a CPU
            if (!result.impact()) {
                gameController.setPlayerTurn(false);
                updateTurnStatus();
                runCPUTurn();
            }
            
        } catch (IllegalStateException e) {
            showMessage("Casilla ya disparada");
        }
    }

    private void runCPUTurn() {
        if (!activeGame) return;
        
        lblMessage.setText("Turno de la CPU...");
        disableCPUboard(true);
        
        // Pausa para dar sensación de pensamiento
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            ShotResult result = gameController.CPUShoots();
            updateCPUShotCell(result);
            showMessage("CPU: " + result.message());
            
            if (result.sunk()) {
                ShowSpecialMessage("¡La CPU hundió tu " + 
                    gameController.getLastSunkShipPlayer().getType().getName() + "!");
            }
            
            if (gameController.isGameFinished()) {
                endGame(false);
            } else if (!result.impact()) {
                gameController.setPlayerTurn(true);
                updateTurnStatus();
                disableCPUboard(false);
            } else {
                // CPU sigue disparando
                runCPUTurn();
            }
        });
        pause.play();
    }

    private void updateFiredCell(Button button, ShotResult result) {
        button.getStyleClass().removeAll("casilla-agua", "casilla-resaltada");
        
        if (result.impact()) {
            button.getStyleClass().add("casilla-impacto");
            button.setText("💥");
        } else {
            button.getStyleClass().add("casilla-fallo");
            button.setText("●");
        }
        
        button.setDisable(true);
    }

    
    private void updateCPUShotCell(ShotResult result) {
        Coordinate lastShot = gameController.getLastShotCPU();
        Button boton = playerButtons[lastShot.x()][lastShot.y()];
        updateFiredCell(boton, result);
    }

    private void updateBoardDisplay(Button[][] buttons, Board board) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = board.getCell(i, j);
                Button button = buttons[i][j];
                
                button.getStyleClass().removeAll(
                    "casilla-agua", "casilla-barco", 
                    "casilla-impacto", "casilla-fallo"
                );
                
                switch (cell.getState()) {
                    case WATER:
                        button.getStyleClass().add("casilla-agua");
                        break;
                    case SHIP:
                        button.getStyleClass().add("casilla-barco");
                        break;
                    case IMPACT:
                        button.getStyleClass().add("casilla-impacto");
                        button.setText("💥");
                        break;
                    case FAIL:
                        button.getStyleClass().add("casilla-fallo");
                        button.setText("●");
                        break;
                }
            }
        }
    }

    private void highlightCPUcell(int x, int y) {
        if (!gameController.isPlayerTurn()) return;
        
        Button button = CPUButtons[x][y];
        if (!button.isDisable()) {
            button.getStyleClass().add("casilla-resaltada");
        }
    }

    private void clearCPUHighlight() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = CPUButtons[i][j];
                if (!button.isDisable()) {
                    button.getStyleClass().remove("casilla-resaltada");
                }
            }
        }
    }

    private void disableCPUboard(boolean disable) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                CPUButtons[i][j].setDisable(disable);
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
            health.setProgress((double) (ship.getType().getSize() - ship.getImpactsRecieved()) / ship.getType().getSize());
            
            // Color según estado
            if (ship.isSunk()) {
                health.getStyleClass().add("barco-hundido");
                name.getStyleClass().add("barco-hundido");
            } else if (ship.getImpactsRecieved() > 0) {
                health.getStyleClass().add("barco-danado");
            }
            
            infoShip.getChildren().addAll(name, health);
            panel.getChildren().add(infoShip);
        }
    }

    private void updateSkills() {
        // TODO: Implementar sistema de habilidades
        panelSkills.getChildren().clear();
        
        Label lblTitle = new Label("Habilidades");
        lblTitle.getStyleClass().add("subtitle-label");
        panelSkills.getChildren().add(lblTitle);
        
        // Ejemplo de habilidad
        Button btnSonar = new Button("Sonar (2)");
        btnSonar.setOnAction(e -> activarSonar());
        panelSkills.getChildren().add(btnSonar);
    }

    private void activarSonar() {
        // TODO: Implementar habilidad sonar
        showMessage("Habilidad Sonar activada!");
    }

    private void useSkill() {
        // TODO: Implementar diálogo de habilidades
        showMessage("Selecciona una habilidad del panel");
    }

    private void pauseGame() {
        activeGame = !activeGame;
        disableCPUboard(!activeGame);
        
        if (!activeGame) {
            lblMessage.setText("JUEGO EN PAUSA");
            btnPause.setText("Reanudar");
        } else {
            updateTurnStatus();
            btnPause.setText("Pausa");
        }
    }

    private void resetGame() {
        try {
            App.changeView("view/MainView");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void endGame(boolean palyerWin) {
        activeGame = false;
        disableCPUboard(true);
        
        if (palyerWin) {
            ShowSpecialMessage("¡VICTORIA! Has hundido toda la flota enemiga.");
        } else {
            ShowSpecialMessage("¡DERROTA! La CPU ha hundido todos tus barcos.");
        }
        
        // Mostrar diálogo de fin de juego
        showDialogEndGame(palyerWin);
    }

    private void showDialogEndGame(boolean victoria) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin del Juego");
        alert.setHeaderText(victoria ? "¡Felicidades! Has Ganado" : "Juego Terminado");
        alert.setContentText(victoria ? 
            "Has demostrado ser un excelente almirante." :
            "La flota enemiga ha prevalecido. Mejor suerte la próxima vez.");
        
        ButtonType btnRevancha = new ButtonType("Revancha");
        ButtonType btnMenu = new ButtonType("Menú Principal");
        
        alert.getButtonTypes().setAll(btnRevancha, btnMenu);
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == btnRevancha) {
                resetGame();
            } else {
                try {
                    App.changeView("view/MainView");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    
    
    

    private void ShowSpecialMessage(String mensaje) {
        lblMessage.getStyleClass().add("mensaje-especial");
        lblMessage.setText(mensaje);
        
        // Remover la clase después de un tiempo
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblMessage.getStyleClass().remove("mensaje-especial"));
        pause.play();
    }






    


}
