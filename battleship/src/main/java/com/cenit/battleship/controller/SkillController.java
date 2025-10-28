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

                    if (x >= 0 && x < Board.BOARD_SIZE && y >= 0 && y < Board.BOARD_SIZE) {
                        Coordinate coord = new Coordinate(x, y);
                        Cell cell = cpuBoard.getCell(coord);
                        
                        if (cell != null) {
                            // Simular revelación temporal - crear una copia del estado
                            revealedArea.add(coord);
                            revealedCells.add(cell);
                        }
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

            // Crear información de detección
            StringBuilder detectionInfo = new StringBuilder();
            detectionInfo.append("Sonar en ").append(center.aNotacion()).append(" detectó: ");
            
            int shipCount = 0;
            for (Cell cell : revealedCells) {
                if (cell.hasShip() && !cell.hasBeenShot()) {
                    shipCount++;
                }
            }
            
            if (shipCount > 0) {
                detectionInfo.append(shipCount).append(" barcos en el área");
            } else {
                detectionInfo.append("solo agua");
            }

            return new SkillResult(
                true, 
                detectionInfo.toString(), 
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
            List<Coordinate> possibleTargets = new ArrayList<>();
            
            for (Ship ship : cpuShips) {
                List<Coordinate> intactCoords = ship.getIntactCoordinates();
                if (!intactCoords.isEmpty()) {
                    possibleTargets.addAll(intactCoords);
                    if (targetShip == null) {
                        targetShip = ship;
                    }
                }
            }

            if (possibleTargets.isEmpty()) {
                return new SkillResult(false, "Todos los barcos detectados ya están dañados");
            }

            // Revelar una posición aleatoria intacta
            Coordinate revealedCoord = possibleTargets.get(random.nextInt(possibleTargets.size()));
            Cell revealedCell = gameController.getCpuBoard().getCell(revealedCoord);

            List<Coordinate> revealedCoordinates = Arrays.asList(revealedCoord);
            List<Cell> revealedCells = Arrays.asList(revealedCell);

            // Sonido y animación
            if (soundController != null) {
                soundController.playRadar();
            }
            
            if (animationController != null) {
                Ship detectedShip = revealedCell.getShip();
                animationController.playRadarAnimation(revealedCoord, detectedShip != null ? detectedShip.getType() : null);
            }

            String shipName = targetShip != null ? targetShip.getType().getName() : "barco enemigo";
            return new SkillResult(
                true, 
                "Radar detectó un " + shipName + " en " + revealedCoord.aNotacion(), 
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
            List<Coordinate> scannedArea = new ArrayList<>();
            List<Cell> scannedCells = new ArrayList<>();
            int shipCount = 0;

            if (isRow) {
                // Escanear fila completa
                for (int x = 0; x < Board.BOARD_SIZE; x++) {
                    Coordinate coord = new Coordinate(x, index);
                    Cell cell = cpuBoard.getCell(coord);
                    if (cell != null) {
                        scannedArea.add(coord);
                        scannedCells.add(cell);
                        if (cell.hasShip() && !cell.hasBeenShot()) {
                            shipCount++;
                        }
                    }
                }
            } else {
                // Escanear columna completa
                for (int y = 0; y < Board.BOARD_SIZE; y++) {
                    Coordinate coord = new Coordinate(index, y);
                    Cell cell = cpuBoard.getCell(coord);
                    if (cell != null) {
                        scannedArea.add(coord);
                        scannedCells.add(cell);
                        if (cell.hasShip() && !cell.hasBeenShot()) {
                            shipCount++;
                        }
                    }
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
                "Dron exploró " + (isRow ? "fila " : "columna ") + (index + 1) + 
                " | Detectó " + shipCount + " barcos", 
                scannedArea, 
                scannedCells
            );
            
        } catch (Exception e) {
            return new SkillResult(false, "Error al usar dron: " + e.getMessage());
        }
    }

    public SkillResult useGuidedMissile(Coordinate target) {
        try {
            Board cpuBoard = gameController.getCpuBoard();
            
            // Verificar que el objetivo sea válido
            if (!isValidCoordinate(target)) {
                return new SkillResult(false, "Coordenada inválida: " + target.aNotacion());
            }

            Cell targetCell = cpuBoard.getCell(target);
            if (targetCell == null) {
                return new SkillResult(false, "Celda no encontrada: " + target.aNotacion());
            }

            // Verificar si ya se disparó aquí
            if (targetCell.hasBeenShot()) {
                return new SkillResult(false, "Ya se disparó en " + target.aNotacion());
            }

            ShotResult result;
            
            // Misil guiado: impacto garantizado si hay barco
            if (targetCell.hasShip()) {
                Ship hitShip = targetCell.getShip();
                boolean wasSunk = hitShip.isSunk();
                
                // Registrar el impacto
                targetCell.shoot();
                boolean isSunk = hitShip.isSunk();
                
                result = isSunk ? ShotResult.SUNK : ShotResult.HIT;
                
                // Mensaje especial si se hundió un barco
                if (isSunk && !wasSunk) {
                    result = ShotResult.SUNK;
                }
            } else {
                // Disparo normal si no hay barco
                result = gameController.processPlayerShot(target);
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
                "Misil guiado: " + getShotResultMessage(result), 
                Arrays.asList(target), 
                Arrays.asList(targetCell)
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
            int hitCount = 0;

            // Patrón de cruz (5 casillas)
            int[][] offsets = {{0,0}, {-1,0}, {1,0}, {0,-1}, {0,1}};
            
            for (int[] offset : offsets) {
                int x = center.getX() + offset[0];
                int y = center.getY() + offset[1];

                if (isValidCoordinate(x, y)) {
                    Coordinate coord = new Coordinate(x, y);
                    Cell cell = cpuBoard.getCell(coord);
                    
                    if (cell != null && !cell.hasBeenShot()) {
                        ShotResult result = gameController.processPlayerShot(coord);
                        results.add(result);
                        
                        if (result.isHit()) {
                            hitCount++;
                        }
                        
                        affectedArea.add(coord);
                        affectedCells.add(cell);
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

            return new SkillResult(
                true, 
                "Bomba de racimo: " + hitCount + " impacto(s) en " + affectedArea.size() + " casillas!", 
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
            gameController.setJammingActive(true);
            
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
            
            if (!isValidCoordinate(position)) {
                return new SkillResult(false, "Coordenada inválida para reparación");
            }
            
            Cell cell = playerBoard.getCell(position);
            
            if (cell == null) {
                return new SkillResult(false, "Celda no encontrada: " + position.aNotacion());
            }
            
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
                Cell cell = playerBoard.getCell(coord);
                if (cell != null) {
                    cell.setShip(null);
                }
            }

            // Colocar en nueva posición
            boolean placed = gameController.placeShip(ship, newPosition, direction);
            
            if (!placed) {
                // Restaurar a posición original si falla
                gameController.placeShip(ship, ship.getStartCoordinate(), ship.getDirection());
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
        
        if (cpuSkills == null) {
            return null;
        }

        // Priorizar habilidades según la situación del juego
        if (shouldUseRadar() && cpuSkills.canUseSkill(Skill.RADAR)) {
            return Skill.RADAR;
        } else if (shouldUseSonar() && cpuSkills.canUseSkill(Skill.SONAR)) {
            return Skill.SONAR;
        } else if (shouldUseJamming() && cpuSkills.canUseSkill(Skill.JAMMING)) {
            return Skill.JAMMING;
        } else if (shouldUseClusterBomb() && cpuSkills.canUseSkill(Skill.CLUSTER_BOMB)) {
            return Skill.CLUSTER_BOMB;
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
        if (skill == null) {
            return new SkillResult(false, "Habilidad no disponible");
        }
        
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
                return new SkillResult(false, "Habilidad no implementada para CPU: " + skill);
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
        gameController.setJammingActive(true);
        return new SkillResult(true, "CPU usó interferencia! Pierdes tu próximo turno");
    }

    private SkillResult useCPUClusterBomb() {
        Coordinate target = findBestCPUTargetForCluster();
        return useClusterBomb(target);
    }

    // ========== LÓGICA DE DECISIÓN DE LA CPU ==========

    private boolean shouldUseRadar() {
        // Usar radar cuando haya pocos barcos del jugador intactos
        return gameController.getRemainingPlayerShips() <= 2;
    }

    private boolean shouldUseSonar() {
        // Usar sonar cuando el jugador tenga barcos grandes sin descubrir
        return hasLargePlayerShipsUndiscovered();
    }

    private boolean shouldUseJamming() {
        // Usar jamming cuando el jugador esté en racha de aciertos
        return gameController.getPlayerHitStreak() >= 2;
    }

    private boolean shouldUseClusterBomb() {
        // Usar cluster bomb cuando haya áreas concentradas de barcos
        return findBestCPUTargetForCluster() != null;
    }

    private boolean hasLargePlayerShipsUndiscovered() {
        for (Ship ship : gameController.getPlayerShipsNotSunk()) {
            if (ship.getType().getSize() >= 4 && ship.getIntactPartsCount() >= 2) {
                return true;
            }
        }
        return false;
    }

    private Coordinate findBestCPUTargetForSonar() {
        // Buscar área con mayor densidad de barcos del jugador
        // Por ahora, posición aleatoria evitando bordes
        return new Coordinate(
            random.nextInt(Board.BOARD_SIZE - 2) + 1,
            random.nextInt(Board.BOARD_SIZE - 2) + 1
        );
    }

    private Coordinate findBestCPUTargetForCluster() {
        // Buscar área donde se sospecha que hay barcos del jugador
        // Basado en disparos previos exitosos
        List<Coordinate> hitCoordinates = gameController.getPlayerHitCoordinates();
        if (!hitCoordinates.isEmpty()) {
            return hitCoordinates.get(random.nextInt(hitCoordinates.size()));
        }
        
        // Posición aleatoria como fallback
        return new Coordinate(
            random.nextInt(Board.BOARD_SIZE),
            random.nextInt(Board.BOARD_SIZE)
        );
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private boolean isValidCoordinate(Coordinate coord) {
        return coord != null && 
               coord.getX() >= 0 && coord.getX() < Board.BOARD_SIZE && 
               coord.getY() >= 0 && coord.getY() < Board.BOARD_SIZE;
    }
    
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < Board.BOARD_SIZE && y >= 0 && y < Board.BOARD_SIZE;
    }
    
    private String getShotResultMessage(ShotResult result) {
        switch (result) {
            case HIT: return "Impacto!";
            case MISS: return "Falló!";
            case SUNK: return "Barco hundido!";
            case ALREADY_SHOT: return "Ya disparado aquí";
            case ALREADY_HIT: return "Ya impactado aquí";
            case INVALID: return "Disparo inválido";
            default: return "Resultado desconocido";
        }
    }
    
    /**
     * Verifica si una habilidad está disponible para el jugador
     */
    public boolean isPlayerSkillAvailable(Skill skill) {
        SkillSystem playerSkills = gameController.getPlayerSkills();
        return playerSkills != null && playerSkills.canUseSkill(skill);
    }
    
    /**
     * Obtiene el costo de una habilidad para el jugador
     */
    public int getPlayerSkillCost(Skill skill) {
        SkillSystem playerSkills = gameController.getPlayerSkills();
        return playerSkills != null ? playerSkills.getSkillCost(skill) : 0;
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
    
    /**
     * Obtiene el nombre amigable de una habilidad
     */
    public String getSkillDisplayName(Skill skill) {
        switch (skill) {
            case SONAR: return "Sonar";
            case RADAR: return "Radar";
            case DRONE: return "Dron de Exploración";
            case GUIDED_MISSILE: return "Misil Guiado";
            case CLUSTER_BOMB: return "Bomba de Racimo";
            case JAMMING: return "Interferencia";
            case REPAIR: return "Reparación";
            case CAMOUFLAGE: return "Camuflaje";
            default: return skill.name();
        }
    }
}