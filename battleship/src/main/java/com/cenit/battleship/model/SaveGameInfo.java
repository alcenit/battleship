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
    private String nombreArchivo;
    private Date fecha;
    private int turnosTranscurridos;
    private int barcosHundidosJugador;
    private int barcosHundidosCPU;
    private long tamañoArchivo;

    public SaveGameInfo(String nombreArchivo, Date fecha, int turnosTranscurridos, 
                       int barcosHundidosJugador, int barcosHundidosCPU, long tamañoArchivo) {
        this.nombreArchivo = nombreArchivo;
        this.fecha = fecha;
        this.turnosTranscurridos = turnosTranscurridos;
        this.barcosHundidosJugador = barcosHundidosJugador;
        this.barcosHundidosCPU = barcosHundidosCPU;
        this.tamañoArchivo = tamañoArchivo;
    }

    // Getters
    public String getNombreArchivo() { return nombreArchivo; }
    public Date getFecha() { return fecha; }
    public int getTurnosTranscurridos() { return turnosTranscurridos; }
    public int getBarcosHundidosJugador() { return barcosHundidosJugador; }
    public int getBarcosHundidosCPU() { return barcosHundidosCPU; }
    public long getTamañoArchivo() { return tamañoArchivo; }
    
    public String getFormattedTamaño() {
        if (tamañoArchivo < 1024) return tamañoArchivo + " B";
        else if (tamañoArchivo < 1024 * 1024) return String.format("%.1f KB", tamañoArchivo / 1024.0);
        else return String.format("%.1f MB", tamañoArchivo / (1024.0 * 1024.0));
    }
    
    public String getFormattedFecha() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
    }
}
