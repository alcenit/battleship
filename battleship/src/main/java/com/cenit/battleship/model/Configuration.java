/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import com.cenit.battleship.controller.CPUController;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author Usuario
 */
public class Configuration {

    private static Configuration instancia;
    
    // Propiedades de configuración
    private CPUController.Difficulty CPUDifficulty;   
    private boolean soundEnabled;
    private boolean animationsEnabled;
    private double soundVolume;
    private double gameSpeed;
    private String playerName;
    private boolean showHelp;
    private String visualTheme;
    
    // Archivo de configuración
    private static final String ARCHIVO_CONFIG = "config.properties";
    private Properties properties;
    
    // Valores por defecto
    private static final CPUController.Difficulty DIFICULTAD_DEFAULT = CPUController.Difficulty.NORMAL;
    private static final boolean DEFAULT_SOUND = true;
    private static final boolean DEFAULT_ANIMATIONS = true;
    private static final double DEFAULT_VOLUME = 0.8;
    private static final double DEFAULT_VELOCITY = 1.0;
    private static final String DEFAULT_NAME = "PLAYER";
    private static final boolean DEFAULT_HELPS = true;
    private static final String DEFAULT_THEME = "default";
    
    private Configuration() {
        loadConfiguration();
    }
    
    public static Configuration getInstance() {
        if (instancia == null) {
            instancia = new Configuration();
        }
        return instancia;
    }
    
    // ========== MÉTODOS DE CARGA Y GUARDADO ==========
    
    private void loadConfiguration() {
        properties = new Properties();
        
        try (InputStream input = new FileInputStream(ARCHIVO_CONFIG)) {
            properties.load(input);
            loadFromProperties();
        } catch (FileNotFoundException e) {
            // Archivo no existe, usar valores por defecto
            System.out.println("Archivo de configuración no encontrado. Usando valores por defecto.");
            useDefaultValues();
        } catch (IOException e) {
            System.err.println("Error al cargar configuración: " + e.getMessage());
            useDefaultValues();
        }
    }
    
    public void saveConfiguration() {
        try (OutputStream output = new FileOutputStream(ARCHIVO_CONFIG)) {
            saveInProperties();
            properties.store(output, "Configuración del juego Battleship");
            System.out.println("Configuración guardada exitosamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }
    
    private void loadFromProperties() {
        try {
            // Dificultad
            String dificultyStr = properties.getProperty("dificultad", "NORMAL");
            this.CPUDifficulty = CPUController.Difficulty.valueOf(dificultyStr);
            
            // Sonido
            this.soundEnabled = Boolean.parseBoolean(properties.getProperty("sonido", "true"));
            this.soundVolume = Double.parseDouble(properties.getProperty("volumen", "0.8"));
            
            // Animaciones
            this.animationsEnabled = Boolean.parseBoolean(properties.getProperty("animaciones", "true"));
            this.gameSpeed = Double.parseDouble(properties.getProperty("velocidad", "1.0"));
            
            // Jugador
            this.playerName = properties.getProperty("nombre", "Jugador");
            this.showHelp = Boolean.parseBoolean(properties.getProperty("ayudas", "true"));
            
            // Tema visual
            this.visualTheme = properties.getProperty("tema", "default");
            
        } catch (Exception e) {
            System.err.println("Error al parsear configuración: " + e.getMessage());
            useDefaultValues();
        }
    }
    
    private void saveInProperties() {
        properties.setProperty("dificultad", CPUDifficulty.name());
        properties.setProperty("sonido", String.valueOf(soundEnabled));
        properties.setProperty("volumen", String.valueOf(soundVolume));
        properties.setProperty("animaciones", String.valueOf(animationsEnabled));
        properties.setProperty("velocidad", String.valueOf(gameSpeed));
        properties.setProperty("nombre", playerName);
        properties.setProperty("ayudas", String.valueOf(showHelp));
        properties.setProperty("tema", visualTheme);
    }
    
    private void useDefaultValues() {
        this.CPUDifficulty = DIFICULTAD_DEFAULT;
        this.soundEnabled = DEFAULT_SOUND;
        this.animationsEnabled = DEFAULT_ANIMATIONS;
        this.soundVolume = DEFAULT_VOLUME;
        this.gameSpeed = DEFAULT_VELOCITY;
        this.playerName = DEFAULT_NAME;
        this.showHelp = DEFAULT_HELPS;
        this.visualTheme = DEFAULT_THEME;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public CPUController.Difficulty getCpuDifficulty() {
        return CPUDifficulty;
    }
    
    public void setCpuDifficulty(CPUController.Difficulty difficultyCPU) {
        this.CPUDifficulty = difficultyCPU;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean sonidoHabilitado) {
        this.soundEnabled = sonidoHabilitado;
    }
    
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
    
    public void setAnimationsEnabled(boolean animacionesHabilitadas) {
        this.animationsEnabled = animacionesHabilitadas;
    }
    
    public double getSoundVolume() {
        return soundVolume;
    }
    
    public void setSoundVolume(double volumenSonido) {
        this.soundVolume = Math.max(0.0, Math.min(1.0, volumenSonido));
    }
    
    public double getGameSpeed() {
        return gameSpeed;
    }
    
    public void setGameSpeed(double velocidadJuego) {
        this.gameSpeed = Math.max(0.1, Math.min(2.0, velocidadJuego));
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String nombreJugador) {
        if (nombreJugador != null && !nombreJugador.trim().isEmpty()) {
            this.playerName = nombreJugador.trim();
        }
    }
    
    public boolean isShowHelp() {
        return showHelp;
    }
    
    public void setShowHelp(boolean mostrarAyudas) {
        this.showHelp = mostrarAyudas;
    }
    
    public String getVisualTheme() {
        return visualTheme;
    }
    
    public void setVisualTheme(String visualthemeSt) {
        if (visualthemeSt != null && !visualthemeSt.trim().isEmpty()) {
            this.visualTheme = visualthemeSt.trim();
        }
    }
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    public String getDifficultyDescription() {
        switch (CPUDifficulty) {
            case EASY:
                return "Fácil - Ideal para principiantes";
            case NORMAL:
                return "Normal - Equilibrado y divertido";
            case HARD:
                return "Difícil - Para jugadores experimentados";
            case EXPERT:
                return "Experto - ¡Solo para los mejores!";
            default:
                return "Normal";
        }
    }
    
    public double getBaseAnimationDuration() {
        // Duración base ajustada por velocidad
        return 1000.0 / gameSpeed; // milisegundos
    }
    
    public void ResetSettings() {
        useDefaultValues();
        saveConfiguration();
    }
    
    public void showSettings() {
        System.out.println("=== CONFIGURACIÓN ACTUAL ===");
        System.out.println("Dificultad: " + CPUDifficulty + " - " + getDifficultyDescription());
        System.out.println("Sonido: " + (soundEnabled ? "Habilitado" : "Deshabilitado"));
        System.out.println("Volumen: " + (int)(soundVolume * 100) + "%");
        System.out.println("Animaciones: " + (animationsEnabled ? "Habilitadas" : "Deshabilitadas"));
        System.out.println("Velocidad: " + (int)(gameSpeed * 100) + "%");
        System.out.println("Nombre: " + playerName);
        System.out.println("Ayudas: " + (showHelp ? "Habilitadas" : "Deshabilitadas"));
        System.out.println("Tema: " + visualTheme);
        System.out.println("=============================");
    }
    
    // ========== CONFIGURACIONES PREDEFINIDAS ==========
    
    public void configureBeginnerMode() {
        this.CPUDifficulty = CPUController.Difficulty.EASY;
        this.showHelp = true;
        this.gameSpeed = 0.8;
        saveConfiguration();
    }
    
    public void configureExpertMode() {
        this.CPUDifficulty = CPUController.Difficulty.EXPERT;
        this.showHelp = false;
        this.gameSpeed = 1.2;
        saveConfiguration();
    }
    
    public void configureCompetitiveMode() {
        this.CPUDifficulty = CPUController.Difficulty.HARD;
        this.showHelp = false;
        this.animationsEnabled = true;
        this.gameSpeed = 1.0;
        saveConfiguration();
    }
}    
    
    
    

