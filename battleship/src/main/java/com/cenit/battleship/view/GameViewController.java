/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.ShotResult;
import java.net.URL;

import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 *
 * @author Usuario
 */

public class GameViewController implements Initializable {
    
    private GameController gameController;
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
    
    // Matrices de botones
    private Button[][] playerButtons = new Button[10][10];
    private Button[][] CPUButtons = new Button[10][10];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameController = new GameController();
        initializeInterface();
        configureEvents();
        startGame();
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
            health.setProgress((double) (ship.getType().getSize() - ship.getShotReceived()) / ship.getType().getSize());
            
            // Color según estado
            if (ship.isSunk()) {
                health.getStyleClass().add("barco-hundido");
                name.getStyleClass().add("barco-hundido");
            } else if (ship.getShotReceived() > 0) {
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

    private void showMessage(String message) {
        lblMessage.setText(message);
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
