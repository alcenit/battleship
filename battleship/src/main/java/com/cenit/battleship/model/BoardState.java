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
public class BoardState {

    private int size;
    private CellState[][] cellStates;

    // Getters y Setters
    public int getSize() { return size; }
    public void setSize(int tamaño) { this.size = tamaño; }
    
    public CellState[][] getCellStates() { return cellStates; }
    public void setCellStates(CellState[][] cellStates) { this.cellStates = cellStates; }
}    

