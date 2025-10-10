/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

/**
 *
 * @author Usuario
 */

public record Coordinate(int x, int y) {
    public Coordinate {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            throw new IllegalArgumentException("Coordenada fuera del tablero: (" + x + ", " + y + ")");
        }
    }
    
    // Método de utilidad para convertir de notación de batalla naval (A1, B5, etc.)
    public static Coordinate desdeNotacion(String notacion) {
        if (notacion == null || notacion.length() < 2) {
            throw new IllegalArgumentException("Notación inválida: " + notacion);
        }
        
        char letra = Character.toUpperCase(notacion.charAt(0));
        int x = letra - 'A';
        int y = Integer.parseInt(notacion.substring(1)) - 1;
        
        return new Coordinate(x, y);
    }
    
    public String aNotacion() {
        char letra = (char) ('A' + x);
        return letra + String.valueOf(y + 1);
    }
}    

