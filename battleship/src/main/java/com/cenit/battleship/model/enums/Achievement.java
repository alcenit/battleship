/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model.enums;



import java.util.*;

public enum Achievement {
    // ========== LOGROS DE PROGRESIÓN ==========
    FIRST_BLOOD(
        "Primera Sangre", 
        "Hunde tu primer barco enemigo",
        "Realiza tu primer hundimiento exitoso",
        10,
        AchievementCategory.PROGRESSION,
        AchievementRarity.COMMON,
        "🔴"
    ),
    
    SHARPSHOOTER(
        "Tirador Certero", 
        "Logra 80% de precisión en una partida",
        "Demuestra tu puntería con una alta precisión de disparos",
        25,
        AchievementCategory.SKILL,
        AchievementRarity.RARE,
        "🎯"
    ),
    
    PERFECT_GAME(
        "Juego Perfecto", 
        "Gana una partida sin que hundan ningún barco tuyo",
        "Completa una partida sin perder ningún barco",
        50,
        AchievementCategory.SKILL,
        AchievementRarity.EPIC,
        "⭐"
    ),
    
    COMBO_MASTER(
        "Maestro de Combos", 
        "Hunde 3 barcos en 5 turnos consecutivos",
        "Demuestra tu habilidad para hundir barcos rápidamente",
        30,
        AchievementCategory.SKILL,
        AchievementRarity.RARE,
        "🔥"
    ),
    
    SKILL_MASTER(
        "Maestro de Habilidades", 
        "Usa todas las habilidades en una partida",
        "Utiliza cada tipo de habilidad al menos una vez en una sola partida",
        40,
        AchievementCategory.STRATEGY,
        AchievementRarity.RARE,
        "🎓"
    ),
    
    COMEBACK_KING(
        "Rey del Remonte", 
        "Gana una partida estando a punto de perder",
        "Gana cuando solo te queda 1 barco intacto",
        35,
        AchievementCategory.STRATEGY,
        AchievementRarity.RARE,
        "👑"
    ),
    
    STREAKER(
        "En Racha", 
        "Gana 5 partidas consecutivas",
        "Mantén una racha de victorias impresionante",
        60,
        AchievementCategory.PROGRESSION,
        AchievementRarity.EPIC,
        "⚡"
    ),
    
    VETERAN(
        "Veterano", 
        "Juega 50 partidas",
        "Demuestra tu dedicación al juego",
        20,
        AchievementCategory.PROGRESSION,
        AchievementRarity.COMMON,
        "🎖️"
    ),
    
    UNSTOPPABLE(
        "Imparable", 
        "Hunde todos los barcos enemigos en menos de 30 turnos",
        "Termina la partida rápidamente con una victoria aplastante",
        45,
        AchievementCategory.SKILL,
        AchievementRarity.EPIC,
        "💨"
    ),
    
    PACIFIST(
        "Pacificador", 
        "Gana una partida usando solo habilidades defensivas",
        "Demuestra que la defensa también puede ganar partidas",
        30,
        AchievementCategory.STRATEGY,
        AchievementRarity.RARE,
        "🕊️"
    ),

    // ========== LOGROS DE HABILIDADES ESPECÍFICAS ==========
    SONAR_EXPERT(
        "Experto en Sonar",
        "Usa el sonar 10 veces exitosamente",
        "Domina el arte de la detección submarina",
        15,
        AchievementCategory.SKILL,
        AchievementRarity.COMMON,
        "📡"
    ),
    
    RADAR_MASTER(
        "Maestro del Radar",
        "Detecta 20 barcos con el radar",
        "Tu radar nunca falla",
        20,
        AchievementCategory.SKILL,
        AchievementRarity.RARE,
        "📡"
    ),
    
    DRONE_OPERATOR(
        "Operador de Dron",
        "Explora 100 casillas con drones",
        "Controla el campo de batalla desde el aire",
        25,
        AchievementCategory.SKILL,
        AchievementRarity.COMMON,
        "🚁"
    ),

    // ========== LOGROS DE BARCOS ESPECÍFICOS ==========
    CARRIER_SINKER(
        "Hundidor de Portaaviones",
        "Hunde 10 portaaviones enemigos",
        "Domina el arte de hundir los barcos más grandes",
        35,
        AchievementCategory.COMBAT,
        AchievementRarity.RARE,
        "🚢"
    ),
    
    SUBMARINE_HUNTER(
        "Cazador de Submarinos", 
        "Hunde 15 submarinos enemigos",
        "Encuentra y destruye a los fantasmas del mar",
        30,
        AchievementCategory.COMBAT,
        AchievementRarity.RARE,
        "🛸"
    ),

    // ========== LOGROS SECRETOS ==========
    PERFECT_ACCURACY(
        "Precisión Perfecta",
        "Logra 100% de precisión en una partida",
        "Cada disparo cuenta - no falles ni uno",
        100,
        AchievementCategory.SKILL,
        AchievementRarity.LEGENDARY,
        "🏹"
    ),
    
    SPEEDRUNNER(
        "Velocista", 
        "Gana una partida en menos de 15 turnos",
        "La velocidad es tu mejor arma",
        75,
        AchievementCategory.SKILL,
        AchievementRarity.LEGENDARY,
        "⏱️"
    ),
    
    COLLECTOR(
        "Coleccionista", 
        "Desbloquea todos los logros",
        "Has conquistado todos los desafíos del juego",
        200,
        AchievementCategory.PROGRESSION,
        AchievementRarity.LEGENDARY,
        "🏆"
    );

    // ========== ENUMS INTERNOS ==========
    
    public enum AchievementCategory {
        PROGRESSION("Progresión", "Logros relacionados con el avance en el juego"),
        SKILL("Habilidad", "Logros que requieren destreza y precisión"),
        STRATEGY("Estrategia", "Logros que premian el pensamiento táctico"),
        COMBAT("Combate", "Logros relacionados con el combate naval"),
        SECRET("Secreto", "Logros ocultos y especiales");

        private final String displayName;
        private final String description;

        AchievementCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public enum AchievementRarity {
        COMMON("Común", 1.0, "#B0B0B0"),
        RARE("Raro", 0.3, "#0070DD"),
        EPIC("Épico", 0.1, "#A335EE"),
        LEGENDARY("Legendario", 0.02, "#FF8000");

        private final String displayName;
        private final double dropRate; // Tasa de aparición aproximada
        private final String colorCode;

        AchievementRarity(String displayName, double dropRate, String colorCode) {
            this.displayName = displayName;
            this.dropRate = dropRate;
            this.colorCode = colorCode;
        }

        public String getDisplayName() { return displayName; }
        public double getDropRate() { return dropRate; }
        public String getColorCode() { return colorCode; }
    }

    // ========== ATRIBUTOS DEL LOGRO ==========
    private final String name;
    private final String shortDescription;
    private final String longDescription;
    private final int points;
    private final AchievementCategory category;
    private final AchievementRarity rarity;
    private final String icon;
    private final boolean secret;

    // ========== CONSTRUCTORES ==========

    Achievement(String name, String shortDescription, String longDescription, 
                int points, AchievementCategory category, AchievementRarity rarity, String icon) {
        this(name, shortDescription, longDescription, points, category, rarity, icon, false);
    }

    Achievement(String name, String shortDescription, String longDescription, 
                int points, AchievementCategory category, AchievementRarity rarity, 
                String icon, boolean secret) {
        this.name = name;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.points = points;
        this.category = category;
        this.rarity = rarity;
        this.icon = icon;
        this.secret = secret;
    }

    // ========== MÉTODOS DE ACCESO ==========

    public String getName() { return name; }
    public String getShortDescription() { return shortDescription; }
    public String getLongDescription() { return longDescription; }
    public int getPoints() { return points; }
    public AchievementCategory getCategory() { return category; }
    public AchievementRarity getRarity() { return rarity; }
    public String getIcon() { return icon; }
    public boolean isSecret() { return secret; }

    // ========== MÉTODOS DE UTILIDAD ==========

    public String getDisplayName() {
        return icon + " " + name;
    }

    public String getFullDescription() {
        return String.format("%s\n\n%s\n\nCategoría: %s\nRareza: %s\nPuntos: %d",
            name, longDescription, category.getDisplayName(), rarity.getDisplayName(), points);
    }

    public String getTooltipText() {
        return String.format("<html><b>%s</b><br/>%s<br/><font color='%s'>%s - %d puntos</font></html>",
            name, shortDescription, rarity.getColorCode(), rarity.getDisplayName(), points);
    }

    public boolean isUnlockedByDefault() {
        return rarity == AchievementRarity.COMMON && category == AchievementCategory.PROGRESSION;
    }

    // ========== MÉTODOS ESTÁTICOS PARA GESTIÓN ==========

    public static List<Achievement> getByCategory(AchievementCategory category) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : values()) {
            if (achievement.getCategory() == category && !achievement.isSecret()) {
                result.add(achievement);
            }
        }
        return result;
    }

    public static List<Achievement> getByRarity(AchievementRarity rarity) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : values()) {
            if (achievement.getRarity() == rarity) {
                result.add(achievement);
            }
        }
        return result;
    }

    public static List<Achievement> getSecretAchievements() {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : values()) {
            if (achievement.isSecret()) {
                result.add(achievement);
            }
        }
        return result;
    }

    public static List<Achievement> getVisibleAchievements() {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : values()) {
            if (!achievement.isSecret()) {
                result.add(achievement);
            }
        }
        return result;
    }

    public static int getTotalPoints() {
        int total = 0;
        for (Achievement achievement : values()) {
            total += achievement.getPoints();
        }
        return total;
    }

    public static int getPointsByCategory(AchievementCategory category) {
        int total = 0;
        for (Achievement achievement : getByCategory(category)) {
            total += achievement.getPoints();
        }
        return total;
    }

    public static Achievement getByName(String name) {
        for (Achievement achievement : values()) {
            if (achievement.getName().equalsIgnoreCase(name)) {
                return achievement;
            }
        }
        return null;
    }

    // ========== MÉTODOS PARA VERIFICACIÓN DE PROGRESO ==========

    public static class AchievementProgress {
        private final Achievement achievement;
        private final int currentProgress;
        private final int targetProgress;
        private final boolean completed;

        public AchievementProgress(Achievement achievement, int currentProgress, int targetProgress, boolean completed) {
            this.achievement = achievement;
            this.currentProgress = currentProgress;
            this.targetProgress = targetProgress;
            this.completed = completed;
        }

        public Achievement getAchievement() { return achievement; }
        public int getCurrentProgress() { return currentProgress; }
        public int getTargetProgress() { return targetProgress; }
        public boolean isCompleted() { return completed; }
        public double getProgressPercentage() {
            return targetProgress > 0 ? (double) currentProgress / targetProgress * 100 : 0;
        }
    }

    // ========== MÉTODOS PARA SERIALIZACIÓN ==========

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("shortDescription", shortDescription);
        map.put("longDescription", longDescription);
        map.put("points", points);
        map.put("category", category.name());
        map.put("rarity", rarity.name());
        map.put("icon", icon);
        map.put("secret", secret);
        return map;
    }

    public static Achievement fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        for (Achievement achievement : values()) {
            if (achievement.getName().equals(name)) {
                return achievement;
            }
        }
        throw new IllegalArgumentException("Logro no encontrado: " + name);
    }

    // ========== MÉTODOS DE PRESENTACIÓN ==========

    public String toFormattedString() {
        return String.format("%s %s\n%s\nPuntos: %d | Categoría: %s | Rareza: %s",
            icon, name, shortDescription, points, category.getDisplayName(), rarity.getDisplayName());
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%d,%s,%s,%s,%b",
            name, shortDescription, longDescription, points, 
            category.name(), rarity.name(), icon, secret);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}