/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import java.util.Date;

/**
 *
 * @author Usuario
 */
public class SaveGameInfo {
    private String filename;
    private Date date;
    private int elapsedTurns;
    private int playerSunkenShips;
    private int cpuSunkenShips;
    private long filesize;

    public SaveGameInfo(String filename, Date date, int elapsedTurns, 
                       int playerSunkenShips, int cpuSunkenShips, long filesize) {
        this.filename = filename;
        this.date = date;
        this.elapsedTurns = elapsedTurns;
        this.playerSunkenShips = playerSunkenShips;
        this.cpuSunkenShips = cpuSunkenShips;
        this.filesize = filesize;
    }

    // Getters
    public String getFilename() { return filename; }
    public Date getDate() { return date; }
    public int getElapsedTurns() { return elapsedTurns; }
    public int getPlayerSunkenShips() { return playerSunkenShips; }
    public int getCpuSunkenShips() { return cpuSunkenShips; }
    public long getFilesize() { return filesize; }
    
    public String getFormattedSize() {
        if (filesize < 1024) return filesize + " B";
        else if (filesize < 1024 * 1024) return String.format("%.1f KB", filesize / 1024.0);
        else return String.format("%.1f MB", filesize / (1024.0 * 1024.0));
    }
    
    public String getFormattedDate() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }
}
