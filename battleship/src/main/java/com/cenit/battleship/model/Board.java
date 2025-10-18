/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.Direction;

/**
 *
 * @author Usuario
 */
public class Board {
 
    private static final int SIZE = 10;
    private final Cell[][] cells;
    
    public Board() {
        this.cells = new Cell[SIZE][SIZE];
        inicializeBoard();
    }
    
    private void inicializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = new Cell();
            }
        }
    }
    
    // Getters
    public Cell getCell(int x, int y) {
        validateCoordinates(x, y);
        return cells[x][y];
    }
    
    public Cell getCell(Coordinate coord) {
        return getCell(coord.x(), coord.y());
    }
    
    public int getSize() { return SIZE; }
    
    // Comportamiento específico
    public boolean isCoordinateValid(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
    
    public ShotResult receiveShot(Coordinate coord) {
        Cell cell = getCell(coord);
        return cell.wasHit();
    }
    
    public boolean canPlaceShip(Coordinate inicio, int tamaño, Direction direction) {
        int x = inicio.x();
        int y = inicio.y();
        
        for (int i = 0; i < tamaño; i++) {
            int coordX = direction == Direction.HORIZONTAL ? x + i : x;
            int coordY = direction == Direction.VERTICAL ? y + i : y;
            
            if (!isCoordinateValid(coordX, coordY) || 
                getCell(coordX, coordY).haveShip()) {
                return false;
            }
        }
        return true;
    }
    
    public void placeShip(Ship ship, Coordinate inicio, Direction direction) {
        if (!canPlaceShip(inicio, ship.getType().getSize(), direction)) {
            throw new IllegalArgumentException("No se puede colocar el barco en esa posición");
        }
        
        int x = inicio.x();
        int y = inicio.y();
        int tamaño = ship.getType().getSize();
        
        for (int i = 0; i < tamaño; i++) {
            int coordX = direction == Direction.HORIZONTAL ? x + i : x;
            int coordY = direction == Direction.VERTICAL ? y + i : y;
            
            ship.addPosition(getCell(coordX, coordY));
        }
    }
    
    public void reset() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j].reset();
            }
        }
    }
    
    private void validateCoordinates(int x, int y) {
        if (!isCoordinateValid(x, y)) {
            throw new IllegalArgumentException("Coordenadas inválidas: (" + x + ", " + y + ")");
        }
    }   
}
