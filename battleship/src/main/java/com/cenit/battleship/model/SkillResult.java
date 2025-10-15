/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class SkillResult {
    
    private final boolean successful;
    private final String message;
    private final List<Coordinate> affectedCoordinates;
    private final List<Cell> revealedCells;

    public SkillResult(boolean successful, String message) {
        this(successful, message, new ArrayList<>(), new ArrayList<>());
    }

    public SkillResult(boolean exitosa, String message, 
                             List<Coordinate> affectedCoordinates, 
                            List<Cell> revealedCells) {
        this.successful = exitosa;
        this.message = message;
        this.affectedCoordinates = affectedCoordinates;
        this.revealedCells = revealedCells;
    }

    // Getters
    public boolean isSuccessful() { return successful; }
    public String getMensaje() { return message; }
    public List<Coordinate> getAffectedCoordinates() { return new ArrayList<>(affectedCoordinates); }
    public List<Cell> getRevealedCells() { return new ArrayList<>(revealedCells); }
}    

