package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Game;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.ShotResult;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillResult;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.enums.ShipType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameController {
   
    private Game game;
    private Coordinate lastShotCPU;
    private Ship lastSunkenShipCPU;
    private Ship lastSunkenShipPlayer;
    private List<Ship> playerShips;
    private List<Ship> CPUShips;
     
    private CPUController cpuController;

    private SkillSystem playerSkills;
    private SkillSystem CPUSkills;
    private SkillController skillController;
    
    private Set<Ship> sunkShipsCPU;  // Corregido el nombre
    private Set<Ship> sunkShipsPlayer; // Corregido el nombre
    
    // Campos faltantes que necesitas
    private int turnosTranscurridos;
    private boolean turnoJugador;
    private GamePhase estadoJuego;

    public GameController() {
        this.game = new Game();
        this.cpuController = new CPUController(CPUController.Difficulty.NORMAL, game.getBoardPlayer());
        this.playerShips = new ArrayList<>();
        this.CPUShips = new ArrayList<>();
        
        this.playerSkills = new SkillSystem(true);
        this.CPUSkills = new SkillSystem(false);
        this.skillController = new SkillController(this);
        
        this.sunkShipsCPU = new HashSet<>();  // Corregido el nombre
        this.sunkShipsPlayer = new HashSet<>(); // Corregido el nombre
        
        this.turnosTranscurridos = 0;
        this.turnoJugador = true;
        this.estadoJuego = GamePhase.IN_PLAY;
        
        initializeShips();
    }
    
    public GameController(CPUController.Difficulty dificultyCPU) {
        this.game = new Game();
        this.cpuController = new CPUController(dificultyCPU, game.getBoardPlayer());
        this.playerShips = new ArrayList<>();
        this.CPUShips = new ArrayList<>();
        
        this.playerSkills = new SkillSystem(true);
        this.CPUSkills = new SkillSystem(false);
        this.skillController = new SkillController(this);
        
        this.sunkShipsCPU = new HashSet<>();
        this.sunkShipsPlayer = new HashSet<>();
        
        this.turnosTranscurridos = 0;
        this.turnoJugador = true;
        this.estadoJuego = GamePhase.IN_PLAY;
        
        initializeShips();
    }
    
    // Métodos necesarios para la carga
    public int getTurnosTranscurridos() {
        return turnosTranscurridos;
    }
    
    public void setTurnosTranscurridos(int turnos) {
        this.turnosTranscurridos = turnos;
    }
    
    public GamePhase getGameState() {
        if (allShipsSunk(CPUShips)) {
            return GamePhase.PLAYER_WIN;
        } else if (allShipsSunk(playerShips)) {
            return GamePhase.CPU_WIN;
        } else {
            return estadoJuego;
        }
    }
    
    public void setGameState(GamePhase state) {
        this.estadoJuego = state;
    }
    
    public void setTurnoJugador(boolean turnoJugador) {
        this.turnoJugador = turnoJugador;
    }
    
    // Métodos para verificar victoria/derrota
    public boolean playerWin() {
        return allShipsSunk(CPUShips);
    }
    
    public boolean cpuWin() {
        return allShipsSunk(playerShips);
    }
    
    private Ship findSunkenShip(List<Ship> ships, Set<Ship> counted) {
        for (Ship ship : ships) {
            if (ship.isSunk() && !counted.contains(ship)) {
                counted.add(ship);  // IMPORTANTE: agregar al set contado
                return ship;
            }
        }
        return null;
    }
        
    public ShotResult playerShoots(Coordinate coord) {
        if (!turnoJugador || estadoJuego != GamePhase.IN_PLAY) {
            throw new IllegalStateException("No es el turno del jugador o el juego no está activo");
        }
        
        ShotResult result = game.playerShot(coord);
        turnosTranscurridos++;

        if (result.impact()) {
            playerSkills.earnHitPoints();
            
            if (result.sunk()) {
                Ship sunkShip = findSunkenShip(CPUShips, sunkShipsCPU);
                if (sunkShip != null) {
                    playerSkills.earnSinkingPoints();  // Método corregido
                    lastSunkenShipCPU = sunkShip;
                    System.out.println("¡Hundiste un " + sunkShip.getType().getName() + "!");
                }
            }
            
            // Jugador mantiene el turno si impacta
        } else {
            // Cambiar turno a CPU si falla
            turnoJugador = false;
        }

        // Verificar si el juego terminó
        if (allShipsSunk(CPUShips)) {
            estadoJuego = GamePhase.PLAYER_WIN;
        }

        return result;
    }
    
    // Para la CPU
    public ShotResult CPUShoots() {
        if (turnoJugador || estadoJuego != GamePhase.IN_PLAY) {
            throw new IllegalStateException("No es el turno de la CPU o el juego no está activo");
        }
        
        Coordinate shot = cpuController.generateShot();
        lastShotCPU = shot;
        
        ShotResult result = game.getBoardPlayer().makeShot(shot);
        cpuController.processResult(shot, result);
        turnosTranscurridos++;
        
        if (result.impact()) {
            CPUSkills.earnHitPoints();
            
            if (result.sunk()) {
                Ship sunkShip = findSunkenShip(playerShips, sunkShipsPlayer);
                if (sunkShip != null) {
                    CPUSkills.earnSinkingPoints();  // Método corregido
                    lastSunkenShipPlayer = sunkShip;
                    System.out.println("La CPU hundió tu " + sunkShip.getType().getName() + "!");
                }
            }
            
            // CPU mantiene el turno si impacta
        } else {
            // Cambiar turno a jugador si falla
            turnoJugador = true;
        }

        // Verificar si el juego terminó
        if (allShipsSunk(playerShips)) {
            estadoJuego = GamePhase.CPU_WIN;
        }
        
        return result;
    }
     
    public SkillResult usePlayerSkill(Skill skill, Object... parameters) {
        if (!playerSkills.canUseSkill(skill)) {
            return new SkillResult(false, "You cannot use this skill");
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
    
    // Habilidades de la CPU
    public void useCPUSkill() {
        Skill cpuSkill = skillController.decideCPUSkill();
        if (cpuSkill != null && CPUSkills.canUseSkill(cpuSkill)) {
            SkillResult result = skillController.runCPUSkill(cpuSkill);
            if (result.isSuccessful()) {
                CPUSkills.useSkill(cpuSkill);
            }
        }
    }
    
    private void initializeShips() {
        // Inicializar barcos del jugador
        playerShips.add(new Ship(ShipType.CARRIER));
        playerShips.add(new Ship(ShipType.BATTLESHIP));
        playerShips.add(new Ship(ShipType.SUBMARINE));
        playerShips.add(new Ship(ShipType.DESTROYER));
        playerShips.add(new Ship(ShipType.FRIGATE));
        playerShips.add(new Ship(ShipType.CRUISER));

        // Inicializar barcos de la CPU (misma flota)
        CPUShips.add(new Ship(ShipType.CARRIER));
        CPUShips.add(new Ship(ShipType.BATTLESHIP));
        CPUShips.add(new Ship(ShipType.SUBMARINE));
        CPUShips.add(new Ship(ShipType.DESTROYER));
        CPUShips.add(new Ship(ShipType.FRIGATE));
        CPUShips.add(new Ship(ShipType.CRUISER));

        // TODO: Colocar barcos en los tableros (necesitas implementar esto)
        placeShipsOnBoards();
    }
    
    private void placeShipsOnBoards() {
        // Implementar lógica para colocar barcos en los tableros
        // Esto es temporal - necesitarás una lógica real de colocación
        
        // Ejemplo muy básico (debes implementar una lógica proper)
        for (Ship ship : playerShips) {
            // Colocar barcos del jugador
            // game.getBoardPlayer().placeShip(ship, new Coordinate(0,0), Direction.HORIZONTAL);
        }
        
        for (Ship ship : CPUShips) {
            // Colocar barcos de la CPU aleatoriamente
            // game.getBoardCPU().placeShipRandomly(ship);
        }
    }
    
    // Métodos para información del juego
    public int getRemainingPlayerShips() {
        int count = 0;
        for (Ship ship : playerShips) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
    }
    
    public int getRemainingCPUShips() {
        int count = 0;
        for (Ship ship : CPUShips) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
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
    
    // Método para reiniciar el juego
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
        
        // Reiniciar juego
        game.reset();
        cpuController.reset();
        
        // Reiniciar contadores
        turnosTranscurridos = 0;
        turnoJugador = true;
        estadoJuego = GamePhase.IN_PLAY;
        
        // Recolocar barcos
        placeShipsOnBoards();
    }

    public boolean isGameOver() {
        return allShipsSunk(playerShips) || allShipsSunk(CPUShips);
    }

    private boolean allShipsSunk(List<Ship> ships) {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    // Getters
    public boolean isPlayerTurn() { 
        return turnoJugador; 
    }
    
    public void setPlayerTurn(boolean turno) { 
        this.turnoJugador = turno; 
    }
      
    public Board getPlayerBoard() { 
        return game.getBoardPlayer(); 
    }
    
    public Board getCPUBoard() { 
        return game.getBoardCPU(); 
    }
    
    public boolean isGameFinished() { 
        return estadoJuego == GamePhase.PLAYER_WIN || estadoJuego == GamePhase.CPU_WIN; 
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
    
    public Set<Ship> getSunkShipsCPU() {
        return new HashSet<>(sunkShipsCPU);
    }
    
    public Set<Ship> getSunkShipsPlayer() {
        return new HashSet<>(sunkShipsPlayer);
    }
    
    public Game getGame() {
        return game;
    }
}
