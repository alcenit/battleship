/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices.dto;

/**
 *
 * @author Usuario
 */
public class BoardDTO {
    
    private int size;
    private String[][] cellStates; // Usar String en lugar de Enum para serialización

    public BoardDTO() {}

    // Getters y Setters
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public String[][] getCellStates() { return cellStates; }
    public void setCellStates(String[][] cellStates) { this.cellStates = cellStates; }
    
}
