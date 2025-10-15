/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.sevices.dto.BoardDTO;
import com.cenit.battleship.sevices.dto.GameStateDTO;
import com.cenit.battleship.sevices.dto.ShipDTO;
import com.cenit.battleship.sevices.dto.SkillDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Usuario
 */
public class GameStateMapper {
  public GameStateDTO toDTO(GameController gameController) {
        GameStateDTO dto = new GameStateDTO();
        
        // Información básica
        dto.setFechaGuardado(new Date());
        dto.setTurnoJugador(gameController.isPlayerTurn());
        dto.setGameState(gameController.getGameState().name());
        
        // Tableros
        dto.setTableroJugador(mapBoard(gameController.getPlayerBoard()));
        dto.setTableroCPU(mapBoard(gameController.getCPUBoard()));
        
        // Barcos
        dto.setBarcosJugador(mapShips(gameController.getPlayerShips()));
        dto.setBarcosCPU(mapShips(gameController.getCPUShips()));
        
        // Habilidades
        dto.setHabilidadesJugador(mapSkills(gameController.getPlayerSkills()));
        dto.setHabilidadesCPU(mapSkills(gameController.getCPUSkills()));
        
        // Barcos hundidos
        dto.setBarcosHundidosJugador(mapSunkenShips(gameController.getSunkShipsPlayer()));
        dto.setBarcosHundidosCPU(mapSunkenShips(gameController.getSunkShipsCPU()));
        
        return dto;
    }
    
    public GameController fromDTO(GameStateDTO dto) {
        GameController gameController = new GameController();
        
        // Restaurar estado básico
        gameController.setTurnoJugador(dto.isTurnoJugador());
        //gameController.setGameState(EstadoJuego.valueOf(dto.getGameState()));
        gameController.setGameState(GamePhase.valueOf(dto.getGameState()));
        
        // Restaurar tableros
        restoreBoard(gameController.getPlayerBoard(), dto.getTableroJugador());
        restoreBoard(gameController.getCPUBoard(), dto.getTableroCPU());
        
        // Restaurar barcos y habilidades (implementación más compleja)
        // ...
        
        return gameController;
    }
    
    // ========== MÉTODOS DE MAPEO ESPECÍFICOS ==========
    
    private BoardDTO mapBoard(Board board) {
        BoardDTO dto = new BoardDTO();
        dto.setSize(board.getSize());
        
        String[][] states = new String[board.getSize()][board.getSize()];
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                states[i][j] = board.getCell(j, j).getState().name();
            }
        }
        dto.setCellStates(states);
        
        return dto;
    }
    
    private List<ShipDTO> mapShips(List<Ship> ships) {
        List<ShipDTO> dtos = new ArrayList<>();
        for (Ship ship : ships) {
            ShipDTO dto = new ShipDTO();
            dto.setType(ship.getType().name());
            dto.setImpactsRecieved(ship.getImpactsRecieved());
            // dto.setPosiciones(mapearPosicionesBarco(barco));
            dtos.add(dto);
        }
        return dtos;
    }
    
    private SkillDTO mapSkills(SkillSystem skills) {
        SkillDTO dto = new SkillDTO();
        dto.setPuntosHabilidad(skills.getSkillPoints());
        
        // Convertir Enums a Strings
        Map<String, Integer> habilidadesDisponibles = new HashMap<>();
        Map<String, Integer> usosRestantes = new HashMap<>();
        
        for (Map.Entry<Skill, Integer> entry : skills.getAvailableSkills().entrySet()) {
            habilidadesDisponibles.put(entry.getKey().name(), entry.getValue());
        }
        
        for (Map.Entry<Skill, Integer> entry : skills.getRemainingUses().entrySet()) {
            usosRestantes.put(entry.getKey().name(), entry.getValue());
        }
        
        dto.setHabilidadesDisponibles(habilidadesDisponibles);
        dto.setUsosRestantes(usosRestantes);
        
        return dto;
    }
    
    private List<String> mapSunkenShips(Set<Ship> barcosHundidos) {
        List<String> nombres = new ArrayList<>();
        for (Ship ship : barcosHundidos) {
            nombres.add(ship.getType().name());
        }
        return nombres;
    }
    
    private void restoreBoard(Board board, BoardDTO dto) {
    if (board == null || dto == null) {
        throw new IllegalArgumentException("Board o DTO no pueden ser nulos");
    }
    
    int size = dto.getSize();
    if (size != board.getSize()) {
        throw new IllegalStateException("Tamaño del tablero no coincide con DTO");
    }
    
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            String estadoStr = dto.getCellStates()[i][j];
            CellState estado = parseCellState(estadoStr);
            board.getCell(i, j).setState(estado);
        }
    }
}

private CellState parseCellState(String estadoStr) {
    if (estadoStr == null) return CellState.WATER;
    
    try {
        return CellState.valueOf(estadoStr);
    } catch (IllegalArgumentException e) {
        // Mapeo de valores alternativos por si acaso
        switch(estadoStr.toUpperCase()) {
            case "AGUA": return CellState.WATER;
            case "BARCO": return CellState.SHIP;
            case "IMPACTO": return CellState.IMPACT;
            case "FALLO": return CellState.FAIL;
            case "AGUA_MARCADA": return CellState.MARKED_WATER;
            default: return CellState.WATER;
        }
    }
}
    
    
    
    
}
