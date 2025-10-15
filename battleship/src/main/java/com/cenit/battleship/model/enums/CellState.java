/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model.enums;

/**
 *
 * @author Usuario
 */
public enum CellState {
 
    WATER,           // Casilla vacía
    SHIP,          // Casilla con barco intacto
    IMPACT,        // Disparo acertado
    FAIL,          // Disparo fallido
    MARKED_WATER    // Agua marcada (para modo difícil)
   
}
