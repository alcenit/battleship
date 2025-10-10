/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Game;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.ShotResult;
import com.cenit.battleship.model.enums.CellState;
import com.cenit.battleship.model.enums.ShipType;
import java.util.ArrayList;
import java.util.List;


public class GameController {

    private Game game;
    private Coordinate lastShotCPU;
    private Ship lastSunkShipCPU;
    private Ship lastSunkShipPlayer;
    private List<Ship> playerShips;
    private List<Ship> CPUShips;
     
    private CPUController cpuController;
    
    
    

    public GameController() {
        this.game = new Game();
        this.cpuController = new CPUController(CPUController.Difficulty.NORMAL, game.getBoardPlayer());
        this.playerShips = new ArrayList<>();
        this.CPUShips = new ArrayList<>();
        initializeShips();
       
    }
    
    public GameController(CPUController.Difficulty dificultyCPU) {
        this.game = new Game();
        this.cpuController = new CPUController(dificultyCPU, game.getBoardPlayer());
        this.playerShips = new ArrayList<>();
        this.CPUShips = new ArrayList<>();
        initializeShips();
    }
    
    private void initializeShips() {
        // Inicializar barcos del jugador
        playerShips.add(new Ship(ShipType.CARRIER));
        playerShips.add(new Ship(ShipType.BATTLESHIP));
        playerShips.add(new Ship(ShipType.SUBMARINE));
        playerShips.add(new Ship(ShipType.DESTROYER));
        playerShips.add(new Ship(ShipType.FRIGATE));
        playerShips.add(new Ship(ShipType.CRUISER));

        // Inicializar barcos de la CPU
        CPUShips.add(new Ship(ShipType.CARRIER));
        CPUShips.add(new Ship(ShipType.BATTLESHIP));
        CPUShips.add(new Ship(ShipType.SUBMARINE));
        CPUShips.add(new Ship(ShipType.DESTROYER));
        CPUShips.add(new Ship(ShipType.FRIGATE));
        CPUShips.add(new Ship(ShipType.CRUISER));

        // TODO: Colocar barcos en los tableros
    }
    
    public ShotResult playerShoots(Coordinate coord) {
        ShotResult result = game.playerShot(coord);
        
        if (result.sunk()) {
            lastSunkShipCPU = getSunkShip(CPUShips);
        }
        
        return result;
    }

    
    

    public ShotResult CPUShoots() {
        // Lógica simple de la CPU (mejorar después)
        Coordinate shot =cpuController.generateShot();
        lastShotCPU = shot;
        
        ShotResult result = game.getBoardPlayer().makeShot(shot);
        cpuController.processResult(shot, result);
        
       
        
        // Verificar si se hundió algún barco
        if (result.sunk()) {
            lastSunkShipPlayer = getSunkShip(playerShips);
        }
        
        return result;
    }
/* reemplazado por metodo en cpuController lo guardo por si las dudas
    private Coordinate generateCPUShot() {
        // Por ahora, disparo aleatorio simple
        Board boardCPU = game.getBoardCPU();
        int x, y;
        do {
            x = (int) (Math.random() * 10);
            y = (int) (Math.random() * 10);
        } while (boardCPU.getCell(x, y).getState() == CellState.IMPACT || 
                 boardCPU.getCell(x, y).getState() == CellState.FAIL);
        
        return new Coordinate(x, y);
    }
  */  
    

    public boolean isGameOver() {
        return allShipsSunk(playerShips) || allShipsSunk(CPUShips);
    }

    private boolean allShipsSunk(List<Ship> ships) {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    private Ship getSunkShip(List<Ship> ships) {
       for (Ship ship : ships) {
            if (ship.isSunk()) {
                return ship;
            }
        }
        return null;
    }
    
    // Getters
    public boolean isPlayerTurn() { return game.isPlayerTurn(); }
    public void setPlayerTurn(boolean turno) { 
     
        // Lógica para cambiar turno
    }
      
    public Board getPlayerBoard() { return game.getBoardPlayer(); }
    public Board getCPUBoard() { return game.getBoardCPU(); }
    public boolean isGameFinished() { return false; } // Implementar
    public Coordinate getLastShotCPU() { return lastShotCPU; }
    public Ship getLastSunkShipCPU() { return lastSunkShipCPU; }
    public Ship getLastSunkShipPlayer() { return lastSunkShipPlayer; }
    public List<Ship> getPlayerShips() { return new ArrayList<>(); } // Implementar
    public List<Ship> getCPUShips() { return new ArrayList<>(); } // Implementar
    public CPUController getCpuController() { return cpuController; }
}    
