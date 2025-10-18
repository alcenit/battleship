/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.cenit.battleship.view;

import com.cenit.battleship.App;
import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Ship;
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
    private List<Ship> shipToPlacement;
    private Ship selectShip;
    private Direction actualDirection = Direction.HORIZONTAL;
    
    @FXML 
    private GridPane boardPlayer;
    @FXML 
    private VBox shipPanel;
    @FXML 
    private Button btnRotate;
    @FXML 
    private Button btnAleatory;
    @FXML 
    private Button btnBeging;
    @FXML 
    private Label lblInstructions;
    @FXML 
    private HBox mainConteiner;

    // Matriz de botones para el tablero
    private Button[][] boardButtons = new Button[10][10];

    public static void setGameMode(String mode) {
        gameMode = mode;
    }
    public static void setGameController(GameController gameController) {
        gameController = gameController;
    }
    public static void setPlayerName(String name) {
        playerName = name;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeShips();
        initializeBoard();
        ConfigureEvents();
        updateInterface();       
        
        
    }

    private void initializeShips() {
        shipToPlacement = new ArrayList<>();
        
        // Seleccionar flota según el modo de juego
        switch(gameMode != null ? gameMode : "Clásico") {
            case "Flota Especial":
             //   shipToPlacement.add(new Ship(TipoBarco.CARRIER_ANCHO));
            //    shipToPlacement.add(new Barco(TipoBarco.ACORAZADO_L));
             //   shipToPlacement.add(new Barco(TipoBarco.PORTAHELICOPTEROS));
             //   shipToPlacement.add(new Barco(TipoBarco.SONAR));
               break;
            case "Táctico":
              //  shipToPlacement.add(new Barco(TipoBarco.RADAR));
            //    shipToPlacement.add(new Barco(TipoBarco.FANTASMA));
            //    shipToPlacement.add(new Barco(TipoBarco.MINADOR));
             //   shipToPlacement.add(new Barco(TipoBarco.MISIL));
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
        // Crear botones para el tablero 10x10
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
                
                boardPlayer.add(button, j, i); // GridPane: col, row
                boardButtons[i][j] = button;
            }
        }
    }

    private void ConfigureEvents() {
        btnRotate.setOnAction(e -> rotateShip());
        btnAleatory.setOnAction(e -> placeRandomShips());
        btnBeging.setOnAction(e -> StartGame());
    }

    private void handleCheckboxClick(int x, int y) {
        if (selectShip == null) return;
        
        // TODO: Implementar lógica de colocación
        System.out.println("Colocar " + selectShip.getType().getName() + 
                          " en (" + x + "," + y + ") dirección: " + actualDirection);
        
        // Simular colocación exitosa
        selectShip = getNextShip();
        updateInterface();
    }

    private void highlightPosition(int x, int y) {
        if (selectShip == null) return;
        
        // Resaltar posición potencial del barco
        clearHighlight();
        
        // TODO: Implementar resaltado de posición válida
    }

    private void clearHighlight() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardButtons[i][j].getStyleClass().remove("casilla-resaltada");
            }
        }
    }

    private void rotateShip() {
        actualDirection = (actualDirection == Direction.HORIZONTAL) ? 
                         Direction.VERTICAL : Direction.HORIZONTAL;
        btnRotate.setText(actualDirection == Direction.HORIZONTAL ? "Horizontal" : "Vertical");
    }

    private void placeRandomShips() {
        // TODO: Implementar colocación aleatoria
        selectShip = null;
        updateInterface();
    }

    private void StartGame() {
        try {
            App.changeView("view/JuegoView");//verificar ruta¡¡¡¡¡¡¡¡
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Ship getNextShip() {
        if (shipToPlacement.isEmpty()) return null;
        return shipToPlacement.remove(0);
    }

    private void updateInterface() {
        if (selectShip != null) {
            lblInstructions.setText("Coloca: " + selectShip.getType().getName());
            btnBeging.setDisable(true);
        } else {
            lblInstructions.setText("¡Todos los barcos colocados! Listo para comenzar.");
            btnBeging.setDisable(false);
        }
        
        // Actualizar panel de barcos
        updateShipsPanel();
    }

    private void updateShipsPanel() {
        shipPanel.getChildren().clear();
        
        Label title = new Label("Barcos por colocar:");
        title.getStyleClass().add("subtitle-label");
        shipPanel.getChildren().add(title);
        
        for (Ship ship : shipToPlacement) {
            HBox rowShip = new HBox(10);
            rowShip.getStyleClass().add("barco-item");
            
            Label name = new Label(ship.getType().getName());
            name.getStyleClass().add("barco-nombre");
            
            // Representación visual del barco
            HBox representation = createShipRepresentation(ship);
            
            rowShip.getChildren().addAll(name, representation);
            shipPanel.getChildren().add(rowShip);
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
