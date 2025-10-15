/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices.dto;

/**
 *
 * @author Usuario
 */
public class CoordinateDTO {
    
    private int x;
    private int y;

    public CoordinateDTO() {}
    
    public CoordinateDTO(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters y Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    
}
