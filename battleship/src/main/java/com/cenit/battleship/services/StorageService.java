/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.services;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.*;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.ShipType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageService {

    private static final String SAVE_DIR = "saves/";
    private static final String SAVE_EXTENSION = ".bsg";
    private Gson gson;

    public StorageService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        createSaveDirectory();
    }

    private void createSaveDirectory() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("Error al crear directorio de guardados: " + e.getMessage());
        }
    }

    // ========== GUARDADO DE PARTIDA ==========
    public boolean saveGame(GameController gameController, String fileName) {
        try {
            GameState gameState = createGameState(gameController);
            String json = gson.toJson(gameState);

            Path file = Paths.get(SAVE_DIR + fileName + SAVE_EXTENSION);
            Files.write(file, json.getBytes());

            System.out.println("Partida guardada: " + file.toAbsolutePath());
            return true;

        } catch (Exception e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
            return false;
        }
    }

    public boolean saveGameAuto(GameController gameController) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String fileName = "game_" + timestamp;
        return saveGame(gameController, fileName);
    }

    // ========== CARGA DE PARTIDA ==========
    public GameController loadGame(String fileName) {
        try {
            Path file = Paths.get(SAVE_DIR + fileName + SAVE_EXTENSION);
            if (!Files.exists(file)) {
                throw new FileNotFoundException("Archivo de guardado no encontrado: " + file);
            }

            String json = new String(Files.readAllBytes(file));
            GameState gameState = gson.fromJson(json, GameState.class);

            return recreateGameController(gameState);

        } catch (Exception e) {
            System.err.println("Error al cargar partida: " + e.getMessage());
            return null;
        }
    }

    // ========== LISTADO Y GESTIÓN DE GUARDADOS ==========
    public List<SaveGameInfo> listSavedGames() {
        List<SaveGameInfo> games = new ArrayList<>();

        try {
            Files.list(Paths.get(SAVE_DIR))
                    .filter(path -> path.toString().endsWith(SAVE_EXTENSION))
                    .forEach(path -> {
                        SaveGameInfo info = getSaveInfo(path);
                        if (info != null) {
                            games.add(info);
                        }
                    });

        } catch (IOException e) {
            System.err.println("Error al listar partidas guardadas: " + e.getMessage());
        }

        // Ordenar por fecha (más reciente primero)
        games.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return games;
    }

    private SaveGameInfo getSaveInfo(Path file) {
        try {
            String json = new String(Files.readAllBytes(file));
            GameState state = gson.fromJson(json, GameState.class);

            return new SaveGameInfo(
                    file.getFileName().toString().replace(SAVE_EXTENSION, ""),
                    state.getSavedDate(),
                    state.getElapsedTurns(),
                    getSunkenShipsCount(state.getPlayerSunkenShips()),
                    getSunkenShipsCount(state.getCPUSunkenShips()),
                    file.toFile().length()
            );

        } catch (Exception e) {
            System.err.println("Error al obtener info del guardado: " + e.getMessage());
            return null;
        }
    }

    private int getSunkenShipsCount(List<Ship> ships) {
        return ships != null ? ships.size() : 0;
    }

    public boolean deleteGame(String fileName) {
        try {
            Path file = Paths.get(SAVE_DIR + fileName + SAVE_EXTENSION);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Error al eliminar partida: " + e.getMessage());
            return false;
        }
    }

    // ========== SERIALIZACIÓN ==========
    private GameState createGameState(GameController gameController) {
        GameState state = new GameState();

        // Información básica
        state.setSavedDate(new Date());
        state.setElapsedTurns(gameController.getElapsedTurns());

        // Estado del juego
        state.setPlayerTurn(gameController.isPlayerTurn());
        state.setGamePhase(gameController.getGamePhase());

        // Barcos
        state.setPlayerShips(gameController.getPlayerShips());
        state.setCPUShips(gameController.getCPUShips());

        // Habilidades - Comentado hasta que implementes los métodos
        // state.setPlayerSkills(serializeSkills(gameController.getPlayerSkills()));
        // state.setCpuSkills(serializeSkills(gameController.getCPUSkills()));

        // Barcos hundidos
        state.setPlayerSunkenShips(new ArrayList<>(gameController.getSunkShipsPlayer()));
        state.setCPUSunkenShips(new ArrayList<>(gameController.getSunkShipsCPU()));

        // Configuración
        state.setConfiguration(Configuration.getInstance());

        return state;
    }

    private GameController recreateGameController(GameState state) {
        // Crear nuevo controlador con la dificultad por defecto
        GameController gameController = new GameController();

        // Restaurar información básica
        gameController.setElapsedTurns(state.getElapsedTurns());
        gameController.setPlayerTurn(state.isPlayerTurn());
        gameController.setGamePhase(state.getGamePhase());

        // Restaurar barcos - necesitarás lógica específica
        restoreShips(gameController, state);

        // Restaurar habilidades - necesitarás lógica específica
        restoreSkills(gameController, state);

        return gameController;
    }

    // ========== MÉTODOS AUXILIARES DE SERIALIZACIÓN ==========
    private BoardState serializeBoard(Board board) {
        BoardState state = new BoardState();
        state.setSize(state.getSize());

        CellState[][] cellStates = new CellState[state.getSize()][state.getSize()];
        for (int i = 0; i < state.getSize(); i++) {
            for (int j = 0; j < state.getSize(); j++) {
                // Necesitas un método getCellState en Board o Cell
                 cellStates[i][j] = board.getCell(i, j).getState();
            }
        }
        state.setCellStates(cellStates);

        return state;
    }

    private void restoreShips(GameController gameController, GameState state) {
        // Implementar lógica para restaurar barcos a sus posiciones
        // Esto depende de tu implementación específica
        try {
            // Ejemplo básico - necesitas adaptar
            List<Ship> playerShips = state.getPlayerShips();
            List<Ship> cpuShips = state.getCPUShips();

            // Aquí necesitas restaurar los barcos a los tableros
            // gameController.getPlayerBoard().restoreShips(playerShips);
            // gameController.getCPUBoard().restoreShips(cpuShips);

        } catch (Exception e) {
            System.err.println("Error al restaurar barcos: " + e.getMessage());
        }
    }

    private SkillsState serializeSkills(SkillSystem skills) {
        SkillsState state = new SkillsState();
        state.setSkillPoints(skills.getSkillPoints());
        
        // Solo serializar si existen los métodos
        if (skills.getAvailableSkills() != null) {
            state.setAvailableSkills(new HashMap<>(skills.getAvailableSkills()));
        }
        
        // Agregar remainingUses si existe el método
        // if (skills.getRemainingUses() != null) {
        //     state.setRemainingUses(new HashMap<>(skills.getRemainingUses()));
        // }
        
        return state;
    }

    private void restoreSkills(GameController gameController, GameState state) {
        try {
            // Comentado hasta que implementes SkillState en GameState
            // SkillsState playerSkills = state.getPlayerSkills();
            // SkillsState cpuSkills = state.getCpuSkills();
            
            // if (playerSkills != null) {
            //     gameController.getPlayerSkills().setSkillPoints(playerSkills.getSkillPoints());
            // }
            
            // if (cpuSkills != null) {
            //     gameController.getCPUSkills().setSkillPoints(cpuSkills.getSkillPoints());
            // }

        } catch (Exception e) {
            System.err.println("Error al restaurar habilidades: " + e.getMessage());
        }
    }

    // ========== CLASES INTERNAS PARA SERIALIZACIÓN ==========
    public static class BoardState {
        private int size;
        private CellState[][] cellStates;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public CellState[][] getCellStates() {
            return cellStates;
        }

        public void setCellStates(CellState[][] cellStates) {
            this.cellStates = cellStates;
        }
    }

    public static class SkillsState {
        private int skillPoints;
        private Map<Skill, Integer> availableSkills;
        private Map<Skill, Integer> remainingUses; // Agregado

        public int getSkillPoints() {
            return skillPoints;
        }

        public void setSkillPoints(int skillPoints) {
            this.skillPoints = skillPoints;
        }

        public Map<Skill, Integer> getAvailableSkills() {
            return availableSkills;
        }

        public void setAvailableSkills(Map<Skill, Integer> availableSkills) {
            this.availableSkills = availableSkills;
        }

        public Map<Skill, Integer> getRemainingUses() {
            return remainingUses;
        }

        public void setRemainingUses(Map<Skill, Integer> remainingUses) {
            this.remainingUses = remainingUses;
        }
    }

    public static class ShipState {
        private ShipType type;
        private int hitsReceived;
        private List<Coordinate> positions;

        public ShipType getType() {
            return type;
        }

        public void setType(ShipType type) {
            this.type = type;
        }

        public int getHitsReceived() {
            return hitsReceived;
        }

        public void setHitsReceived(int hitsReceived) {
            this.hitsReceived = hitsReceived;
        }

        public List<Coordinate> getPositions() {
            return positions;
        }

        public void setPositions(List<Coordinate> positions) {
            this.positions = positions;
        }
    }
}
