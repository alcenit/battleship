package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el estado principal del juego de batalla naval
 * @author Usuario
 */
public class Game {
    
    private final Board boardPlayer;
    private final Board boardCPU;
    private boolean playerTurn;
    private GamePhase phase;
    private int turnCount;
    private List<GameEventListener> listeners;
    
    public Game() {
        this.boardPlayer = new Board();
        this.boardCPU = new Board();
        this.playerTurn = true;
        this.phase = GamePhase.SHIP_PLACEMENT;
        this.turnCount = 0;
        this.listeners = new ArrayList<>();
    }
    
    // ========== GETTERS ==========
    
    public Board getBoardPlayer() { 
        return boardPlayer; 
    }
    
    public Board getBoardCPU() { 
        return boardCPU; 
    }
    
    public boolean isPlayerTurn() { 
        return playerTurn; 
    }
    
    public GamePhase getPhase() { 
        return phase; 
    }
    
    public int getTurnCount() {
        return turnCount;
    }
    
    // ========== SETTERS ==========
    
    public void setPlayerTurn(boolean playerTurn) {
        boolean previousTurn = this.playerTurn;
        this.playerTurn = playerTurn;
        
        if (previousTurn != playerTurn) {
            notifyTurnChanged(playerTurn);
        }
    }
    
    public void setPhase(GamePhase phase) {
        GamePhase previousPhase = this.phase;
        this.phase = phase;
        
        if (previousPhase != phase) {
            notifyPhaseChanged(phase);
        }
    }
    
    // ========== MÉTODOS DE COLOCACIÓN DE BARCOS ==========
    
    /**
     * Verifica si un barco puede colocarse en el tablero del jugador
     */
    public boolean canPlacePlayerShip(Ship ship, Coordinate coord, Direction direction) {
        if (ship == null || coord == null || direction == null) {
            System.err.println("❌ Parámetros inválidos para canPlacePlayerShip");
            return false;
        }

        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, coord, direction);
            return canPlaceShipOnBoard(boardPlayer, shipCoordinates);
        } catch (Exception e) {
            System.err.println("❌ Error en canPlacePlayerShip: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si un barco puede colocarse en el tablero de la CPU
     */
    public boolean canPlaceCPUShip(Ship ship, Coordinate coord, Direction direction) {
        if (ship == null || coord == null || direction == null) {
            System.err.println("❌ Parámetros inválidos para canPlaceCPUShip");
            return false;
        }

        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, coord, direction);
            return canPlaceShipOnBoard(boardCPU, shipCoordinates);
        } catch (Exception e) {
            System.err.println("❌ Error en canPlaceCPUShip: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si un barco puede colocarse en un tablero específico
     */
    private boolean canPlaceShipOnBoard(Board board, List<Coordinate> coordinates) {
        if (!areAllCoordinatesValid(coordinates)) {
            return false;
        }

        if (hasShipOverlapOnBoard(board, coordinates)) {
            return false;
        }

        if (!respectsSeparationRulesOnBoard(board, coordinates)) {
            return false;
        }

        return true;
    }
    
    /**
     * Coloca un barco en el tablero del jugador
     */
    public boolean placePlayerShip(Ship ship, Coordinate startCoord, Direction direction) {
        if (phase != GamePhase.SHIP_PLACEMENT) {
            throw new IllegalStateException("Solo se pueden colocar barcos durante la fase de colocación");
        }
        
        if (!canPlacePlayerShip(ship, startCoord, direction)) {
            return false;
        }
        
        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, startCoord, direction);
            
            // Establecer la posición en el objeto Ship
            ship.setPosition(shipCoordinates);
            
            // Colocar en el tablero
            boardPlayer.placeShip(ship, shipCoordinates);
            
            System.out.println("✅ Barco del jugador " + ship.getType().getName() + " colocado exitosamente");
            notifyShipPlaced(true, ship, shipCoordinates);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error al colocar barco del jugador: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Coloca un barco en el tablero de la CPU
     */
    public boolean placeCPUShip(Ship ship, Coordinate startCoord, Direction direction) {
        if (phase != GamePhase.SHIP_PLACEMENT) {
            throw new IllegalStateException("Solo se pueden colocar barcos durante la fase de colocación");
        }
        
        if (!canPlaceCPUShip(ship, startCoord, direction)) {
            return false;
        }
        
        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, startCoord, direction);
            
            // Establecer la posición en el objeto Ship
            ship.setPosition(shipCoordinates);
            
            // Colocar en el tablero
            boardCPU.placeShip(ship, shipCoordinates);
            
            System.out.println("✅ Barco de la CPU " + ship.getType().getName() + " colocado exitosamente");
            notifyShipPlaced(false, ship, shipCoordinates);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error al colocar barco de la CPU: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calcula las coordenadas que ocuparía el barco
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
     * Verifica que todas las coordenadas estén dentro del tablero
     */
    private boolean areAllCoordinatesValid(List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            if (coord.getX() < 0 || coord.getX() >= Board.BOARD_SIZE
                    || coord.getY() < 0 || coord.getY() >= Board.BOARD_SIZE) {
                System.out.println("🚫 Coordenada fuera del tablero: " + coord.aNotacion());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica si hay superposición con barcos existentes en un tablero
     */
    private boolean hasShipOverlapOnBoard(Board board, List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            if (board.hasShipAt(coord)) {
                System.out.println("🚫 Superposición en: " + coord.aNotacion());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica reglas de separación entre barcos en un tablero
     */
    private boolean respectsSeparationRulesOnBoard(Board board, List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    try {
                        Coordinate adjacent = coord.desplazar(dx, dy);
                        if (board.hasShipAt(adjacent)) {
                            System.out.println("🚫 Barco muy cercano en: " + adjacent.aNotacion());
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        // Coordenada fuera del tablero, ignorar
                    }
                }
            }
        }
        return true;
    }
    
    // ========== COMPORTAMIENTO DEL JUEGO ==========
    
    /**
     * Realiza un disparo del jugador en el tablero de la CPU
     */
    public ShotResult playerShot(Coordinate coord) {
        if (!playerTurn || phase != GamePhase.IN_PLAY) {
            throw new IllegalStateException("No es el turno del jugador o el juego no está activo");
        }
        
        // Verificar que la coordenada sea válida para disparar
        if (!boardCPU.canShootAt(coord)) {
            return ShotResult.ALREADY_SHOT;
        }
        
        ShotResult result = boardCPU.shootAt(coord);
        turnCount++;
        
        // Notificar el disparo
        notifyShotFired(true, coord, result);
        
        // Cambiar turno si no fue impacto (en algunas variantes)
        if (!result.allowsAnotherTurn()) {
            setPlayerTurn(false);
        }
        
        // Verificar fin del juego
        verifyEndGame();
        
        return result;
    }
    
    /**
     * Realiza un disparo de la CPU en el tablero del jugador
     */
    public ShotResult cpuShot(Coordinate coord) {
        if (playerTurn || phase != GamePhase.IN_PLAY) {
            throw new IllegalStateException("No es el turno de la CPU o el juego no está activo");
        }
        
        // Verificar que la coordenada sea válida para disparar
        if (!boardPlayer.canShootAt(coord)) {
            return ShotResult.ALREADY_SHOT;
        }
        
        ShotResult result = boardPlayer.shootAt(coord);
        turnCount++;
        
        // Notificar el disparo
        notifyShotFired(false, coord, result);
        
        // Cambiar turno si no fue impacto (en algunas variantes)
        if (!result.allowsAnotherTurn()) {
            setPlayerTurn(true);
        }
        
        // Verificar fin del juego
        verifyEndGame();
        
        return result;
    }
    
    /**
     * Recibe un disparo en el tablero del jugador (para uso de la CPU)
     */
    public ShotResult receiveShot(Coordinate coord) {
        if (playerTurn || phase != GamePhase.IN_PLAY) {
            return ShotResult.INVALID;
        }
        
        return boardPlayer.shootAt(coord);
    }
    
    /**
     * Inicia el juego, cambiando de fase de colocación a juego activo
     */
    public void startGame() {
        if (phase != GamePhase.SHIP_PLACEMENT) {
            throw new IllegalStateException("El juego solo puede iniciarse desde la fase de colocación");
        }
        
        // Verificar que ambos jugadores tengan barcos colocados
        if (!areBoardsReady()) {
            throw new IllegalStateException("Ambos jugadores deben tener barcos colocados para iniciar el juego");
        }
        
        setPhase(GamePhase.IN_PLAY);
        setPlayerTurn(true);
        turnCount = 0;
        
        System.out.println("🎮 ¡Juego iniciado! Es el turno del jugador");
        notifyGameStarted();
    }
    
    /**
     * Verifica si ambos tableros están listos para jugar
     */
    private boolean areBoardsReady() {
        boolean playerReady = boardPlayer.getShips().size() > 0;
        boolean cpuReady = boardCPU.getShips().size() > 0;
        
        if (!playerReady) {
            System.err.println("❌ El jugador no tiene barcos colocados");
        }
        if (!cpuReady) {
            System.err.println("❌ La CPU no tiene barcos colocados");
        }
        
        return playerReady && cpuReady;
    }
    
    /**
     * Verifica si el juego ha terminado
     */
    private void verifyEndGame() {
        if (boardCPU.allShipsSunk()) {
            setPhase(GamePhase.PLAYER_WIN);
            notifyGameEnded(true);
            System.out.println("🎉 ¡El jugador ha ganado la partida!");
        } else if (boardPlayer.allShipsSunk()) {
            setPhase(GamePhase.CPU_WIN);
            notifyGameEnded(false);
            System.out.println("💀 La CPU ha ganado la partida");
        }
    }
    
    /**
     * Reinicia el juego completamente
     */
    public void reset() {
        boardPlayer.reset();
        boardCPU.reset();
        setPlayerTurn(true);
        setPhase(GamePhase.SHIP_PLACEMENT);
        turnCount = 0;
        
        System.out.println("🔄 Juego reiniciado");
        notifyGameReset();
    }
    
    /**
     * Obtiene estadísticas del juego
     */
    public GameStats getStats() {
        return new GameStats(
            turnCount,
            boardPlayer.getRemainingShips(),
            boardCPU.getRemainingShips(),
            boardPlayer.getShips().size(),
            boardCPU.getShips().size(),
            phase
        );
    }
    
    // ========== SISTEMA DE EVENTOS ==========
    
    public void addEventListener(GameEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeEventListener(GameEventListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyTurnChanged(boolean isPlayerTurn) {
        for (GameEventListener listener : listeners) {
            listener.onTurnChanged(isPlayerTurn);
        }
    }
    
    private void notifyPhaseChanged(GamePhase phase) {
        for (GameEventListener listener : listeners) {
            listener.onPhaseChanged(phase);
        }
    }
    
    private void notifyShotFired(boolean isPlayer, Coordinate coord, ShotResult result) {
        for (GameEventListener listener : listeners) {
            listener.onShotFired(isPlayer, coord, result);
        }
    }
    
    private void notifyShipPlaced(boolean isPlayer, Ship ship, List<Coordinate> coordinates) {
        for (GameEventListener listener : listeners) {
            listener.onShipPlaced(isPlayer, ship, coordinates);
        }
    }
    
    private void notifyGameStarted() {
        for (GameEventListener listener : listeners) {
            listener.onGameStarted();
        }
    }
    
    private void notifyGameEnded(boolean playerWon) {
        for (GameEventListener listener : listeners) {
            listener.onGameEnded(playerWon);
        }
    }
    
    private void notifyGameReset() {
        for (GameEventListener listener : listeners) {
            listener.onGameReset();
        }
    }
    
    // ========== CLASE INTERNA PARA ESTADÍSTICAS ==========
    
    public static class GameStats {
        private final int turnCount;
        private final int playerShipsRemaining;
        private final int cpuShipsRemaining;
        private final int playerTotalShips;
        private final int cpuTotalShips;
        private final GamePhase phase;
        
        public GameStats(int turnCount, int playerShipsRemaining, int cpuShipsRemaining, 
                        int playerTotalShips, int cpuTotalShips, GamePhase phase) {
            this.turnCount = turnCount;
            this.playerShipsRemaining = playerShipsRemaining;
            this.cpuShipsRemaining = cpuShipsRemaining;
            this.playerTotalShips = playerTotalShips;
            this.cpuTotalShips = cpuTotalShips;
            this.phase = phase;
        }
        
        // Getters
        public int getTurnCount() { return turnCount; }
        public int getPlayerShipsRemaining() { return playerShipsRemaining; }
        public int getCpuShipsRemaining() { return cpuShipsRemaining; }
        public int getPlayerTotalShips() { return playerTotalShips; }
        public int getCpuTotalShips() { return cpuTotalShips; }
        public GamePhase getPhase() { return phase; }
        
        public double getPlayerProgress() {
            return playerTotalShips > 0 ? 
                (double) (playerTotalShips - playerShipsRemaining) / playerTotalShips * 100 : 0;
        }
        
        public double getCpuProgress() {
            return cpuTotalShips > 0 ? 
                (double) (cpuTotalShips - cpuShipsRemaining) / cpuTotalShips * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Turno: %d | Jugador: %d/%d (%.1f%%) | CPU: %d/%d (%.1f%%)",
                turnCount, playerShipsRemaining, playerTotalShips, getPlayerProgress(),
                cpuShipsRemaining, cpuTotalShips, getCpuProgress()
            );
        }
    }
    
    // ========== INTERFACE PARA LISTENERS ==========
    
    public interface GameEventListener {
        void onTurnChanged(boolean isPlayerTurn);
        void onPhaseChanged(GamePhase phase);
        void onShotFired(boolean isPlayer, Coordinate coord, ShotResult result);
        void onShipPlaced(boolean isPlayer, Ship ship, List<Coordinate> coordinates);
        void onGameStarted();
        void onGameEnded(boolean playerWon);
        void onGameReset();
    }
}