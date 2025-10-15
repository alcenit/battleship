/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.sevices.dto;

import java.util.Map;

/**
 *
 * @author Usuario
 */
public class SkillDTO {
 private int skillPoints;
    private Map<String, Integer> habilidadesDisponibles; // String en lugar de Enum
    private Map<String, Integer> usosRestantes;

    public SkillDTO() {}

    // Getters y Setters
    public int getPuntosHabilidad() { return skillPoints; }
    public void setPuntosHabilidad(int puntosHabilidad) { this.skillPoints = puntosHabilidad; }
    
    public Map<String, Integer> getHabilidadesDisponibles() { return habilidadesDisponibles; }
    public void setHabilidadesDisponibles(Map<String, Integer> habilidadesDisponibles) { this.habilidadesDisponibles = habilidadesDisponibles; }
    
    public Map<String, Integer> getUsosRestantes() { return usosRestantes; }
    public void setUsosRestantes(Map<String, Integer> usosRestantes) { this.usosRestantes = usosRestantes; }    
}
