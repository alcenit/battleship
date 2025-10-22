package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.List;
import java.util.Random;

/**
 * Representa la CPU como jugador controlado por la computadora
 */
public class CPU extends Player {
    
    private Random random;
    private List<Coordinate> availableShots;
    private Coordinate lastHit;
    private Direction searchDirection;
    private boolean isHuntingMode;
    
    public CPU(Difficulty difficulty) {
        super("CPU", new Board(), true, difficulty);
        this.random = new Random();
        initializeAvailableShots();
        this.isHuntingMode = false;
    }
    
    public CPU() {
        this(Difficulty.NORMAL);
    }
    
    /**
     * Inicializa la lista de todos los posibles disparos
     */
    private void initializeAvailableShots() {
        availableShots = new java.util.ArrayList<>();
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                availableShots.add(new Coordinate(x, y));
            }
        }
    }
    
    /**
     * Coloca todos los barcos de la CPU aleatoriamente
     */
    public void placeShipsRandomly() {
        System.out.println("🤖 CPU colocando barcos...");
        
        for (Ship ship : getShips()) {
            boolean placed = false;
            int attempts = 0;
            final int MAX_ATTEMPTS = 100;
            
            while (!placed && attempts < MAX_ATTEMPTS) {
                try {
                    int x = random.nextInt(Board.SIZE);
                    int y = random.nextInt(Board.SIZE);
                    Direction direction = random.nextBoolean() ? Direction.HORIZONTAL : Direction.VERTICAL;
                    
                    Coordinate coord = new Coordinate(x, y);
                    
                    if (canPlaceShip(ship, coord, direction)) {
                        placeShip(ship, coord, direction);
                        placed = true;
                        System.out.println("✅ CPU colocó " + ship.getType().getName());
                    }
                    
                } catch (IllegalArgumentException e) {
                    // Coordenada inválida, intentar de nuevo
                }
                attempts++;
            }
            
            if (!placed) {
                System.err.println("❌ CPU no pudo colocar: " + ship.getType().getName());
            }
        }
        
        System.out.println("🎯 CPU terminó de colocar barcos");
    }
    
    /**
     * Elige una coordenada para disparar según la dificultad
     */
    public Coordinate chooseShotTarget(Player opponent) {
        switch (getDifficulty()) {
            case EASY:
                return chooseShotEasy();
            case NORMAL:
                return chooseShotNormal();
            case HARD:
                return chooseShotHard(opponent);
            case EXPERT:
                return chooseShotExpert(opponent);
            default:
                return chooseShotNormal();
        }
    }
    
    /**
     * Disparo aleatorio simple (fácil)
     */
    private Coordinate chooseShotEasy() {
        if (availableShots.isEmpty()) {
            initializeAvailableShots();
        }
        
        int index = random.nextInt(availableShots.size());
        Coordinate shot = availableShots.remove(index);
        System.out.println("🎯 CPU (Fácil) disparando en: " + shot.aNotacion());
        return shot;
    }
    
    /**
     * Disparo con patrón básico (normal)
     */
    private Coordinate chooseShotNormal() {
        if (availableShots.isEmpty()) {
            initializeAvailableShots();
        }
        
        // 70% aleatorio, 30% en patrón de tablero de ajedrez
        if (random.nextDouble() < 0.7 || availableShots.size() < 10) {
            return chooseShotEasy();
        }
        
        // Patrón de tablero de ajedrez (solo casillas del mismo color)
        List<Coordinate> chessPatternShots = new java.util.ArrayList<>();
        for (Coordinate coord : availableShots) {
            if ((coord.getX() + coord.getY()) % 2 == 0) { // Mismo color
                chessPatternShots.add(coord);
            }
        }
        
        if (!chessPatternShots.isEmpty()) {
            int index = random.nextInt(chessPatternShots.size());
            Coordinate shot = chessPatternShots.get(index);
            availableShots.remove(shot);
            System.out.println("🎯 CPU (Normal) disparando en patrón: " + shot.aNotacion());
            return shot;
        }
        
        return chooseShotEasy();
    }
    
    /**
     * Disparo con búsqueda inteligente (difícil)
     */
    private Coordinate chooseShotHard(Player opponent) {
        // Si estamos en modo caza y tenemos un último impacto
        if (isHuntingMode && lastHit != null) {
            Coordinate nextShot = getNextHuntingShot(opponent);
            if (nextShot != null) {
                availableShots.remove(nextShot);
                System.out.println("🎯 CPU (Difícil) cazando desde: " + lastHit.aNotacion());
                return nextShot;
            }
        }
        
        // Si no, buscar nuevo objetivo
        return findNewTarget(opponent);
    }
    
    /**
     * Disparo con algoritmo avanzado (experto)
     */
    private Coordinate chooseShotExpert(Player opponent) {
        // 50% usar algoritmo difícil, 50% usar probabilidad de densidad
        if (random.nextBoolean()) {
            return chooseShotHard(opponent);
        }
        
        return chooseShotByProbability(opponent);
    }
    
    /**
     * Encuentra el siguiente disparo cuando se está cazando un barco
     */
    private Coordinate getNextHuntingShot(Player opponent) {
        // Intentar en la dirección actual primero
        if (searchDirection != null) {
            Coordinate next = tryDirection(lastHit, searchDirection, opponent);
            if (next != null) {
                return next;
            }
        }
        
        // Si no funciona, probar otras direcciones
        for (Direction dir : Direction.values()) {
            if (searchDirection != dir) {
                Coordinate next = tryDirection(lastHit, dir, opponent);
                if (next != null) {
                    searchDirection = dir;
                    return next;
                }
            }
        }
        
        // Si no hay más disparos en esta caza, resetear
        isHuntingMode = false;
        lastHit = null;
        searchDirection = null;
        return null;
    }
    
    private Coordinate tryDirection(Coordinate from, Direction direction, Player opponent) {
        int dx = (direction == Direction.HORIZONTAL) ? 1 : 0;
        int dy = (direction == Direction.VERTICAL) ? 1 : 0;
        
        // Probar en ambas direcciones
        for (int i = 1; i <= 3; i++) { // Máximo 3 casillas de distancia
            for (int sign = -1; sign <= 1; sign += 2) {
                try {
                    Coordinate target = from.desplazar(dx * i * sign, dy * i * sign);
                    if (availableShots.contains(target) && isValidHuntingShot(target, opponent)) {
                        return target;
                    }
                } catch (IllegalArgumentException e) {
                    // Fuera del tablero
                }
            }
        }
        
        return null;
    }
    
    private boolean isValidHuntingShot(Coordinate coord, Player opponent) {
    // Verificar que la coordenada esté en la lista de disparos disponibles
    if (!availableShots.contains(coord)) {
        return false;
    }
    
    // Verificar que la celda exista
    Cell targetCell = opponent.getBoard().getCell(coord);
    if (targetCell == null) {
        return false;
    }
    
    // Verificar que no se haya disparado ya en esta celda
    return !targetCell.hasBeenShot();
}
    
    /**
     * Encuentra un nuevo objetivo usando probabilidad de densidad
     */
    private Coordinate findNewTarget(Player opponent) {
        // Crear mapa de probabilidades
        double[][] probabilities = createProbabilityMap(opponent);
        
        // Encontrar la coordenada con mayor probabilidad
        Coordinate bestShot = null;
        double bestProb = -1;
        
        for (Coordinate coord : availableShots) {
            double prob = probabilities[coord.getX()][coord.getY()];
            if (prob > bestProb) {
                bestProb = prob;
                bestShot = coord;
            }
        }
        
        if (bestShot != null) {
            availableShots.remove(bestShot);
            System.out.println("🎯 CPU (Difícil) nuevo objetivo: " + bestShot.aNotacion());
            return bestShot;
        }
        
        return chooseShotNormal();
    }
    
    /**
     * Crea un mapa de probabilidades basado en barcos restantes
     */
    private double[][] createProbabilityMap(Player opponent) {
        double[][] probMap = new double[Board.SIZE][Board.SIZE];
        
        // Para cada barco posible que pueda quedar
        for (Ship ship : opponent.getShips()) {
            if (!ship.isSunk()) {
                addShipProbability(probMap, ship.getType().getSize());
            }
        }
        
        return probMap;
    }
    
    private void addShipProbability(double[][] probMap, int shipSize) {
        // Aumentar probabilidad en todas las posiciones donde podría caber el barco
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                for (Direction dir : Direction.values()) {
                    if (canFitShip(x, y, shipSize, dir)) {
                        for (int i = 0; i < shipSize; i++) {
                            int dx = (dir == Direction.HORIZONTAL) ? i : 0;
                            int dy = (dir == Direction.VERTICAL) ? i : 0;
                            if (x + dx < Board.SIZE && y + dy < Board.SIZE) {
                                probMap[x + dx][y + dy] += 1.0;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean canFitShip(int x, int y, int size, Direction direction) {
        for (int i = 0; i < size; i++) {
            int checkX = x + ((direction == Direction.HORIZONTAL) ? i : 0);
            int checkY = y + ((direction == Direction.VERTICAL) ? i : 0);
            
            if (checkX >= Board.SIZE || checkY >= Board.SIZE) {
                return false;
            }
            
            // Verificar que no sea una casilla ya revelada con agua
            Coordinate coord = new Coordinate(checkX, checkY);
            if (!availableShots.contains(coord)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Disparo basado en probabilidad de densidad (algoritmo más avanzado)
     */
    private Coordinate chooseShotByProbability(Player opponent) {
        double[][] probMap = createProbabilityMap(opponent);
        
        // Encontrar la mejor coordenada
        Coordinate bestShot = null;
        double bestProb = -1;
        
        for (Coordinate coord : availableShots) {
            double prob = probMap[coord.getX()][coord.getY()];
            if (prob > bestProb) {
                bestProb = prob;
                bestShot = coord;
            }
        }
        
        if (bestShot != null) {
            availableShots.remove(bestShot);
            System.out.println("🎯 CPU (Experto) disparo probabilístico: " + bestShot.aNotacion());
            return bestShot;
        }
        
        return chooseShotHard(opponent);
    }
    
    /**
 * Actualiza el estado de la CPU después de un disparo
 */
public void updateAfterShot(Coordinate shot, ShotResult result) {
    switch (result) {
        case HIT:
            lastHit = shot;
            isHuntingMode = true;
            if (getDifficulty() == Difficulty.EXPERT) {
                searchDirection = null;
            }
            break;
            
        case SUNK:
            lastHit = null;
            isHuntingMode = false;
            searchDirection = null;
            break;
            
        case MISS:
            // En modo experto, puede cambiar de estrategia después de varios misses
            if (getDifficulty() == Difficulty.EXPERT && random.nextDouble() < 0.3) {
                isHuntingMode = false;
            }
            break;
            
        default:
            // No hacer nada para otros resultados
            break;
    }
}
    
   
    
    /**
     * Reinicia la CPU para una nueva partida
     */
    @Override
    public void reset() {
        super.reset();
        initializeAvailableShots();
        lastHit = null;
        searchDirection = null;
        isHuntingMode = false;
    }
}