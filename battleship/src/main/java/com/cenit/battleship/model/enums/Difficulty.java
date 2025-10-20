package com.cenit.battleship.model.enums;

public enum Difficulty {
    EASY("Fácil", "Disparos aleatorios"),
    NORMAL("Normal", "Estrategia básica"),
    HARD("Difícil", "Búsqueda inteligente"),
    EXPERT("Experto", "Algoritmo avanzado");
    
    private final String displayName;
    private final String description;
    
    Difficulty(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}