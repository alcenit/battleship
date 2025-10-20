/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cenit.battleship.model;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.enums.Achievement;
import com.cenit.battleship.model.enums.Achievement.AchievementProgress;
import com.cenit.battleship.model.enums.ShipType;
import java.util.*;

public class AchievementTracker {
    private Map<Achievement, AchievementProgress> progressMap;
    private Set<Achievement> unlockedAchievements;
    private PlayerProfile playerProfile;

    public AchievementTracker(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
        this.progressMap = new HashMap<>();
        this.unlockedAchievements = new HashSet<>();
        initializeTracker();
    }

    private void initializeTracker() {
        // Inicializar progreso para todos los logros no secretos
        for (Achievement achievement : Achievement.getVisibleAchievements()) {
            progressMap.put(achievement, new AchievementProgress(achievement, 0, getTargetForAchievement(achievement), false));
        }
    }

    // ========== MÉTODOS DE SEGUIMIENTO ==========

    public void recordGameResult(GameController gameController, boolean won) {
        // Registrar estadísticas del juego para verificar logros
        updateCombatAchievements(gameController);
        updateSkillAchievements(gameController);
        updateProgressionAchievements();
        updateStrategyAchievements(gameController, won);
    }

    public void recordShipSunk(ShipType shipType) {
        // Logro: Hundidor de Portaaviones
        if (shipType == ShipType.CARRIER) {
            incrementProgress(Achievement.CARRIER_SINKER, 1);
        }
        
        // Logro: Cazador de Submarinos
        if (shipType == ShipType.SUBMARINE) {
            incrementProgress(Achievement.SUBMARINE_HUNTER, 1);
        }
        
        // Logro: Primera Sangre (se desbloquea automáticamente)
        if (playerProfile.getStatistics().getShipsSunk() == 1) {
            unlockAchievement(Achievement.FIRST_BLOOD);
        }
    }

    public void recordSkillUsed(Skill skill) {
        // Logros específicos de habilidades
        switch (skill) {
            case SONAR:
                incrementProgress(Achievement.SONAR_EXPERT, 1);
                break;
            case RADAR:
                incrementProgress(Achievement.RADAR_MASTER, 1);
                break;
            case DRONE:
                incrementProgress(Achievement.DRONE_OPERATOR, 1);
                break;
        }
        
        // Verificar si se usaron todas las habilidades
        checkAllSkillsUsed();
    }

    // ========== MÉTODOS DE VERIFICACIÓN ==========

    private void updateCombatAchievements(GameController gameController) {
        // Verificar logros de combate
        int shipsSunk = gameController.getSunkShipsCPU().size();
        
        // Combo Master: 3 barcos en 5 turnos
        if (shipsSunk >= 3) {
            // Lógica para verificar si fueron en turnos consecutivos
            // (necesitarías trackear esto en GameController)
        }
    }

    private void updateSkillAchievements(GameController gameController) {
        // Verificar precisión
        double accuracy = playerProfile.getStatistics().getAccuracy();
        
        if (accuracy >= 80.0) {
            unlockAchievement(Achievement.SHARPSHOOTER);
        }
        
        if (accuracy >= 100.0) {
            unlockAchievement(Achievement.PERFECT_ACCURACY);
        }
    }

    private void updateProgressionAchievements() {
        // Verificar logros de progresión
        int gamesPlayed = playerProfile.getStatistics().getGamesPlayed();
        int winStreak = playerProfile.getStatistics().getCurrentWinStreak();
        
        if (gamesPlayed >= 50) {
            unlockAchievement(Achievement.VETERAN);
        }
        
        if (winStreak >= 5) {
            unlockAchievement(Achievement.STREAKER);
        }
    }

    private void updateStrategyAchievements(GameController gameController, boolean won) {
        if (won) {
            // Verificar si fue un comeback
            int remainingShips = gameController.getRemainingPlayerShips();
            if (remainingShips == 1) {
                unlockAchievement(Achievement.COMEBACK_KING);
            }
            
            // Verificar si fue un juego perfecto
            if (remainingShips == gameController.getPlayerShips().size()) {
                unlockAchievement(Achievement.PERFECT_GAME);
            }
            
            // Verificar velocidad
            int turns = gameController.getElapsedTurns();
            if (turns <= 15) {
                unlockAchievement(Achievement.SPEEDRUNNER);
            }
            if (turns <= 30) {
                unlockAchievement(Achievement.UNSTOPPABLE);
            }
        }
    }

    private void checkAllSkillsUsed() {
        // Verificar si se usaron todas las habilidades en la partida actual
        // (necesitarías trackear esto en GameController)
    }

    // ========== MÉTODOS DE GESTIÓN ==========

    private void incrementProgress(Achievement achievement, int amount) {
        AchievementProgress progress = progressMap.get(achievement);
        if (progress != null && !progress.isCompleted()) {
            int newProgress = progress.getCurrentProgress() + amount;
            int target = progress.getTargetProgress();
            
            if (newProgress >= target) {
                unlockAchievement(achievement);
            } else {
                progressMap.put(achievement, 
                    new AchievementProgress(achievement, newProgress, target, false));
            }
        }
    }

    private void unlockAchievement(Achievement achievement) {
        if (!unlockedAchievements.contains(achievement)) {
            unlockedAchievements.add(achievement);
            playerProfile.unlockAchievement(achievement);
            
            // Actualizar progreso
            progressMap.put(achievement, 
                new AchievementProgress(achievement, 
                    getTargetForAchievement(achievement), 
                    getTargetForAchievement(achievement), 
                    true));
            
            System.out.println("🎉 ¡Logro desbloqueado: " + achievement.getDisplayName() + "!");
        }
    }

    private int getTargetForAchievement(Achievement achievement) {
        switch (achievement) {
            case SONAR_EXPERT: return 10;
            case RADAR_MASTER: return 20;
            case DRONE_OPERATOR: return 100;
            case CARRIER_SINKER: return 10;
            case SUBMARINE_HUNTER: return 15;
            default: return 1; // La mayoría de logros se desbloquean con 1 evento
        }
    }

    // ========== GETTERS ==========

    public Set<Achievement> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public Map<Achievement, AchievementProgress> getProgressMap() {
        return new HashMap<>(progressMap);
    }

    public AchievementProgress getProgress(Achievement achievement) {
        return progressMap.getOrDefault(achievement, 
            new AchievementProgress(achievement, 0, 1, false));
    }

    public boolean isAchievementUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public int getUnlockedCount() {
        return unlockedAchievements.size();
    }

    public double getCompletionPercentage() {
        int total = Achievement.getVisibleAchievements().size();
        return total > 0 ? (double) getUnlockedCount() / total * 100 : 0;
    }
}    

