package com.cenit.battleship.model;


import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un jugador en el juego de batalla naval
 */
public class Player {
    private String name;
    private Board board;
    private List<Ship> ships;
    private PlayerProfile profile; // Opcional: enlace al perfil
    private boolean isCPU;
    private Difficulty difficulty; // Para CPU
    
    public Player(String name) {
        this(name, new Board(), false, Difficulty.NORMAL);
    }
    
    public Player(String name, Board board, boolean isCPU, Difficulty difficulty) {
        this.name = name;
        this.board = board;
        this.ships = new ArrayList<>();
        this.isCPU = isCPU;
        this.difficulty = difficulty;
        initializeShips();
    }
    
    /**
     * Inicializa los barcos del jugador según el modo de juego
     */
    private void initializeShips() {
        // Por defecto, flota clásica
        ships.add(new Ship(ShipType.CARRIER));
        ships.add(new Ship(ShipType.BATTLESHIP));
        ships.add(new Ship(ShipType.CRUISER));
        ships.add(new Ship(ShipType.DESTROYER));
        ships.add(new Ship(ShipType.SUBMARINE));
    }
    
    /**
     * Coloca un barco en el tablero
     */
    public boolean placeShip(Ship ship, Coordinate startCoord, Direction direction) {
        try {
            // Calcular coordenadas del barco
            List<Coordinate> coordinates = calculateShipCoordinates(ship, startCoord, direction);
            
            // Verificar si se puede colocar
            if (!canPlaceShip(ship, startCoord, direction)) {
                return false;
            }
            
            // Establecer posición en el barco
            ship.setPosition(coordinates);
            
            // Colocar en el tablero
            for (Coordinate coord : coordinates) {
                Cell cell = board.getCell(coord);
                cell.setShip(ship);
            }
            
            System.out.println("✅ " + name + " colocó " + ship.getType().getName() + 
                             " en " + startCoord.aNotacion() + " (" + direction + ")");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error al colocar barco: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calcula las coordenadas que ocuparía un barco
     */
    private List<Coordinate> calculateShipCoordinates(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = new ArrayList<>();
        int size = ship.getType().getSize();
        
        int dx = 0, dy = 0;
        if (direction == Direction.HORIZONTAL) {
            dx = 1;
        } else {
            dy = 1;
        }
        
        for (int i = 0; i < size; i++) {
            int x = start.getX() + (dx * i);
            int y = start.getY() + (dy * i);
            coordinates.add(new Coordinate(x, y));
        }
        
        return coordinates;
    }
    
    /**
     * Verifica si un barco puede colocarse en una posición
     */
    public boolean canPlaceShip(Ship ship, Coordinate startCoord, Direction direction) {
        List<Coordinate> coordinates = calculateShipCoordinates(ship, startCoord, direction);
        
        // Verificar límites del tablero
        for (Coordinate coord : coordinates) {
            if (coord.getX() < 0 || coord.getX() >= Board.BOARD_SIZE || 
                coord.getY() < 0 || coord.getY() >= Board.BOARD_SIZE) {
                return false;
            }
        }
        
        // Verificar superposición con otros barcos
        for (Coordinate coord : coordinates) {
            if (board.hasShipAt(coord)) {
                return false;
            }
        }
        
        // Verificar separación (1 casilla de distancia)
        for (Coordinate coord : coordinates) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    try {
                        Coordinate adjacent = coord.desplazar(dx, dy);
                        if (board.hasShipAt(adjacent)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        // Fuera del tablero, ignorar
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
 * Realiza un disparo en el tablero del oponente
 */
public ShotResult shootAt(Player opponent, Coordinate target) {
    Board opponentBoard = opponent.getBoard();
    Cell targetCell = opponentBoard.getCell(target);
    
    // Verificar si la celda existe
    if (targetCell == null) {
        return ShotResult.INVALID;
    }
    
    // Verificar si ya se disparó aquí (usando el nuevo método)
    if (targetCell.hasBeenShot()) {
        return ShotResult.ALREADY_SHOT;
    }
    
    // Verificar coordenada válida
    if (target.getX() < 0 || target.getX() >= Board.BOARD_SIZE || 
        target.getY() < 0 || target.getY() >= Board.BOARD_SIZE) {
        return ShotResult.INVALID;
    }
    
    // Realizar el disparo usando el nuevo método shoot() de Cell
    ShotResult result = targetCell.shoot();
    
    // Registrar el resultado
    switch (result) {
        case HIT:
            Ship hitShip = targetCell.getShip();
            System.out.println("💥 " + name + " impactó " + hitShip.getType().getName() + 
                             " en " + target.aNotacion());
            break;
            
        case SUNK:
            Ship sunkShip = targetCell.getShip();
            System.out.println("💀 " + name + " hundió " + sunkShip.getType().getName() + 
                             " en " + target.aNotacion() + "!");
            break;
            
        case MISS:
            System.out.println("💧 " + name + " disparó al agua en " + target.aNotacion());
            break;
            
        case ALREADY_HIT:
            System.out.println("⚠️ " + name + " impactó nuevamente en " + target.aNotacion());
            break;
            
        default:
            System.out.println("❌ " + name + " disparo inválido en " + target.aNotacion());
            break;
    }
    
    return result;
}
    
    /**
     * Verifica si el jugador ha perdido (todos sus barcos hundidos)
     */
    public boolean hasLost() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Obtiene la cantidad de barcos aún a flote
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
     * Obtiene información del estado de la flota
     */
    public String getFleetStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flota de ").append(name).append(":\n");
        
        for (Ship ship : ships) {
            String status = ship.isSunk() ? "💀 HUNDIDO" : 
                          ship.isPlaced() ? "🚢 OPERATIVO" : "⏳ NO COLOCADO";
            sb.append(String.format(" - %s: %s (%d/%d)\n", 
                ship.getType().getName(), status, 
                ship.getType().getSize() - ship.getImpactsReceived(), 
                ship.getType().getSize()));
        }
        
        return sb.toString();
    }
    
    /**
     * Reinicia el jugador para una nueva partida
     */
    public void reset() {
        board = new Board();
        ships.clear();
        initializeShips();
    }
    
    // Getters y Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Board getBoard() {
        return board;
    }
    
    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }
    
    public PlayerProfile getProfile() {
        return profile;
    }
    
    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }
    
    public boolean isCPU() {
        return isCPU;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    @Override
    public String toString() {
        return String.format("Player{name='%s', CPU=%s, barcos=%d/%d}", 
            name, isCPU, getRemainingShips(), ships.size());
    }
}