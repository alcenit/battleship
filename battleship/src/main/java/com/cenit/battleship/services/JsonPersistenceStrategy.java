/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.services;


import com.cenit.battleship.model.GameState;
import com.cenit.battleship.model.enums.GamePhase;

import com.cenit.battleship.services.dto.GameStateDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */

public class JsonPersistenceStrategy implements PersistenceStrategy {
    
    private static final String SAVE_DIR = "saves/";
    private static final String FILE_EXTENSION = ".json";
    private Gson gson;
    private GameStateMapper mapper;

    public JsonPersistenceStrategy() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
        this.mapper = new GameStateMapper();
        createDirectory();
    }

    @Override
    public boolean guardar(GameState gameState, String nombreArchivo) {
        try {
            // Convertir GameState a DTO
            GameStateDTO dto = convertirAGameStateDTO(gameState);
            
            String json = gson.toJson(dto);
            Path archivo = Paths.get(SAVE_DIR + nombreArchivo + FILE_EXTENSION);
            Files.write(archivo, json.getBytes());
            
            System.out.println("Partida guardada como JSON: " + archivo.toAbsolutePath());
            return true;
            
        } catch (Exception e) {
            System.err.println("Error guardando JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GameState cargar(String nombreArchivo) {
        try {
            Path archivo = Paths.get(SAVE_DIR + nombreArchivo + FILE_EXTENSION);
            if (!Files.exists(archivo)) {
                throw new FileNotFoundException("Archivo no encontrado: " + archivo);
            }

            String json = new String(Files.readAllBytes(archivo));
            GameStateDTO dto = gson.fromJson(json, GameStateDTO.class);
            
            // Convertir DTO de vuelta a GameState
            return convertirDeGameStateDTO(dto);
            
        } catch (Exception e) {
            System.err.println("Error cargando JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> listarGuardados() {
        List<String> guardados = new ArrayList<>();
        try {
            Files.list(Paths.get(SAVE_DIR))
                .filter(path -> path.toString().endsWith(FILE_EXTENSION))
                .forEach(path -> guardados.add(
                    path.getFileName().toString().replace(FILE_EXTENSION, "")
                ));
        } catch (IOException e) {
            System.err.println("Error listando guardados JSON: " + e.getMessage());
        }
        return guardados;
    }

    @Override
    public boolean eliminar(String nombreArchivo) {
        try {
            return Files.deleteIfExists(Paths.get(SAVE_DIR + nombreArchivo + FILE_EXTENSION));
        } catch (IOException e) {
            System.err.println("Error eliminando JSON: " + e.getMessage());
            return false;
        }
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========
    
    private GameStateDTO convertirAGameStateDTO(GameState gameState) {
        // Implementar conversión de GameState a GameStateDTO
        // Esto requiere mapear todos los campos
        GameStateDTO dto = new GameStateDTO();
        dto.setFechaGuardado(gameState.getSavedDate());
        dto.setTurnoJugador(gameState.isPlayerTurn());
     //   dto.setGameState(gameState.getGameState().getname());
        // ... mapear el resto de campos
        
        return dto;
    }
    
    private GameState convertirDeGameStateDTO(GameStateDTO dto) {
        // Implementar conversión de GameStateDTO a GameState
        GameState gameState = new GameState();
        
        gameState.setSavedDate(dto.getSavedDate());
        gameState.setPlayerTurn(dto.isTurnoJugador());
      //  gameState.setGameState(GamePhase.valueOfCustom(dto.getGameState()));
        
        // ... mapear el resto de campos
        
        return gameState;
    }

    private void createDirectory() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("Error creando directorio: " + e.getMessage());
        }
    }
}
