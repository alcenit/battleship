/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model.enums;

/**
 *
 * @author Usuario
 */
public enum GamePhase {
  
    // Valores del enum
    IN_PLAY("IN_PLAY", "En juego"),
    PLAYER_WIN("PLAYER_WIN", "Jugador gana"),
    CPU_WIN("CPU_WIN", "CPU gana"),
    SHIP_PLACEMENT("SHIP_PLACEMENT", "Colocando barcos"),
    PAUSED("PAUSED", "Pausado"),
    GAME_OVER("GAME_OVER", "Juego terminado");
    
    private final String code;
    private final String description;

 
    GamePhase(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Método valueOf personalizado que es más flexible que el valueOf por defecto
     * @param value El string a convertir (puede ser código, descripción o nombre del enum)
     * @return El GameState correspondiente
     * @throws IllegalArgumentException si el valor no es reconocido
     */
    public static GamePhase fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return IN_PLAY; // Valor por defecto
        }

        String normalizedValue = value.trim().toUpperCase();

        // Primero intenta con el valueOf estándar
        try {
            return GamePhase.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // Si falla, busca por código o descripción
        }

        // Buscar por código (sin normalizar, para mayor precisión)
        for (GamePhase state : GamePhase.values()) {
            if (state.code.equalsIgnoreCase(value) || 
                state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }

        // Buscar por descripción (búsqueda más flexible)
        for (GamePhase state : GamePhase.values()) {
            if (state.description.equalsIgnoreCase(value)) {
                return state;
            }
        }

        // Búsqueda por palabras clave como fallback
        if (value.contains("PLAYER") && value.contains("WIN")) return PLAYER_WIN;
        if (value.contains("CPU") && value.contains("WIN")) return CPU_WIN;
        if (value.contains("PLAY") || value.contains("JUEGO")) return IN_PLAY;
        if (value.contains("PLACEMENT") || value.contains("COLOCANDO")) return SHIP_PLACEMENT;
        if (value.contains("PAUSE") || value.contains("PAUSADO")) return PAUSED;
        if (value.contains("OVER") || value.contains("TERMINADO")) return GAME_OVER;

        throw new IllegalArgumentException("Valor no reconocido para GameState: " + value);
    }

    /**
     * Versión segura que no lanza excepción
     * @param value El string a convertir
     * @return El GameState correspondiente o IN_PLAY por defecto si hay error
     */
    public static GamePhase safeFromValue(String value) {
        try {
            return fromValue(value);
        } catch (IllegalArgumentException e) {
            System.err.println("Error convirtiendo GameState: " + value + " - Usando IN_PLAY por defecto");
            return IN_PLAY;
        }
    }

    /**
     * Método que simula el valueOf pero con nuestra lógica personalizada
     * @param name El nombre del estado (igual que valueOf estándar)
     * @return El GameState correspondiente
     */
    public static GamePhase valueOfCustom(String name) {
        return fromValue(name);
    }

    // El valueOf() estándar de Java sigue disponible para: GameState.valueOf("IN_PLAY")
    
}
