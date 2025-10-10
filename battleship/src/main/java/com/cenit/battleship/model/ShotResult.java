/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

/**
 *
 * @author Usuario
 */
public record ShotResult(boolean impact, boolean sunk, String message) {
    public ShotResult {
        if (message == null) {
            message = impact ? "Impact" : "Water";
        }
    }
}    

