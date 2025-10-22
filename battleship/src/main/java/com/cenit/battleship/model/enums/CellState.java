package com.cenit.battleship.model.enums;

public enum CellState {

    WATER, // Casilla vacía
    SHIP, // Casilla con barco intacto
    IMPACT, // Disparo acertado
    MISS, // Disparo fallido
    MARKED_WATER, // Agua marcada (para modo difícil)
    SUNK_SHIP       // Barco hundido
}
