package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.CPU;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Game;
import com.cenit.battleship.model.Player;
import com.cenit.battleship.model.PlayerProfile;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillResult;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameController {

    private Game game;
    private Coordinate lastShotCPU;
    private Ship lastSunkenShipCPU;
    private Ship lastSunkenShipPlayer;
    private List<Ship> playerShips;
    private List<Ship> CPUShips;

    private CPUController cpuController;

    private Difficulty difficulty;

    private SkillSystem playerSkills;
    private SkillSystem CPUSkills;
    private SkillController skillController;

    private Set<Ship> sunkShipsCPU;
    private Set<Ship> sunkShipsPlayer;

    // Campos para estado del juego
    private int elapsedTurns;
    private boolean playerTurn;
    private GamePhase gamePhase;
    private boolean jammingActive;

    private Board playerBoard;
    private Board cpuBoard;
    private Player humanPlayer;
    private Player cpuPlayer;
    private PlayerProfile currentProfile;

    // ========== CONSTRUCTORES ==========
    public GameController(PlayerProfile profile, Difficulty difficulty) {
        this.currentProfile = profile;
        this.difficulty = difficulty;

        // Inicializar tableros y jugadores
        this.playerBoard = new Board();
        this.cpuBoard = new Board();
        this.humanPlayer = new Player(profile.getPlayerName());
        this.humanPlayer.setProfile(profile);
        this.cpuPlayer = new CPU(difficulty);

        // Inicializar otros componentes
        this.game = new Game();
        this.cpuController = new CPUController(convertDifficulty(difficulty), playerBoard);
        this.playerShips = new ArrayList<>();
        this.CPUShips = new ArrayList<>();
        this.playerSkills = new SkillSystem(true);
        this.CPUSkills = new SkillSystem(false);
        this.skillController = new SkillController(this);
        this.sunkShipsCPU = new HashSet<>();
        this.sunkShipsPlayer = new HashSet<>();
        this.elapsedTurns = 0;
        this.playerTurn = true;
        this.gamePhase = GamePhase.IN_PLAY;
        this.jammingActive = false;

        // Colocar barcos de la CPU automáticamente
        ((CPU) cpuPlayer).placeShipsRandomly();

        initializeShips();
        initializeSkills();
    }

    public GameController() {
        // Llama al constructor completo con valores por defecto
        this(new PlayerProfile("Jugador"), Difficulty.NORMAL);
    }

    public GameController(CPUController.Difficulty difficultyCPU) {
        this(new PlayerProfile("Jugador"), convertFromCPUControllerDifficulty(difficultyCPU));
    }

    // ========== INICIALIZACIÓN ==========
    private void initializeShips() {
        // Inicializar barcos del jugador (si no se configuran manualmente)
        if (playerShips.isEmpty()) {
            setStandardFleet(); // Usar flota estándar por defecto
        }

        // Los barcos de la CPU ya se colocaron en el constructor
        // Los barcos del jugador se colocarán manualmente mediante placeShip()
    }

    private void initializeSkills() {
        // Configurar habilidades iniciales según dificultad
        setupSkillsByDifficulty();
    }

    private void setupSkillsByDifficulty() {
        playerSkills.reset();
        CPUSkills.reset();

        switch (difficulty) {
            case EASY:
                // Más habilidades para jugador en fácil
                playerSkills.addSkill(Skill.SONAR, 3);
                playerSkills.addSkill(Skill.REPAIR, 2);
                playerSkills.setSkillPoints(6);
                CPUSkills.addSkill(Skill.SONAR, 1);
                CPUSkills.setSkillPoints(2);
                break;
            case NORMAL:
                playerSkills.addSkill(Skill.SONAR, 2);
                playerSkills.addSkill(Skill.RADAR, 1);
                playerSkills.setSkillPoints(4);
                CPUSkills.addSkill(Skill.SONAR, 1);
                CPUSkills.addSkill(Skill.RADAR, 1);
                CPUSkills.setSkillPoints(3);
                break;
            case HARD:
                playerSkills.addSkill(Skill.SONAR, 1);
                playerSkills.setSkillPoints(2);
                CPUSkills.addSkill(Skill.SONAR, 2);
                CPUSkills.addSkill(Skill.RADAR, 1);
                CPUSkills.addSkill(Skill.JAMMING, 1);
                CPUSkills.setSkillPoints(5);
                break;
            case EXPERT:
                playerSkills.addSkill(Skill.SONAR, 1);
                playerSkills.setSkillPoints(1);
                CPUSkills.addSkill(Skill.SONAR, 2);
                CPUSkills.addSkill(Skill.RADAR, 2);
                CPUSkills.addSkill(Skill.JAMMING, 2);
                CPUSkills.addSkill(Skill.CLUSTER_BOMB, 1);
                CPUSkills.setSkillPoints(8);
                break;
        }
    }

    // ========== MÉTODOS DE CONVERSIÓN DE DIFICULTAD ==========
    private CPUController.Difficulty convertDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return CPUController.Difficulty.EASY;
            case NORMAL:
                return CPUController.Difficulty.NORMAL;
            case HARD:
                return CPUController.Difficulty.HARD;
            case EXPERT:
                return CPUController.Difficulty.EXPERT;
            default:
                return CPUController.Difficulty.NORMAL;
        }
    }

    private static Difficulty convertFromCPUControllerDifficulty(CPUController.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return Difficulty.EASY;
            case NORMAL:
                return Difficulty.NORMAL;
            case HARD:
                return Difficulty.HARD;
            case EXPERT:
                return Difficulty.EXPERT;
            default:
                return Difficulty.NORMAL;
        }
    }

    // ========== MÉTODOS DE COLOCACIÓN DE BARCOS ==========
    /**
     * Verifica si un barco puede colocarse en la posición especificada
     */
    public boolean canPlaceShip(Ship ship, Coordinate coord, Direction direction) {
        if (ship == null || coord == null || direction == null) {
            System.err.println("❌ Parámetros inválidos para canPlaceShip");
            return false;
        }

        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, coord, direction);

            if (!areAllCoordinatesValid(shipCoordinates)) {
                return false;
            }

            if (hasShipOverlap(shipCoordinates)) {
                return false;
            }

            if (!respectsSeparationRules(shipCoordinates)) {
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en canPlaceShip: " + e.getMessage());
            return false;
        }
    }

    /**
     * Coloca un barco en el tablero usando el nuevo sistema de coordenadas
     * @param ship
     * @param startCoord
     * @param direction
     * @return 
     */
    public boolean placeShip(Ship ship, Coordinate startCoord, Direction direction) {
        if (!canPlaceShip(ship, startCoord, direction)) {
            return false;
        }

        try {
            List<Coordinate> shipCoordinates = calculateShipCoordinates(ship, startCoord, direction);

            // Establecer la posición en el objeto Ship
            ship.setPosition(shipCoordinates);

            // También agregar a las celdas del tablero
            for (Coordinate coord : shipCoordinates) {
                Cell cell = playerBoard.getCell(coord);
                if (cell != null) {
                    cell.setShip(ship);
                }
            }

            // Agregar a la lista de barcos del jugador si no está ya
            if (!playerShips.contains(ship)) {
                playerShips.add(ship);
            }

            System.out.println("✅ " + ship.getType().getName() + " colocado exitosamente en " + 
                             startCoord.aNotacion() + " (" + direction + ")");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al colocar barco: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calcula todas las coordenadas que ocuparía el barco
     */
    private List<Coordinate> calculateShipCoordinates(Ship ship, Coordinate start, Direction direction) {
        List<Coordinate> coordinates = new ArrayList<>();
        int size = ship.getType().getSize();

        int dx = 0, dy = 0;
        if (direction == Direction.HORIZONTAL) {
            dy = 1; // Horizontal: misma fila, columnas diferentes
        } else {
            dx = 1; // Vertical: misma columna, filas diferentes
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
            if (coord.getX() < 0 || coord.getX() >= Board.SIZE ||
                coord.getY() < 0 || coord.getY() >= Board.SIZE) {
                System.out.println("🚫 Coordenada fuera del tablero: " + coord.aNotacion());
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si hay superposición con barcos existentes
     */
    private boolean hasShipOverlap(List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            if (playerBoard.hasShipAt(coord)) {
                System.out.println("🚫 Superposición en: " + coord.aNotacion());
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica reglas de separación entre barcos (1 casilla de separación)
     */
    private boolean respectsSeparationRules(List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    int x = coord.getX() + dx;
                    int y = coord.getY() + dy;

                    if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                        Coordinate adjacent = new Coordinate(x, y);
                        if (playerBoard.hasShipAt(adjacent)) {
                            System.out.println("🚫 Barco muy cercano en: " + adjacent.aNotacion());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // ========== MÉTODOS DE BARCOS ==========
    /**
     * Obtiene el barco en una coordenada específica del tablero del jugador humano
     */
    public Ship getShipAt(Coordinate coord) {
        if (playerBoard == null) {
            System.err.println("❌ Tablero del jugador no inicializado");
            return null;
        }
        return playerBoard.getShipAt(coord);
    }

    /**
     * Obtiene el barco en una coordenada específica del tablero de la CPU
     */
    public Ship getShipAtCPU(Coordinate coord) {
        if (cpuBoard == null) {
            System.err.println("❌ Tablero de la CPU no inicializado");
            return null;
        }
        return cpuBoard.getShipAt(coord);
    }

    /**
     * Obtiene todos los barcos del jugador humano
     */
    public List<Ship> getHumanShips() {
        return humanPlayer != null ? humanPlayer.getShips() : new ArrayList<>();
    }

    /**
     * Obtiene todos los barcos de la CPU
     */
    public List<Ship> getCPUShips() {
        return cpuPlayer != null ? cpuPlayer.getShips() : new ArrayList<>();
    }
    

    /**
     * Verifica si hay un barco en la coordenada del jugador humano
     */
    public boolean hasShipAt(Coordinate coord) {
        return getShipAt(coord) != null;
    }

    /**
     * Verifica si hay un barco en la coordenada de la CPU
     */
    public boolean hasShipAtCPU(Coordinate coord) {
        return getShipAtCPU(coord) != null;
    }

    /**
     * Obtiene los barcos hundidos del jugador
     */
    public Set<Ship> getSunkShipsPlayer() {
        return playerShips.stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene los barcos hundidos de la CPU
     */
    public Set<Ship> getSunkShipsCPU() {
        return CPUShips.stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toSet());
    }

    // ========== MÉTODOS DE DISPARO ==========
    /**
     * Procesa un disparo del jugador humano
     */
    public ShotResult processPlayerShot(Coordinate target) {
        if (!playerTurn || gamePhase != GamePhase.IN_PLAY) {
            return ShotResult.INVALID;
        }

        if (!isValidCoordinate(target)) {
            return ShotResult.INVALID;
        }

        // Verificar si ya se disparó aquí
        Cell targetCell = cpuBoard.getCell(target);
        if (targetCell == null || targetCell.hasBeenShot()) {
            return ShotResult.ALREADY_SHOT;
        }

        ShotResult result = targetCell.shoot();
        elapsedTurns++;

        if (result.isHit()) {
            playerSkills.earnHitPoints();

            if (result.isSunk()) {
                Ship sunkShip = findSunkenShip(CPUShips, sunkShipsCPU);
                if (sunkShip != null) {
                    playerSkills.earnSinkingPoints();
                    lastSunkenShipCPU = sunkShip;
                    System.out.println("¡Hundiste un " + sunkShip.getType().getName() + "!");
                }
            }

            // Jugador mantiene el turno si impacta
            playerTurn = true;
        } else {
            // Cambiar turno a CPU si falla
            playerTurn = false;
        }

        // Verificar si el juego terminó
        checkGameEnd();

        return result;
    }

    /**
     * Procesa un disparo de la CPU
     * @return 
     */
    public ShotResult processCPUShot() {
        if (playerTurn || gamePhase != GamePhase.IN_PLAY || jammingActive) {
            if (jammingActive) {
                jammingActive = false; // Jamming solo afecta un turno
                playerTurn = true; // Jugador recupera el turno
                return ShotResult.INVALID;
            }
            return ShotResult.INVALID;
        }

        Coordinate target = ((CPU) cpuPlayer).chooseShotTarget(humanPlayer);
        lastShotCPU = target;

        if (!isValidCoordinate(target)) {
            return ShotResult.INVALID;
        }

        Cell targetCell = playerBoard.getCell(target);
        if (targetCell == null || targetCell.hasBeenShot()) {
            return ShotResult.ALREADY_SHOT;
        }

        ShotResult result = targetCell.shoot();
        cpuController.processResult(target, result);
        ((CPU) cpuPlayer).updateAfterShot(target, result);
        elapsedTurns++;

        if (result.isHit()) {
            CPUSkills.earnHitPoints();

            if (result.isSunk()) {
                Ship sunkShip = findSunkenShip(playerShips, sunkShipsPlayer);
                if (sunkShip != null) {
                    CPUSkills.earnSinkingPoints();
                    lastSunkenShipPlayer = sunkShip;
                    System.out.println("La CPU hundió tu " + sunkShip.getType().getName() + "!");
                }
            }

            // CPU mantiene el turno si impacta
            playerTurn = false;
        } else {
            // Cambiar turno a jugador si falla
            playerTurn = true;
        }

        // Verificar si el juego terminó
        checkGameEnd();

        return result;
    }

    /**
     * Encuentra el último barco hundido
     */
    private Ship findSunkenShip(List<Ship> ships, Set<Ship> counted) {
        for (Ship ship : ships) {
            if (ship.isSunk() && !counted.contains(ship)) {
                counted.add(ship);
                return ship;
            }
        }
        return null;
    }

    /**
     * Verifica si el juego ha terminado
     */
    private void checkGameEnd() {
        boolean cpuLost = cpuBoard.allShipsSunk();
        boolean playerLost = playerBoard.allShipsSunk();

        if (cpuLost) {
            gamePhase = GamePhase.PLAYER_WIN;
            if (currentProfile != null) {
                currentProfile.updateFromGame(this, true);
            }
            System.out.println("🎉 ¡VICTORIA! " + humanPlayer.getName() + " ganó la partida");
        } else if (playerLost) {
            gamePhase = GamePhase.CPU_WIN;
            if (currentProfile != null) {
                currentProfile.updateFromGame(this, false);
            }
            System.out.println("💀 DERROTA! La CPU ganó la partida");
        }
    }

    // ========== MÉTODOS DE HABILIDADES ==========
    public SkillResult usePlayerSkill(Skill skill, Object... parameters) {
        if (!playerSkills.canUseSkill(skill)) {
            return new SkillResult(false, "No puedes usar esta habilidad");
        }

        SkillResult result = null;

        try {
            switch (skill) {
                case SONAR:
                    result = skillController.useSonar((Coordinate) parameters[0]);
                    break;
                case RADAR:
                    result = skillController.useRadar();
                    break;
                case DRONE:
                    result = skillController.useDrone((Boolean) parameters[0], (Integer) parameters[1]);
                    break;
                case GUIDED_MISSILE:
                    result = skillController.useGuidedMissile((Coordinate) parameters[0]);
                    break;
                case CLUSTER_BOMB:
                    result = skillController.useClusterBomb((Coordinate) parameters[0]);
                    break;
                case JAMMING:
                    result = skillController.useJamming();
                    break;
                case REPAIR:
                    result = skillController.useRepair((Coordinate) parameters[0]);
                    break;
                case CAMOUFLAGE:
                    result = skillController.useCamouflage((Ship) parameters[0],
                            (Coordinate) parameters[1], (Direction) parameters[2]);
                    break;
                default:
                    return new SkillResult(false, "Habilidad no implementada");
            }

            if (result != null && result.isSuccessful()) {
                playerSkills.useSkill(skill);
            }

        } catch (Exception e) {
            result = new SkillResult(false, "Error al usar habilidad: " + e.getMessage());
        }

        return result;
    }

    /**
     * Habilidades de la CPU
     */
    public void useCPUSkill() {
        Skill cpuSkill = skillController.decideCPUSkill();
        if (cpuSkill != null && CPUSkills.canUseSkill(cpuSkill)) {
            SkillResult result = skillController.executeCPUSkill(cpuSkill);
            if (result.isSuccessful()) {
                CPUSkills.useSkill(cpuSkill);
            }
        }
    }

    // ========== MÉTODOS DE CONFIGURACIÓN DE FLOTAS ==========
    /**
     * Configura la flota estándar del juego
     */
    public void setStandardFleet() {
        List<Ship> standardFleet = createStandardFleet();
        setBothFleets(standardFleet);
        System.out.println("🎯 Flota estándar configurada");
    }

    /**
     * Establece la misma flota para ambos jugadores
     * @param fleet
     */
    public void setBothFleets(List<Ship> fleet) {
        if (fleet == null || fleet.isEmpty()) {
            throw new IllegalArgumentException("La flota no puede ser nula o vacía");
        }

        try {
            // Crear copias independientes para cada jugador
            List<Ship> playerFleetCopy = createFleetCopy(fleet);
            List<Ship> cpuFleetCopy = createFleetCopy(fleet);

            // Limpiar flotas existentes
            this.playerShips.clear();
            this.CPUShips.clear();

            // Asignar nuevas flotas
            this.playerShips.addAll(playerFleetCopy);
            this.CPUShips.addAll(cpuFleetCopy);

            // Reiniciar el estado de los barcos
            resetShipsState(this.playerShips);
            resetShipsState(this.CPUShips);

            // Colocar barcos de la CPU automáticamente
            if (cpuPlayer instanceof CPU) {
                ((CPU) cpuPlayer).placeShipsRandomly();
            }

            System.out.println("🔄 Flota asignada a ambos jugadores: " + fleet.size() + " barcos");

        } catch (Exception e) {
            System.err.println("❌ Error al establecer flota para ambos jugadores: " + e.getMessage());
            throw new RuntimeException("No se pudo configurar las flotas", e);
        }
    }

    /**
     * Crea una copia independiente de una flota
     */
    private List<Ship> createFleetCopy(List<Ship> originalFleet) {
        List<Ship> copy = new ArrayList<>();
        for (Ship originalShip : originalFleet) {
            Ship newShip = new Ship(originalShip.getType());
            copy.add(newShip);
        }
        return copy;
    }

    /**
     * Reinicia el estado de todos los barcos en una flota
     */
    private void resetShipsState(List<Ship> ships) {
        for (Ship ship : ships) {
            ship.reset();
        }
    }

    

    // ========== MÉTODOS AUXILIARES ==========
    private boolean isValidCoordinate(Coordinate coord) {
        return coord != null && 
               coord.getX() >= 0 && coord.getX() < Board.SIZE && 
               coord.getY() >= 0 && coord.getY() < Board.SIZE;
    }

    /**
     * Obtiene las coordenadas donde el jugador ha impactado
     * @return 
     */
    public List<Coordinate> getPlayerHitCoordinates() {
        List<Coordinate> hits = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Coordinate coord = new Coordinate(i, j);
                Cell cell = cpuBoard.getCell(coord);
                if (cell != null && cell.hasBeenShot() && cell.isHit()) {
                    hits.add(coord);
                }
            }
        }
        return hits;
    }

    /**
     * Obtiene la racha de aciertos del jugador
     * @return 
     */
    public int getPlayerHitStreak() {
        // Implementación simple - contar aciertos consecutivos en los últimos turnos
        int streak = 0;
        // Esta es una implementación básica, puedes mejorarla
        return Math.min(streak, 5); // Máximo 5 para balance
    }

    // ========== GETTERS Y SETTERS ==========
    public int getElapsedTurns() {
        return elapsedTurns;
    }

    public void setElapsedTurns(int turns) {
        this.elapsedTurns = turns;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase phase) {
        this.gamePhase = phase;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean turno) {
        this.playerTurn = turno;
    }

    public void setJammingActive(boolean active) {
        this.jammingActive = active;
    }

    public boolean isGameOver() {
        return gamePhase == GamePhase.PLAYER_WIN || gamePhase == GamePhase.CPU_WIN;
    }

    public boolean playerWin() {
        return gamePhase == GamePhase.PLAYER_WIN;
    }

    public boolean cpuWin() {
        return gamePhase == GamePhase.CPU_WIN;
    }

    public int getRemainingPlayerShips() {
        return (int) playerShips.stream().filter(ship -> !ship.isSunk()).count();
    }

    public int getRemainingCPUShips() {
        return (int) CPUShips.stream().filter(ship -> !ship.isSunk()).count();
    }

    public List<Ship> getPlayerShipsNotSunk() {
        return playerShips.stream().filter(ship -> !ship.isSunk()).collect(Collectors.toList());
    }

    public List<Ship> getCPUShipsNotSunk() {
        return CPUShips.stream().filter(ship -> !ship.isSunk()).collect(Collectors.toList());
    }
     

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getCpuBoard() {
        return cpuBoard;
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }

    public Player getCpuPlayer() {
        return cpuPlayer;
    }

    public Coordinate getLastShotCPU() {
        return lastShotCPU;
    }

    public Ship getLastSunkShipCPU() {
        return lastSunkenShipCPU;
    }

    public Ship getLastSunkShipPlayer() {
        return lastSunkenShipPlayer;
    }

    public List<Ship> getPlayerShips() {
        return new ArrayList<>(playerShips);
    }

   

    public CPUController getCpuController() {
        return cpuController;
    }

    public SkillSystem getPlayerSkills() {
        return playerSkills;
    }

    public SkillSystem getCPUSkills() {
        return CPUSkills;
    }

    public Game getGame() {
        return game;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        setupSkillsByDifficulty(); // Reconfigurar habilidades al cambiar dificultad
    }

    // ========== MÉTODOS DE REINICIO ==========
    /**
     * Reinicia el juego completo
     */
    public void reset() {
        // Reiniciar barcos
        for (Ship ship : playerShips) {
            ship.reset();
        }
        for (Ship ship : CPUShips) {
            ship.reset();
        }

        // Limpiar registros de hundimientos
        sunkShipsCPU.clear();
        sunkShipsPlayer.clear();
        lastSunkenShipCPU = null;
        lastSunkenShipPlayer = null;

        // Reiniciar habilidades
        playerSkills.reset();
        CPUSkills.reset();
        setupSkillsByDifficulty();

        // Reiniciar tableros
        playerBoard.reset();
        cpuBoard.reset();

        // Reiniciar controladores
        cpuController.reset();

        // Reiniciar contadores
        elapsedTurns = 0;
        playerTurn = true;
        gamePhase = GamePhase.IN_PLAY;
        jammingActive = false;

        // Recolocar barcos de la CPU
        ((CPU) cpuPlayer).placeShipsRandomly();

        System.out.println("🔄 Juego reiniciado");
    }

    /**
     * Verifica si una coordenada ha sido disparada en el tablero del jugador
     * @param coord
     * @return 
     */
    public boolean hasBeenShotAt(Coordinate coord) {
        if (playerBoard == null) return false;
        Cell cell = playerBoard.getCell(coord);
        return cell != null && cell.hasBeenShot();
    }

    /**
     * Verifica si se puede disparar en una coordenada del tablero de la CPU
     * @param coord
     * @return 
     */
    public boolean canShootAtCPU(Coordinate coord) {
        if (cpuBoard == null) return false;
        Cell cell = cpuBoard.getCell(coord);
        return cell != null && !cell.hasBeenShot();
    }
    // ========== MÉTODOS DE CONFIGURACIÓN DE FLOTAS PREDEFINIDAS ==========
    
   

    /**
     * Configura flota especial con barcos únicos y habilidades mejoradas
     */
    public void setSpecialFleet() {
        List<Ship> specialFleet = createSpecialFleet();
        setBothFleets(specialFleet);
        enhanceSkillsForSpecialFleet();
        System.out.println("🚀 Flota especial configurada - Barcos únicos con habilidades mejoradas");
    }

    /**
     * Configura flota táctica con barcos balanceados y habilidades estratégicas
     */
    public void setTacticalFleet() {
        List<Ship> tacticalFleet = createTacticalFleet();
        setBothFleets(tacticalFleet);
        setupSkillsForTacticalGame();
        System.out.println("🎓 Flota táctica configurada - Enfoque en estrategia y habilidades");
    }

    /**
     * Configura flota asimétrica (diferente para jugador y CPU)
     */
    public void setAsymmetricFleet() {
        List<Ship> playerFleet = createAsymmetricPlayerFleet();
        List<Ship> cpuFleet = createAsymmetricCPUFleet();
        setAsymmetricFleets(playerFleet, cpuFleet);
        setupSkillsForAsymmetricGame();
        System.out.println("⚖️ Flota asimétrica configurada - Jugador: ofensivo, CPU: defensivo");
    }

    /**
     * Configura flota mínima para partidas rápidas
     */
    public void setMinimalFleet() {
        List<Ship> minimalFleet = createMinimalFleet();
        setBothFleets(minimalFleet);
        setupSkillsForLightningGame();
        System.out.println("⚡ Flota mínima configurada - Partidas rápidas y dinámicas");
    }

    /**
     * Configura flota enjambre con muchos barcos pequeños
     */
    public void setSwarmFleet() {
        List<Ship> swarmFleet = createSwarmFleet();
        setBothFleets(swarmFleet);
        setupSkillsForSwarmGame();
        System.out.println("🐝 Flota enjambre configurada - Muchos barcos pequeños");
    }
    /**
 * Establece flotas asimétricas (diferentes para jugador y CPU)
 */
public void setAsymmetricFleets(List<Ship> playerFleet, List<Ship> cpuFleet) {
    if (playerFleet == null || cpuFleet == null) {
        throw new IllegalArgumentException("Las flotas no pueden ser nulas");
    }

    if (playerFleet.isEmpty() || cpuFleet.isEmpty()) {
        throw new IllegalArgumentException("Las flotas no pueden estar vacías");
    }

    try {
        // Validar ambas flotas
        validateFleet(playerFleet, "Jugador");
        validateFleet(cpuFleet, "CPU");

        // Crear copias independientes
        List<Ship> playerFleetCopy = createFleetCopy(playerFleet);
        List<Ship> cpuFleetCopy = createFleetCopy(cpuFleet);

        // Limpiar flotas existentes
        this.playerShips.clear();
        this.CPUShips.clear();

        // Asignar nuevas flotas
        this.playerShips.addAll(playerFleetCopy);
        this.CPUShips.addAll(cpuFleetCopy);

        // Reiniciar el estado de los barcos
        resetShipsState(this.playerShips);
        resetShipsState(this.CPUShips);

        // Colocar barcos de la CPU automáticamente
        if (cpuPlayer instanceof CPU) {
            ((CPU) cpuPlayer).placeShipsRandomly();
        }

        // Registrar la configuración asimétrica
        logAsymmetricFleetAssignment(playerFleet, cpuFleet);

        // Analizar el balance de las flotas asimétricas
        analyzeAsymmetricBalance();

        System.out.println("⚖️ Flotas asimétricas configuradas: Jugador=" + playerFleet.size()
                + " barcos, CPU=" + cpuFleet.size() + " barcos");

    } catch (Exception e) {
        System.err.println("❌ Error al establecer flotas asimétricas: " + e.getMessage());
        throw new RuntimeException("No se pudo configurar las flotas asimétricas", e);
    }
}

/**
 * Valida que una flota sea válida
 */
private void validateFleet(List<Ship> fleet, String owner) {
    if (fleet == null) {
        throw new IllegalArgumentException(owner + ": La flota no puede ser nula");
    }

    if (fleet.isEmpty()) {
        throw new IllegalArgumentException(owner + ": La flota no puede estar vacía");
    }

    Set<ShipType> seenTypes = new HashSet<>();
    int totalCells = 0;
    int maxAllowedCells = getMaxFleetCells();

    for (Ship ship : fleet) {
        if (ship == null) {
            throw new IllegalArgumentException(owner + ": La flota contiene un barco nulo");
        }

        ShipType type = ship.getType();

        // Verificar tipo duplicado (opcional, dependiendo de las reglas)
        if (seenTypes.contains(type)) {
            System.out.println("⚠️  " + owner + ": Múltiples barcos de tipo " + type.getName());
        }
        seenTypes.add(type);

        // Verificar que el barco no esté ya en otro tablero
        if (ship.isPlaced()) {
            System.out.println("🔄 " + owner + ": Reiniciando barco " + type.getName() + " que ya estaba colocado");
            ship.reset();
        }

        // Calcular tamaño total
        totalCells += type.getSize();
    }

    // Validar tamaño total de la flota
    if (totalCells > maxAllowedCells) {
        System.out.println("📏 " + owner + ": Flota muy grande (" + totalCells
                + " casillas), máximo recomendado: " + maxAllowedCells);
    }

    if (totalCells < getMinFleetCells()) {
        System.out.println("📏 " + owner + ": Flota muy pequeña (" + totalCells
                + " casillas), mínimo recomendado: " + getMinFleetCells());
    }
}



/**
 * Registra la asignación de flotas asimétricas
 */
private void logAsymmetricFleetAssignment(List<Ship> playerFleet, List<Ship> cpuFleet) {
    // Información de la flota del jugador
    Map<ShipType, Integer> playerComposition = new HashMap<>();
    int playerTotalCells = 0;

    for (Ship ship : playerFleet) {
        ShipType type = ship.getType();
        playerComposition.merge(type, 1, Integer::sum);
        playerTotalCells += type.getSize();
    }

    // Información de la flota de la CPU
    Map<ShipType, Integer> cpuComposition = new HashMap<>();
    int cpuTotalCells = 0;

    for (Ship ship : cpuFleet) {
        ShipType type = ship.getType();
        cpuComposition.merge(type, 1, Integer::sum);
        cpuTotalCells += type.getSize();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("📊 CONFIGURACIÓN ASIMÉTRICA:\n");

    // Flota del jugador
    sb.append("👤 JUGADOR - ").append(playerFleet.size()).append(" barcos, ").append(playerTotalCells).append(" casillas: ");
    for (Map.Entry<ShipType, Integer> entry : playerComposition.entrySet()) {
        sb.append(entry.getValue()).append("x ").append(entry.getKey().getName()).append(", ");
    }
    if (sb.length() > 2) {
        sb.setLength(sb.length() - 2);
    }

    sb.append("\n");

    // Flota de la CPU
    sb.append("🤖 CPU - ").append(cpuFleet.size()).append(" barcos, ").append(cpuTotalCells).append(" casillas: ");
    for (Map.Entry<ShipType, Integer> entry : cpuComposition.entrySet()) {
        sb.append(entry.getValue()).append("x ").append(entry.getKey().getName()).append(", ");
    }
    if (sb.length() > 2) {
        sb.setLength(sb.length() - 2);
    }

    System.out.println(sb.toString());
}

/**
 * Analiza el balance de las flotas asimétricas
 */
private void analyzeAsymmetricBalance() {
    int playerPower = calculateFleetPower(playerShips);
    int cpuPower = calculateFleetPower(CPUShips);
    int playerCells = playerShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();
    int cpuCells = CPUShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();

    double powerRatio = (double) Math.min(playerPower, cpuPower) / Math.max(playerPower, cpuPower);
    double cellRatio = (double) Math.min(playerCells, cpuCells) / Math.max(playerCells, cpuCells);

    String balanceLevel;
    if (powerRatio >= 0.8) {
        balanceLevel = "⚖️ BALANCEADO";
    } else if (powerRatio >= 0.6) {
        balanceLevel = "⚡ MODERADAMENTE ASIMÉTRICO";
    } else if (powerRatio >= 0.4) {
        balanceLevel = "🎯 ALTAMENTE ASIMÉTRICO";
    } else {
        balanceLevel = "💀 EXTREMADAMENTE ASIMÉTRICO";
    }

    System.out.println("📈 Análisis de balance asimétrico:");
    System.out.println("   Poder - Jugador: " + playerPower + ", CPU: " + cpuPower
            + ", Ratio: " + String.format("%.2f", powerRatio));
    System.out.println("   Casillas - Jugador: " + playerCells + ", CPU: " + cpuCells
            + ", Ratio: " + String.format("%.2f", cellRatio));
    System.out.println("   Nivel: " + balanceLevel);

    // Sugerir estrategias basadas en la asimetría
    suggestAsymmetricStrategies(playerPower, cpuPower, playerCells, cpuCells);
}

/**
 * Sugiere estrategias basadas en la configuración asimétrica
 */
private void suggestAsymmetricStrategies(int playerPower, int cpuPower, int playerCells, int cpuCells) {
    System.out.println("🎯 Estrategias sugeridas:");

    if (playerPower > cpuPower) {
        // Jugador tiene ventaja de poder
        if (playerCells > cpuCells) {
            System.out.println("   👤 JUGADOR: Ataque agresivo - Usa tu superioridad numérica");
            System.out.println("   🤖 CPU: Defensa estratégica - Enfócate en barcos grandes del jugador");
        } else {
            System.out.println("   👤 JUGADOR: Ataque preciso - Tus barcos son más poderosos pero menos numerosos");
            System.out.println("   🤖 CPU: Guerra de desgaste - Aprovecha tu mayor número de blancos");
        }
    } else {
        // CPU tiene ventaja de poder
        if (cpuCells > playerCells) {
            System.out.println("   👤 JUGADOR: Defensa inteligente - Evita confrontaciones directas");
            System.out.println("   🤖 CPU: Ataque constante - Presiona con tu superioridad numérica");
        } else {
            System.out.println("   👤 JUGADOR: Tácticas de guerrilla - Ataca puntos débiles");
            System.out.println("   🤖 CPU: Ataque concentrado - Enfócate en eliminar barcos clave");
        }
    }
}

/**
 * Obtiene el número máximo de casillas permitidas para una flota
 */
private int getMaxFleetCells() {
    // Basado en un tablero 10x10, dejando espacio para maniobras
    return 25;
}

/**
 * Obtiene el número mínimo de casillas recomendadas para una flota
 */
private int getMinFleetCells() {
    // Mínimo para que el juego sea interesante
    return 8;
}

    // ========== MÉTODOS DE CREACIÓN DE FLOTAS ==========
    
    private List<Ship> createStandardFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.CARRIER));      // 5 casillas
        fleet.add(new Ship(ShipType.BATTLESHIP));   // 4 casillas  
        fleet.add(new Ship(ShipType.CRUISER));      // 3 casillas
        fleet.add(new Ship(ShipType.SUBMARINE));    // 3 casillas
        fleet.add(new Ship(ShipType.DESTROYER));    // 2 casillas
        return fleet;
    }

    private List<Ship> createSpecialFleet() {
        List<Ship> fleet = new ArrayList<>();
        try {
            // Flota especial con barcos no tradicionales
            fleet.add(createSpecialShip("Portaaviones Ancho", 6));
            fleet.add(createSpecialShip("Acorazado L", 5));
            fleet.add(new Ship(ShipType.CRUISER));
            fleet.add(new Ship(ShipType.SUBMARINE));
            fleet.add(createSpecialShip("Patrullera", 2));
        } catch (Exception e) {
            System.err.println("❌ Error creando flota especial, usando flota mejorada: " + e.getMessage());
            return createEnhancedFleet();
        }
        return fleet;
    }

    private List<Ship> createTacticalFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.DESTROYER));
        return fleet;
    }

    private List<Ship> createAsymmetricPlayerFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        return fleet;
    }

    private List<Ship> createAsymmetricCPUFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        return fleet;
    }

    private List<Ship> createMinimalFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.FRIGATE));
        return fleet;
    }

    private List<Ship> createSwarmFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.CRUISER));
        return fleet;
    }

    private List<Ship> createEnhancedFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.DESTROYER));
        return fleet;
    }

    // ========== MÉTODOS AUXILIARES PARA FLOTAS ESPECIALES ==========
    
    private Ship createSpecialShip(String specialType, int size) {
        Ship ship;
        switch (specialType) {
            case "Portaaviones Ancho":
                ship = new Ship(ShipType.CARRIER);
                break;
            case "Acorazado L":
                ship = new Ship(ShipType.BATTLESHIP);
                break;
            case "Patrullera":
                ship = new Ship(ShipType.FRIGATE);
                break;
            default:
                ship = new Ship(ShipType.CRUISER);
        }
        return ship;
    }

    // ========== MÉTODOS DE CONFIGURACIÓN DE HABILIDADES ==========
    
    private void enhanceSkillsForSpecialFleet() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 4);
        skills.addSkill(Skill.RADAR, 3);
        skills.addSkill(Skill.DRONE, 3);
        skills.addSkill(Skill.GUIDED_MISSILE, 2);
        skills.setSkillPoints(8);
    }

    private void setupSkillsForTacticalGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 2);
        skills.addSkill(Skill.RADAR, 2);
        skills.addSkill(Skill.DRONE, 2);
        skills.addSkill(Skill.GUIDED_MISSILE, 1);
        skills.addSkill(Skill.CLUSTER_BOMB, 1);
        skills.addSkill(Skill.JAMMING, 1);
        skills.setSkillPoints(8);
    }

    private void setupSkillsForAsymmetricGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 3);
        skills.addSkill(Skill.JAMMING, 2);
        skills.addSkill(Skill.REPAIR, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 2);
        skills.setSkillPoints(7);
    }

    private void setupSkillsForLightningGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.GUIDED_MISSILE, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 3);
        skills.addSkill(Skill.DRONE, 1);
        skills.setSkillPoints(6);
    }

    private void setupSkillsForSwarmGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();
        skills.addSkill(Skill.SONAR, 4);
        skills.addSkill(Skill.RADAR, 3);
        skills.addSkill(Skill.DRONE, 3);
        skills.setSkillPoints(8);
    }

    // ========== MÉTODOS DE INFORMACIÓN ==========
    
    /**
     * Verifica si ambas flotas están listas para jugar
     */
    public boolean areFleetsReady() {
        boolean playerReady = !playerShips.isEmpty();
        boolean cpuReady = !CPUShips.isEmpty();

        if (!playerReady) {
            System.err.println("❌ Flota del jugador no configurada");
        }
        if (!cpuReady) {
            System.err.println("❌ Flota de la CPU no configurada");
        }

        return playerReady && cpuReady;
    }

    /**
     * Obtiene información sobre las flotas
     */
    public String getFleetInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DE FLOTAS ===\n");
        
        int playerCells = playerShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();
        int cpuCells = CPUShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();
        
        info.append("Jugador: ").append(playerShips.size()).append(" barcos (").append(playerCells).append(" casillas)\n");
        info.append("CPU: ").append(CPUShips.size()).append(" barcos (").append(cpuCells).append(" casillas)");

        return info.toString();
    }

    /**
     * Verifica si las flotas están balanceadas
     */
    public boolean areFleetsBalanced() {
        int playerPower = calculateFleetPower(playerShips);
        int cpuPower = calculateFleetPower(CPUShips);

        double balanceRatio = (double) Math.min(playerPower, cpuPower) / Math.max(playerPower, cpuPower);
        boolean balanced = balanceRatio >= 0.7;

        System.out.println("⚖️ Balance de flotas: Jugador=" + playerPower
                + ", CPU=" + cpuPower + ", Ratio=" + String.format("%.2f", balanceRatio)
                + ", Balanceado=" + balanced);

        return balanced;
    }

    /**
     * Calcula el poder de una flota
     */
    private int calculateFleetPower(List<Ship> fleet) {
        return fleet.stream()
                .mapToInt(ship -> {
                    int basePower = ship.getType().getSize();
                    if (ship.getType().getSize() >= 4) {
                        return basePower * 2;
                    } else if (ship.getType().getSize() >= 3) {
                        return basePower + 1;
                    }
                    return basePower;
                })
                .sum();
    }
    
    
    
}