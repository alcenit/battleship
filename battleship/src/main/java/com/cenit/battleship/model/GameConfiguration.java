package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.Difficulty;
import com.cenit.battleship.model.enums.GameMode;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Clase singleton para gestionar la configuración del juego
 * @author Usuario
 */
public class GameConfiguration {

    private static GameConfiguration instancia;
    
    // Propiedades de configuración
    private Difficulty cpuDifficulty;   
    private boolean soundEnabled;
    private boolean animationsEnabled;
    private double soundVolume;
    private double gameSpeed;
    private String playerName;
    private GameMode gameMode;
    private boolean showHelp;
    private String visualTheme;
    private String language;
    private int boardSize;
    private int cellSize;
    
    // Archivo de configuración
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties;
    
    // Valores por defecto
    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    private static final boolean DEFAULT_SOUND = true;
    private static final boolean DEFAULT_ANIMATIONS = true;
    private static final double DEFAULT_VOLUME = 0.8;
    private static final double DEFAULT_SPEED = 1.0;
    private static final String DEFAULT_NAME = "Almirante";
    private static final GameMode DEFAULT_GAME_MODE = GameMode.CLASSIC; // Corregido
    private static final boolean DEFAULT_HELP = true;
    private static final String DEFAULT_THEME = "default";
    private static final String DEFAULT_LANGUAGE = "es";
    
    // Valores por defecto para el tablero
    public static final int DEFAULT_BOARD_SIZE = 15;
    public static final int DEFAULT_CELL_SIZE = 40;
    
    private GameConfiguration() {
        loadConfiguration();
    }
    
    public static GameConfiguration getInstance() {
        if (instancia == null) {
            instancia = new GameConfiguration();
        }
        return instancia;
    }
    
    // ========== MÉTODOS DE CARGA Y GUARDADO ==========
    
    private void loadConfiguration() {
        properties = new Properties();
        
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            loadFromProperties();
            System.out.println("✅ Configuración cargada desde: " + CONFIG_FILE);
        } catch (FileNotFoundException e) {
            // Archivo no existe, usar valores por defecto
            System.out.println("📁 Archivo de configuración no encontrado. Usando valores por defecto.");
            useDefaultValues();
            saveConfiguration(); // Crear archivo con valores por defecto
        } catch (IOException e) {
            System.err.println("❌ Error al cargar configuración: " + e.getMessage());
            useDefaultValues();
        }
    }
    
    public void saveConfiguration() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            saveToProperties();
            properties.store(output, "Configuración del juego Battleship");
            System.out.println("💾 Configuración guardada en: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("❌ Error al guardar configuración: " + e.getMessage());
        }
    }
    
    private void loadFromProperties() {
        try {
            // Dificultad de la CPU
            String difficultyStr = properties.getProperty("cpuDifficulty", "NORMAL");
            this.cpuDifficulty = Difficulty.valueOf(difficultyStr);
            
            // Sonido
            this.soundEnabled = Boolean.parseBoolean(properties.getProperty("soundEnabled", "true"));
            this.soundVolume = Double.parseDouble(properties.getProperty("soundVolume", "0.8"));
            
            // Animaciones
            this.animationsEnabled = Boolean.parseBoolean(properties.getProperty("animationsEnabled", "true"));
            this.gameSpeed = Double.parseDouble(properties.getProperty("gameSpeed", "1.0"));
            
            // Jugador
            this.playerName = properties.getProperty("playerName", DEFAULT_NAME);
            
            // Modo de juego - Corregido
            String gameModeStr = properties.getProperty("gameMode", "CLASSIC");
            this.gameMode = GameMode.getByName(gameModeStr);
            
            this.showHelp = Boolean.parseBoolean(properties.getProperty("showHelp", "true"));
            
            // Interfaz
            this.visualTheme = properties.getProperty("visualTheme", "default");
            this.language = properties.getProperty("language", "es");
            
            // Tablero
            this.boardSize = Integer.parseInt(properties.getProperty("boardSize", String.valueOf(DEFAULT_BOARD_SIZE)));
            this.cellSize = Integer.parseInt(properties.getProperty("cellSize", String.valueOf(DEFAULT_CELL_SIZE)));
            
        } catch (Exception e) {
            System.err.println("❌ Error al parsear configuración: " + e.getMessage());
            useDefaultValues();
        }
    }
    
    private void saveToProperties() {
        // Dificultad y modo de juego
        properties.setProperty("cpuDifficulty", cpuDifficulty.name());
        properties.setProperty("gameMode", gameMode.getName()); // Usar getName() en lugar de toString()
        
        // Sonido
        properties.setProperty("soundEnabled", String.valueOf(soundEnabled));
        properties.setProperty("soundVolume", String.valueOf(soundVolume));
        
        // Animaciones
        properties.setProperty("animationsEnabled", String.valueOf(animationsEnabled));
        properties.setProperty("gameSpeed", String.valueOf(gameSpeed));
        
        // Jugador
        properties.setProperty("playerName", playerName);
        properties.setProperty("showHelp", String.valueOf(showHelp));
        
        // Interfaz
        properties.setProperty("visualTheme", visualTheme);
        properties.setProperty("language", language);
        
        // Tablero
        properties.setProperty("boardSize", String.valueOf(this.boardSize));
        properties.setProperty("cellSize", String.valueOf(this.cellSize));
    }
    
    private void useDefaultValues() {
        this.cpuDifficulty = DEFAULT_DIFFICULTY;
        this.soundEnabled = DEFAULT_SOUND;
        this.animationsEnabled = DEFAULT_ANIMATIONS;
        this.soundVolume = DEFAULT_VOLUME;
        this.gameSpeed = DEFAULT_SPEED;
        this.playerName = DEFAULT_NAME;
        this.gameMode = DEFAULT_GAME_MODE;
        this.showHelp = DEFAULT_HELP;
        this.visualTheme = DEFAULT_THEME;
        this.language = DEFAULT_LANGUAGE;
        this.boardSize = DEFAULT_BOARD_SIZE;
        this.cellSize = DEFAULT_CELL_SIZE;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public int getBoardSize() {
        return boardSize;
    }

    /**
     * Establece un nuevo tamaño para el tablero.
     * Debe usarse con precaución, ya que afecta a toda la lógica del juego.
     * @param size Tamaño del tablero
     */
    public void setBoardSize(int size) {
        if (size > 0 && size <= 30) { // Poner un límite razonable
            this.boardSize = size;
            System.out.println("📐 Tamaño del tablero establecido a: " + size + "x" + size);
        } else {
            System.err.println("⚠️ Tamaño de tablero inválido: " + size + ". Debe estar entre 1 y 30.");
        }
    }

    public int getCellSize() {
        return cellSize;
    }

    /**
     * Establece un nuevo tamaño para las casillas del tablero.
     * @param size El nuevo tamaño en píxeles.
     */
    public void setCellSize(int size) {
        if (size > 10 && size <= 100) { // Poner un límite razonable
            this.cellSize = size;
            System.out.println("📏 Tamaño de casilla establecido a: " + size + "px");
        } else {
            System.err.println("⚠️ Tamaño de casilla inválido: " + size + ". Debe estar entre 10 y 100.");
        }
    }
    
    public Difficulty getCpuDifficulty() {
        return cpuDifficulty;
    }
    
    public void setCpuDifficulty(Difficulty difficulty) {
        this.cpuDifficulty = difficulty;
        System.out.println("🎯 Dificultad establecida: " + difficulty);
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        System.out.println("🔊 Sonido " + (enabled ? "habilitado" : "deshabilitado"));
    }
    
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
    
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
        System.out.println("🎬 Animaciones " + (enabled ? "habilitadas" : "deshabilitadas"));
    }
    
    public double getSoundVolume() {
        return soundVolume;
    }
    
    public void setSoundVolume(double volume) {
        this.soundVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("🔊 Volumen establecido: " + (int)(soundVolume * 100) + "%");
    }
    
    public double getGameSpeed() {
        return gameSpeed;
    }
    
    public void setGameSpeed(double speed) {
        this.gameSpeed = Math.max(0.1, Math.min(2.0, speed));
        System.out.println("⚡ Velocidad del juego: " + (int)(gameSpeed * 100) + "%");
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
            System.out.println("👤 Nombre del jugador: " + playerName);
        }
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(GameMode gameMode) {
        if (gameMode != null) {
            this.gameMode = gameMode;
            System.out.println("🎮 Modo de juego: " + gameMode.getName());
        }
    }
    
    public boolean isShowHelp() {
        return showHelp;
    }
    
    public void setShowHelp(boolean show) {
        this.showHelp = show;
        System.out.println("💡 Ayudas " + (show ? "habilitadas" : "deshabilitadas"));
    }
    
    public String getVisualTheme() {
        return visualTheme;
    }
    
    public void setVisualTheme(String theme) {
        if (theme != null && !theme.trim().isEmpty()) {
            this.visualTheme = theme.trim();
            System.out.println("🎨 Tema visual: " + visualTheme);
        }
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String lang) {
        if (lang != null && !lang.trim().isEmpty()) {
            this.language = lang.trim();
            System.out.println("🌐 Idioma: " + language);
        }
    }
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    public String getDifficultyDescription() {
        switch (cpuDifficulty) {
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
    
    public String getDifficultyDisplayName() {
        switch (cpuDifficulty) {
            case EASY: return "Fácil";
            case NORMAL: return "Normal";
            case HARD: return "Difícil";
            case EXPERT: return "Experto";
            default: return "Normal";
        }
    }
    
    public double getBaseAnimationDuration() {
        // Duración base ajustada por velocidad (milisegundos)
        return 1000.0 / gameSpeed;
    }
    
    public void resetToDefaults() {
        System.out.println("🔄 Restableciendo configuración a valores por defecto...");
        useDefaultValues();
        saveConfiguration();
    }
    
    public void showCurrentSettings() {
        System.out.println("=== CONFIGURACIÓN ACTUAL ===");
        System.out.println("🎯 Dificultad: " + cpuDifficulty + " - " + getDifficultyDescription());
        System.out.println("🔊 Sonido: " + (soundEnabled ? "HABILITADO" : "DESHABILITADO"));
        System.out.println("📊 Volumen: " + (int)(soundVolume * 100) + "%");
        System.out.println("🎬 Animaciones: " + (animationsEnabled ? "HABILITADAS" : "DESHABILITADAS"));
        System.out.println("⚡ Velocidad: " + (int)(gameSpeed * 100) + "%");
        System.out.println("👤 Jugador: " + playerName);
        System.out.println("🎮 Modo: " + (gameMode != null ? gameMode.getName() : "No establecido"));
        System.out.println("💡 Ayudas: " + (showHelp ? "HABILITADAS" : "DESHABILITADAS"));
        System.out.println("🎨 Tema: " + visualTheme);
        System.out.println("🌐 Idioma: " + language);
        System.out.println("📐 Tamaño tablero: " + boardSize + "x" + boardSize);
        System.out.println("📏 Tamaño casilla: " + cellSize + "px");
        System.out.println("=============================");
    }
    
    // ========== CONFIGURACIONES PREDEFINIDAS ==========
    
    public void configureBeginnerMode() {
        System.out.println("👶 Configurando modo Principiante...");
        this.cpuDifficulty = Difficulty.EASY;
        this.showHelp = true;
        this.gameSpeed = 0.8;
        this.soundVolume = 0.7;
        this.gameMode = GameMode.CLASSIC;
        saveConfiguration();
        System.out.println("✅ Modo Principiante configurado");
    }
    
    public void configureNormalMode() {
        System.out.println("🎯 Configurando modo Normal...");
        this.cpuDifficulty = Difficulty.NORMAL;
        this.showHelp = true;
        this.gameSpeed = 1.0;
        this.soundVolume = 0.8;
        this.gameMode = GameMode.CLASSIC;
        saveConfiguration();
        System.out.println("✅ Modo Normal configurado");
    }
    
    public void configureHardMode() {
        System.out.println("🚀 Configurando modo Difícil...");
        this.cpuDifficulty = Difficulty.HARD;
        this.showHelp = false;
        this.gameSpeed = 1.2;
        this.soundVolume = 0.9;
        this.gameMode = GameMode.TACTICAL;
        saveConfiguration();
        System.out.println("✅ Modo Difícil configurado");
    }
    
    public void configureExpertMode() {
        System.out.println("💀 Configurando modo Experto...");
        this.cpuDifficulty = Difficulty.EXPERT;
        this.showHelp = false;
        this.gameSpeed = 1.5;
        this.soundVolume = 1.0;
        this.gameMode = GameMode.TACTICAL;
        saveConfiguration();
        System.out.println("✅ Modo Experto configurado");
    }
    
    public void configureCompetitiveMode() {
        System.out.println("🏆 Configurando modo Competitivo...");
        this.cpuDifficulty = Difficulty.HARD;
        this.showHelp = false;
        this.animationsEnabled = true;
        this.gameSpeed = 1.0;
        this.soundVolume = 0.9;
        this.gameMode = GameMode.TACTICAL;
        saveConfiguration();
        System.out.println("✅ Modo Competitivo configurado");
    }
    
    public void configureCasualMode() {
        System.out.println("😊 Configurando modo Casual...");
        this.cpuDifficulty = Difficulty.EASY;
        this.showHelp = true;
        this.animationsEnabled = true;
        this.gameSpeed = 0.9;
        this.soundVolume = 0.6;
        this.gameMode = GameMode.CLASSIC;
        saveConfiguration();
        System.out.println("✅ Modo Casual configurado");
    }
    
    // ========== MÉTODOS DE VALIDACIÓN ==========
    
    public boolean isValidPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        
        // Longitud válida (2-20 caracteres)
        if (trimmedName.length() < 2 || trimmedName.length() > 20) {
            return false;
        }
        
        // Solo letras, números y espacios
        if (!trimmedName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+$")) {
            return false;
        }
        
        // No solo espacios
        if (trimmedName.replaceAll("\\s+", "").isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    // ========== MÉTODOS PARA OBTENER CONFIGURACIONES ESPECÍFICAS ==========
    
    public Properties getSoundSettings() {
        Properties soundProps = new Properties();
        soundProps.setProperty("enabled", String.valueOf(soundEnabled));
        soundProps.setProperty("volume", String.valueOf(soundVolume));
        return soundProps;
    }
    
    public Properties getDisplaySettings() {
        Properties displayProps = new Properties();
        displayProps.setProperty("animationsEnabled", String.valueOf(animationsEnabled));
        displayProps.setProperty("gameSpeed", String.valueOf(gameSpeed));
        displayProps.setProperty("visualTheme", visualTheme);
        displayProps.setProperty("showHelp", String.valueOf(showHelp));
        return displayProps;
    }
    
    public Properties getGameSettings() {
        Properties gameProps = new Properties();
        gameProps.setProperty("cpuDifficulty", cpuDifficulty.name());
        gameProps.setProperty("gameMode", gameMode.getName());
        gameProps.setProperty("playerName", playerName);
        gameProps.setProperty("boardSize", String.valueOf(boardSize));
        gameProps.setProperty("cellSize", String.valueOf(cellSize));
        return gameProps;
    }
    
    // ========== MÉTODOS PARA IMPORTAR/EXPORTAR CONFIGURACIÓN ==========
    
    public void importConfiguration(Properties importedProps) {
        try {
            if (importedProps.containsKey("cpuDifficulty")) {
                this.cpuDifficulty = Difficulty.valueOf(importedProps.getProperty("cpuDifficulty"));
            }
            if (importedProps.containsKey("soundEnabled")) {
                this.soundEnabled = Boolean.parseBoolean(importedProps.getProperty("soundEnabled"));
            }
            if (importedProps.containsKey("soundVolume")) {
                this.soundVolume = Double.parseDouble(importedProps.getProperty("soundVolume"));
            }
            if (importedProps.containsKey("gameMode")) {
                this.gameMode = GameMode.getByName(importedProps.getProperty("gameMode"));
            }
            if (importedProps.containsKey("playerName")) {
                this.playerName = importedProps.getProperty("playerName");
            }
            if (importedProps.containsKey("boardSize")) {
                this.boardSize = Integer.parseInt(importedProps.getProperty("boardSize"));
            }
            if (importedProps.containsKey("cellSize")) {
                this.cellSize = Integer.parseInt(importedProps.getProperty("cellSize"));
            }
            
            saveConfiguration();
            System.out.println("✅ Configuración importada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al importar configuración: " + e.getMessage());
        }
    }
    
    public Properties exportConfiguration() {
        Properties exportProps = new Properties();
        saveToProperties(); // Usar el mismo método de guardado
        exportProps.putAll(properties);
        return exportProps;
    }
    
    // ========== MÉTODOS PARA COMPATIBILIDAD CON CÓDIGO EXISTENTE ==========
    
    /**
     * Método para compatibilidad con código existente que usa String para gameMode
     * @deprecated Usar setGameMode(GameMode) en su lugar
     */
    @Deprecated
    public void setGameMode(String gameModeName) {
        this.gameMode = GameMode.getByName(gameModeName);
    }
    
    /**
     * Método para compatibilidad con código existente
     * @deprecated Usar getGameMode() en su lugar
     */
    @Deprecated
    public String getGameModeName() {
        return gameMode != null ? gameMode.getName() : "Clásico";
    }
}