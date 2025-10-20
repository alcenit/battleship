package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.Ship;
import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.model.enums.ShotResult;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.List;

/**
 * Controlador para gestionar todas las animaciones del juego de batalla naval
 * @author Usuario
 */
public class AnimationController {
    
    private static AnimationController instance;
    private Pane animationLayer;
    private boolean animationsEnabled = true;
    
    private AnimationController() {
        // Constructor privado para singleton
    }
    
    public static AnimationController getInstance() {
        if (instance == null) {
            instance = new AnimationController();
        }
        return instance;
    }
    
    // ========== CONFIGURACIÓN ==========
    
    /**
     * Establece la capa donde se mostrarán las animaciones
     */
    public void setAnimationLayer(Pane animationLayer) {
        this.animationLayer = animationLayer;
    }
    
    /**
     * Habilita o deshabilita las animaciones
     */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
    
    public boolean areAnimationsEnabled() {
        return animationsEnabled;
    }
    
    // ========== ANIMACIONES BÁSICAS ==========
    
    /**
     * Animación de disparo al agua
     */
    public void playWaterSplashAnimation(Coordinate coord, ShotResult result) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = coord.getY() * 40 + 20; // Ajustar según tamaño de celda
        double centerY = coord.getX() * 40 + 20;
        
        // Crear círculos concéntricos para efecto de salpicadura
        Circle splash1 = createSplashCircle(centerX, centerY, 5, Color.rgb(100, 150, 255, 0.8));
        Circle splash2 = createSplashCircle(centerX, centerY, 10, Color.rgb(80, 130, 255, 0.6));
        Circle splash3 = createSplashCircle(centerX, centerY, 15, Color.rgb(60, 110, 255, 0.4));
        
        animationLayer.getChildren().addAll(splash1, splash2, splash3);
        
        // Animaciones de escala y fade
        ScaleTransition scale1 = createScaleTransition(splash1, 1.0, 3.0, 400);
        FadeTransition fade1 = createFadeTransition(splash1, 0.8, 0.0, 400);
        
        ScaleTransition scale2 = createScaleTransition(splash2, 1.0, 2.5, 500);
        FadeTransition fade2 = createFadeTransition(splash2, 0.6, 0.0, 500);
        
        ScaleTransition scale3 = createScaleTransition(splash3, 1.0, 2.0, 600);
        FadeTransition fade3 = createFadeTransition(splash3, 0.4, 0.0, 600);
        
        ParallelTransition parallel = new ParallelTransition(scale1, fade1, scale2, fade2, scale3, fade3);
        parallel.setOnFinished(e -> animationLayer.getChildren().removeAll(splash1, splash2, splash3));
        parallel.play();
    }
    
    /**
     * Animación de impacto en barco
     */
    public void playExplosionAnimation(Coordinate coord, ShotResult result) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = coord.getY() * 40 + 20;
        double centerY = coord.getX() * 40 + 20;
        
        // Crear efecto de explosión
        for (int i = 0; i < 8; i++) {
            createParticle(centerX, centerY, i * 45, result.isSunk() ? Color.RED : Color.ORANGE);
        }
        
        // Destello central
        Circle flash = createSplashCircle(centerX, centerY, 2, Color.YELLOW);
        animationLayer.getChildren().add(flash);
        
        ScaleTransition flashScale = createScaleTransition(flash, 1.0, 4.0, 200);
        FadeTransition flashFade = createFadeTransition(flash, 1.0, 0.0, 200);
        
        ParallelTransition flashAnim = new ParallelTransition(flashScale, flashFade);
        flashAnim.setOnFinished(e -> animationLayer.getChildren().remove(flash));
        flashAnim.play();
    }
    
    /**
     * Animación de barco hundiéndose
     */
    public void playSinkingAnimation(Ship ship) {
        if (!animationsEnabled || animationLayer == null) return;
        
        List<Coordinate> positions = ship.getCoordinates();
        if (positions.isEmpty()) return;
        
        // Crear burbujas en todas las posiciones del barco
        for (Coordinate coord : positions) {
            double centerX = coord.getY() * 40 + 20;
            double centerY = coord.getX() * 40 + 20;
            
            // Crear múltiples burbujas
            for (int i = 0; i < 5; i++) {
                createBubble(centerX, centerY, i * 200);
            }
        }
    }
    
    // ========== ANIMACIONES DE HABILIDADES ==========
    
    /**
     * Animación para la habilidad Sonar
     */
    public void playSonarAnimation(Coordinate center, List<Coordinate> revealedArea) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = center.getY() * 40 + 20;
        double centerY = center.getX() * 40 + 20;
        
        // Crear onda de sonar
        Circle sonarWave = createSplashCircle(centerX, centerY, 5, Color.rgb(0, 255, 255, 0.7));
        animationLayer.getChildren().add(sonarWave);
        
        ScaleTransition scale = createScaleTransition(sonarWave, 1.0, 15.0, 1500);
        FadeTransition fade = createFadeTransition(sonarWave, 0.7, 0.0, 1500);
        
        ParallelTransition sonarAnim = new ParallelTransition(scale, fade);
        sonarAnim.setOnFinished(e -> animationLayer.getChildren().remove(sonarWave));
        sonarAnim.play();
        
        // Destacar celdas reveladas
        for (Coordinate coord : revealedArea) {
            highlightCell(coord, Color.CYAN, 1500);
        }
    }
    
    /**
     * Animación para la habilidad Radar
     */
    public void playRadarAnimation(Coordinate detectedCoord, ShipType shipType) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = detectedCoord.getY() * 40 + 20;
        double centerY = detectedCoord.getX() * 40 + 20;
        
        // Crear efecto de escaneo
        Rectangle scanBeam = new Rectangle(centerX - 50, centerY - 2, 100, 4);
        scanBeam.setFill(Color.LIME);
        scanBeam.setOpacity(0.8);
        animationLayer.getChildren().add(scanBeam);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), scanBeam);
        rotate.setByAngle(360);
        rotate.setCycleCount(2);
        
        FadeTransition fade = createFadeTransition(scanBeam, 0.8, 0.0, 1000);
        
        ParallelTransition radarAnim = new ParallelTransition(rotate, fade);
        radarAnim.setOnFinished(e -> animationLayer.getChildren().remove(scanBeam));
        radarAnim.play();
        
        // Destacar celda detectada
        highlightCell(detectedCoord, Color.LIME, 2000);
    }
    
    /**
     * Animación para la habilidad Dron
     */
    public void playDroneAnimation(boolean isRow, int index) {
        if (!animationsEnabled || animationLayer == null) return;
        
        // Crear representación visual del dron
        Rectangle drone = new Rectangle(0, 0, 30, 10);
        drone.setFill(Color.GRAY);
        drone.setStroke(Color.WHITE);
        
        double startX, startY, endX, endY;
        
        if (isRow) {
            startX = -50;
            startY = index * 40 + 20;
            endX = Board.SIZE * 40 + 50;
            endY = startY;
        } else {
            startX = index * 40 + 20;
            startY = -50;
            endX = startX;
            endY = Board.SIZE * 40 + 50;
        }
        
        drone.setTranslateX(startX);
        drone.setTranslateY(startY);
        animationLayer.getChildren().add(drone);
        
        TranslateTransition move = new TranslateTransition(Duration.millis(2000), drone);
        move.setToX(endX - startX);
        move.setToY(endY - startY);
        
        FadeTransition fadeOut = createFadeTransition(drone, 1.0, 0.0, 500);
        fadeOut.setDelay(Duration.millis(1500));
        
        SequentialTransition droneAnim = new SequentialTransition(move, fadeOut);
        droneAnim.setOnFinished(e -> animationLayer.getChildren().remove(drone));
        droneAnim.play();
    }
    
    /**
     * Animación para la habilidad Misil Guiado
     */
    public void playMissileAnimation(Coordinate target, ShotResult result) {
        if (!animationsEnabled || animationLayer == null) return;
        
        // Crear misil que viene desde fuera del tablero
        Rectangle missile = new Rectangle(0, 0, 20, 8);
        missile.setFill(Color.ORANGE);
        missile.setStroke(Color.RED);
        
        double startX = -50;
        double startY = target.getY() * 40 + 20;
        double endX = target.getY() * 40 + 20;
        double endY = target.getX() * 40 + 20;
        
        missile.setTranslateX(startX);
        missile.setTranslateY(startY);
        animationLayer.getChildren().add(missile);
        
        // Animación de trayectoria
        TranslateTransition move = new TranslateTransition(Duration.millis(800), missile);
        move.setToX(endX - startX);
        move.setToY(endY - startY);
        
        // Humo trasero
        createSmokeTrail(missile, 800);
        
        move.setOnFinished(e -> {
            animationLayer.getChildren().remove(missile);
            // Explosión al llegar al objetivo
            if (result.isImpact()) {
                playExplosionAnimation(target, result);
            } else {
                playWaterSplashAnimation(target, result);
            }
        });
        
        move.play();
    }
    
    /**
     * Animación para la habilidad Bomba de Racimo
     */
    public void playClusterBombAnimation(Coordinate center, List<Coordinate> affectedArea) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = center.getY() * 40 + 20;
        double centerY = center.getX() * 40 + 20;
        
        // Crear bomba principal
        Circle bomb = createSplashCircle(centerX, centerY, 8, Color.RED);
        animationLayer.getChildren().add(bomb);
        
        // Explosión de la bomba principal
        ScaleTransition bombScale = createScaleTransition(bomb, 1.0, 2.0, 300);
        FadeTransition bombFade = createFadeTransition(bomb, 1.0, 0.0, 300);
        
        ParallelTransition bombAnim = new ParallelTransition(bombScale, bombFade);
        bombAnim.setOnFinished(e -> {
            animationLayer.getChildren().remove(bomb);
            
            // Explosiones secundarias en el área afectada
            for (Coordinate coord : affectedArea) {
                if (!coord.equals(center)) {
                    PauseTransition delay = new PauseTransition(Duration.millis(100 * Math.abs(coord.getX() - center.getX()) + 
                                                                                Math.abs(coord.getY() - center.getY())));
                    delay.setOnFinished(event -> playExplosionAnimation(coord, ShotResult.HIT));
                    delay.play();
                }
            }
        });
        
        bombAnim.play();
    }
    
    /**
     * Animación para la habilidad Interferencia
     */
    public void playJammingAnimation() {
        if (!animationsEnabled || animationLayer == null) return;
        
        // Crear efecto de interferencia en todo el tablero
        Rectangle interference = new Rectangle(0, 0, Board.SIZE * 40, Board.SIZE * 40);
        interference.setFill(Color.rgb(255, 0, 0, 0.3));
        animationLayer.getChildren().add(interference);
        
        FadeTransition flicker = createFadeTransition(interference, 0.3, 0.1, 100);
        flicker.setCycleCount(10);
        flicker.setAutoReverse(true);
        
        flicker.setOnFinished(e -> animationLayer.getChildren().remove(interference));
        flicker.play();
    }
    
    /**
     * Animación para la habilidad Reparación
     */
    public void playRepairAnimation(Coordinate position, Ship ship) {
        if (!animationsEnabled || animationLayer == null) return;
        
        double centerX = position.getY() * 40 + 20;
        double centerY = position.getX() * 40 + 20;
        
        // Crear efecto de reparación (partículas verdes)
        for (int i = 0; i < 6; i++) {
            createParticle(centerX, centerY, i * 60, Color.GREEN);
        }
        
        // Destello de reparación
        Circle repairFlash = createSplashCircle(centerX, centerY, 3, Color.LIME);
        animationLayer.getChildren().add(repairFlash);
        
        ScaleTransition scale = createScaleTransition(repairFlash, 1.0, 6.0, 800);
        FadeTransition fade = createFadeTransition(repairFlash, 0.8, 0.0, 800);
        
        ParallelTransition repairAnim = new ParallelTransition(scale, fade);
        repairAnim.setOnFinished(e -> animationLayer.getChildren().remove(repairFlash));
        repairAnim.play();
    }
    
    /**
     * Animación para la habilidad Camuflaje
     */
    public void playCamouflageAnimation(Ship ship, Coordinate newPosition) {
        if (!animationsEnabled || animationLayer == null) return;
        
        // Efecto de desvanecimiento en posición antigua
        List<Coordinate> oldPositions = ship.getCoordinates();
        for (Coordinate coord : oldPositions) {
            highlightCell(coord, Color.GRAY, 500);
        }
        
        // Efecto de aparición en nueva posición
        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(e -> {
            List<Coordinate> newPositions = ship.getCoordinates();
            for (Coordinate coord : newPositions) {
                highlightCell(coord, Color.LIGHTGREEN, 500);
            }
        });
        delay.play();
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private Circle createSplashCircle(double centerX, double centerY, double radius, Color color) {
        Circle circle = new Circle(centerX, centerY, radius, color);
        circle.setStroke(color.brighter());
        circle.setStrokeWidth(1);
        return circle;
    }
    
    private ScaleTransition createScaleTransition(Node node, double from, double to, double duration) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(duration), node);
        scale.setFromX(from);
        scale.setFromY(from);
        scale.setToX(to);
        scale.setToY(to);
        return scale;
    }
    
    private FadeTransition createFadeTransition(Node node, double from, double to, double duration) {
        FadeTransition fade = new FadeTransition(Duration.millis(duration), node);
        fade.setFromValue(from);
        fade.setToValue(to);
        return fade;
    }
    
    private void createParticle(double startX, double startY, double angle, Color color) {
        double radians = Math.toRadians(angle);
        double distance = 20;
        
        Circle particle = new Circle(startX, startY, 2, color);
        animationLayer.getChildren().add(particle);
        
        TranslateTransition move = new TranslateTransition(Duration.millis(600), particle);
        move.setByX(Math.cos(radians) * distance);
        move.setByY(Math.sin(radians) * distance);
        
        FadeTransition fade = createFadeTransition(particle, 1.0, 0.0, 600);
        
        ParallelTransition particleAnim = new ParallelTransition(move, fade);
        particleAnim.setOnFinished(e -> animationLayer.getChildren().remove(particle));
        particleAnim.play();
    }
    
    private void createBubble(double startX, double startY, double delay) {
        Circle bubble = new Circle(startX, startY, 1, Color.rgb(200, 200, 255, 0.8));
        animationLayer.getChildren().add(bubble);
        
        PauseTransition initialDelay = new PauseTransition(Duration.millis(delay));
        initialDelay.setOnFinished(e -> {
            TranslateTransition rise = new TranslateTransition(Duration.millis(1000), bubble);
            rise.setByY(-30);
            
            ScaleTransition expand = createScaleTransition(bubble, 1.0, 3.0, 1000);
            FadeTransition fade = createFadeTransition(bubble, 0.8, 0.0, 1000);
            
            ParallelTransition bubbleAnim = new ParallelTransition(rise, expand, fade);
            bubbleAnim.setOnFinished(event -> animationLayer.getChildren().remove(bubble));
            bubbleAnim.play();
        });
        
        initialDelay.play();
    }
    
    private void createSmokeTrail(Node follower, double duration) {
        for (int i = 0; i < 5; i++) {
            Circle smoke = new Circle(0, 0, 2, Color.rgb(100, 100, 100, 0.6));
            smoke.setTranslateX(follower.getTranslateX());
            smoke.setTranslateY(follower.getTranslateY());
            animationLayer.getChildren().add(smoke);
            
            PauseTransition delay = new PauseTransition(Duration.millis(i * 100));
            delay.setOnFinished(e -> {
                FadeTransition fade = createFadeTransition(smoke, 0.6, 0.0, 500);
                ScaleTransition expand = createScaleTransition(smoke, 1.0, 3.0, 500);
                ParallelTransition smokeAnim = new ParallelTransition(fade, expand);
                smokeAnim.setOnFinished(event -> animationLayer.getChildren().remove(smoke));
                smokeAnim.play();
            });
            delay.play();
        }
    }
    
    private void highlightCell(Coordinate coord, Color color, double duration) {
        double x = coord.getY() * 40;
        double y = coord.getX() * 40;
        
        Rectangle highlight = new Rectangle(x, y, 40, 40);
        highlight.setFill(color);
        highlight.setOpacity(0.3);
        animationLayer.getChildren().add(highlight);
        
        FadeTransition fade = createFadeTransition(highlight, 0.3, 0.0, duration);
        fade.setOnFinished(e -> animationLayer.getChildren().remove(highlight));
        fade.play();
    }
    
    /**
     * Limpia todas las animaciones en curso
     */
    public void clearAllAnimations() {
        if (animationLayer != null) {
            animationLayer.getChildren().clear();
        }
    }
    
    /**
     * Detiene todas las animaciones en curso
     */
    public void stopAllAnimations() {
        if (animationLayer != null) {
            // En una implementación real, guardarías referencias a las animaciones
            // y las detendrías individualmente
            animationLayer.getChildren().forEach(node -> {
                if (node.getUserData() instanceof Animation) {
                    ((Animation) node.getUserData()).stop();
                }
            });
        }
    }
}
