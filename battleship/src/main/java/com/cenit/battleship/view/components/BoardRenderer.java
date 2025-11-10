package com.cenit.battleship.view.components;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.GameConfiguration;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.Direction;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class BoardRenderer {
    private final GameConfiguration config;
    private final int cellSize;
    private final int boardSize;
    
    // Interfaz funcional para manejar clicks con coordenadas
    @FunctionalInterface
    public interface BoardClickHandler {
        void handleClick(int x, int y);
    }
    
    // Constructor
    public BoardRenderer() {
        this.config = GameConfiguration.getInstance();
        this.cellSize = config.getCellSize();
        this.boardSize = config.getBoardSize();
    }
    
    // Constructor con configuración personalizada (opcional)
    public BoardRenderer(int cellSize, int boardSize) {
        this.config = GameConfiguration.getInstance();
        this.cellSize = cellSize;
        this.boardSize = boardSize;
    }
    
    // Métodos de instancia (sin static)
    public void initializeBoard(GridPane board, Button[][] buttons, 
                               boolean isClickable, BoardClickHandler clickHandler) {
        board.getChildren().clear();
        
        // Agregar labels de coordenadas
        addCoordinateLabels(board);
        
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Button button = createBoardButton(i, j, isClickable, clickHandler);
                board.add(button, j + 1, i + 1);
                buttons[i][j] = button;
            }
        }
    }
    
    private Button createBoardButton(int x, int y, boolean isClickable, 
                                   BoardClickHandler clickHandler) {
        Button button = new Button();
        button.setPrefSize(cellSize, cellSize);
        button.getStyleClass().add("casilla-agua");
        
        if (isClickable && clickHandler != null) {
            // Pasar las coordenadas al handler
            final int coordX = x;
            final int coordY = y;
            button.setOnMouseClicked(e -> clickHandler.handleClick(coordX, coordY));
            
            // También puedes agregar hover effects si los necesitas
            button.setOnMouseEntered(e -> {
                button.getStyleClass().add("casilla-resaltada");
            });
            
            button.setOnMouseExited(e -> {
                button.getStyleClass().remove("casilla-resaltada");
            });
        }
        
        return button;
    }
    
    private void addCoordinateLabels(GridPane board) {
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
    
    public void updateBoardDisplay(Button[][] buttons, Board board, 
                                  boolean showShips, boolean isInteractive) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Coordinate coord = new Coordinate(i, j);
                Cell cell = board.getCell(coord);
                Button button = buttons[i][j];
                
                updateButtonAppearance(button, cell, showShips);
                button.setDisable(!isInteractive || (cell != null && cell.hasBeenShot()));
            }
        }
    }
    
    private void updateButtonAppearance(Button button, Cell cell, boolean showShips) {
        if (cell == null) {
            button.getStyleClass().removeAll("casilla-agua", "casilla-barco", "casilla-impacto", 
                "casilla-fallo", "casilla-hundido", "casilla-resaltada");
            button.getStyleClass().add("casilla-agua");
            return;
        }
        
        // Limpiar estilos previos
        button.getStyleClass().removeAll("casilla-agua", "casilla-barco", "casilla-impacto", 
            "casilla-fallo", "casilla-hundido", "casilla-resaltada");
        
        button.setText("");
        button.setGraphic(null);
        
        // Aplicar estilos según el estado de la celda
        CellState state = cell.getState();
        switch (state) {
            case WATER:
                button.getStyleClass().add("casilla-agua");
                if (showShips && cell.hasShip()) {
                    button.getStyleClass().add("casilla-barco");
                    addShipOverlay(button, cell.getShip(), cell.getCoordinate());
                }
                break;
            case SHIP:
                if (showShips) {
                    button.getStyleClass().add("casilla-barco");
                    addShipOverlay(button, cell.getShip(), cell.getCoordinate());
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
                if (showShips) {
                    addShipOverlay(button, cell.getShip(), cell.getCoordinate());
                }
                break;
        }
    }
    
    private void addShipOverlay(Button button, Ship ship, Coordinate coord) {
        if (ship != null && isFirstSegment(ship, coord)) {
            StackPane overlay = createShipOverlay(ship);
            button.setGraphic(overlay);
        }
    }
    
    private StackPane createShipOverlay(Ship ship) {
        StackPane overlay = new StackPane();
        boolean isVertical = (ship.getDirection() == Direction.VERTICAL);
        int shipSize = ship.getType().getSize();
        
        double width = isVertical ? cellSize : cellSize * shipSize;
        double height = isVertical ? cellSize * shipSize : cellSize;
        
        overlay.setPrefSize(width, height);
        
        ImageView shipImage = ShipRenderer.renderShipCorrected(ship, width, height, isVertical);
        if (shipImage != null) {
            overlay.getChildren().add(shipImage);
        }
        
        return overlay;
    }
    
    private boolean isFirstSegment(Ship ship, Coordinate coord) {
        return ship != null && !ship.getSegments().isEmpty() && 
               ship.getSegments().get(0).equals(coord);
    }
    
    // Getters para la configuración
    public int getCellSize() {
        return cellSize;
    }
    
    public int getBoardSize() {
        return boardSize;
    }
    
    public GameConfiguration getConfig() {
        return config;
    }
    
    // Métodos utilitarios específicos para diferentes tipos de tableros
    public void initializePlayerBoard(GridPane board, Button[][] buttons, BoardClickHandler clickHandler) {
        initializeBoard(board, buttons, true, clickHandler);
    }
    
    public void initializeCPUBoard(GridPane board, Button[][] buttons, BoardClickHandler clickHandler) {
        initializeBoard(board, buttons, true, clickHandler);
    }
    
    public void initializeDisplayBoard(GridPane board, Button[][] buttons) {
        initializeBoard(board, buttons, false, null);
    }
    
    // Método para limpiar completamente un tablero
    public void clearBoard(GridPane board, Button[][] buttons) {
        board.getChildren().clear();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                buttons[i][j] = null;
            }
        }
    }
}