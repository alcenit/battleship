/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.CellState;

/**
 *
 * @author Usuario
 */
public class Cell {

    private CellState state;
    private Ship ship; // null si no hay barco
    
    public Cell() {
        this.state = CellState.WATER;
        this.ship = null;
    }
    
    // Getters
    public CellState getState() { return state; }
    public Ship getShip() { return ship; }
    public boolean haveShip() { return ship != null; }
    
    // Setters controlados
    public void setShip(Ship ship) {
        if (this.ship != null) {
            throw new IllegalStateException("La casilla ya tiene un barco");
        }
        this.ship = ship;
        this.state = CellState.SHIP;
    }
    
    public ShotResult wasHit() {
        if (state == CellState.IMPACT || state == CellState.FAIL) {
            throw new IllegalStateException("Esta casilla ya fue disparada");
        }
        
        if (haveShip()) {
            state = CellState.IMPACT;
            ship.recordImpact();
            return new ShotResult(true, ship.isSunk(), "¡Impact!");
        } else {
            state = CellState.FAIL;
            return new ShotResult(false, false, "water");
        }
    }
    
    public void reset() {
        this.state = CellState.WATER;
        this.ship = null;
    }
    
}
