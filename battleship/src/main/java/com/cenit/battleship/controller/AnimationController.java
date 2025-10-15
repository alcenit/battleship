/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.controller;

/**
 *
 * @author Usuario
 */
public class AnimationController {
    
   private static AnimationController instance;  
    
    
    public static AnimationController getInstance() {
        if (instance == null) {
            instance = new AnimationController();
        }
        return instance;
    }
    
}
