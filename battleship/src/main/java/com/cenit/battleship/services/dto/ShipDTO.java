/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.services.dto;

import java.util.List;

/**
 *
 * @author Usuario
 */
public class ShipDTO {

    private String type;
    private int impactsRecieved;
    private List<CoordinateDTO> positions;
    private boolean sunk;

    public ShipDTO() {
    }

    // Getters y Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getImpactsRecieved() {
        return impactsRecieved;
    }

    public void setImpactsRecieved(int impactsRecieved) {
        this.impactsRecieved = impactsRecieved;
    }

    public List<CoordinateDTO> getPositions() {
        return positions;
    }

    public void setPositions(List<CoordinateDTO> positions) {
        this.positions = positions;
    }

    public boolean isSunk() {
        return sunk;
    }

    public void setSunk(boolean sunk) {
        this.sunk = sunk;
    }

}
