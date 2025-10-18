/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.model;

/**
 *
 * @author Usuario
 */
public enum Skill {

    SONAR("Sonar", "Revela un área 3x3 del tablero enemigo", 2, 3, 14),
    RADAR("Radar", "Revela la posición de un barco aleatorio", 3, 2, 14),
    DRONE("Dron de Reconocimiento", "Explora una fila o columna completa", 2, 2, 14),
    GUIDED_MISSILE("Misil Guiado", "Disparo garantizado que no falla", 1, 1, 14),
    CLUSTER_BOMB("Bomba de Racimo", "Dispara en un patrón de cruz (5 casillas)", 2, 2, 14),
    JAMMING("Interferencia", "El enemigo pierde su próximo turno", 3, 1, 14),
    REPAIR("Reparación Rápida", "Repara una casilla dañada de tu barco", 2, 2, 14),
    CAMOUFLAGE("Camuflaje", "Tu barco se mueve a nueva posición", 4, 1, 14);

    private final String name;
    private final String description;
    private final int cost; // In skill points
    private final int usesPerGame;
    private final int maxUsesPerGame;

    Skill(String name, String description, int cost, int usesPerGame, int maxUsesPerGame) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.usesPerGame = usesPerGame;
        this.maxUsesPerGame = maxUsesPerGame;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCost() { return cost; }
    public int getUsesPerGame() { return usesPerGame; }
    public int getMaxUsesPerGame(){return maxUsesPerGame;}
}
