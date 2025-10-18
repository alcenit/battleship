/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.GamePhase;

/**
 *
 * @author Usuario
 */
public class Game {
 
    private final Board boardPlayer;
    private final Board boardCPU;
    private boolean playerTurn;
    private GamePhase state;
    
    public Game() {
        this.boardPlayer = new Board();
        this.boardCPU = new Board();
        this.playerTurn = true;
        this.state = GamePhase.SHIP_PLACEMENT;
    }
    
    // Getters
    public Board getBoardPlayer() { return boardPlayer; }
    public Board getBoardCPU() { return boardCPU; }
    public boolean isPlayerTurn() { return playerTurn; }
    public GamePhase getState() { return state; }
    
    // Comportamiento del juego
    public ShotResult playerShot(Coordinate coord) {
        if (!playerTurn || state != GamePhase.IN_PLAY) {
            throw new IllegalStateException("No es el turno del jugador");
        }
        
        ShotResult result = boardCPU.receiveShot(coord);
        verifyEndGame();
        
        if (!result.impact()) {
            playerTurn = false;
        }
        
        return result;
    }
    
    public void startGame() {
        this.state = GamePhase.IN_PLAY;
        this.playerTurn = true;
    }
    
    public void reset() {
        boardPlayer.reset();
        boardCPU.reset();
        this.playerTurn = true;
        this.state = GamePhase.SHIP_PLACEMENT;
    }
    
    private void verifyEndGame() {
        // Lógica para verificar si algún jugador ganó
    }
}
