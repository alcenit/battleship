package com.cenit.battleship.model.enums;

/**
 * Resultado de un disparo en el juego de batalla naval
 */
public enum ShotResult {
    HIT(true, false, "💥 ¡Impacto!", "Has golpeado un barco enemigo"),
    MISS(false, false, "💧 Agua", "Disparo al agua"),
    SUNK(true, true, "💀 ¡Barco hundido!", "Has hundido un barco enemigo"),
    ALREADY_SHOT(false, false, "⚠️ Ya disparado", "Ya habías disparado en esta posición"),
    ALREADY_HIT(false, false, "Ya se impactó", " Ya habías disparado en esta parte del barco"),
    INVALID(false, false, "❌ Inválido", "Coordenada de disparo inválida");

    private final boolean impact;
    private final boolean sunk;
    private final String symbol;
    private final String description;

    ShotResult(boolean impact, boolean sunk, String symbol, String description) {
        this.impact = impact;
        this.sunk = sunk;
        this.symbol = symbol;
        this.description = description;
    }

    // Getters
    public boolean isHit() {
        return impact;
    }

    public boolean isSunk() {
        return sunk;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return symbol + " " + description;
    }

    /**
     * Crea un ShotResult basado en si hubo impacto y si se hundió un barco
     */
    public static ShotResult fromImpact(boolean impact, boolean sunk) {
        if (sunk) {
            return SUNK;
        }
        return impact ? HIT : MISS;
    }

    /**
     * Verifica si el resultado permite continuar el turno
     */
    public boolean allowsAnotherTurn() {
        // En algunas variantes del juego, los impactos permiten otro turno
        return this == HIT || this == SUNK;
    }

    /**
     * Obtiene el color asociado al resultado (para UI)
     */
    public String getColor() {
        switch (this) {
            case HIT:
                return "#FF6B6B"; // Rojo
            case MISS:
                return "#4FC3F7"; // Azul
            case SUNK:
                return "#D32F2F"; // Rojo oscuro
            case ALREADY_SHOT:
                return "#9E9E9E"; // Gris
            case INVALID:
                return "#F44336"; // Rojo error
            default:
                return "#FFFFFF"; // Blanco
        }
    }

    /**
     * Obtiene el sonido asociado al resultado
     */
    public String getSoundEffect() {
        switch (this) {
            case HIT:
                return "explosion.wav";
            case MISS:
                return "splash.wav";
            case SUNK:
                return "sinking.wav";
            case ALREADY_SHOT:
                return "error.wav";
            case INVALID:
                return "error.wav";
            default:
                return "click.wav";
        }
    }

    /**
     * Obtiene los puntos asociados al resultado
     */
    public int getPoints() {
        switch (this) {
            case HIT:
                return 10;
            case MISS:
                return 0;
            case SUNK:
                return 50;
            case ALREADY_SHOT:
                return -5;
            case INVALID:
                return -10;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
