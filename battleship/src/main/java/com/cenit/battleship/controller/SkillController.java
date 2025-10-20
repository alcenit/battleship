package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Player;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillResult;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.Direction;
import com.cenit.battleship.model.enums.ShotResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Controlador para gestionar las habilidades especiales del juego
 * @author Usuario
 */
public class SkillController {

    private GameController gameController;
    private SoundController soundController;
    private AnimationController animationController;
    private Random random;

    public SkillController(GameController gameController) {
        this.gameController = gameController;
        this.soundController = SoundController.getInstance();
        this.animationController = AnimationController.getInstance();
        this.random = new Random();
    }

    // ========== IMPLEMENTACIÓN DE HABILIDADES DEL JUGADOR ==========

    public SkillResult useSonar(Coordinate center) {
        try {
            Board cpuBoard = gameController.getCpuBoard();
            List<Coordinate> revealedArea = new ArrayList<>();
            List<Cell> revealedCells = new ArrayList<>();

            // Revelar área 3x3 alrededor del centro
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x = center.getX() + dx;
                    int y = center.getY() + dy;

                    if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                        Coordinate coord = new Coordinate(x, y);
                        Cell cell = cpuBoard.getCell(coord);
                        
                        // Revelar la celda temporalmente
                        cell.setRevealed(true);
                        
                        revealedArea.add(coord);
                        revealedCells.add(cell);
                    }
                }
            }

            // Sonido y animación
            if (soundController != null) {
                soundController.playSonar();
            }
            
            if (animationController != null) {
                animationController.playSonarAnimation(center, revealedArea);
            }

            return new SkillResult(
                true, 
                "Sonar activado! Área 3x3 revelada alrededor de " + center.aNotacion(), 
                revealedArea, 
                revealedCells
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al activar sonar: " + e.getMessage());
        }
    }

    public SkillResult useRadar() {
        try {
            List<Ship> cpuShips = gameController.getCPUShipsNotSunk();
            
            if (cpuShips.isEmpty()) {
                return new SkillResult(false, "No hay barcos enemigos para detectar");
            }

            // Encontrar un barco no hundido con posiciones intactas
            Ship targetShip = null;
            for (Ship ship : cpuShips) {
                if (!ship.getIntactCoordinates().isEmpty()) {
                    targetShip = ship;
                    break;
                }
            }

            if (targetShip == null) {
                return new SkillResult(false, "Todos los barcos detectados ya están dañados");
            }

            // Revelar una posición aleatoria intacta del barco
            List<Coordinate> intactCoords = targetShip.getIntactCoordinates();
            Coordinate revealedCoord = intactCoords.get(random.nextInt(intactCoords.size()));
            
            // Marcar como revelada temporalmente
            Cell revealedCell = gameController.getCpuBoard().getCell(revealedCoord);
            revealedCell.setRevealed(true);

            List<Coordinate> revealedCoordinates = Arrays.asList(revealedCoord);
            List<Cell> revealedCells = Arrays.asList(revealedCell);

            // Sonido y animación
            if (soundController != null) {
                soundController.playRadar();
            }
            
            if (animationController != null) {
                animationController.playRadarAnimation(revealedCoord, targetShip.getType());
            }

            return new SkillResult(
                true, 
                "Radar detectó un " + targetShip.getType().getName() + " en " + revealedCoord.aNotacion(), 
                revealedCoordinates, 
                revealedCells
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al activar radar: " + e.getMessage());
        }
    }

    public SkillResult useDrone(boolean isRow, int index) {
        try {
            Board cpuBoard = gameController.getCpuBoard();
            List<Coordinate> revealedArea = new ArrayList<>();
            List<Cell> revealedCells = new ArrayList<>();

            if (isRow) {
                // Revelar fila completa
                for (int x = 0; x < Board.SIZE; x++) {
                    Coordinate coord = new Coordinate(x, index);
                    Cell cell = cpuBoard.getCell(coord);
                    cell.setRevealed(true);
                    revealedArea.add(coord);
                    revealedCells.add(cell);
                }
            } else {
                // Revelar columna completa
                for (int y = 0; y < Board.SIZE; y++) {
                    Coordinate coord = new Coordinate(index, y);
                    Cell cell = cpuBoard.getCell(coord);
                    cell.setRevealed(true);
                    revealedArea.add(coord);
                    revealedCells.add(cell);
                }
            }

            // Sonido y animación
            if (soundController != null) {
                soundController.playDrone();
            }
            
            if (animationController != null) {
                animationController.playDroneAnimation(isRow, index);
            }

            return new SkillResult(
                true, 
                "Dron exploró " + (isRow ? "fila " : "columna ") + (index + 1), 
                revealedArea, 
                revealedCells
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al usar dron: " + e.getMessage());
        }
    }

    public SkillResult useGuidedMissile(Coordinate target) {
        try {
            // Verificar que el objetivo sea válido
            if (!gameController.getCpuBoard().canShootAt(target)) {
                return new SkillResult(false, "No se puede disparar en " + target.aNotacion());
            }

            // Disparo garantizado que ignora habilidades de evasión
            ShotResult result = gameController.processPlayerShot(target);
            
            // Forzar impacto si era un miss (característica especial del misil guiado)
            if (result == ShotResult.MISS) {
                // Buscar si hay un barco en esa posición y forzar impacto
                Cell targetCell = gameController.getCpuBoard().getCell(target);
                if (targetCell.hasShip()) {
                    Ship hitShip = targetCell.getShip();
                    hitShip.recordImpact();
                    result = hitShip.isSunk() ? ShotResult.SUNK : ShotResult.HIT;
                }
            }

            // Sonido y animación
            if (soundController != null) {
                soundController.playMissile();
            }
            
            if (animationController != null) {
                animationController.playMissileAnimation(target, result);
            }

            return new SkillResult(
                true, 
                "Misil guiado: " + result.getMessage(), 
                Arrays.asList(target), 
                Arrays.asList(gameController.getCpuBoard().getCell(target))
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al lanzar misil guiado: " + e.getMessage());
        }
    }

    public SkillResult useClusterBomb(Coordinate center) {
        try {
            Board cpuBoard = gameController.getCpuBoard();
            List<Coordinate> affectedArea = new ArrayList<>();
            List<Cell> affectedCells = new ArrayList<>();
            List<ShotResult> results = new ArrayList<>();

            // Patrón de cruz (5 casillas)
            int[][] offsets = {{0,0}, {-1,0}, {1,0}, {0,-1}, {0,1}};
            
            for (int[] offset : offsets) {
                int x = center.getX() + offset[0];
                int y = center.getY() + offset[1];

                if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                    Coordinate coord = new Coordinate(x, y);
                    
                    // Solo disparar si no se ha disparado antes
                    if (cpuBoard.canShootAt(coord)) {
                        ShotResult result = gameController.processPlayerShot(coord);
                        results.add(result);
                        
                        affectedArea.add(coord);
                        affectedCells.add(cpuBoard.getCell(coord));
                    }
                }
            }

            // Sonido y animación
            if (soundController != null) {
                soundController.playClusterBomb();
            }
            
            if (animationController != null) {
                animationController.playClusterBombAnimation(center, affectedArea);
            }

            int hits = (int) results.stream().filter(ShotResult::isImpact).count();
            
            return new SkillResult(
                true, 
                "Bomba de racimo: " + hits + " impactos en área!", 
                affectedArea, 
                affectedCells
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al detonar bomba de racimo: " + e.getMessage());
        }
    }

    public SkillResult useJamming() {
        try {
            // La CPU pierde su próximo turno - jugador mantiene el turno
            gameController.setPlayerTurn(true);
            
            // Sonido y animación
            if (soundController != null) {
                soundController.playJamming();
            }
            
            if (animationController != null) {
                animationController.playJammingAnimation();
            }

            return new SkillResult(
                true, 
                "Interferencia activada! La CPU pierde su próximo turno"
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al activar interferencia: " + e.getMessage());
        }
    }

    public SkillResult useRepair(Coordinate position) {
    try {
        Board playerBoard = gameController.getPlayerBoard();
        Cell cell = playerBoard.getCell(position);
        
        if (!cell.hasShip()) {
            return new SkillResult(false, "No hay barco para reparar en " + position.aNotacion());
        }

        Ship ship = cell.getShip();
        
        // Verificar si el barco puede ser reparado
        if (!ship.canBeRepaired()) {
            return new SkillResult(false, "Este barco no puede ser reparado");
        }
        
        // Verificar si esta parte específica está dañada
        if (!ship.isPartDamaged(position)) {
            return new SkillResult(false, "Esta parte del barco ya está intacta");
        }

        // Reparar la parte específica
        boolean repaired = ship.repairAtCoordinate(position);
        
        if (!repaired) {
            return new SkillResult(false, "Error al reparar el barco");
        }

        // Sonido y animación
        if (soundController != null) {
            soundController.playRepair();
        }
        
        if (animationController != null) {
            animationController.playRepairAnimation(position, ship);
        }

        return new SkillResult(
            true, 
            ship.getType().getName() + " reparado en " + position.aNotacion() + 
            " | Integridad: " + (int)(ship.getIntegrityPercentage() * 100) + "%"
        );
        
    } catch (Exception e) {
        return new SkillResult(false, "Error al reparar barco: " + e.getMessage());
    }
}

    public SkillResult useCamouflage(Ship ship, Coordinate newPosition, Direction direction) {
        try {
            // Verificar que el barco pertenezca al jugador
            if (!gameController.getPlayerShips().contains(ship)) {
                return new SkillResult(false, "Barco no pertenece al jugador");
            }

            // Verificar nueva posición
            if (!gameController.canPlaceShip(ship, newPosition, direction)) {
                return new SkillResult(false, "No se puede mover el barco a la nueva posición");
            }

            // Remover barco de posición actual
            Board playerBoard = gameController.getPlayerBoard();
            for (Coordinate coord : ship.getCoordinates()) {
                playerBoard.getCell(coord).removeShip();
            }

            // Colocar en nueva posición
            boolean placed = gameController.placeShip(ship, newPosition, direction);
            
            if (!placed) {
                return new SkillResult(false, "Error al recolocar el barco");
            }

            // Sonido y animación
            if (soundController != null) {
                soundController.playCamouflage();
            }
            
            if (animationController != null) {
                animationController.playCamouflageAnimation(ship, newPosition);
            }

            return new SkillResult(
                true, 
                ship.getType().getName() + " movido exitosamente a " + newPosition.aNotacion()
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al usar camuflaje: " + e.getMessage());
        }
    }

    // ========== HABILIDADES DE LA CPU ==========

    public Skill decideCPUSkill() {
        SkillSystem cpuSkills = gameController.getCPUSkills();
        
        // Priorizar habilidades según la situación del juego
        if (shouldUseRadar()) {
            return Skill.RADAR;
        } else if (shouldUseSonar()) {
            return Skill.SONAR;
        } else if (shouldUseJamming()) {
            return Skill.JAMMING;
        }
        
        // Buscar cualquier habilidad disponible
        for (Skill skill : cpuSkills.getAvailableSkills().keySet()) {
            if (cpuSkills.canUseSkill(skill)) {
                return skill;
            }
        }
        
        return null;
    }

    public SkillResult executeCPUSkill(Skill skill) {
        switch (skill) {
            case SONAR:
                return useCPUSonar();
            case RADAR:
                return useCPURadar();
            case JAMMING:
                return useCPUJamming();
            case CLUSTER_BOMB:
                return useCPUClusterBomb();
            default:
                return new SkillResult(false, "Habilidad no disponible para la CPU");
        }
    }

    private SkillResult useCPUSonar() {
        Coordinate target = findBestCPUTargetForSonar();
        return useSonar(target);
    }

    private SkillResult useCPURadar() {
        return useRadar();
    }

    private SkillResult useCPUJamming() {
        // CPU usa jamming para robar turno
        gameController.setPlayerTurn(false);
        return new SkillResult(true, "CPU usó interferencia! Pierdes tu próximo turno");
    }

    private SkillResult useCPUClusterBomb() {
        Coordinate target = findBestCPUTargetForCluster();
        return useClusterBomb(target);
    }

    // ========== LÓGICA DE DECISIÓN DE LA CPU ==========

    private boolean shouldUseRadar() {
        // Usar radar cuando haya pocos barcos del jugador intactos
        return gameController.getRemainingPlayerShips() <= 3;
    }

    private boolean shouldUseSonar() {
        // Usar sonar cuando el jugador tenga barcos grandes sin descubrir
        return hasLargePlayerShipsUndiscovered();
    }

    private boolean shouldUseJamming() {
        // Usar jamming cuando el jugador esté en racha de aciertos
        return gameController.isPlayerTurn() && random.nextDouble() < 0.3;
    }

    private boolean hasLargePlayerShipsUndiscovered() {
        for (Ship ship : gameController.getPlayerShipsNotSunk()) {
            if (ship.getType().getSize() >= 4 && ship.getDamagePercentage() < 0.5) {
                return true;
            }
        }
        return false;
    }

    private Coordinate findBestCPUTargetForSonar() {
        // Buscar área con mayor densidad de barcos del jugador
        // Por ahora, posición aleatoria evitando bordes
        return new Coordinate(
            random.nextInt(Board.SIZE - 2) + 1,
            random.nextInt(Board.SIZE - 2) + 1
        );
    }

    private Coordinate findBestCPUTargetForCluster() {
        // Buscar área donde se sospecha que hay barcos del jugador
        // Por ahora, posición aleatoria
        return new Coordinate(
            random.nextInt(Board.SIZE),
            random.nextInt(Board.SIZE)
        );
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Verifica si una habilidad está disponible para el jugador
     */
    public boolean isPlayerSkillAvailable(Skill skill) {
        return gameController.getPlayerSkills().canUseSkill(skill);
    }
    
    /**
     * Obtiene el costo de una habilidad para el jugador
     */
    public int getPlayerSkillCost(Skill skill) {
        return gameController.getPlayerSkills().getSkillCost(skill);
    }
    
    /**
     * Obtiene la descripción de una habilidad
     */
    public String getSkillDescription(Skill skill) {
        switch (skill) {
            case SONAR: return "Revela un área 3x3 en el tablero enemigo";
            case RADAR: return "Detecta la posición de un barco enemigo aleatorio";
            case DRONE: return "Explora una fila o columna completa";
            case GUIDED_MISSILE: return "Disparo garantizado que ignora evasión";
            case CLUSTER_BOMB: return "Dispara en patrón de cruz (5 casillas)";
            case JAMMING: return "La CPU pierde su próximo turno";
            case REPAIR: return "Repara una parte dañada de tu barco";
            case CAMOUFLAGE: return "Mueve un barco a nueva posición";
            default: return "Habilidad no descrita";
        }
    }
}