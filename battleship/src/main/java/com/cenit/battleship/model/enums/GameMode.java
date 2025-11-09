package com.cenit.battleship.model.enums;

import com.cenit.battleship.model.Skill;
import java.util.Arrays;
import java.util.List;

public class GameMode {
    private final String name;
    private final String description;
    private final int skillPoints;
    private final FleetConfiguration fleetConfig;
    private final boolean timeLimited;
    private final int timeLimitMinutes;
    private final List<Skill> availableSkills;
    
    public GameMode(String name, String description, int skillPoints, 
                   FleetConfiguration fleetConfig, boolean timeLimited, 
                   int timeLimitMinutes, List<Skill> availableSkills) {
        this.name = name;
        this.description = description;
        this.skillPoints = skillPoints;
        this.fleetConfig = fleetConfig;
        this.timeLimited = timeLimited;
        this.timeLimitMinutes = timeLimitMinutes;
        this.availableSkills = availableSkills;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getSkillPoints() { return skillPoints; }
    public FleetConfiguration getFleetConfig() { return fleetConfig; }
    public boolean isTimeLimited() { return timeLimited; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public List<Skill> getAvailableSkills() { return availableSkills; }
    
    // Modos de juego predefinidos (estáticos)
    public static final GameMode CLASSIC = new GameMode(
        "Clásico",
        "Modo tradicional de Batalla Naval con flota estándar",
        4,
        FleetConfiguration.STANDARD,
        false,
        0,
        Arrays.asList(Skill.SONAR, Skill.RADAR)
    );
    
    public static final GameMode SPECIAL_FLEET = new GameMode(
        "Flota Especial",
        "Flotas con barcos especiales y habilidades mejoradas",
        6,
        FleetConfiguration.SPECIAL,
        false,
        0,
        Arrays.asList(Skill.SONAR, Skill.RADAR, Skill.DRONE)
    );
    
    public static final GameMode TACTICAL = new GameMode(
        "Táctico",
        "Enfocado en estrategia con amplio uso de habilidades",
        8,
        FleetConfiguration.TACTICAL,
        false,
        0,
        Arrays.asList(Skill.SONAR, Skill.RADAR, Skill.DRONE, 
                     Skill.GUIDED_MISSILE, Skill.CLUSTER_BOMB)
    );
    
    public static final GameMode ASYMMETRIC = new GameMode(
        "Asimétrico",
        "Flotas y habilidades diferentes para cada jugador",
        7,
        FleetConfiguration.ASYMMETRIC,
        false,
        0,
        Arrays.asList(Skill.SONAR, Skill.JAMMING, Skill.REPAIR, Skill.CLUSTER_BOMB)
    );
    
    public static final GameMode LIGHTNING = new GameMode(
        "Relámpago",
        "Partidas rápidas con tiempo limitado",
        6,
        FleetConfiguration.MINIMAL,
        true,
        10, // 10 minutos
        Arrays.asList(Skill.GUIDED_MISSILE, Skill.CLUSTER_BOMB, Skill.DRONE)
    );
    
    public static final GameMode SWARM = new GameMode(
        "Enjambre",
        "Muchos barcos pequeños, combate intenso",
        8,
        FleetConfiguration.SWARM,
        false,
        0,
        Arrays.asList(Skill.SONAR, Skill.RADAR, Skill.DRONE)
    );
    
    // Método para obtener todos los modos disponibles
    public static List<GameMode> getAllModes() {
        return Arrays.asList(CLASSIC, SPECIAL_FLEET, TACTICAL, 
                           ASYMMETRIC, LIGHTNING, SWARM);
    }
    
    // Método para buscar por nombre
    public static GameMode getByName(String name) {
        return getAllModes().stream()
            .filter(mode -> mode.getName().equals(name))
            .findFirst()
            .orElse(CLASSIC); // Valor por defecto
    }
    
    @Override
    public String toString() {
        return name;
    }
}