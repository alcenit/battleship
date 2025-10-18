/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

/*import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 */
import java.util.*;

public class SkillSystem {

    private Map<Skill, Integer> availableSkills;
    private Map<Skill, Integer> remainingUses;
    private int skillPoints;
    private boolean isPlayer;

    private Set<Ship> countedSunkShips;

// Diferentes valores de puntos
    private static final int IMPACT_POINTS = 1;
    private static final int SINKING_POINTS = 3;
    private static final int EXTRA_SKILL_POINTS = 2; // Por usar habilidad exitosamente

    public SkillSystem(boolean isPlayer) {
        this.isPlayer = isPlayer;
        this.availableSkills = new HashMap<>();
        this.remainingUses = new HashMap<>();
        this.countedSunkShips = new HashSet<>();
        this.skillPoints = 0;
        initializeSkills();
    }

    // ========== INICIALIZACIÓN PRIVADA ==========
    private void initializeSkills() {
        availableSkills.clear();
        remainingUses.clear();

        if (isPlayer) {
            // Jugador tiene acceso a todas las habilidades
            for (Skill skill : Skill.values()) {
                availableSkills.put(skill, skill.getCost());
                remainingUses.put(skill, skill.getMaxUsesPerGame());
            }
        } else {
            // CPU tiene habilidades limitadas
            availableSkills.put(Skill.SONAR, Skill.SONAR.getCost());
            availableSkills.put(Skill.RADAR, Skill.RADAR.getCost());
            remainingUses.put(Skill.SONAR, 1);
            remainingUses.put(Skill.RADAR, 1);
        }

    }

    // ========== MÉTODO SETSKILLPOINTS ==========
    /**
     * Establece directamente los puntos de habilidad
     *
     * @param points Los puntos a establecer
     */
    public void setSkillPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Los puntos de habilidad no pueden ser negativos");
        }
        this.skillPoints = points;
        System.out.println("Puntos de habilidad establecidos a: " + points);
    }

    /**
     * Establece puntos de habilidad con validación de máximo
     *
     * @param points Los puntos a establecer
     * @param maxPoints El máximo permitido (opcional)
     */
    public void setSkillPoints(int points, int maxPoints) {
        if (points < 0) {
            throw new IllegalArgumentException("Los puntos de habilidad no pueden ser negativos");
        }
        this.skillPoints = Math.min(points, maxPoints);
        System.out.println("Puntos de habilidad establecidos a: " + this.skillPoints + " (máximo: " + maxPoints + ")");
    }

    public boolean canUseSkill(Skill skill) {
        return availableSkills.containsKey(skill)
                && skillPoints >= availableSkills.get(skill)
                && remainingUses.getOrDefault(skill, 0) > 0;
    }

    public boolean useSkill(Skill skill) {
        if (!canUseSkill(skill)) {
            return false;
        }

        int cost = availableSkills.get(skill);
        if (skillPoints >= cost) {
            skillPoints -= cost;

            // Reducir usos restantes
            int remaining = remainingUses.get(skill) - 1;
            remainingUses.put(skill, remaining);

            // Si se agotaron los usos, remover la habilidad
            if (remaining <= 0) {
                availableSkills.remove(skill);
                remainingUses.remove(skill);
            }

            System.out.println("Habilidad " + skill.getName() + " usada. Puntos restantes: " + skillPoints);
            return true;
        }

        return false;
    }

    public void gainSkillPoints(int points) {
        this.skillPoints += EXTRA_SKILL_POINTS;
    }

    public void gainHitPoints() {

        this.skillPoints += IMPACT_POINTS;
        System.out.println("+1 punto por impacto! Total: " + skillPoints);
    }

    public void gainSinkPoints() {
        this.skillPoints += SINKING_POINTS;
        System.out.println("+3 puntos por hundimiento! Total: " + skillPoints);
    }

    public void registerSunkenShip(Ship ship) {
        if (!countedSunkShips.contains(ship)) {
            countedSunkShips.add(ship);
            gainSinkPoints();
        }
    }

// Getters
    public int getSkillPoints() {
        return skillPoints;
    }

    public Map<Skill, Integer> getAvailableSkills() {
        return new HashMap<>(availableSkills);
    }

    public Map<Skill, Integer> getRemainingUses() {
        return new HashMap<>(remainingUses);
    }

    public void earnHitPoints() {
        // En la clase SistemaHabilidades (PlayerSkills en tu código)
        this.skillPoints += 1;
        System.out.println("+1 punto por impacto! Total: " + skillPoints);
    }

    public void earnSinkingPoints() {
        // En la clase SistemaHabilidades
        this.skillPoints += 3;
        System.out.println("+3 puntos por hundimiento! Total: " + skillPoints);
    }

    public void earnSkillPoints(int points) {
        if (points > 0) {
            this.skillPoints += points;
            System.out.println("+" + points + " puntos de habilidad! Total: " + skillPoints);
        }
    }

    // ========== MÉTODOS DE ACCESO MEJORADOS ==========
    public int getRemainingUses(Skill skill) {
        return remainingUses.getOrDefault(skill, 0);
    }

    public int getSkillCost(Skill skill) {
        return availableSkills.getOrDefault(skill, Integer.MAX_VALUE);
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========
    public boolean hasSkill(Skill skill) {
        return availableSkills.containsKey(skill);
    }

    // ========== MÉTODOS DE GESTIÓN DE HABILIDADES ==========
    public void addSkill(Skill skill, int uses) {
        availableSkills.put(skill, skill.getCost());
        remainingUses.put(skill, uses);
    }

    public void removeSkill(Skill skill) {
        availableSkills.remove(skill);
        remainingUses.remove(skill);
    }

    public void resetSkillUses(Skill skill) {
        if (remainingUses.containsKey(skill)) {
            remainingUses.put(skill, skill.getMaxUsesPerGame());
        }
    }

    // ========== MÉTODOS DE REGISTRO DE HUNDIMIENTOS ==========
    public void registerSunkShip(Ship ship) {
        if (!countedSunkShips.contains(ship)) {
            countedSunkShips.add(ship);
            earnSinkingPoints();
        }
    }

    // ========== MÉTODOS DE REINICIO ==========
    public void reset() {
        this.skillPoints = 0;
        this.countedSunkShips.clear();
        initializeSkills();
    }

    public void resetPointsOnly() {
        this.skillPoints = 0;
        System.out.println("Puntos de habilidad reiniciados a 0");
    }

    // ========== MÉTODOS DE INFORMACIÓN ==========
    public String getSkillInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Puntos de habilidad: ").append(skillPoints).append("\n");
        sb.append("Habilidades disponibles:\n");

        for (Map.Entry<Skill, Integer> entry : availableSkills.entrySet()) {
            Skill skill = entry.getKey();
            int cost = entry.getValue();
            int remaining = remainingUses.getOrDefault(skill, 0);

            sb.append("  - ").append(skill.getName())
                    .append(" (Costo: ").append(cost)
                    .append(", Usos: ").append(remaining)
                    .append(")\n");
        }

        return sb.toString();
    }

}
