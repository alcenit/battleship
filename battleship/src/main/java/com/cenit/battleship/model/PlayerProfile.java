
package com.cenit.battleship.model;

import com.cenit.battleship.controller.GameController;
import com.cenit.battleship.model.enums.Achievement;
import java.util.*;
import java.io.Serializable;

public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private String avatar;
    private Date creationDate;
    private Date lastPlayed;
    private GameStatistics statistics;
    private Set<Achievement> unlockedAchievements;
    private int totalScore;
    private int currentLevel;
    private int experiencePoints;
    private Map<String, Object> preferences;
    private List<String> unlockedShips;
    private List<String> unlockedThemes;

    // ========== CONSTRUCTORES ==========

    public PlayerProfile(String playerName) {
        this(UUID.randomUUID().toString(), playerName, "🚢", new Date());
    }
    public PlayerProfile(String playerName,String avatar) {
        this(UUID.randomUUID().toString(), playerName, "🚢", new Date());
    }

    public PlayerProfile(String playerId, String playerName, String avatar, Date creationDate) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.avatar = avatar;
        this.creationDate = creationDate;
        this.lastPlayed = creationDate;
        this.statistics = new GameStatistics();
        this.unlockedAchievements = new HashSet<>();
        this.totalScore = 0;
        this.currentLevel = 1;
        this.experiencePoints = 0;
        this.preferences = new HashMap<>();
        this.unlockedShips = new ArrayList<>();
        this.unlockedThemes = new ArrayList<>();
        initializeDefaultPreferences();
        initializeDefaultUnlocks();
    }

        // ========== INICIALIZACIÓN ==========

    private void initializeDefaultPreferences() {
        preferences.put("soundEnabled", true);
        preferences.put("musicEnabled", true);
        preferences.put("animationsEnabled", true);
        preferences.put("difficulty", "NORMAL");
        preferences.put("theme", "default");
        preferences.put("language", "es");
    }

    private void initializeDefaultUnlocks() {
        // Desbloqueos iniciales
        unlockedShips.add("FRIGATE");
        unlockedShips.add("DESTROYER");
        unlockedThemes.add("default");
        unlockedThemes.add("classic");
    }

    // ========== GESTIÓN DE LOGROS ==========

    public boolean unlockAchievement(Achievement achievement) {
        if (!unlockedAchievements.contains(achievement)) {
            unlockedAchievements.add(achievement);
            totalScore += achievement.getPoints();
            addExperience(achievement.getPoints() * 10);
            
            System.out.println("🎉 Logro desbloqueado: " + achievement.getName());
            System.out.println("➕ " + achievement.getPoints() + " puntos obtenidos");
            
            return true;
        }
        return false;
    }

    public boolean hasAchievement(Achievement achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public int getAchievementCount() {
        return unlockedAchievements.size();
    }

    public int getTotalAchievements() {
        return Achievement.values().length;
    }

    public double getAchievementProgress() {
        return (double) getAchievementCount() / getTotalAchievements() * 100;
    }

    // ========== SISTEMA DE NIVELES ==========

    public void addExperience(int exp) {
        if (exp <= 0) return;
        
        experiencePoints += exp;
        System.out.println("➕ " + exp + " EXP obtenida. Total: " + experiencePoints);
        
        checkLevelUp();
    }

    private void checkLevelUp() {
        int expRequired = getExperienceForNextLevel();
        
        while (experiencePoints >= expRequired && currentLevel < getMaxLevel()) {
            experiencePoints -= expRequired;
            currentLevel++;
            
            // Recompensas por subir de nivel
            grantLevelUpRewards();
            
            expRequired = getExperienceForNextLevel();
            System.out.println("🎊 ¡Nivel " + currentLevel + " alcanzado!");
        }
    }

    private void grantLevelUpRewards() {
        // Recompensas según el nivel
        switch (currentLevel) {
            case 5:
                unlockedShips.add("CRUISER");
                System.out.println("🎁 ¡Nuevo barco desbloqueado: CRUISER!");
                break;
            case 10:
                unlockedShips.add("BATTLESHIP");
                System.out.println("🎁 ¡Nuevo barco desbloqueado: BATTLESHIP!");
                break;
            case 15:
                unlockedShips.add("CARRIER");
                System.out.println("🎁 ¡Nuevo barco desbloqueado: CARRIER!");
                break;
            case 20:
                unlockedThemes.add("premium");
                System.out.println("🎁 ¡Nuevo tema desbloqueado: PREMIUM!");
                break;
        }
        
        // Puntos adicionales cada 5 niveles
        if (currentLevel % 5 == 0) {
            totalScore += 50;
            System.out.println("💰 +50 puntos por alcanzar nivel " + currentLevel);
        }
    }

    public int getExperienceForNextLevel() {
        // Fórmula: 100 * nivel^1.5
        return (int) (100 * Math.pow(currentLevel, 1.5));
    }

    public int getMaxLevel() {
        return 100;
    }

    public double getLevelProgress() {
        int expForNext = getExperienceForNextLevel();
        int expForCurrent = getExperienceForLevel(currentLevel);
        int expInLevel = experiencePoints - expForCurrent;
        int expNeeded = expForNext - expForCurrent;
        
        return expNeeded > 0 ? (double) expInLevel / expNeeded : 0.0;
    }

    private int getExperienceForLevel(int level) {
        if (level <= 1) return 0;
        return (int) (100 * Math.pow(level - 1, 1.5));
    }

    // ========== ACTUALIZACIÓN DE ESTADÍSTICAS ==========

    public void updateFromGame(GameController gameController, boolean won) {
        lastPlayed = new Date();
        statistics.recordGameStart();
        
        if (won) {
            statistics.recordGameWin();
            grantVictoryRewards(gameController);
            checkVictoryAchievements();
        } else {
            statistics.recordGameLoss();
        }
        
        // Registrar disparos y hundimientos
        recordGameStatistics(gameController);
        
        // Verificar logros de progresión
        checkProgressionAchievements();
    }

    private void grantVictoryRewards(GameController gameController) {
        int baseExp = 50;
        int bonusExp = gameController.getRemainingPlayerShips() * 10; // Bonus por barcos intactos
        int totalExp = baseExp + bonusExp;
        
        addExperience(totalExp);
        totalScore += 25; // Puntos base por victoria
        
        System.out.println("🏆 Victoria! +" + totalExp + " EXP, +25 puntos");
    }

    private void recordGameStatistics(GameController gameController) {
        // Aquí puedes registrar estadísticas específicas del juego
        // como precisión, barcos hundidos, etc.
    }

    // ========== VERIFICACIÓN DE LOGROS ==========

    private void checkVictoryAchievements() {
        // Logros basados en victorias
        if (statistics.getCurrentWinStreak() >= 5 && !hasAchievement(Achievement.STREAKER)) {
            unlockAchievement(Achievement.STREAKER);
        }
        
        if (statistics.getGamesWon() == 1 && !hasAchievement(Achievement.FIRST_BLOOD)) {
            unlockAchievement(Achievement.FIRST_BLOOD);
        }
        
        // Juego perfecto (ningún barco perdido)
        // if (gameController.getRemainingPlayerShips() == gameController.getPlayerShips().size()) {
        //     unlockAchievement(Achievement.PERFECT_GAME);
        // }
    }

    private void checkProgressionAchievements() {
        // Logros basados en progresión general
        if (statistics.getGamesPlayed() >= 10 && !hasAchievement(Achievement.VETERAN)) {
            unlockAchievement(Achievement.VETERAN);
        }
        
        if (statistics.getAccuracy() >= 80.0 && !hasAchievement(Achievement.SHARPSHOOTER)) {
            unlockAchievement(Achievement.SHARPSHOOTER);
        }
        
        if (currentLevel >= 25 && !hasAchievement(Achievement.UNSTOPPABLE)) {
            unlockAchievement(Achievement.UNSTOPPABLE);
        }
    }

    // ========== GESTIÓN DE PREFERENCIAS ==========

    public void setPreference(String key, Object value) {
        preferences.put(key, value);
    }

    public Object getPreference(String key) {
        return preferences.get(key);
    }

    public Object getPreference(String key, Object defaultValue) {
        return preferences.getOrDefault(key, defaultValue);
    }

    public boolean getBooleanPreference(String key) {
        Object value = preferences.get(key);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    public String getStringPreference(String key) {
        Object value = preferences.get(key);
        return value instanceof String ? (String) value : "";
    }

    // ========== GESTIÓN DE DESBLOQUEOS ==========

    public boolean unlockShip(String shipType) {
        if (!unlockedShips.contains(shipType)) {
            unlockedShips.add(shipType);
            return true;
        }
        return false;
    }

    public boolean hasShipUnlocked(String shipType) {
        return unlockedShips.contains(shipType);
    }

    public boolean unlockTheme(String theme) {
        if (!unlockedThemes.contains(theme)) {
            unlockedThemes.add(theme);
            return true;
        }
        return false;
    }

    public boolean hasThemeUnlocked(String theme) {
        return unlockedThemes.contains(theme);
    }

    // ========== MÉTODOS DE INFORMACIÓN ==========

    public String getRank() {
        if (currentLevel >= 80) return "Almirante Legendario";
        if (currentLevel >= 60) return "Almirante Experto";
        if (currentLevel >= 40) return "Capitán Veterano";
        if (currentLevel >= 20) return "Teniente";
        if (currentLevel >= 10) return "Alférez";
        return "Marinero";
    }

    public String getPlayerSummary() {
        return String.format(
            "👤 %s (%s)\n" +
            "⭐ Nivel %d | %s\n" +
            "🏆 Puntos: %,d | EXP: %,d/%,d\n" +
            "📊 Partidas: %d | Victorias: %d (%.1f%%)\n" +
            "🎯 Precisión: %.1f%% | Barcos hundidos: %d\n" +
            "🏅 Logros: %d/%d (%.1f%%)",
            playerName, getRank(), currentLevel,
            totalScore, experiencePoints, getExperienceForNextLevel(),
            statistics.getGamesPlayed(), statistics.getGamesWon(), statistics.getWinPercentage(),
            statistics.getAccuracy(), statistics.getShipsSunk(),
            getAchievementCount(), getTotalAchievements(), getAchievementProgress()
        );
    }

    public Map<String, Object> getProfileData() {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("playerName", playerName);
        data.put("avatar", avatar);
        data.put("creationDate", creationDate);
        data.put("lastPlayed", lastPlayed);
        data.put("totalScore", totalScore);
        data.put("currentLevel", currentLevel);
        data.put("experiencePoints", experiencePoints);
        data.put("rank", getRank());
        data.put("achievementsUnlocked", getAchievementCount());
        data.put("shipsUnlocked", unlockedShips.size());
        data.put("themesUnlocked", unlockedThemes.size());
        data.put("statistics", statistics.getDetailedStats());
        
        return data;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    public void resetStatistics() {
        statistics.reset();
        totalScore = 0;
        currentLevel = 1;
        experiencePoints = 0;
        unlockedAchievements.clear();
        System.out.println("🔄 Estadísticas reiniciadas");
    }

    public void mergeProfile(PlayerProfile other) {
        // Fusionar estadísticas
        this.statistics.merge(other.statistics);
        
        // Fusionar logros
        this.unlockedAchievements.addAll(other.unlockedAchievements);
        
        // Tomar el mayor nivel y puntuación
        this.totalScore = Math.max(this.totalScore, other.totalScore);
        this.currentLevel = Math.max(this.currentLevel, other.currentLevel);
        this.experiencePoints = Math.max(this.experiencePoints, other.experiencePoints);
        
        // Fusionar desbloqueos
        this.unlockedShips.addAll(other.unlockedShips);
        this.unlockedThemes.addAll(other.unlockedThemes);
    }

    // ========== GETTERS Y SETTERS ==========

    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Date getCreationDate() { return creationDate; }
    public Date getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Date lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public GameStatistics getStatistics() { return statistics; }
    public Set<Achievement> getUnlockedAchievements() { return new HashSet<>(unlockedAchievements); }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    
    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) { this.experiencePoints = experiencePoints; }
    
    public Map<String, Object> getPreferences() { return new HashMap<>(preferences); }
    public List<String> getUnlockedShips() { return new ArrayList<>(unlockedShips); }
    public List<String> getUnlockedThemes() { return new ArrayList<>(unlockedThemes); }

    @Override
    public String toString() {
        return String.format("PlayerProfile{name='%s', level=%d, score=%,d}", 
            playerName, currentLevel, totalScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerProfile that = (PlayerProfile) o;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
}
    
    
    
    

