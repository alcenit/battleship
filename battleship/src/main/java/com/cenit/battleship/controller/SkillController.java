/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.ShotResult;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillResult;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.Direction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class SkillController {

    private GameController gameController;
    private SoundController soundController;
    private AnimationController animationController;

    public SkillController(GameController gameController) {
        this.gameController = gameController;
        this.soundController = SoundController.getInstance();
        this.animationController = AnimationController.getInstance();
    }

    // ========== IMPLEMENTACIÓN DE HABILIDADES ==========

    public SkillResult useSonar(Coordinate center) {
        Board CPUBoard = gameController.getCPUBoard();
        List<Coordinate> revealedArea = new ArrayList<>();
        List<Cell> revealedCells = new ArrayList<>();

        // Revelar área 3x3 alrededor del centro
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = center.x() + dx;
                int y = center.y() + dy;

                if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                    Coordinate coord = new Coordinate(x, y);
                    revealedArea.add(coord);
                    revealedCells.add(CPUBoard.getCell(coord));
                }
            }
        }

        soundController.playSonar();
        
        return new SkillResult(
            true, 
            "Sonar activado! Área 3x3 revelada", 
            revealedArea, 
            revealedCells
        );
    }

    public SkillResult useRadar() {
        Board CPUBoard = gameController.getCPUBoard();
        List<Ship> CPUShips = gameController.getCPUShips();
        
        // Encontrar un barco no hundido
        Ship objetiveShip = null;
        for (Ship ship : CPUShips) {
            if (!ship.isSunk() && ship.isPlace()) {
                objetiveShip = ship;
                break;
            }
        }

        if (objetiveShip == null) {
            return new SkillResult(false, "No hay barcos para detectar");
        }

        // Revelar una posición aleatoria del barco
        List<Cell> positions = objetiveShip.getPositions();
        Cell revealedPosition = positions.get((int) (Math.random() * positions.size()));
        
        List<Coordinate> revealedCoordinates = new ArrayList<>();
        // Encontrar coordenada de la casilla revelada (esto requiere implementación adicional)
        
        soundController.playRadar();
        
        return new SkillResult(
            true, 
            "Radar detectó un " + objetiveShip.getType().getName(), 
            revealedCoordinates, 
            Arrays.asList(revealedPosition)
        );
    }

    public SkillResult useDrone(boolean isRow, int index) {
        Board CPUBoard = gameController.getCPUBoard();
        List<Coordinate> revealedArea = new ArrayList<>();
        List<Cell> revealledCells = new ArrayList<>();

        if (isRow) {
            // Revelar fila completa
            for (int x = 0; x < 10; x++) {
                Coordinate coord = new Coordinate(x, index);
                revealedArea.add(coord);
                revealledCells.add(CPUBoard.getCell(coord));
            }
        } else {
            // Revelar columna completa
            for (int y = 0; y < 10; y++) {
                Coordinate coord = new Coordinate(index, y);
                revealedArea.add(coord);
                revealledCells.add(CPUBoard.getCell(coord));
            }
        }

        return new SkillResult(
            true, 
            "Dron exploró " + (isRow ? "fila " : "columna ") + (index + 1), 
            revealedArea, 
            revealledCells
        );
    }

    public SkillResult useGuidedMissile(Coordinate target) {
        Board CPUBoard = gameController.getCPUBoard();
        
        // Disparo garantizado
        ShotResult result = gameController.playerShoots(target);
        
        return new SkillResult(
            true, 
            "Misil guiado: " + result.message(), 
            Arrays.asList(target), 
            Arrays.asList(CPUBoard.getCell(target))
        );
    }

    public SkillResult useClusterBomb(Coordinate center) {
        Board CPUBoard = gameController.getCPUBoard();
        List<Coordinate> affectedArea = new ArrayList<>();
        List<Cell> affectedCells = new ArrayList<>();

        // Patrón de cruz (5 casillas)
        int[][] offsets = {{0,0}, {-1,0}, {1,0}, {0,-1}, {0,1}};
        
        for (int[] offset : offsets) {
            int x = center.x() + offset[0];
            int y = center.y() + offset[1];

            if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                Coordinate coord = new Coordinate(x, y);
                affectedArea.add(coord);
                affectedCells.add(CPUBoard.getCell(coord));
                
                // Realizar disparo en cada casilla
                if (CPUBoard.getCell(coord).getState() == CellState.WATER || 
                    CPUBoard.getCell(coord).getState() == CellState.SHIP) {
                    gameController.playerShoots(coord);
                }
            }
        }

        return new SkillResult(
            true, 
            "Bomba de racimo detonada!", 
            affectedArea, 
            affectedCells
        );
    }

    public SkillResult useJamming() {
        // La CPU pierde su próximo turno
        gameController.setPlayerTurn(true); // Jugador mantiene el turno
        
        return new SkillResult(true, "Interferencia activada! CPU pierde turno");
    }

    public SkillResult useRepair(Coordinate position) {
        Board  playerBoard = gameController.getPlayerBoard();
        Cell cell = playerBoard.getCell(position);
        
        if (cell.getState() == CellState.IMPACT && cell.haveShip()) {
            // Reparar la casilla (volver a estado BARCO)
            // Esto requiere modificar el modelo para permitir reparaciones
            return new SkillResult(true, "Barco reparado exitosamente");
        }
        
        return new SkillResult(false, "No se puede reparar esta casilla");
    }

    public SkillResult useCamouflage(Ship ship, Coordinate newPosition, Direction direction) {
        // Mover barco a nueva posición
        // Esto requiere lógica compleja de recolocación
        return new SkillResult(true, "Barco movido exitosamente");
    }

    // ========== HABILIDADES DE LA CPU ==========

    public Skill decideCPUSkill() {
        SkillSystem CPUSkills = gameController.getCPUSkills();
        
        // Lógica simple de decisión para la CPU
        for (Skill skill : CPUSkills.getAvailableSkills().keySet()) {
            if (CPUSkills.canUseSkill(skill)) {
                // Priorizar habilidades ofensivas
                if (skill == Skill.SONAR || skill == Skill.RADAR) {
                    return skill;
                }
            }
        }
        
        return null;
    }

    public SkillResult runCPUSkill(Skill skill) {
        switch (skill) {
            case SONAR:
                return useCPUSonar();
            case RADAR:
                return useCPURadar();
            default:
                return new SkillResult(false, "Habilidad no disponible");
        }
    }

    private SkillResult useCPUSonar() {
        // CPU usa sonar en área con alta probabilidad de barcos
        Coordinate bestTarget = findBestTargetSonar();
        return useSonar(bestTarget);
    }

    private SkillResult useCPURadar() {
        return useRadar();
    }

    private Coordinate findBestTargetSonar() {
        // Lógica para encontrar la mejor posición para usar sonar
        // Por ahora, posición aleatoria
        return new Coordinate(
            (int) (Math.random() * 8) + 1, // Evitar bordes
            (int) (Math.random() * 8) + 1
        );
    }    
}
