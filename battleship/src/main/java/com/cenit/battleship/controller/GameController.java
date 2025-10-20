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

    private Board playerBoard;
    private Board cpuBoard;
    private Player humanPlayer;
    private Player cpuPlayer;
    private PlayerProfile currentProfile;

    // ========== CONSTRUCTORES ==========
    public GameController(PlayerProfile profile, Difficulty difficulty) {
        this.currentProfile = profile;

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

        // Colocar barcos de la CPU automáticamente
        ((CPU) cpuPlayer).placeShipsRandomly();

        initializeShips();
    }

    public GameController() {
        // Llama al constructor completo con valores por defecto
        this(new PlayerProfile("Jugador"), Difficulty.NORMAL); // O la dificultad por defecto que quieras
    }

    public GameController(CPUController.Difficulty difficultyCPU) {
        this(new PlayerProfile("Jugador"), convertFromCPUControllerDifficulty(difficultyCPU));
    }

    // Método setter para la dificultad
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        // Aquí puedes agregar lógica adicional cuando cambia la dificultad
        System.out.println("🔄 Dificultad configurada a: " + difficulty.getDisplayName());
    }

    // Getter para la dificultad
    public Difficulty getDifficulty() {
        return difficulty;
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
                cell.setShip(ship);
            }

            // Agregar a la lista de barcos del jugador si no está ya
            if (!playerShips.contains(ship)) {
                playerShips.add(ship);
            }

            System.out.println("✅ " + ship.getType().getName() + " colocado exitosamente");
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
     * Obtiene los barcos hundidos del jugador
     *
     * @return Set de barcos hundidos del jugador
     */
    public Set<Ship> getSunkShipsPlayer() {
        return playerBoard.getShips().stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene los barcos hundidos de la CPU
     *
     * @return Set de barcos hundidos de la CPU
     */
    public Set<Ship> getSunkShipsCPU() {
        return cpuBoard.getShips().stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toSet());
    }

    /**
     * Verifica que todas las coordenadas estén dentro del tablero
     */
    private boolean areAllCoordinatesValid(List<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            if (coord.getX() < 0 || coord.getX() >= Board.SIZE
                    || coord.getY() < 0 || coord.getY() >= Board.SIZE) {
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
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    try {
                        Coordinate adjacent = coord.desplazar(dx, dy);
                        if (playerBoard.hasShipAt(adjacent)) {
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

    // ========== MÉTODOS PARA CONFIGURAR FLOTAS PREDEFINIDAS ==========
    /**
     * Configura la flota estándar del juego (flota clásica)
     */
    public void setStandardFleet() {
        List<Ship> standardFleet = createStandardFleet();
        setBothFleets(standardFleet);
        System.out.println("🎯 Flota estándar configurada - Flota clásica balanceada");
    }

    /**
     * Configura flota especial con barcos únicos y habilidades mejoradas
     */
    public void setSpecialFleet() {
        List<Ship> specialFleet = createSpecialFleet();
        setBothFleets(specialFleet);

        // Habilidades mejoradas para modo especial
        enhanceSkillsForSpecialFleet();

        System.out.println("🚀 Flota especial configurada - Barcos únicos con habilidades mejoradas");
    }

    /**
     * Configura flota mínima para partidas rápidas
     */
    public void setMinimalFleet() {
        List<Ship> minimalFleet = createMinimalFleet();
        setBothFleets(minimalFleet);

        // Habilidades ofensivas para partidas rápidas
        setupSkillsForLightningGame();

        System.out.println("⚡ Flota mínima configurada - Partidas rápidas y dinámicas");
    }

    /**
     * Configura flota enjambre con muchos barcos pequeños
     */
    public void setSwarmFleet() {
        List<Ship> swarmFleet = createSwarmFleet();
        setBothFleets(swarmFleet);

        // Habilidades de exploración para encontrar muchos objetivos
        setupSkillsForSwarmGame();

        System.out.println("🐝 Flota enjambre configurada - Muchos barcos pequeños");
    }

    /**
     * Configura flota táctica con barcos balanceados y habilidades estratégicas
     */
    public void setTacticalFleet() {
        List<Ship> tacticalFleet = createTacticalFleet();
        setBothFleets(tacticalFleet);

        // Habilidades tácticas variadas
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

        // Habilidades específicas para flota asimétrica
        setupSkillsForAsymmetricGame();

        System.out.println("⚖️ Flota asimétrica configurada - Jugador: ofensivo, CPU: defensivo");
    }

    /**
     * Establece la misma flota para ambos jugadores (jugador y CPU)
     *
     * @param fleet Lista de barcos que se asignará a ambos jugadores
     */
    public void setBothFleets(List<Ship> fleet) {
        if (fleet == null || fleet.isEmpty()) {
            throw new IllegalArgumentException("La flota no puede ser nula o vacía");
        }

        try {
            // Validar la flota antes de asignarla
            validateFleet(fleet, "Ambos jugadores");

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

            // Registrar la configuración
            logFleetAssignment(fleet, "Ambos jugadores");

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
            // Crear nuevo barco del mismo tipo
            Ship newShip = new Ship(originalShip.getType());
            copy.add(newShip);
        }

        return copy;
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
     * Establece flotas asimétricas (diferentes para jugador y CPU)
     *
     * @param playerFleet Flota para el jugador
     * @param cpuFleet Flota para la CPU
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

// ========== MÉTODOS DE CONFIGURACIÓN ASIMÉTRICA PREDEFINIDA ==========
    /**
     * Configura flota asimétrica ofensiva (jugador) vs defensiva (CPU)
     */
    public void setOffensiveVsDefensiveFleets() {
        List<Ship> offensiveFleet = createOffensiveFleet();
        List<Ship> defensiveFleet = createDefensiveFleet();
        setAsymmetricFleets(offensiveFleet, defensiveFleet);
    }

    /**
     * Configura flota asimétrica de cantidad vs calidad
     */
    public void setQuantityVsQualityFleets() {
        List<Ship> quantityFleet = createQuantityFleet();
        List<Ship> qualityFleet = createQualityFleet();
        setAsymmetricFleets(quantityFleet, qualityFleet);
    }

    /**
     * Configura flota asimétrica rápida vs poderosa
     */
    public void setSpeedVsPowerFleets() {
        List<Ship> speedFleet = createSpeedFleet();
        List<Ship> powerFleet = createPowerFleet();
        setAsymmetricFleets(speedFleet, powerFleet);
    }

// ========== MÉTODOS DE CREACIÓN DE FLOTAS ASIMÉTRICAS PREDEFINIDAS ==========
    /**
     * Crea flota ofensiva (muchos barcos pequeños y rápidos)
     */
    private List<Ship> createOffensiveFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota ofensiva: muchos barcos pequeños para ataques rápidos
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.SUBMARINE));

        System.out.println("⚔️ Flota ofensiva creada: 7 barcos pequeños y rápidos");
        return fleet;
    }

    /**
     * Crea flota defensiva (pocos barcos grandes y resistentes)
     */
    private List<Ship> createDefensiveFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota defensiva: pocos barcos grandes y resistentes
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.CRUISER));

        System.out.println("🛡️ Flota defensiva creada: 4 barcos grandes y resistentes");
        return fleet;
    }

    /**
     * Crea flota de cantidad (muchos barcos)
     */
    private List<Ship> createQuantityFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota de cantidad: máxima cantidad de barcos
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.CRUISER));

        System.out.println("🔢 Flota de cantidad creada: 9 barcos diversos");
        return fleet;
    }

    /**
     * Crea flota de calidad (barcos poderosos)
     */
    private List<Ship> createQualityFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota de calidad: barcos más poderosos pero menos numerosos
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.BATTLESHIP));

        System.out.println("⭐ Flota de calidad creada: 3 barcos poderosos");
        return fleet;
    }

    /**
     * Crea flota rápida (barcos pequeños y ágiles)
     */
    private List<Ship> createSpeedFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota rápida: barcos pequeños y ágiles
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));

        System.out.println("💨 Flota rápida creada: 6 barcos ágiles");
        return fleet;
    }

    /**
     * Crea flota poderosa (barcos grandes y lentos)
     */
    private List<Ship> createPowerFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota poderosa: barcos grandes y lentos pero poderosos
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.CRUISER));

        System.out.println("💪 Flota poderosa creada: 4 barcos grandes y fuertes");
        return fleet;
    }

// ========== MÉTODOS DE INFORMACIÓN ESPECÍFICA PARA FLOTAS ASIMÉTRICAS ==========
    /**
     * Obtiene un análisis detallado de la asimetría actual
     */
    public String getAsymmetryAnalysis() {
        int playerPower = calculateFleetPower(playerShips);
        int cpuPower = calculateFleetPower(CPUShips);
        int playerCells = playerShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();
        int cpuCells = CPUShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();

        double powerRatio = (double) playerPower / cpuPower;
        double cellRatio = (double) playerCells / cpuCells;

        StringBuilder analysis = new StringBuilder();
        analysis.append("=== ANÁLISIS DE ASIMETRÍA ===\n");

        analysis.append("Poder: Jugador ").append(playerPower).append(" vs CPU ").append(cpuPower);
        analysis.append(" (").append(String.format("%.2f", powerRatio)).append(" ratio)\n");

        analysis.append("Casillas: Jugador ").append(playerCells).append(" vs CPU ").append(cpuCells);
        analysis.append(" (").append(String.format("%.2f", cellRatio)).append(" ratio)\n");

        analysis.append("Barcos: Jugador ").append(playerShips.size()).append(" vs CPU ").append(CPUShips.size()).append("\n");

        // Determinar ventaja
        if (powerRatio > 1.2) {
            analysis.append("🎯 VENTAJA: JUGADOR (poder superior)\n");
        } else if (powerRatio < 0.8) {
            analysis.append("🎯 VENTAJA: CPU (poder superior)\n");
        } else {
            analysis.append("⚖️ EQUILIBRADO (poder similar)\n");
        }

        if (cellRatio > 1.2) {
            analysis.append("🔢 VENTAJA: JUGADOR (más objetivos)\n");
        } else if (cellRatio < 0.8) {
            analysis.append("🔢 VENTAJA: CPU (más objetivos)\n");
        } else {
            analysis.append("🔢 EQUILIBRADO (objetivos similares)\n");
        }

        return analysis.toString();
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

    /**
     * Reinicia el estado de todos los barcos en una flota
     */
    private void resetShipsState(List<Ship> ships) {
        for (Ship ship : ships) {
            ship.reset();
        }
    }

    /**
     * Registra la asignación de flota
     */
    private void logFleetAssignment(List<Ship> fleet, String owner) {
        Map<ShipType, Integer> composition = new HashMap<>();
        int totalCells = 0;

        for (Ship ship : fleet) {
            ShipType type = ship.getType();
            composition.merge(type, 1, Integer::sum);
            totalCells += type.getSize();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Composición de flota para ").append(owner).append(": ");

        for (Map.Entry<ShipType, Integer> entry : composition.entrySet()) {
            sb.append(entry.getValue()).append("x ").append(entry.getKey().getName()).append(", ");
        }

        // Remover la última coma y espacio
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        sb.append(" | Total: ").append(totalCells).append(" casillas");

        System.out.println(sb.toString());
    }

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
     * Obtiene información sobre la flota del jugador
     */
    public String getPlayerFleetInfo() {
        return getFleetInfo(playerShips, "Jugador");
    }

    /**
     * Obtiene información sobre la flota de la CPU
     */
    public String getCpuFleetInfo() {
        return getFleetInfo(CPUShips, "CPU");
    }

    /**
     * Genera información detallada de una flota
     */
    private String getFleetInfo(List<Ship> fleet, String owner) {
        if (fleet.isEmpty()) {
            return owner + ": Sin flota configurada";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(owner).append(" - ").append(fleet.size()).append(" barcos:\n");

        Map<ShipType, Integer> countByType = new HashMap<>();
        int totalHealth = 0;
        int sunkShips = 0;

        for (Ship ship : fleet) {
            countByType.merge(ship.getType(), 1, Integer::sum);
            totalHealth += ship.getType().getSize();
            if (ship.isSunk()) {
                sunkShips++;
            }
        }

        for (Map.Entry<ShipType, Integer> entry : countByType.entrySet()) {
            sb.append("  ").append(entry.getValue()).append("x ")
                    .append(entry.getKey().getName())
                    .append(" (").append(entry.getKey().getSize()).append(" casillas)\n");
        }

        sb.append("Total: ").append(totalHealth).append(" casillas | Hundidos: ").append(sunkShips);

        return sb.toString();
    }

    /**
     * Obtiene estadísticas de las flotas
     */
    public Map<String, Object> getFleetStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("playerShipsCount", playerShips.size());
        stats.put("cpuShipsCount", CPUShips.size());

        stats.put("playerTotalCells", playerShips.stream().mapToInt(ship -> ship.getType().getSize()).sum());
        stats.put("cpuTotalCells", CPUShips.stream().mapToInt(ship -> ship.getType().getSize()).sum());

        stats.put("playerSunkShips", (int) playerShips.stream().filter(Ship::isSunk).count());
        stats.put("cpuSunkShips", (int) CPUShips.stream().filter(Ship::isSunk).count());

        stats.put("playerOperationalShips", (int) playerShips.stream().filter(ship -> !ship.isSunk()).count());
        stats.put("cpuOperationalShips", (int) CPUShips.stream().filter(ship -> !ship.isSunk()).count());

        return stats;
    }

// ========== MÉTODOS DE CREACIÓN DE FLOTAS ==========
    /**
     * Crea la flota estándar del juego
     */
    private List<Ship> createStandardFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota clásica balanceada
        fleet.add(new Ship(ShipType.CARRIER));      // 5 casillas
        fleet.add(new Ship(ShipType.BATTLESHIP));   // 4 casillas  
        fleet.add(new Ship(ShipType.CRUISER));      // 3 casillas
        fleet.add(new Ship(ShipType.SUBMARINE));    // 3 casillas
        fleet.add(new Ship(ShipType.DESTROYER));    // 2 casillas

        System.out.println("📊 Flota estándar creada: 5 barcos, 17 casillas totales");
        return fleet;
    }

    /**
     * Crea una flota especial con barcos únicos
     */
    private List<Ship> createSpecialFleet() {
        List<Ship> fleet = new ArrayList<>();

        try {
            // Flota especial con barcos no tradicionales
            // Nota: Asumiendo que tienes estos tipos especiales, si no, usa los estándar

            // Intentar crear barcos especiales
            fleet.add(createSpecialShip("Portaaviones Ancho", 6));  // Barco ancho
            fleet.add(createSpecialShip("Acorazado L", 5));         // Forma en L
            fleet.add(new Ship(ShipType.CRUISER));                  // Barco estándar
            fleet.add(new Ship(ShipType.SUBMARINE));                // Barco estándar
            fleet.add(createSpecialShip("Patrullera", 2));          // Barco pequeño rápido

            System.out.println("🚀 Flota especial creada: 5 barcos especiales");

        } catch (Exception e) {
            System.err.println("❌ Error creando flota especial, usando flota mejorada: " + e.getMessage());
            // Fallback a flota mejorada
            return createEnhancedFleet();
        }

        return fleet;
    }

    /**
     * Crea una flota mínima para partidas rápidas
     */
    private List<Ship> createMinimalFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota mínima para partidas rápidas
        fleet.add(new Ship(ShipType.BATTLESHIP));   // 4 casillas
        fleet.add(new Ship(ShipType.CRUISER));      // 3 casillas
        fleet.add(new Ship(ShipType.DESTROYER));    // 2 casillas
        fleet.add(new Ship(ShipType.FRIGATE));      // 2 casillas

        System.out.println("⚡ Flota mínima creada: 4 barcos, 11 casillas totales");
        return fleet;
    }

    /**
     * Crea flota enjambre con muchos barcos pequeños
     */
    private List<Ship> createSwarmFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Muchos barcos pequeños
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.SUBMARINE));
        fleet.add(new Ship(ShipType.CRUISER));

        System.out.println("🐝 Flota enjambre creada: 8 barcos, 18 casillas totales");
        return fleet;
    }

    /**
     * Crea flota táctica con barcos balanceados
     */
    private List<Ship> createTacticalFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota balanceada para juego táctico
        fleet.add(new Ship(ShipType.CARRIER));      // Grande - objetivo principal
        fleet.add(new Ship(ShipType.BATTLESHIP));   // Grande - defensa
        fleet.add(new Ship(ShipType.CRUISER));      // Mediano - versátil
        fleet.add(new Ship(ShipType.SUBMARINE));    // Mediano - sigilo
        fleet.add(new Ship(ShipType.DESTROYER));    // Pequeño - rápido

        System.out.println("🎓 Flota táctica creada: 5 barcos balanceados, 17 casillas");
        return fleet;
    }

    /**
     * Crea flota asimétrica para el jugador (ofensiva)
     */
    private List<Ship> createAsymmetricPlayerFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Jugador: flota ofensiva con muchos barcos pequeños y rápidos
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.FRIGATE));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.DESTROYER));
        fleet.add(new Ship(ShipType.SUBMARINE));

        System.out.println("⚔️ Flota jugador (ofensiva): 6 barcos pequeños, 14 casillas");
        return fleet;
    }

    /**
     * Crea flota asimétrica para la CPU (defensiva)
     */
    private List<Ship> createAsymmetricCPUFleet() {
        List<Ship> fleet = new ArrayList<>();

        // CPU: flota defensiva con pocos barcos grandes
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));

        System.out.println("🛡️ Flota CPU (defensiva): 3 barcos grandes, 12 casillas");
        return fleet;
    }

    /**
     * Crea flota mejorada (fallback para flota especial)
     */
    private List<Ship> createEnhancedFleet() {
        List<Ship> fleet = new ArrayList<>();

        // Flota mejorada con barcos estándar pero configuración especial
        fleet.add(new Ship(ShipType.CARRIER));
        fleet.add(new Ship(ShipType.BATTLESHIP));
        fleet.add(new Ship(ShipType.CRUISER));
        fleet.add(new Ship(ShipType.CRUISER)); // Extra cruiser
        fleet.add(new Ship(ShipType.DESTROYER));

        System.out.println("💫 Flota mejorada creada: 5 barcos, 18 casillas");
        return fleet;
    }

// ========== MÉTODOS AUXILIARES PARA FLOTAS ESPECIALES ==========
    /**
     * Crea un barco especial (método placeholder para tipos especiales)
     */
    private Ship createSpecialShip(String specialType, int size) {
        // Este método sería para crear barcos con formas especiales
        // Por ahora, crea barcos estándar con nombres especiales

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

        // Aquí podrías configurar propiedades especiales del barco
        // ship.setSpecialProperty(...);
        return ship;
    }

    /**
     * Mejora habilidades para flota especial
     */
    private void enhanceSkillsForSpecialFleet() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();

        // Habilidades mejoradas para flota especial
        skills.addSkill(Skill.SONAR, 4);
        skills.addSkill(Skill.RADAR, 3);
        skills.addSkill(Skill.DRONE, 3);
        skills.addSkill(Skill.GUIDED_MISSILE, 2);
        skills.setSkillPoints(8);

        System.out.println("✨ Habilidades mejoradas para flota especial");
    }

    /**
     * Configura habilidades para partidas rápidas
     */
    private void setupSkillsForLightningGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();

        // Habilidades ofensivas rápidas
        skills.addSkill(Skill.GUIDED_MISSILE, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 3);
        skills.addSkill(Skill.DRONE, 1);
        skills.setSkillPoints(6);

        System.out.println("⚡ Habilidades configuradas para partida rápida");
    }

    /**
     * Configura habilidades para flota enjambre
     */
    private void setupSkillsForSwarmGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();

        // Habilidades de exploración para encontrar muchos objetivos
        skills.addSkill(Skill.SONAR, 4);
        skills.addSkill(Skill.RADAR, 3);
        skills.addSkill(Skill.DRONE, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 2);
        skills.setSkillPoints(8);

        System.out.println("🔍 Habilidades de exploración para flota enjambre");
    }

    /**
     * Configura habilidades para juego táctico
     */
    private void setupSkillsForTacticalGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();

        // Habilidades tácticas variadas
        skills.addSkill(Skill.SONAR, 2);
        skills.addSkill(Skill.RADAR, 2);
        skills.addSkill(Skill.DRONE, 2);
        skills.addSkill(Skill.GUIDED_MISSILE, 1);
        skills.addSkill(Skill.CLUSTER_BOMB, 1);
        skills.addSkill(Skill.JAMMING, 1);
        skills.setSkillPoints(8);

        System.out.println("🎯 Habilidades tácticas configuradas");
    }

    /**
     * Configura habilidades para juego asimétrico
     */
    private void setupSkillsForAsymmetricGame() {
        SkillSystem skills = getPlayerSkills();
        skills.reset();

        // Habilidades de sabotaje y reparación para flota asimétrica
        skills.addSkill(Skill.SONAR, 3);
        skills.addSkill(Skill.JAMMING, 2);
        skills.addSkill(Skill.REPAIR, 3);
        skills.addSkill(Skill.CLUSTER_BOMB, 2);
        skills.setSkillPoints(7);

        System.out.println("🔄 Habilidades asimétricas configuradas");
    }

// ========== MÉTODOS DE INFORMACIÓN DE FLOTAS ==========
    /**
     * Obtiene información detallada de la flota actual
     */
    public String getFleetInfo() {
        StringBuilder info = new StringBuilder();

        info.append("=== INFORMACIÓN DE FLOTAS ===\n");
        info.append("Jugador: ").append(getPlayerFleetInfo()).append("\n");
        info.append("CPU: ").append(getCpuFleetInfo()).append("\n");

        int playerShipsInt = playerShips.size();
        int cpuShipsInt = CPUShips.size();
        int playerCells = playerShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();
        int cpuCells = CPUShips.stream().mapToInt(ship -> ship.getType().getSize()).sum();

        info.append("Resumen: Jugador ").append(playerShipsInt).append(" barcos (").append(playerCells).append(" casillas)");
        info.append(" vs CPU ").append(cpuShipsInt).append(" barcos (").append(cpuCells).append(" casillas)");

        return info.toString();
    }

    /**
     * Verifica si las flotas están balanceadas
     */
    public boolean areFleetsBalanced() {
        int playerPower = calculateFleetPower(playerShips);
        int cpuPower = calculateFleetPower(CPUShips);

        double balanceRatio = (double) Math.min(playerPower, cpuPower) / Math.max(playerPower, cpuPower);
        boolean balanced = balanceRatio >= 0.7; // Considerar balanceado si no hay más de 30% de diferencia

        System.out.println("⚖️ Balance de flotas: Jugador=" + playerPower
                + ", CPU=" + cpuPower + ", Ratio=" + String.format("%.2f", balanceRatio)
                + ", Balanceado=" + balanced);

        return balanced;
    }

    /**
     * Calcula el poder de una flota (basado en tamaño y tipo de barcos)
     */
    private int calculateFleetPower(List<Ship> fleet) {
        return fleet.stream()
                .mapToInt(ship -> {
                    int basePower = ship.getType().getSize();
                    // Barcos más grandes tienen poder adicional
                    if (ship.getType().getSize() >= 4) {
                        return basePower * 2;
                    } else if (ship.getType().getSize() >= 3) {
                        return basePower + 1;
                    }
                    return basePower;
                })
                .sum();
    }

    // ========== MÉTODOS DE DISPARO ==========
    /**
     * Procesa un disparo del jugador humano
     */
    public ShotResult processPlayerShot(Coordinate target) {
        if (!playerTurn || gamePhase != GamePhase.IN_PLAY) {
            return ShotResult.INVALID;
        }

        ShotResult result = humanPlayer.shootAt(cpuPlayer, target);
        elapsedTurns++;

        if (result.isImpact()) {
            playerSkills.earnHitPoints();

            if (result.isSunk()) {
                Ship sunkShip = findSunkenShip(CPUShips, sunkShipsCPU);
                if (sunkShip != null) {
                    playerSkills.earnSinkingPoints();
                    lastSunkenShipCPU = sunkShip;
                    System.out.println("¡Hundiste un " + sunkShip.getType().getName() + "!");
                }
            }

            // Jugador mantiene el turno si impacta (en algunas variantes)
            // playerTurn = true; // Ya es true por defecto
        } else {
            // Cambiar turno a CPU si falla
            playerTurn = false;
        }

        // Verificar si el juego terminó
        if (cpuPlayer.hasLost()) {
            gamePhase = GamePhase.PLAYER_WIN;
            if (currentProfile != null) {
                currentProfile.updateFromGame(this, true);
            }
            System.out.println("🎉 ¡VICTORIA! " + humanPlayer.getName() + " ganó la partida");
        }

        return result;
    }

    /**
     * Procesa un disparo de la CPU
     */
    public ShotResult processCPUShot() {
        if (playerTurn || gamePhase != GamePhase.IN_PLAY) {
            return ShotResult.INVALID;
        }

        Coordinate target = ((CPU) cpuPlayer).chooseShotTarget(humanPlayer);
        lastShotCPU = target;

        ShotResult result = cpuPlayer.shootAt(humanPlayer, target);
        cpuController.processResult(target, result);
        ((CPU) cpuPlayer).updateAfterShot(target, result);
        elapsedTurns++;

        if (result.isImpact()) {
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
            // playerTurn = false; // Ya es false por defecto
        } else {
            // Cambiar turno a jugador si falla
            playerTurn = true;
        }

        // Verificar si el juego terminó
        if (humanPlayer.hasLost()) {
            gamePhase = GamePhase.CPU_WIN;
            if (currentProfile != null) {
                currentProfile.updateFromGame(this, false);
            }
            System.out.println("💀 DERROTA! La CPU ganó la partida");
        }

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

    // ========== MÉTODOS DE HABILIDADES ==========
    public SkillResult usePlayerSkill(Skill skill, Object... parameters) {
        if (!playerSkills.canUseSkill(skill)) {
            return new SkillResult(false, "No puedes usar esta habilidad");
        }

        SkillResult result = null;

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

    // ========== INICIALIZACIÓN ==========
    private void initializeShips() {
        // Inicializar barcos del jugador (si no se configuran manualmente)
        if (playerShips.isEmpty()) {
            playerShips.add(new Ship(ShipType.CARRIER));
            playerShips.add(new Ship(ShipType.BATTLESHIP));
            playerShips.add(new Ship(ShipType.SUBMARINE));
            playerShips.add(new Ship(ShipType.DESTROYER));
            playerShips.add(new Ship(ShipType.FRIGATE));
            playerShips.add(new Ship(ShipType.CRUISER));
        }

        // Inicializar barcos de la CPU (misma flota por defecto)
        if (CPUShips.isEmpty()) {
            CPUShips.add(new Ship(ShipType.CARRIER));
            CPUShips.add(new Ship(ShipType.BATTLESHIP));
            CPUShips.add(new Ship(ShipType.SUBMARINE));
            CPUShips.add(new Ship(ShipType.DESTROYER));
            CPUShips.add(new Ship(ShipType.FRIGATE));
            CPUShips.add(new Ship(ShipType.CRUISER));
        }

        // Los barcos de la CPU ya se colocaron en el constructor
        // Los barcos del jugador se colocarán manualmente mediante placeShip()
    }

    // ========== MÉTODOS PARA CONFIGURAR FLOTAS ==========
    /**
     * Establece una flota personalizada para el jugador
     */
    public void setPlayerShips(List<Ship> playerShips) {
        if (playerShips == null || playerShips.isEmpty()) {
            throw new IllegalArgumentException("La flota del jugador no puede ser nula o vacía");
        }

        validateFleet(playerShips, "Jugador");
        this.playerShips.clear();
        this.playerShips.addAll(playerShips);
        resetShipsState(this.playerShips);

        System.out.println("✅ Flota del jugador configurada: " + playerShips.size() + " barcos");
        logFleetComposition(playerShips, "Jugador");
    }

    /**
     * Establece una flota personalizada para la CPU
     */
    public void setCpuShips(List<Ship> cpuShips) {
        if (cpuShips == null || cpuShips.isEmpty()) {
            throw new IllegalArgumentException("La flota de la CPU no puede ser nula o vacía");
        }

        validateFleet(cpuShips, "CPU");
        this.CPUShips.clear();
        this.CPUShips.addAll(cpuShips);
        resetShipsState(this.CPUShips);

        System.out.println("✅ Flota de la CPU configurada: " + cpuShips.size() + " barcos");
        logFleetComposition(cpuShips, "CPU");

        // Recolocar barcos de la CPU
        ((CPU) cpuPlayer).placeShipsRandomly();
    }

    /**
     * Registra la composición de una flota
     */
    private void logFleetComposition(List<Ship> fleet, String owner) {
        Map<ShipType, Integer> composition = new HashMap<>();

        for (Ship ship : fleet) {
            composition.merge(ship.getType(), 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Composición de flota ").append(owner).append(": ");

        for (Map.Entry<ShipType, Integer> entry : composition.entrySet()) {
            sb.append(entry.getValue()).append("x ").append(entry.getKey().getName()).append(", ");
        }

        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        System.out.println(sb.toString());
    }

    // ========== MÉTODOS DE INFORMACIÓN ==========
    public int getElapsedTurns() {
        return elapsedTurns;
    }

    public void setElapsedTurns(int turns) {
        this.elapsedTurns = turns;
    }

    public GamePhase getGamePhase() {
        if (cpuPlayer.hasLost()) {
            return GamePhase.PLAYER_WIN;
        } else if (humanPlayer.hasLost()) {
            return GamePhase.CPU_WIN;
        } else {
            return gamePhase;
        }
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

    public boolean isGameOver() {
        return humanPlayer.hasLost() || cpuPlayer.hasLost();
    }

    public boolean playerWin() {
        return cpuPlayer.hasLost();
    }

    public boolean cpuWin() {
        return humanPlayer.hasLost();
    }

    public int getRemainingPlayerShips() {
        return humanPlayer.getRemainingShips();
    }

    public int getRemainingCPUShips() {
        return cpuPlayer.getRemainingShips();
    }

    public List<Ship> getPlayerShipsNotSunk() {
        List<Ship> notSunk = new ArrayList<>();
        for (Ship ship : playerShips) {
            if (!ship.isSunk()) {
                notSunk.add(ship);
            }
        }
        return notSunk;
    }

    public List<Ship> getCPUShipsNotSunk() {
        List<Ship> notSunk = new ArrayList<>();
        for (Ship ship : CPUShips) {
            if (!ship.isSunk()) {
                notSunk.add(ship);
            }
        }
        return notSunk;
    }

    // ========== GETTERS ==========
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

    public List<Ship> getCPUShips() {
        return new ArrayList<>(CPUShips);
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

    // Método adicional para compatibilidad
    public GamePhase getGameState() {
        return getGamePhase();
    }

    // ========== MÉTODOS AUXILIARES ==========
    private int getMaxFleetSize() {
        return 25;
    }

    private void updateStatistics(ShotResult result) {
        // Implementar según necesidades específicas
        switch (result) {
            case HIT:
                // Incrementar contador de impactos
                break;
            case SUNK:
                // Incrementar contador de barcos hundidos
                break;
            case MISS:
                // Incrementar contador de disparos fallidos
                break;
        }
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

        // Reiniciar tableros y jugadores
        playerBoard.reset();
        cpuBoard.reset();
        humanPlayer.reset();
        cpuPlayer.reset();

        // Reiniciar controladores
        cpuController.reset();

        // Reiniciar contadores
        elapsedTurns = 0;
        playerTurn = true;
        gamePhase = GamePhase.IN_PLAY;

        // Recolocar barcos de la CPU
        ((CPU) cpuPlayer).placeShipsRandomly();

        System.out.println("🔄 Juego reiniciado");
    }
}
