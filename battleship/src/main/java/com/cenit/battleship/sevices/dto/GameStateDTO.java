/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices.dto;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class GameStateDTO {
    private String version = "1.0";
    private Date savedDate;
    private int turnsElapsed;
    private boolean playerTurn;
    private String gameState;
    
    private BoardDTO playerBoard;
    private BoardDTO CPUBoard;
    
    private List<ShipDTO> playerShips;
    private List<ShipDTO> CPUShips;
    
    private SkillDTO playerSkills;
    private SkillDTO CPUSkills;
    
    private List<String> playerSunkenShips; // Nombres de barcos hundidos
    private List<String> CPUSunkenShips;

    // Constructor vacío necesario para GSON
    public GameStateDTO() {}

    // Getters y Setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public Date getSavedDate() { return savedDate; }
    public void setFechaGuardado(Date savedDate) { this.savedDate = savedDate; }
    
    public int getTurnosTranscurridos() { return turnsElapsed; }
    public void setTurnosTranscurridos(int turnosTranscurridos) { this.turnsElapsed = turnosTranscurridos; }
    
    public boolean isTurnoJugador() { return playerTurn; }
    public void setTurnoJugador(boolean turnoJugador) { this.playerTurn = turnoJugador; }
    
    public String getGameState() { return gameState; }
    public void setGameState(String gameState) { this.gameState = gameState; }
    
    public BoardDTO getTableroJugador() { return playerBoard; }
    public void setTableroJugador(BoardDTO tableroJugador) { this.playerBoard = tableroJugador; }
    
    public BoardDTO getTableroCPU() { return CPUBoard; }
    public void setTableroCPU(BoardDTO tableroCPU) { this.CPUBoard = tableroCPU; }
    
    public List<ShipDTO> getBarcosJugador() { return playerShips; }
    public void setBarcosJugador(List<ShipDTO> barcosJugador) { this.playerShips = barcosJugador; }
    
    public List<ShipDTO> getBarcosCPU() { return CPUShips; }
    public void setBarcosCPU(List<ShipDTO> barcosCPU) { this.CPUShips = barcosCPU; }
    
    public SkillDTO getHabilidadesJugador() { return playerSkills; }
    public void setHabilidadesJugador(SkillDTO habilidadesJugador) { this.playerSkills = habilidadesJugador; }
    
    public SkillDTO getHabilidadesCPU() { return CPUSkills; }
    public void setHabilidadesCPU(SkillDTO habilidadesCPU) { this.CPUSkills = habilidadesCPU; }
    
    public List<String> getBarcosHundidosJugador() { return playerSunkenShips; }
    public void setBarcosHundidosJugador(List<String> barcosHundidosJugador) { this.playerSunkenShips = barcosHundidosJugador; }
    
    public List<String> getBarcosHundidosCPU() { return CPUSunkenShips; }
    public void setBarcosHundidosCPU(List<String> barcosHundidosCPU) { this.CPUSunkenShips = barcosHundidosCPU; }
    
}
