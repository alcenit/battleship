package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.model.enums.ShotResult;

/**
 * Representa una celda en el tablero de batalla naval
 */
public class Cell {

    private Ship ship;
    private boolean revealed;
    private boolean hasBeenShot;

    public Cell() {
        this.ship = null;
        this.revealed = false;
        this.hasBeenShot = false;
    }

    // ========== MÉTODOS DE ESTADO ==========
    /**
     * Establece el estado de la celda desde un enum CellState
     *
     * @param state El estado a establecer
     */
    public void setState(CellState state) {
        switch (state) {
            case WATER:
            case MARKED_WATER:
                this.ship = null;
                this.revealed = false;
                this.hasBeenShot = false;
                break;

            case SHIP:
                this.ship = new Ship(ShipType.DESTROYER); // Necesitarás crear el barco apropiado
                this.revealed = false;
                this.hasBeenShot = false;
                break;

            case FAIL:
                this.ship = null;
                this.revealed = true;
                this.hasBeenShot = true;
                break;

            case IMPACT:
                // Necesitas tener una referencia al barco
                this.revealed = true;
                this.hasBeenShot = true;
                if (this.ship != null) {
                    this.ship.recordImpact();
                }
                break;

            case SUNK_SHIP:
                this.revealed = true;
                this.hasBeenShot = true;
                if (this.ship != null) {
                    // Marcar el barco como hundido
                    while (!this.ship.isSunk()) {
                        this.ship.recordImpact();
                    }
                }
                break;
        }
    }

    public CellState getState() {
        if (!revealed) {
            if (hasShip()) {
                return CellState.SHIP;
            } else {
                return hasBeenShot ? CellState.MARKED_WATER : CellState.WATER;
            }
        } else {
            if (hasShip()) {
                return ship.isSunk() ? CellState.SUNK_SHIP : CellState.IMPACT;
            } else {
                return CellState.FAIL;
            }
        }
    }

    /**
     * Obtiene el estado visual de la celda para la UI
     *
     * @return String representando el estado de la celda
     */
    public String getDisplayState(boolean showShips) {
        CellState state = getState();

        switch (state) {
            case WATER:
                return "🌊";
            case SHIP:
                return showShips ? "🚢" : "🌊";
            case IMPACT:
                return "💥";
            case FAIL:
                return "💧";
            case MARKED_WATER:
                return "🌀";
            case SUNK_SHIP:
                return "💀";
            default:
                return "🌊";
        }
    }

    /**
     * Verifica si la celda ha sido revelada (disparada)
     *
     * @return true si la celda ha sido disparada
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Marca la celda como revelada (cuando se dispara en ella)
     */
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
        if (revealed) {
            this.hasBeenShot = true;
        }
    }

    /**
     * Verifica si la celda tiene un barco
     *
     * @return true si hay un barco en esta celda
     */
    public boolean hasShip() {
        return ship != null;
    }

    /**
     * Verifica si la celda ha sido disparada
     *
     * @return true si se ha disparado en esta celda
     */
    public boolean hasBeenShot() {
        return hasBeenShot;
    }

    /**
     * Verifica si la celda está disponible para disparar
     *
     * @return true si no se ha disparado aún en esta celda
     */
    public boolean isShotAvailable() {
        return !hasBeenShot;
    }

    // ========== MÉTODOS DE BARCO ==========
    /**
     * Coloca un barco en esta celda
     *
     * @param ship El barco a colocar
     */
    public void setShip(Ship ship) {
        this.ship = ship;
    }

    /**
     * Obtiene el barco en esta celda
     *
     * @return El barco o null si no hay barco
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Remueve el barco de esta celda
     */
    public void removeShip() {
        this.ship = null;
    }

    // ========== MÉTODOS DE ACCIÓN ==========
    /**
     * Realiza un disparo en esta celda
     *
     * @return El resultado del disparo
     */
    public ShotResult shoot() {
        if (hasBeenShot) {
            return ShotResult.ALREADY_SHOT;
        }

        this.revealed = true;
        this.hasBeenShot = true;

        if (hasShip()) {
            ship.recordImpact();
            boolean sunk = ship.isSunk();
            return ShotResult.fromImpact(true, sunk);
        }

        return ShotResult.MISS;
    }


    /**
     * Obtiene el color de la celda para la UI
     *
     * @return Color en formato CSS
     */
    public String getColor(boolean showShips) {
        if (!revealed) {
            return showShips && hasShip() ? "#FFA500" : "#1E90FF"; // Naranja si muestra barco, azul si no
        }

        if (hasShip()) {
            return ship.isSunk() ? "#8B0000" : "#FF0000"; // Rojo oscuro si hundido, rojo si impactado
        } else {
            return "#87CEEB"; // Azul claro para agua
        }
    }

    /**
     * Reinicia la celda a su estado inicial
     */
    public void reset() {
        this.ship = null;
        this.revealed = false;
        this.hasBeenShot = false;
    }

    /**
     * Obtiene información de debug de la celda
     */
    public String getDebugInfo() {
        return String.format("Cell{ship=%s, revealed=%s, shot=%s}",
                hasShip() ? ship.getType().getName() : "null",
                revealed,
                hasBeenShot
        );
    }

    @Override
    public String toString() {
        return getDisplayState(false);
    }
}
