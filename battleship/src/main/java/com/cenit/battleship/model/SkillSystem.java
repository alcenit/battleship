/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Usuario
 */
public class SkillSystem {

private Map<Skill, Integer> availableSkills;
private Map<Skill, Integer> remainingUses;
private int skillPoints;
private boolean isPlayer;

private Set<Ship> sunkenShipsCounted;

// Diferentes valores de puntos
private static final int IMPACT_POINTS = 1;
private static final int SINKING_POINTS = 3;
private static final int EXTRA_SKILL_POINTS = 2; // Por usar habilidad exitosamente


public SkillSystem(boolean isPlayer) {
this. isPlayer = isPlayer;
this. availableSkills = new HashMap<>();
this. remainingUses = new HashMap<>();
this.sunkenShipsCounted = new HashSet<>();
this. skillPoints = 0;
initializeSkills();
}

private void initializeSkills() {
if (isPlayer) {
// Player has access to all skills
for (Skill skill: Skill. values()) {
availableSkills. put(skill, skill. getCost());
remainingUses.put(skill, skill.getUsesPerGame());
}
} else {
// CPU has limited skills based on difficulty
availableSkills.put(Skill.SONAR, Skill.SONAR.getCost());
availableSkills.put(Skill.RADAR, Skill.RADAR.getCost());
remainingUses.put(Skill.SONAR, 1);
remainingUses.put(Skill.RADAR, 1);
}
}

public boolean canUseSkill(Skill skill) {
return availableSkills.containsKey(skill) &&
skillPoints >= skill.getCost() &&
remainingUses.getOrDefault(skill, 0) > 0;
}

public boolean useSkill(Skill skill) {
if (!canUseSkill(skill)) {
return false;
}

skillPoints -= skill.getCost();
remainingUses.put(skill, remainingUses.get(skill) - 1);

// If the skill has run out of uses, remove it
if (remainingUses.get(skill) <= 0) {
availableSkills.remove(skill);
}

return true;
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
        if (!sunkenShipsCounted.contains(ship)) {
            sunkenShipsCounted.add(ship);
            gainSinkPoints();
        }
    }  

// Getters
public int getSkillPoints() { return skillPoints; }
public Map<Skill, Integer> getAvailableSkills() {
return new HashMap<>(availableSkills);
}
public Map<Skill, Integer> getRemainingUses() {
return new HashMap<>(remainingUses);
}


public void earnHitPoints() {
    // En la clase SistemaHabilidades (PlayerSkills en tu código)
    this.skillPoints += 1; // O cualquier valor que decidas
}
public void earnSinkingPoints() {
    // En la clase SistemaHabilidades
    this.skillPoints += 3; // Más puntos por hundir un barco completo
}



public void reset() {
skillPoints = 0;
initializeSkills();
}
    
}
