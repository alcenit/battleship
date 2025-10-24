package com.cenit.battleship.controller;

import com.cenit.battleship.model.GameConfiguration;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Controlador para gestionar todos los sonidos y música del juego
 * @author Usuario
 */
public class SoundController {
    private static SoundController instance;
    private Map<String, AudioClip> soundEffects;
    private MediaPlayer backgroundMusic;
    private GameConfiguration config;
    private boolean soundsLoaded;
    private boolean musicEnabled;
    private boolean effectsEnabled;

    // Constructor privado para singleton
    private SoundController() {
        this.config = GameConfiguration.getInstance();
        this.soundEffects = new HashMap<>();
        this.soundsLoaded = false;
        this.musicEnabled = config.isSoundEnabled();
        this.effectsEnabled = config.isSoundEnabled();
        loadSounds();
    }

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    // ========== CARGA DE SONIDOS ==========

    private void loadSounds() {
        try {
            // Cargar efectos de sonido básicos del juego
            loadEffect("water_splash", "/sounds/water_splash.mp3");
            loadEffect("explosion", "/sounds/explosion.wav");
            loadEffect("ship_sinking", "/sounds/ship_sinking.wav");
            loadEffect("place_ship", "/sounds/place_ship.wav");
            loadEffect("victory", "/sounds/victory.wav");
            loadEffect("defeat", "/sounds/defeat.wav");
            loadEffect("button_hover", "/sounds/button_hover.wav");
            loadEffect("button_click", "/sounds/button_click.wav");
            loadEffect("error", "/sounds/error.wav");
            
            // Cargar efectos de habilidades
            loadEffect("sonar", "/sounds/sonar.wav");
            loadEffect("radar", "/sounds/radar.wav");
            loadEffect("drone", "/sounds/drone.wav");
            loadEffect("missile", "/sounds/missile.wav");
            loadEffect("cluster_bomb", "/sounds/cluster_bomb.wav");
            loadEffect("jamming", "/sounds/jamming.wav");
            loadEffect("repair", "/sounds/repair.wav");
            loadEffect("camouflage", "/sounds/camouflage.wav");
            
            // Cargar música de fondo
            loadBackgroundMusic("/sounds/background_music.mp3");

            soundsLoaded = true;
            System.out.println("✅ Sonidos cargados exitosamente - " + soundEffects.size() + " efectos disponibles");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar sonidos: " + e.getMessage());
            soundsLoaded = false;
        }
    }

    private void loadEffect(String name, String path) {
        try {
            URL soundUrl = getClass().getResource(path);
            if (soundUrl != null) {
                AudioClip clip = new AudioClip(soundUrl.toString());
                soundEffects.put(name, clip);
            } else {
                System.err.println("⚠️ No se pudo encontrar el archivo de sonido: " + path);
                // Crear un clip vacío para evitar NullPointerException
                soundEffects.put(name, new AudioClip(""));
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar efecto " + name + ": " + e.getMessage());
            // Asegurar que siempre haya una entrada en el mapa
            soundEffects.put(name, new AudioClip(""));
        }
    }

    private void loadBackgroundMusic(String musicPath) {
        try {
            URL musicUrl = getClass().getResource(musicPath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(getMusicVolume());
                System.out.println("🎵 Música de fondo cargada: " + musicPath);
            } else {
                System.err.println("⚠️ No se pudo cargar la música de fondo: " + musicPath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar música de fondo: " + e.getMessage());
        }
    }

    // ========== MÉTODOS PÚBLICOS PRINCIPALES ==========

    /**
     * Reproduce un efecto de sonido
     */
    public void playEffect(String name) {
        if (!effectsEnabled || !soundsLoaded) return;

        AudioClip clip = soundEffects.get(name);
        if (clip != null && !clip.getSource().isEmpty()) {
            try {
                clip.play(getEffectsVolume());
            } catch (Exception e) {
                System.err.println("❌ Error al reproducir efecto " + name + ": " + e.getMessage());
            }
        }
    }

    /**
     * Reproduce un efecto de sonido con volumen personalizado
     */
    public void playEffect(String name, double customVolume) {
        if (!effectsEnabled || !soundsLoaded) return;

        AudioClip clip = soundEffects.get(name);
        if (clip != null && !clip.getSource().isEmpty()) {
            try {
                double volume = Math.min(getEffectsVolume(), Math.max(0.0, customVolume));
                clip.play(volume);
            } catch (Exception e) {
                System.err.println("❌ Error al reproducir efecto " + name + ": " + e.getMessage());
            }
        }
    }

    /**
     * Inicia la música de fondo
     */
    public void startBackgroundMusic() {
        if (!musicEnabled || backgroundMusic == null) return;

        try {
            if (backgroundMusic.getStatus() != MediaPlayer.Status.PLAYING) {
                backgroundMusic.setVolume(getMusicVolume());
                backgroundMusic.play();
                System.out.println("🎵 Música de fondo iniciada");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar música de fondo: " + e.getMessage());
        }
    }

    /**
     * Pausa la música de fondo
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            backgroundMusic.pause();
            System.out.println("⏸️ Música de fondo pausada");
        }
    }

    /**
     * Detiene la música de fondo
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            System.out.println("⏹️ Música de fondo detenida");
        }
    }

    // ========== EFECTOS ESPECÍFICOS DEL JUEGO ==========

    // Disparos y acciones básicas
    public void playWaterSplash() {
        playEffect("water_splash");
    }

    public void playExplosion() {
        playEffect("explosion");
    }

    public void playShipSinking() {
        playEffect("ship_sinking", getEffectsVolume() * 1.2); // Más fuerte
    }

    public void playPlaceShip() {
        playEffect("place_ship");
    }

    public void playVictory() {
        playEffect("victory");
        stopBackgroundMusic(); // Detener música al ganar
    }

    public void playDefeat() {
        playEffect("defeat");
        stopBackgroundMusic(); // Detener música al perder
    }

    // Interfaz de usuario
    public void playButtonHover() {
        playEffect("button_hover", getEffectsVolume() * 0.3); // Más suave
    }

    public void playButtonClick() {
        playEffect("button_click");
    }

    public void playError() {
        playEffect("error");
    }

    // Habilidades especiales
    public void playSonar() {
        playEffect("sonar");
    }

    public void playRadar() {
        playEffect("radar");
    }

    public void playDrone() {
        playEffect("drone");
    }

    public void playMissile() {
        playEffect("missile");
    }

    public void playClusterBomb() {
        playEffect("cluster_bomb");
    }

    public void playJamming() {
        playEffect("jamming");
    }

    public void playRepair() {
        playEffect("repair");
    }

    public void playCamouflage() {
        playEffect("camouflage");
    }

    // ========== CONTROL DE VOLUMEN Y ESTADO ==========

    /**
     * Obtiene el volumen para efectos (70% del volumen general)
     */
    private double getEffectsVolume() {
        return config.getSoundVolume() * 0.7;
    }

    /**
     * Obtiene el volumen para música (50% del volumen general)
     */
    private double getMusicVolume() {
        return config.getSoundVolume() * 0.5;
    }

    /**
     * Actualiza todos los volúmenes según la configuración
     */
    public void updateVolumes() {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(getMusicVolume());
        }
        System.out.println("🔊 Volúmenes actualizados - Efectos: " + getEffectsVolume() + ", Música: " + getMusicVolume());
    }

    /**
     * Habilita o deshabilita todos los sonidos
     */
    public void setSoundEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        this.effectsEnabled = enabled;
        
        if (enabled) {
            startBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
        
        System.out.println("🔊 Sonidos " + (enabled ? "habilitados" : "deshabilitados"));
    }

    /**
     * Habilita o deshabilita solo la música
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        
        if (enabled) {
            startBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
        
        System.out.println("🎵 Música " + (enabled ? "habilitada" : "deshabilitada"));
    }

    /**
     * Habilita o deshabilita solo los efectos
     */
    public void setEffectsEnabled(boolean enabled) {
        this.effectsEnabled = enabled;
        System.out.println("🎯 Efectos de sonido " + (enabled ? "habilitados" : "deshabilitados"));
    }

    /**
     * Verifica si los sonidos están cargados
     */
    public boolean areSoundsLoaded() {
        return soundsLoaded;
    }

    /**
     * Verifica si la música está reproduciéndose
     */
    public boolean isMusicPlaying() {
        return backgroundMusic != null && backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING;
    }

    /**
     * Reinicia el controlador de sonido
     */
    public void restart() {
        stopBackgroundMusic();
        if (musicEnabled) {
            startBackgroundMusic();
        }
        System.out.println("🔄 Controlador de sonido reiniciado");
    }

    /**
     * Cambia la música de fondo
     */
    public void changeBackgroundMusic(String newMusicPath) {
        stopBackgroundMusic();
        loadBackgroundMusic(newMusicPath);
        if (musicEnabled) {
            startBackgroundMusic();
        }
    }

    /**
     * Pre-carga un efecto de sonido específico
     */
    public void preloadEffect(String name, String path) {
        loadEffect(name, path);
    }

    /**
     * Limpia todos los recursos de sonido
     */
    public void dispose() {
        try {
            // Detener y liberar música
            if (backgroundMusic != null) {
                backgroundMusic.stop();
                backgroundMusic.dispose();
            }
            
            // Parar todos los efectos en reproducción
            for (AudioClip clip : soundEffects.values()) {
                clip.stop();
            }
            
            // Limpiar mapa
            soundEffects.clear();
            
            System.out.println("🧹 Recursos de sonido liberados");
        } catch (Exception e) {
            System.err.println("❌ Error al liberar recursos de sonido: " + e.getMessage());
        }
    }

    // ========== MÉTODOS DE INFORMACIÓN ==========

    /**
     * Obtiene información del estado del controlador de sonido
     */
    public String getSoundInfo() {
        return String.format(
            "Estado Sonido: %s | Música: %s | Efectos: %s | Cargados: %d efectos",
            (musicEnabled || effectsEnabled) ? "ACTIVO" : "INACTIVO",
            musicEnabled ? "ON" : "OFF",
            effectsEnabled ? "ON" : "OFF",
            soundEffects.size()
        );
    }

    /**
     * Muestra todos los efectos de sonido disponibles
     */
    public void printAvailableSounds() {
        System.out.println("🎵 Efectos de sonido disponibles:");
        soundEffects.keySet().forEach(System.out::println);
    }
}
