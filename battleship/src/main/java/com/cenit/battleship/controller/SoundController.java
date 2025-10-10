/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.controller;

import com.cenit.battleship.model.Configuration;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author Usuario
 */
public class SoundController {
    private static SoundController instance;
    private Map<String, AudioClip> soundEfects;
    private MediaPlayer backgroundMusic;
    private Configuration config;
    private boolean loadedSounds;

    private SoundController() {
        this.config = Configuration.getInstance();
        this.soundEfects = new HashMap<>();
        this.loadedSounds = false;
        loadSounds();
    }

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    private void loadSounds() {
        try {
            // Efectos de sonido
            loadEffect("disparo_agua", "/sounds/water_splash.wav");
            loadEffect("disparo_impacto", "/sounds/explosion.wav");
            loadEffect("barco_hundido", "/sounds/ship_sinking.wav");
            loadEffect("colocar_barco", "/sounds/place_ship.wav");
            loadEffect("victoria", "/sounds/victory.wav");
            loadEffect("derrota", "/sounds/defeat.wav");
            loadEffect("hover", "/sounds/button_hover.wav");
            loadEffect("click", "/sounds/button_click.wav");
            loadEffect("error", "/sounds/error.wav");
            loadEffect("sonar", "/sounds/sonar.wav");
            loadEffect("radar", "/sounds/radar.wav");

            // Música de fondo
            URL musicaUrl = getClass().getResource("/sounds/background_music.mp3");
            if (musicaUrl != null) {
                Media media = new Media(musicaUrl.toString());
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(config.getSoundVolume() * 0.5); // Música más baja que efectos
            }

            loadedSounds = true;
            System.out.println("Sonidos cargados exitosamente");

        } catch (Exception e) {
            System.err.println("Error al cargar sonidos: " + e.getMessage());
            loadedSounds = false;
        }
    }

    private void loadEffect(String nombre, String ruta) {
        try {
            URL soundUrl = getClass().getResource(ruta);
            if (soundUrl != null) {
                AudioClip clip = new AudioClip(soundUrl.toString());
                soundEfects.put(nombre, clip);
            } else {
                System.err.println("No se pudo cargar el sonido: " + ruta);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar efecto " + nombre + ": " + e.getMessage());
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public void playEffect(String nombre) {
        if (!config.isSoundEnabled() || !loadedSounds) return;

        AudioClip clip = soundEfects.get(nombre);
        if (clip != null) {
            clip.play(config.getSoundVolume());
        }
    }

    public void playEfecto(String nombre, double volumenPersonalizado) {
        if (!config.isSoundEnabled() || !loadedSounds) return;

        AudioClip clip = soundEfects.get(nombre);
        if (clip != null) {
            double volumen = Math.min(config.getSoundVolume(), volumenPersonalizado);
            clip.play(volumen);
        }
    }

    public void startMusicBackground() {
        if (!config.isSoundEnabled() || backgroundMusic == null) return;

        if (backgroundMusic.getStatus() != MediaPlayer.Status.PLAYING) {
            backgroundMusic.setVolume(config.getSoundVolume() * 0.5);
            backgroundMusic.play();
        }
    }

    public void pauseMusicBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    public void stopMusicBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void setMusicVolume(double volumen) {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volumen * 0.5); // Música al 50% del volumen general
        }
    }

    public void updateVolumes() {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(config.getSoundVolume() * 0.5);
        }
    }

    // ========== EFECTOS ESPECÍFICOS DEL JUEGO ==========

    public void playDisparoAgua() {
        playEffect("disparo_agua");
    }

    public void playDisparoImpacto() {
        playEffect("disparo_impacto");
    }

    public void playBarcoHundido() {
        playEfecto("barco_hundido", config.getSoundVolume() * 1.2); // Más fuerte
    }

    public void playColocarBarco() {
        playEffect("colocar_barco");
    }

    public void playVictoria() {
        playEffect("victoria");
        stopMusicBackground();
    }

    public void playDerrota() {
        playEffect("derrota");
        stopMusicBackground();
    }

    public void playHoverBoton() {
        playEfecto("hover", config.getSoundVolume() * 0.3); // Más suave
    }

    public void playClickBoton() {
        playEffect("click");
    }

    public void playError() {
        playEffect("error");
    }

    public void playSonar() {
        playEffect("sonar");
    }

    public void playRadar() {
        playEffect("radar");
    }

    // ========== CONTROL DE ESTADO ==========

    public void reiniciar() {
        stopMusicBackground();
        if (config.isSoundEnabled()) {
            startMusicBackground();
        }
    }

    public void dispose() {
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        soundEfects.clear();
    }
}
