package com.cenit.battleship.services;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Cell;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.Skill;
import com.cenit.battleship.model.SkillSystem;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.GamePhase;
import com.cenit.battleship.services.dto.BoardDTO;
import com.cenit.battleship.services.dto.GameStateDTO;
import com.cenit.battleship.services.dto.ShipDTO;
import com.cenit.battleship.services.dto.SkillDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameStateMapper {

    public GameStateDTO toDTO(GameController gameController) {
        GameStateDTO dto = new GameStateDTO();
        
        // Información básica
        dto.setFechaGuardado(new Date());
        dto.setTurnoJugador(gameController.isPlayerTurn());
        dto.setGameState(gameController.getGamePhase().name());
        
        // Tableros - CORREGIDO
        dto.setTableroJugador(mapBoardState(gameController.getPlayerBoard()));
        dto.setTableroCPU(mapBoardState(gameController.getCpuBoard()));
        
        // Barcos
        dto.setBarcosJugador(mapShips(gameController.getPlayerShips()));
        dto.setBarcosCPU(mapShips(gameController.getCPUShips()));
        
        // Habilidades
        dto.setHabilidadesJugador(mapSkills(gameController.getPlayerSkills()));
        dto.setHabilidadesCPU(mapSkills(gameController.getCPUSkills()));
        
        // Estadísticas
        dto.setBarcosHundidosJugador(mapSunkenShips(gameController.getSunkShipsPlayer()));
        dto.setBarcosHundidosCPU(mapSunkenShips(gameController.getSunkShipsCPU()));
        
        return dto;
    }
    
    public GameController fromDTO(GameStateDTO dto) {
        GameController gameController = new GameController();
        
        try {
            // Restaurar estado básico
            gameController.setPlayerTurn(dto.isTurnoJugador());
            gameController.setGamePhase(GamePhase.valueOf(dto.getGameState()));
            
            // Restaurar tableros - CORREGIDO
            restoreBoard(gameController.getPlayerBoard(), dto.getTableroJugador());
            restoreBoard(gameController.getCpuBoard(), dto.getTableroCPU());
            
            // Restaurar barcos
            restoreShips(gameController, dto);
            
            // Restaurar habilidades
            restoreSkills(gameController.getPlayerSkills(), dto.getHabilidadesJugador());
            restoreSkills(gameController.getCPUSkills(), dto.getHabilidadesCPU());
            
            System.out.println("✅ Estado del juego restaurado desde DTO");
            
        } catch (Exception e) {
            System.err.println("❌ Error restaurando desde DTO: " + e.getMessage());
            throw new RuntimeException("Error restaurando partida", e);
        }
        
        return gameController;
    }
    
    // ========== MÉTODOS DE MAPEO ESPECÍFICOS ==========
    
    private BoardDTO mapBoardState(Board board) {
        BoardDTO dto = new BoardDTO();
        dto.setSize(Board.SIZE);
        
        String[][] states = new String[Board.SIZE][Board.SIZE];
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Cell cell = board.getCell(i, j);
                states[i][j] = cell.getState().name();
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
            dto.setImpactsRecieved(ship.getImpactsReceived());
            dto.setSunk(ship.isSunk());
            // Aquí podrías agregar más información como posiciones si es necesario
            dtos.add(dto);
        }
        return dtos;
    }
    
    private SkillDTO mapSkills(SkillSystem skills) {
        SkillDTO dto = new SkillDTO();
        dto.setPuntosHabilidad(skills.getSkillPoints());
        
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
        return barcosHundidos.stream()
                .map(ship -> ship.getType().name())
                .collect(Collectors.toList());
    }
    
   private void restoreBoard(Board board, BoardDTO dto) {
    if (board == null || dto == null) {
        throw new IllegalArgumentException("Board o DTO no pueden ser nulos");
    }
    
    int size = dto.getSize();
    if (size != Board.SIZE) {
        throw new IllegalStateException("Tamaño del tablero no coincide: " + size + " vs " + Board.SIZE);
    }
    
    // Primero limpiar el tablero
    board.reset();
    
    // Restaurar el estado de cada celda
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            String estadoStr = dto.getCellStates()[i][j];
            CellState estado = parseCellState(estadoStr);
            Coordinate coord = new Coordinate(i, j);
            Cell cell = board.getCell(coord);
            
            restoreCellState(cell, estado);
        }
    }
    
   
}
   
private void restoreCellState(Cell cell, CellState estado) {
    if (cell == null) return;
    
    // Resetear la celda primero
    cell.reset();
    
    switch (estado) {
        case WATER:
            // Estado por defecto, no hacer nada
            break;
            
        case SHIP:
            // Para SHIP necesitamos tener información del barco
            // Esto se manejará en restoreShips()
            break;
            
        case IMPACT:
            // Simular un disparo acertado
            if (cell.hasShip()) {
                cell.shoot(); // Esto marcará hasBeenShot = true e isHit = true
            }
            break;
            
        case MISS:
            // Simular un disparo fallido
            if (!cell.hasShip()) {
                // Forzar el estado de disparo fallido
                cell.shoot(); // Esto marcará hasBeenShot = true
            }
            break;
            
        case SUNK_SHIP:
            // Para SUNK_SHIP, el barco debe estar marcado como hundido
            if (cell.hasShip()) {
                cell.shoot(); // Marcar como impactado
                // El estado hundido se maneja automáticamente si el barco está hundido
            }
            break;
    }
}
    
    private CellState parseCellState(String estadoStr) {
        if (estadoStr == null) return CellState.WATER;
        
        try {
            return CellState.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Estado de celda no reconocido: " + estadoStr + ", usando WATER");
            return CellState.WATER;
        }
    }
    
    private void restoreShips(GameController gameController, GameStateDTO dto) {
        // Implementación básica - puedes expandir según tus necesidades
        System.out.println("🔧 Restaurando barcos...");
        // Aquí necesitarías lógica más compleja para reconstruir los barcos
        // en sus posiciones originales
    }
    
    private void restoreSkills(SkillSystem skillSystem, SkillDTO skillDTO) {
        if (skillDTO == null) return;
        
        skillSystem.setSkillPoints(skillDTO.getPuntosHabilidad());
        
        // Restaurar habilidades disponibles
        if (skillDTO.getHabilidadesDisponibles() != null) {
            for (Map.Entry<String, Integer> entry : skillDTO.getHabilidadesDisponibles().entrySet()) {
                try {
                    Skill skill = Skill.valueOf(entry.getKey());
                    skillSystem.addSkill(skill, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Habilidad no reconocida: " + entry.getKey());
                }
            }
        }
    }
}