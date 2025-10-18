/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.ShipType;
import java.util.*;
import java.io.Serializable;

public class GameStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // ========== ESTADÍSTICAS BÁSICAS ==========
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int totalShotsFired;
    private int successfulShots;
    private int shipsSunk;
    private int shipsLost;
    
    // ========== RACHAS Y RÉCORDS ==========
    private int currentWinStreak;
    private int longestWinStreak;
    private int currentLossStreak;
    private int longestLossStreak;
    
    // ========== TIEMPO Y FECHAS ==========
    private Date firstGameDate;
    private Date lastGameDate;
    private long totalPlayTime; // en segundos
    private int fastestVictory; // en turnos
    private int slowestVictory; // en turnos
    
    // ========== ESTADÍSTICAS DETALLADAS ==========
    private Map<ShipType, Integer> shipsSunkByType;
    private Map<ShipType, Integer> shipsLostByType;
    private Map<String, Integer> victoriesByDifficulty;
    private Map<Skill, Integer> skillsUsed;
    private Map<String, Integer> victoriesByGameMode;
    
    // ========== RÉCORDS PERSONALES ==========
    private int mostShipsSunkInGame;
    private int mostAccurateGame; // porcentaje
    private int mostShotsInGame;
    private int leastShotsInGame;
    
    // ========== CONSTRUCTORES ==========
    
    public GameStatistics() {
        initializeStatistics();
    }
    
    public GameStatistics(Date firstGameDate) {
        this.firstGameDate = firstGameDate;
        this.lastGameDate = firstGameDate;
        initializeStatistics();
    }
    
    private void initializeStatistics() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.totalShotsFired = 0;
        this.successfulShots = 0;
        this.shipsSunk = 0;
        this.shipsLost = 0;
        
        this.currentWinStreak = 0;
        this.longestWinStreak = 0;
        this.currentLossStreak = 0;
        this.longestLossStreak = 0;
        
        this.totalPlayTime = 0;
        this.fastestVictory = Integer.MAX_VALUE;
        this.slowestVictory = 0;
        
        this.mostShipsSunkInGame = 0;
        this.mostAccurateGame = 0;
        this.mostShotsInGame = 0;
        this.leastShotsInGame = Integer.MAX_VALUE;
        
        initializeMaps();
    }
    
    private void initializeMaps() {
        shipsSunkByType = new HashMap<>();
        shipsLostByType = new HashMap<>();
        victoriesByDifficulty = new HashMap<>();
        skillsUsed = new HashMap<>();
        victoriesByGameMode = new HashMap<>();
        
        // Inicializar contadores para cada tipo de barco
        for (ShipType type : ShipType.values()) {
            shipsSunkByType.put(type, 0);
            shipsLostByType.put(type, 0);
        }
        
        // Inicializar contadores para habilidades
        for (Skill skill : Skill.values()) {
            skillsUsed.put(skill, 0);
        }
        
        // Inicializar dificultades comunes
        victoriesByDifficulty.put("EASY", 0);
        victoriesByDifficulty.put("NORMAL", 0);
        victoriesByDifficulty.put("HARD", 0);
        victoriesByDifficulty.put("EXPERT", 0);
    }
    
    // ========== MÉTODOS PARA ACTUALIZAR ESTADÍSTICAS ==========
    
    
    
    public void recordGameResult(boolean won, int turns, int shipsSunkThisGame, 
                                int shotsThisGame, int successfulShotsThisGame, 
                                String difficulty, String gameMode) {
        if (won) {
            recordVictory(turns, shipsSunkThisGame, shotsThisGame, successfulShotsThisGame, difficulty, gameMode);
        } else {
            recordDefeat(shipsSunkThisGame, shotsThisGame, successfulShotsThisGame);
        }
        
        updateRecords(shipsSunkThisGame, shotsThisGame, successfulShotsThisGame);
    }
    
    private void recordVictory(int turns, int shipsSunkThisGame, int shotsThisGame, 
                              int successfulShotsThisGame, String difficulty, String gameMode) {
        gamesWon++;
        currentWinStreak++;
        currentLossStreak = 0;
        
        // Actualizar racha más larga
        longestWinStreak = Math.max(longestWinStreak, currentWinStreak);
        
        // Actualizar récords de velocidad
        if (turns < fastestVictory) {
            fastestVictory = turns;
        }
        if (turns > slowestVictory) {
            slowestVictory = turns;
        }
        
        // Registrar victoria por dificultad y modo
        victoriesByDifficulty.merge(difficulty, 1, Integer::sum);
        victoriesByGameMode.merge(gameMode, 1, Integer::sum);
        
        // Actualizar disparos y hundimientos
        updateShotsAndSinks(shotsThisGame, successfulShotsThisGame, shipsSunkThisGame);
    }
    
    private void recordDefeat(int shipsSunkThisGame, int shotsThisGame, int successfulShotsThisGame) {
        gamesLost++;
        currentLossStreak++;
        currentWinStreak = 0;
        
        // Actualizar racha de derrotas más larga
        longestLossStreak = Math.max(longestLossStreak, currentLossStreak);
        
        // Actualizar disparos y hundimientos
        updateShotsAndSinks(shotsThisGame, successfulShotsThisGame, shipsSunkThisGame);
    }
    
    
    
    // ========== MÉTODOS PARA VICTORIAS Y DERROTAS ==========

/**
 * Registra una victoria en las estadísticas
 */
public void recordGameWin() {
    gamesWon++;
    currentWinStreak++;
    currentLossStreak = 0;
    
    // Actualizar la racha más larga de victorias
    if (currentWinStreak > longestWinStreak) {
        longestWinStreak = currentWinStreak;
    }
    
    // Actualizar fecha del último juego
    lastGameDate = new Date();
    
    System.out.println("✅ Victoria registrada. Racha actual: " + currentWinStreak);
}

/**
 * Registra una victoria con detalles adicionales
 * @param turns Número de turnos de la partida
 * @param difficulty Dificultad del juego
 * @param gameMode Modo de juego
 */
public void recordGameWin(int turns, String difficulty, String gameMode) {
    recordGameWin();
    
    // Actualizar récords de velocidad
    updateVictoryRecords(turns);
    
    // Registrar victoria por dificultad y modo
    recordVictoryByDifficulty(difficulty);
    recordVictoryByGameMode(gameMode);
}

/**
 * Registra una derrota en las estadísticas
 */
public void recordGameLoss() {
    gamesLost++;
    currentLossStreak++;
    currentWinStreak = 0;
    
    // Actualizar la racha más larga de derrotas
    if (currentLossStreak > longestLossStreak) {
        longestLossStreak = currentLossStreak;
    }
    
    // Actualizar fecha del último juego
    lastGameDate = new Date();
    
    System.out.println("❌ Derrota registrada. Racha de derrotas: " + currentLossStreak);
}

/**
 * Registra una derrota con detalles adicionales
 * @param shipsSunk Barcos hundidos en la partida
 * @param difficulty Dificultad del juego
 */
public void recordGameLoss(int shipsSunk, String difficulty) {
    recordGameLoss();
    
    // Registrar barcos hundidos incluso en derrota
    if (shipsSunk > 0) {
        System.out.println("💥 Hundiste " + shipsSunk + " barcos enemigos antes de caer");
    }
}

// ========== MÉTODOS AUXILIARES PRIVADOS ==========

private void updateVictoryRecords(int turns) {
    // Récord de victoria más rápida
    if (turns < fastestVictory) {
        fastestVictory = turns;
        System.out.println("⚡ ¡Nuevo récord! Victoria más rápida en " + turns + " turnos");
    }
    
    // Récord de victoria más lenta
    if (turns > slowestVictory) {
        slowestVictory = turns;
    }
}

private void recordVictoryByDifficulty(String difficulty) {
    if (difficulty != null && !difficulty.trim().isEmpty()) {
        victoriesByDifficulty.merge(difficulty.toUpperCase(), 1, Integer::sum);
    }
}

private void recordVictoryByGameMode(String gameMode) {
    if (gameMode != null && !gameMode.trim().isEmpty()) {
        victoriesByGameMode.merge(gameMode, 1, Integer::sum);
    }
}

// ========== MÉTODOS ADICIONALES ÚTILES ==========

/**
 * Registra el inicio de una nueva partida
 */
public void recordGameStart() {
    gamesPlayed++;
    
    if (firstGameDate == null) {
        firstGameDate = new Date();
    }
    lastGameDate = new Date();
    
    System.out.println("🎮 Iniciando partida #" + gamesPlayed);
}

/**
 * Registra el resultado completo de una partida
 * @param won true si ganó, false si perdió
 * @param turns Número de turnos
 * @param shipsSunk Barcos hundidos
 * @param shipsLost Barcos perdidos
 * @param difficulty Dificultad
 * @param gameMode Modo de juego
 */
public void recordGameResult(boolean won, int turns, int shipsSunk, int shipsLost, 
                           String difficulty, String gameMode) {
    recordGameStart();
    
    if (won) {
        recordGameWin(turns, difficulty, gameMode);
    } else {
        recordGameLoss(shipsSunk, difficulty);
    }
    
    // Actualizar estadísticas de barcos
    this.shipsSunk += shipsSunk;
    this.shipsLost += shipsLost;
    
    // Actualizar récords
    updateGameRecords(shipsSunk, turns);
}

/**
 * Registra una partida con estadísticas detalladas de disparos
 * @param won true si ganó, false si perdió
 * @param turns Número de turnos
 * @param totalShots Total de disparos realizados
 * @param successfulShots Disparos acertados
 * @param shipsSunk Barcos hundidos
 * @param shipsLost Barcos perdidos
 * @param difficulty Dificultad
 * @param gameMode Modo de juego
 */
public void recordDetailedGameResult(boolean won, int turns, int totalShots, int successfulShots,
                                   int shipsSunk, int shipsLost, String difficulty, String gameMode) {
    recordGameResult(won, turns, shipsSunk, shipsLost, difficulty, gameMode);
    
    // Registrar estadísticas de disparos
    this.totalShotsFired += totalShots;
    this.successfulShots += successfulShots;
    
    System.out.println(String.format("🎯 Precisión esta partida: %.1f%%", 
        totalShots > 0 ? (double) successfulShots / totalShots * 100 : 0));
}

private void updateGameRecords(int shipsSunkThisGame, int turns) {
    // Récord de más barcos hundidos en un juego
    if (shipsSunkThisGame > mostShipsSunkInGame) {
        mostShipsSunkInGame = shipsSunkThisGame;
        System.out.println("🚀 ¡Nuevo récord! " + shipsSunkThisGame + " barcos hundidos en una partida");
    }
    
    // Récord de más disparos en un juego (si se está trackeando)
    if (turns > mostShotsInGame) {
        mostShotsInGame = turns;
    }
}

// ========== MÉTODOS DE CONSULTA DE RACHAS ==========

/**
 * Obtiene información sobre la racha actual
 * @return String descriptivo de la racha
 */
public String getStreakInfo() {
    if (currentWinStreak > 0) {
        return "Racha de victorias: " + currentWinStreak + " 🔥";
    } else if (currentLossStreak > 0) {
        return "Racha de derrotas: " + currentLossStreak + " 💔";
    } else {
        return "Sin racha activa";
    }
}

/**
 * Verifica si el jugador está en una racha impresionante
 * @return true si está en una racha notable
 */
public boolean isOnImpressiveStreak() {
    return currentWinStreak >= 3;
}

/**
 * Obtiene la racha más impresionante del jugador
 * @return Descripción de la mejor racha
 */
public String getBestStreakInfo() {
    if (longestWinStreak >= 5) {
        return "Mejor racha: " + longestWinStreak + " victorias consecutivas! 🌟";
    } else if (longestWinStreak >= 3) {
        return "Mejor racha: " + longestWinStreak + " victorias consecutivas";
    } else {
        return "Mejor racha: " + longestWinStreak + " victorias";
    }
}

// ========== MÉTODOS DE RESET DE RACHAS ==========

/**
 * Reinicia las rachas actuales (útil para nuevo perfil o temporada)
 */
public void resetStreaks() {
    currentWinStreak = 0;
    currentLossStreak = 0;
    System.out.println("🔄 Rachas reiniciadas");
}

/**
 * Reinicia todas las estadísticas (para nuevo perfil)
 */
public void resetAll() {
    initializeStatistics();
    System.out.println("🔄 Todas las estadísticas reiniciadas");
}
    
    
    
    
    
    
    
    private void updateShotsAndSinks(int shots, int successfulShots, int shipsSunk) {
        totalShotsFired += shots;
        successfulShots += successfulShots;
        shipsSunk += shipsSunk;
    }
    
    private void updateRecords(int shipsSunkThisGame, int shotsThisGame, int successfulShotsThisGame) {
        // Récord de más barcos hundidos en un juego
        mostShipsSunkInGame = Math.max(mostShipsSunkInGame, shipsSunkThisGame);
        
        // Récord de más y menos disparos en un juego
        mostShotsInGame = Math.max(mostShotsInGame, shotsThisGame);
        if (shotsThisGame > 0) {
            leastShotsInGame = Math.min(leastShotsInGame, shotsThisGame);
        }
        
        // Récord de precisión en un juego
        if (shotsThisGame > 0) {
            int accuracyThisGame = (int) ((double) successfulShotsThisGame / shotsThisGame * 100);
            mostAccurateGame = Math.max(mostAccurateGame, accuracyThisGame);
        }
    }
    
    // ========== MÉTODOS ESPECÍFICOS ==========
    
    public void recordShot(boolean wasHit) {
        totalShotsFired++;
        if (wasHit) {
            successfulShots++;
        }
    }
    
    public void recordShipSunk(ShipType shipType) {
        shipsSunk++;
        shipsSunkByType.merge(shipType, 1, Integer::sum);
    }
    
    public void recordShipLost(ShipType shipType) {
        shipsLost++;
        shipsLostByType.merge(shipType, 1, Integer::sum);
    }
    
    public void recordSkillUsed(Skill skill) {
        skillsUsed.merge(skill, 1, Integer::sum);
    }
    
    public void recordPlayTime(long seconds) {
        totalPlayTime += seconds;
    }
    
    // ========== CÁLCULOS Y MÉTRICAS ==========
    
    public double getWinPercentage() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesWon / gamesPlayed * 100;
    }
    
    public double getLossPercentage() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesLost / gamesPlayed * 100;
    }
    
    public double getAccuracy() {
        if (totalShotsFired == 0) return 0.0;
        return (double) successfulShots / totalShotsFired * 100;
    }
    
    public double getAverageShotsPerGame() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalShotsFired / gamesPlayed;
    }
    
    public double getAverageShipsSunkPerGame() {
        if (gamesPlayed == 0) return 0.0;
        return (double) shipsSunk / gamesPlayed;
    }
    
    public double getAverageShipsLostPerGame() {
        if (gamesPlayed == 0) return 0.0;
        return (double) shipsLost / gamesPlayed;
    }
    
    public double getKillDeathRatio() {
        if (shipsLost == 0) return shipsSunk; // Evitar división por cero
        return (double) shipsSunk / shipsLost;
    }
    
    public double getAveragePlayTimePerGame() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalPlayTime / gamesPlayed;
    }
    
    public String getFormattedTotalPlayTime() {
        long hours = totalPlayTime / 3600;
        long minutes = (totalPlayTime % 3600) / 60;
        long seconds = totalPlayTime % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    // ========== MÉTODOS DE INFORMACIÓN ESPECÍFICA ==========
    
    public ShipType getMostSunkShipType() {
        return shipsSunkByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(ShipType.FRIGATE);
    }
    
    public ShipType getMostLostShipType() {
        return shipsLostByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(ShipType.FRIGATE);
    }
    
    public Skill getMostUsedSkill() {
        return skillsUsed.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Skill.SONAR);
    }
    
    public String getMostSuccessfulDifficulty() {
        return victoriesByDifficulty.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("NORMAL");
    }
    
    public String getFavoriteGameMode() {
        return victoriesByGameMode.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Clásico");
    }
    
    public int getTotalSkillsUsed() {
        return skillsUsed.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    // ========== MÉTODOS DE INFORMACIÓN GENERAL ==========
    
    public String getSummary() {
        return String.format(
            "📊 ESTADÍSTICAS GENERALES\n" +
            "🎮 Partidas jugadas: %,d\n" +
            "🏆 Victorias: %,d (%.1f%%)\n" +
            "💔 Derrotas: %,d (%.1f%%)\n" +
            "⚡ Racha actual: %,d | Más larga: %,d\n" +
            "🎯 Precisión: %.1f%%\n" +
            "🚢 Barcos hundidos: %,d | Perdidos: %,d\n" +
            "⏱️ Tiempo total: %s",
            gamesPlayed, gamesWon, getWinPercentage(), gamesLost, getLossPercentage(),
            currentWinStreak, longestWinStreak, getAccuracy(),
            shipsSunk, shipsLost, getFormattedTotalPlayTime()
        );
    }
    
    public Map<String, Object> getDetailedStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas básicas
        stats.put("gamesPlayed", gamesPlayed);
        stats.put("gamesWon", gamesWon);
        stats.put("gamesLost", gamesLost);
        stats.put("winPercentage", getWinPercentage());
        stats.put("lossPercentage", getLossPercentage());
        
        // Disparos y precisión
        stats.put("totalShots", totalShotsFired);
        stats.put("successfulShots", successfulShots);
        stats.put("accuracy", getAccuracy());
        stats.put("averageShotsPerGame", getAverageShotsPerGame());
        
        // Barcos
        stats.put("shipsSunk", shipsSunk);
        stats.put("shipsLost", shipsLost);
        stats.put("averageShipsSunkPerGame", getAverageShipsSunkPerGame());
        stats.put("averageShipsLostPerGame", getAverageShipsLostPerGame());
        stats.put("killDeathRatio", getKillDeathRatio());
        
        // Rachas
        stats.put("currentWinStreak", currentWinStreak);
        stats.put("longestWinStreak", longestWinStreak);
        stats.put("currentLossStreak", currentLossStreak);
        stats.put("longestLossStreak", longestLossStreak);
        
        // Tiempo
        stats.put("firstGame", firstGameDate);
        stats.put("lastGame", lastGameDate);
        stats.put("totalPlayTime", totalPlayTime);
        stats.put("formattedPlayTime", getFormattedTotalPlayTime());
        stats.put("averagePlayTimePerGame", getAveragePlayTimePerGame());
        
        // Récords
        stats.put("fastestVictory", fastestVictory);
        stats.put("slowestVictory", slowestVictory);
        stats.put("mostShipsSunkInGame", mostShipsSunkInGame);
        stats.put("mostAccurateGame", mostAccurateGame);
        stats.put("mostShotsInGame", mostShotsInGame);
        stats.put("leastShotsInGame", leastShotsInGame == Integer.MAX_VALUE ? 0 : leastShotsInGame);
        
        // Estadísticas detalladas
        stats.put("shipsSunkByType", new HashMap<>(shipsSunkByType));
        stats.put("shipsLostByType", new HashMap<>(shipsLostByType));
        stats.put("victoriesByDifficulty", new HashMap<>(victoriesByDifficulty));
        stats.put("skillsUsed", new HashMap<>(skillsUsed));
        stats.put("victoriesByGameMode", new HashMap<>(victoriesByGameMode));
        
        return stats;
    }
    
    public List<String> getFunFacts() {
        List<String> facts = new ArrayList<>();
        
        if (gamesPlayed > 0) {
            facts.add(String.format("Tu barco más efectivo: %s (%d hundidos)", 
                getMostSunkShipType().getName(), shipsSunkByType.get(getMostSunkShipType())));
            
            facts.add(String.format("Habilidad favorita: %s (%d usos)", 
                getMostUsedSkill().getName(), skillsUsed.get(getMostUsedSkill())));
            
            if (longestWinStreak >= 3) {
                facts.add(String.format("Racha de victorias más larga: %d partidas", longestWinStreak));
            }
            
            if (mostAccurateGame >= 80) {
                facts.add(String.format("Mejor precisión en una partida: %d%%", mostAccurateGame));
            }
            
            if (fastestVictory < Integer.MAX_VALUE) {
                facts.add(String.format("Victoria más rápida: %d turnos", fastestVictory));
            }
        }
        
        return facts;
    }
    
    // ========== MÉTODOS DE FUSIÓN Y REINICIO ==========
    
    public void merge(GameStatistics other) {
        // Fusionar estadísticas básicas
        this.gamesPlayed += other.gamesPlayed;
        this.gamesWon += other.gamesWon;
        this.gamesLost += other.gamesLost;
        this.totalShotsFired += other.totalShotsFired;
        this.successfulShots += other.successfulShots;
        this.shipsSunk += other.shipsSunk;
        this.shipsLost += other.shipsLost;
        
        // Fusionar rachas (tomar las más largas)
        this.longestWinStreak = Math.max(this.longestWinStreak, other.longestWinStreak);
        this.longestLossStreak = Math.max(this.longestLossStreak, other.longestLossStreak);
        
        // Fusionar tiempos
        this.totalPlayTime += other.totalPlayTime;
        this.fastestVictory = Math.min(this.fastestVictory, other.fastestVictory);
        this.slowestVictory = Math.max(this.slowestVictory, other.slowestVictory);
        
        // Fusionar récords
        this.mostShipsSunkInGame = Math.max(this.mostShipsSunkInGame, other.mostShipsSunkInGame);
        this.mostAccurateGame = Math.max(this.mostAccurateGame, other.mostAccurateGame);
        this.mostShotsInGame = Math.max(this.mostShotsInGame, other.mostShotsInGame);
        this.leastShotsInGame = Math.min(this.leastShotsInGame, other.leastShotsInGame);
        
        // Fusionar mapas
        mergeMaps(this.shipsSunkByType, other.shipsSunkByType);
        mergeMaps(this.shipsLostByType, other.shipsLostByType);
        mergeMaps(this.victoriesByDifficulty, other.victoriesByDifficulty);
        mergeMaps(this.skillsUsed, other.skillsUsed);
        mergeMaps(this.victoriesByGameMode, other.victoriesByGameMode);
        
        // Actualizar fechas
        if (other.firstGameDate.before(this.firstGameDate)) {
            this.firstGameDate = other.firstGameDate;
        }
        if (other.lastGameDate.after(this.lastGameDate)) {
            this.lastGameDate = other.lastGameDate;
        }
    }
    
    private <K> void mergeMaps(Map<K, Integer> target, Map<K, Integer> source) {
        for (Map.Entry<K, Integer> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }
    
    public void reset() {
        initializeStatistics();
    }
    
    // ========== GETTERS ==========
    
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public int getTotalShotsFired() { return totalShotsFired; }
    public int getSuccessfulShots() { return successfulShots; }
    public int getShipsSunk() { return shipsSunk; }
    public int getShipsLost() { return shipsLost; }
    public int getCurrentWinStreak() { return currentWinStreak; }
    public int getLongestWinStreak() { return longestWinStreak; }
    public int getCurrentLossStreak() { return currentLossStreak; }
    public int getLongestLossStreak() { return longestLossStreak; }
    public Date getFirstGameDate() { return firstGameDate; }
    public Date getLastGameDate() { return lastGameDate; }
    public long getTotalPlayTime() { return totalPlayTime; }
    public int getFastestVictory() { return fastestVictory; }
    public int getSlowestVictory() { return slowestVictory; }
    public Map<ShipType, Integer> getShipsSunkByType() { return new HashMap<>(shipsSunkByType); }
    public Map<ShipType, Integer> getShipsLostByType() { return new HashMap<>(shipsLostByType); }
    public Map<String, Integer> getVictoriesByDifficulty() { return new HashMap<>(victoriesByDifficulty); }
    public Map<Skill, Integer> getSkillsUsed() { return new HashMap<>(skillsUsed); }
    public Map<String, Integer> getVictoriesByGameMode() { return new HashMap<>(victoriesByGameMode); }
    public int getMostShipsSunkInGame() { return mostShipsSunkInGame; }
    public int getMostAccurateGame() { return mostAccurateGame; }
    public int getMostShotsInGame() { return mostShotsInGame; }
    public int getLeastShotsInGame() { return leastShotsInGame == Integer.MAX_VALUE ? 0 : leastShotsInGame; }
}    

