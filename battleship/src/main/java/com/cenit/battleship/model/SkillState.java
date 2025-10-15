/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

import java.util.Map;

/**
 *
 * @author Usuario
 */
public class SkillState {

    private int skillPoints;
    private Map<Skill, Integer> availableSkills;
    private Map<Skill, Integer> remainingUses;

    // Getters y Setters
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    
    public Map<Skill, Integer> getAvailableSkills() { return availableSkills; }
    public void setAvailableSkills(Map<Skill, Integer> availableSkills) { this.availableSkills = availableSkills; }
    
    public Map<Skill, Integer> getRemainingUses() { return remainingUses; }
    public void setRemainingUses(Map<Skill, Integer> remainingUses) { this.remainingUses = remainingUses; }
}    

