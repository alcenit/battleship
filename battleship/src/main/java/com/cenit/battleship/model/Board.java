package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de juego de batalla naval
 */
public class Board {
    public static final int BOARD_SIZE = 15;
    private Cell[][] grid;
    private List<Ship> ships;
    
    public Board() {
        this.grid = new Cell[BOARD_SIZE][BOARD_SIZE];
        this.ships = new ArrayList<>();
        initializeGrid();
    }
    
    /**
     * Inicializa el grid con celdas vacías
     */
    private void initializeGrid() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                grid[i][j] = new Cell();
            }
        }
    }
    
    
    
    /**
     * Obtiene el barco en una coordenada específica
     * @param coord Coordenada a verificar
     * @return El barco en esa coordenada, o null si no hay barco
     */
    public Ship getShipAt(Coordinate coord) {
        if (coord == null) {
            throw new IllegalArgumentException("La coordenada no puede ser nula");
        }
        
        int x = coord.getX();
        int y = coord.getY();
        
        // Validar coordenadas
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            System.err.println("❌ Coordenada fuera de los límites: " + coord);
            return null;
        }
        
        // Buscar en la celda
        Cell cell = grid[x][y];
        if (cell != null && cell.hasShip()) {
            return cell.getShip();
        }
        
        // Buscar en la lista de barcos (backup)
        for (Ship ship : ships) {
            if (ship.occupiesCoordinate(coord)) {
                return ship;
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene la celda en una coordenada específica
     * @param coord Coordenada de la celda
     * @return La celda en esa coordenada
     */
    public Cell getCellAt(Coordinate coord) {
        if (coord == null) {
            throw new IllegalArgumentException("La coordenada no puede ser nula");
        }
        
        int x = coord.getX();
        int y = coord.getY();
        
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            throw new IllegalArgumentException("Coordenada fuera de los límites: " + coord);
        }
        
        return grid[x][y];
    }
    // ========== MÉTODOS DE DISPARO ==========
    
      
    
    /**
     * Realiza un disparo en una coordenada
     * @param coord Coordenada del disparo
     * @return Resultado del disparo
     */
    public ShotResult shootAt(Coordinate coord) {
        if (coord == null) {
            return ShotResult.INVALID;
        }
        
        int x = coord.getX();
        int y = coord.getY();
        
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return ShotResult.INVALID;
        }
        
        Cell cell = grid[x][y];
        return cell != null ? cell.shoot() : ShotResult.INVALID;
    }
    
    /**
     * Verifica si hay un barco en la coordenada
     * @param coord Coordenada a verificar
     * @return true si hay un barco en esa coordenada
     */
    public boolean hasShipAt(Coordinate coord) {
        return getShipAt(coord) != null;
    }
    
    /**
     * Obtiene el estado de la celda en la coordenada
     * @param coord Coordenada a verificar
     * @return El estado de la celda (con barco, impactada, etc.)
     */
    public CellState getCellState(Coordinate coord) {
        Cell cell = getCellAt(coord);
        return cell != null ? cell.getState() : CellState.WATER;
    }
    
    /**
     * Agrega un barco al tablero
     * @param ship Barco a agregar
     */
    public void addShip(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("El barco no puede ser nulo");
        }
        
        if (!ships.contains(ship)) {
            ships.add(ship);
            System.out.println("✅ Barco " + ship.getType().getName() + " agregado al tablero");
        }
    }
    
    /**
     * Obtiene todos los barcos del tablero
     * @return Lista de barcos
     */
    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }
    
    /**
     * Obtiene los barcos que aún no han sido hundidos
     * @return Lista de barcos activos
     */
    public List<Ship> getActiveShips() {
        List<Ship> activeShips = new ArrayList<>();
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                activeShips.add(ship);
            }
        }
        return activeShips;
    }
    
    /**
     * Verifica si todos los barcos han sido hundidos
     * @return true si todos los barcos están hundidos
     */
    public boolean allShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    // ========== MÉTODOS DE ACCESO A CELDAS ==========
    
    /**
     * Obtiene la celda en la coordenada especificada
     * @param coord Coordenada de la celda
     * @return La celda en esa posición
     * @throws IllegalArgumentException si la coordenada está fuera del tablero
     */
    public Cell getCell(Coordinate coord) {
        if (coord.getX() < 0 || coord.getX() >= BOARD_SIZE || 
            coord.getY() < 0 || coord.getY() >= BOARD_SIZE) {
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
    
    
    
    // ========== MÉTODOS DE INFORMACIÓN ==========
    
        
    
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
    
  
    /**
     * Obtiene una representación visual del tablero
     * @param showShips true para mostrar barcos ocultos
     * @return Matriz de strings representando el tablero
     */
    public String[][] getBoardDisplay(boolean showShips) {
        String[][] display = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
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
        for (int j = 0; j < BOARD_SIZE; j++) {
            System.out.print(" " + (char)('A' + j) + " ");
        }
        System.out.println();
        
        // Imprimir filas con números
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
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
    
    
}