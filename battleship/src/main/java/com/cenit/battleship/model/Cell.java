package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.ShotResult;

/**
 * Representa una celda en el tablero de batalla naval
 */
public class Cell {

    private Coordinate coordinate;
    private Ship ship;
    private boolean hasBeenShot;
    private boolean isHit;

    public Cell() {
        this.coordinate = null;
        this.ship = null;
        this.hasBeenShot = false;
        this.isHit = false;
    }

    public Cell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.ship = null;
        this.hasBeenShot = false;
        this.isHit = false;
    }

    /**
     * Obtiene el estado actual de la celda
     *
     * @return Estado de la celda
     */
    public CellState getState() {
        if (!hasBeenShot) {
            // Celda no disparada
            return hasShip() ? CellState.SHIP : CellState.WATER;
        } else {
            // Celda disparada
            if (isHit) {
                if (hasShip() && ship.isSunk()) {
                    return CellState.SUNK_SHIP;
                } else {
                    return CellState.IMPACT;
                }
            } else {
                return CellState.MISS;
            }
        }
    }

    /**
     * Realiza un disparo en esta celda
     *
     * @return Resultado del disparo
     */
    public ShotResult shoot() {
        if (hasBeenShot) {
            return ShotResult.ALREADY_SHOT;
        }

        this.hasBeenShot = true;

        if (hasShip()) {
            this.isHit = true;
            boolean hitRegistered = ship.registerHitAtCoordinate(coordinate);
            boolean sunk = ship.isSunk();

            if (hitRegistered) {
                return ShotResult.fromImpact(true, sunk);
            } else {
                return ShotResult.ALREADY_HIT;
            }
        }

        return ShotResult.MISS;
    }

    /**
     * Obtiene el estado visual para mostrar en la UI
     *
     * @param showShips Si debe mostrar barcos no disparados
     * @return String representando el estado visual
     */
    public String getDisplayState(boolean showShips) {
        CellState state = getState();

        switch (state) {
            case WATER:
                return "🌊"; // Agua no disparada
            case SHIP:
                return showShips ? "🚢" : "🌊"; // Barco (solo visible si showShips=true)
            case IMPACT:
                return "💥"; // Impacto en barco
            case MISS:
                return "💧"; // Disparo fallido
            case SUNK_SHIP:
                return "💀"; // Barco hundido
            default:
                return "🌊";
        }
    }

    /**
     * Obtiene el color para la UI
     *
     * @param showShips Si debe mostrar barcos no disparados
     * @return Color en formato CSS
     */
    public String getColor(boolean showShips) {
        CellState state = getState();

        switch (state) {
            case WATER:
                return "#1E90FF"; // Azul
            case SHIP:
                return showShips ? "#FFA500" : "#1E90FF"; // Naranja si muestra barcos, azul si no
            case IMPACT:
                return "#FF0000"; // Rojo
            case MISS:
                return "#87CEEB"; // Azul claro
            case SUNK_SHIP:
                return "#8B0000"; // Rojo oscuro
            default:
                return "#1E90FF";
        }
    }

    /**
     * Obtiene la clase CSS para estilizar la celda
     *
     * @param showShips Si debe mostrar barcos no disparados
     * @return Nombre de la clase CSS
     */
    public String getCssClass(boolean showShips) {
        CellState state = getState();

        switch (state) {
            case WATER:
                return "casilla-agua";
            case SHIP:
                return showShips ? "casilla-barco" : "casilla-agua";
            case IMPACT:
                return "casilla-impacto";
            case MISS:
                return "casilla-fallo";
            case SUNK_SHIP:
                return "casilla-hundido";
            default:
                return "casilla-agua";
        }
    }

    // ========== MÉTODOS DE CONSULTA ==========
    public boolean hasShip() {
        return ship != null;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public boolean hasBeenShot() {
        return hasBeenShot;
    }

    public boolean isHit() {
        return isHit;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public boolean isShotAvailable() {
        return !hasBeenShot;
    }

    /**
     * Verifica si esta celda es parte de un barco hundido
     */
    public boolean isPartOfSunkShip() {
        return hasShip() && ship.isSunk();
    }

    /**
     * Verifica si esta celda es un barco intacto (no disparado)
     */
    public boolean isIntactShip() {
        return hasShip() && !hasBeenShot;
    }

    // ========== MÉTODOS DE RESET ==========
    /**
     * Reinicia la celda a su estado inicial
     */
    public void reset() {
        this.ship = null;
        this.hasBeenShot = false;
        this.isHit = false;
    }

    /**
     * Reinicia solo el estado de disparo (mantiene el barco si existe)
     */
    public void resetShotState() {
        this.hasBeenShot = false;
        this.isHit = false;
    }

    // ========== MÉTODOS DE DEBUG ==========
    public String getDebugInfo() {
        return String.format("Cell{coord=%s, ship=%s, shot=%s, hit=%s, state=%s}",
                coordinate != null ? coordinate.aNotacion() : "null",
                hasShip() ? ship.getType().getName() : "null",
                hasBeenShot,
                isHit,
                getState()
        );
    }

    @Override
    public String toString() {
        return getDisplayState(false); // Por defecto no muestra barcos
    }
}
