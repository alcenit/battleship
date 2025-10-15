/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.ShipType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 Barco de vela: sailboat
Crucero: cruise ship
Portaaviones: aircraft carrier
Acorazado: battleship
Carguero: freighter / cargo ship
Barco pesquero: fishing boat/`posible daño colateral descontar puntuacion
Remolcador: tugboat
Submarino: submarine
Yate: yacht
Ferry: ferry
Barco de guerra: warship
Bote salvavidas: lifeboat
Velero: sailboat
Buque mercante: merchant ship
Barco de vapor: steamboat
Barco pirata: pirate ship
Barco vikingo: Viking ship
Buque portacontenedores: container ship
Rompehielos: icebreaker
 */
public class Ship {
    
    private final ShipType type;
    private final List<Cell> positions;
    private int impactsReceived;
    
    public Ship(ShipType type) {
        this.type = type;
        this.positions = new ArrayList<>();
        this.impactsReceived = 0;
    }
    
    // Getters
    public ShipType getType() { return type; }
    public int getImpactsRecieved() { return impactsReceived; }
    
    // Comportamiento en lugar de getters que exponen estado interno
    public boolean isSunk() {
        return impactsReceived >= type.getSize();
    }
    
    public boolean isPlace() {
        return !positions.isEmpty();
    }
    
    public void addPosition(Cell cell) {
        if (positions.size() >= type.getSize()) {
            throw new IllegalStateException("El barco ya tiene todas sus posiciones");
        }
        positions.add(cell);
        cell.setShip(this);
    }
    
    public void recordImpact() {
        if (!isSunk()) {
            impactsReceived++;
        }
    }
    
    // Getter seguro que no permite modificar la lista interna
    public List<Cell> getPositions() {
        return Collections.unmodifiableList(positions);
    }
    
    public void reset() {
        for (Cell cell : positions) {
            cell.reset();
        }
        positions.clear();
        impactsReceived = 0;
    }
    
}
