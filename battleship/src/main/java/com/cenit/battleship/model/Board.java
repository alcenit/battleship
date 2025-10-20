package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de juego de batalla naval
 */
public class Board {
    public static final int SIZE = 10;
    private Cell[][] grid;
    private List<Ship> ships;
    
    public Board() {
        this.grid = new Cell[SIZE][SIZE];
        this.ships = new ArrayList<>();
        initializeGrid();
    }
    
    /**
     * Inicializa el grid con celdas vacías
     */
    private void initializeGrid() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = new Cell();
            }
        }
    }
    
    // ========== MÉTODOS DE ACCESO A CELDAS ==========
    
    /**
     * Obtiene la celda en la coordenada especificada
     * @param coord Coordenada de la celda
     * @return La celda en esa posición
     * @throws IllegalArgumentException si la coordenada está fuera del tablero
     */
    public Cell getCell(Coordinate coord) {
        if (coord.getX() < 0 || coord.getX() >= SIZE || 
            coord.getY() < 0 || coord.getY() >= SIZE) {
            throw new IllegalArgumentException("Coordenada fuera del tablero: " + coord);
        }
        return grid[coord.getX()][coord.getY()];
    }
    
    /**
     * Obtiene la celda en las coordenadas (x, y)
     * @param x Coordenada X (0-9)
     * @param y Coordenada Y (0-9)
     * @return La celda en esa posición
     */
    public Cell getCell(int x, int y) {
        return getCell(new Coordinate(x, y));
    }
    
    /**
     * Verifica si hay un barco en la coordenada especificada
     * @param coord Coordenada a verificar
     * @return true si hay un barco en esa celda
     */
    public boolean hasShipAt(Coordinate coord) {
        return getCell(coord).hasShip();
    }
    
    /**
     * Verifica si la celda en la coordenada ha sido revelada
     * @param coord Coordenada a verificar
     * @return true si la celda ha sido revelada
     */
    public boolean isCellRevealed(Coordinate coord) {
        return getCell(coord).isRevealed();
    }
    
    /**
     * Verifica si se puede disparar en la coordenada
     * @param coord Coordenada a verificar
     * @return true si se puede disparar en esa celda
     */
    public boolean canShootAt(Coordinate coord) {
        return getCell(coord).isShotAvailable();
    }
    
    // ========== MÉTODOS DE COLOCACIÓN DE BARCOS ==========
    
    /**
     * Coloca un barco en las coordenadas especificadas
     * @param ship El barco a colocar
     * @param coordinates Lista de coordenadas que ocupará el barco
     * @throws IllegalArgumentException si alguna coordenada no es válida
     */
    public void placeShip(Ship ship, List<Coordinate> coordinates) {
        // Validar que todas las coordenadas estén libres
        for (Coordinate coord : coordinates) {
            if (hasShipAt(coord)) {
                throw new IllegalArgumentException("Ya hay un barco en: " + coord.aNotacion());
            }
        }
        
        // Colocar el barco en todas las coordenadas
        for (Coordinate coord : coordinates) {
            Cell cell = getCell(coord);
            cell.setShip(ship);
        }
        
        ships.add(ship);
        System.out.println("✅ Barco " + ship.getType().getName() + " colocado en " +
                         coordinates.get(0).aNotacion() + " a " + 
                         coordinates.get(coordinates.size() - 1).aNotacion());
    }
    
    // ========== MÉTODOS DE DISPARO ==========
    
    /**
     * Realiza un disparo en la coordenada especificada
     * @param coord Coordenada del disparo
     * @return Resultado del disparo
     */
    public ShotResult shootAt(Coordinate coord) {
        Cell cell = getCell(coord);
        return cell.shoot();
    }
    
    // ========== MÉTODOS DE INFORMACIÓN ==========
    
    /**
     * Obtiene todos los barcos en el tablero
     * @return Lista de barcos
     */
    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }
    
    /**
     * Obtiene la cantidad de barcos aún a flote
     * @return Número de barcos no hundidos
     */
    public int getRemainingShips() {
        int count = 0;
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Verifica si todos los barcos han sido hundidos
     * @return true si no quedan barcos a flote
     */
    public boolean allShipsSunk() {
       return ships.stream().allMatch(Ship::isSunk);
    }
  
    /**
     * Obtiene una representación visual del tablero
     * @param showShips true para mostrar barcos ocultos
     * @return Matriz de strings representando el tablero
     */
    public String[][] getBoardDisplay(boolean showShips) {
        String[][] display = new String[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                display[i][j] = grid[i][j].getDisplayState(showShips);
            }
        }
        return display;
    }
    
    /**
     * Imprime el tablero en la consola (para debugging)
     * @param showShips true para mostrar barcos ocultos
     */
    public void printBoard(boolean showShips) {
        System.out.println("=== TABLERO ===");
        String[][] display = getBoardDisplay(showShips);
        
        // Imprimir letras de columnas
        System.out.print("  ");
        for (int j = 0; j < SIZE; j++) {
            System.out.print(" " + (char)('A' + j) + " ");
        }
        System.out.println();
        
        // Imprimir filas con números
        for (int i = 0; i < SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < SIZE; j++) {
                System.out.print(display[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Reinicia el tablero a su estado inicial
     */
    public void reset() {
        initializeGrid();
        ships.clear();
    }
    
    /**
     * Obtiene estadísticas del tablero
     * @return String con estadísticas
     */
    public String getStats() {
        int totalCells = SIZE * SIZE;
        int revealedCells = 0;
        int shipCells = 0;
        int hitCells = 0;
        
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Cell cell = grid[i][j];
                if (cell.isRevealed()) revealedCells++;
                if (cell.hasShip()) shipCells++;
                if (cell.hasShip() && cell.isRevealed()) hitCells++;
            }
        }
        
        return String.format(
            "Celdas: %d total, %d reveladas (%.1f%%), %d barcos, %d impactos",
            totalCells, revealedCells, (revealedCells * 100.0 / totalCells),
            shipCells, hitCells
        );
    }
}