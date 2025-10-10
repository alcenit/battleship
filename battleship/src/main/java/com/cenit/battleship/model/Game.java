/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.GameState;

/**
 *
 * @author Usuario
 */
public class Game {
 
    private final Board boardPlayer;
    private final Board boardCPU;
    private boolean playerTurn;
    private GameState state;
    
    public Game() {
        this.boardPlayer = new Board();
        this.boardCPU = new Board();
        this.playerTurn = true;
        this.state = GameState.PLACING_SHIPS;
    }
    
    // Getters
    public Board getBoardPlayer() { return boardPlayer; }
    public Board getBoardCPU() { return boardCPU; }
    public boolean isPlayerTurn() { return playerTurn; }
    public GameState getState() { return state; }
    
    // Comportamiento del juego
    public ShotResult playerShot(Coordinate coord) {
        if (!playerTurn || state != GameState.IN_PLAY) {
            throw new IllegalStateException("No es el turno del jugador");
        }
        
        ShotResult result = boardCPU.makeShot(coord);
        verifyEndGame();
        
        if (!result.impact()) {
            playerTurn = false;
        }
        
        return result;
    }
    
    public void startGame() {
        this.state = GameState.IN_PLAY;
        this.playerTurn = true;
    }
    
    public void reset() {
        boardPlayer.reset();
        boardCPU.reset();
        this.playerTurn = true;
        this.state = GameState.PLACING_SHIPS;
    }
    
    private void verifyEndGame() {
        // Lógica para verificar si algún jugador ganó
    }
}
