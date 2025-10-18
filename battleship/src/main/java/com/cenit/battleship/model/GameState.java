/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.enums.GamePhase;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class GameState {

    private Date savedDate;
    private int elapsedTurn;
    private boolean playerTurn;
    private GameState gameState;

    private GamePhase gamePhase;
    
    
    private BoardState playerBoard;
    private BoardState CPUBoard;

    private List<Ship> playerShips;
    private List<Ship> CPUShips;

    private SkillState playerSkills;
    private SkillState CPUSkills;

    private List<Ship> playerSunkenShips;
    private List<Ship> CPUSunkenShips;

    private Configuration configuration;

    // Getters y Setters
    public Date getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(Date savedDate) {
        this.savedDate = savedDate;
    }

    public int getElapsedTurns() {
        return elapsedTurn;
    }

    public void setElapsedTurns(int elapsedTurn) {
        this.elapsedTurn = elapsedTurn;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }
    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public BoardState getPlayerBoard() {
        return playerBoard;
    }

    public void setPlayerBoard(BoardState playerBoard) {
        this.playerBoard = playerBoard;
    }

    public BoardState getCPUBoard() {
        return CPUBoard;
    }

    public void setCPUBoard(BoardState CPUBoard) {
        this.CPUBoard = CPUBoard;
    }

    public List<Ship> getPlayerShips() {
        return playerShips;
    }

    public void setPlayerShips(List<Ship> playerShips) {
        this.playerShips = playerShips;
    }

    public List<Ship> getCPUShips() {
        return CPUShips;
    }

    public void setCPUShips(List<Ship> CPUShips) {
        this.CPUShips = CPUShips;
    }

    public SkillState getPlayerSkills() {
        return playerSkills;
    }

    public void setPlayerSkills(SkillState playerSkills) {
        this.playerSkills = playerSkills;
    }

    public SkillState getCPUSkills() {
        return CPUSkills;
    }

    public void setCPUSkills(SkillState CPUSkills) {
        this.CPUSkills = CPUSkills;
    }

    public List<Ship> getPlayerSunkenShips() {
        return playerSunkenShips;
    }

    public void setPlayerSunkenShips(List<Ship> playerSunkenShips) {
        this.playerSunkenShips = playerSunkenShips;
    }

    public List<Ship> getCPUSunkenShips() {
        return CPUSunkenShips;
    }

    public void setCPUSunkenShips(List<Ship> CPUSunkenShips) {
        this.CPUSunkenShips = CPUSunkenShips;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
