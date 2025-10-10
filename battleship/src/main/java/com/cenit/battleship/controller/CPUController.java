/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cenit.battleship.controller;

import com.cenit.battleship.model.Board;
import com.cenit.battleship.model.Coordinate;
import com.cenit.battleship.model.ShotResult;
import com.cenit.battleship.model.enums.Direction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class CPUController {
    
    public enum Difficulty {
        EASY,      // Disparos completamente aleatorios
        NORMAL,     // Estrategia básica de búsqueda y destrucción
        HARD,    // Estrategia avanzada con patrones
        EXPERT     // Uso de probabilidades y análisis del tablero
    }
    
    private Difficulty difficulty;
    private Board boardPlayer;
    private List<Coordinate>shotsFired;
    private List<Coordinate> shotsPending;
    private boolean inSearchMode;
    private Coordinate lastImpact;
    private Direction actualDirection;
    private boolean establishedAddress;
    
    // Para modo EXPERT: mapa de probabilidades
    private int[][] mapOdds;
    
    public CPUController(Difficulty difficulty, Board boardPlayer) {
        this.difficulty = difficulty;
        this.boardPlayer = boardPlayer;
        this.shotsFired = new ArrayList<>();
        this.shotsPending = new ArrayList<>();
        this.inSearchMode = true;
        this.mapOdds = new int[10][10];
        
        if (difficulty == Difficulty.EXPERT) {
            initializeMapProbabilities();
        }
    }
    
    public Coordinate generateShot() {
        Coordinate shot;
        
        switch (difficulty) {
            case EASY:
                shot = generateEasyShot();
                break;
            case NORMAL:
                shot = generateNormalShot();
                break;
            case HARD:
                shot = generateHardShot();
                break;
            case EXPERT:
                shot = generateExpertShot();
                break;
            default:
                shot = generateNormalShot();
        }
        
        
shotsFired.add(shot);
        return shot;
    }
    
    public void processResult(Coordinate shot, ShotResult result) {
        if (result.impact()) {
            // Cambiar a modo destrucción si no estábamos en él
            if (inSearchMode) {
                inSearchMode = false;
                lastImpact = shot;
                establishedAddress = false;
                generateAdjacentShots(shot);
            } else {
                // Continuar en la dirección actual
                extendShot(shot);
            }
            
            if (result.sunk()) {
                // Volver a modo búsqueda
                inSearchMode = true;
                shotsPending.clear();
                lastImpact = null;
                establishedAddress = false;
                
                if (difficulty == Difficulty.EXPERT) {
                    updateSunkenProbabilitiesMap();
                }
            }
        } else if (!inSearchMode && shotsPending.isEmpty()) {
            // Cambiar de dirección si fallamos en modo destrucción
            changeDirection();
        }
        
        if (difficulty == Difficulty.EXPERT) {
            updateProbabilitiesMap(shot, result);
        }
    }
    
    // ========== ESTRATEGIAS POR DIFICULTAD ==========
    
    private Coordinate generateEasyShot() {
        // Disparo completamente aleatorio
        return generateRandomCoordinate();
    }
    
    private Coordinate generateNormalShot() {
        // Priorizar disparos pendientes, sino modo búsqueda
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        if (inSearchMode) {
            return generateShotSearch();
        } else {
            return generateShotDestruction();
        }
    }
    
    private Coordinate generateHardShot() {
        // Estrategia más agresiva en modo destrucción
        if (!shotsPending.isEmpty()) {
            // Ordenar disparos pendientes por prioridad
            ordenarDisparosPendientes();
            return shotsPending.remove(0);
        }
        
        if (inSearchMode) {
            return generateShotSearchWithPattern();
        } else {
            return generateShotDestructionAgresive();
        }
    }
    
    private Coordinate generateExpertShot() {
        // Usar mapa de probabilidades
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        return generateProbabilityShot();
    }
    
    // ========== MÉTODOS DE ESTRATEGIA ==========
    
    private Coordinate generateShotSearch() {
        // Disparo en patrón de tablero de ajedrez (más eficiente para encontrar barcos)
        List<Coordinate> candidates = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Patrón de ajedrez: solo casillas donde (i+j) es par
                if ((i + j) % 2 == 0) {
                    Coordinate coord = new Coordinate(i, j);
                    if (!
shotsFired.contains(coord)) {
                        candidates.add(coord);
                    }
                }
            }
        }
        
        if (!candidates.isEmpty()) {
            return candidates.get((int) (Math.random() * candidates.size()));
        }
        
        // Si no hay candidatos en el patrón, buscar cualquier casilla disponible
        return generateRandomCoordinate();
    }
    
    private Coordinate generateShotSearchWithPattern() {
        // Patrón más avanzado: espiral o por secciones
        List<Coordinate> candidates = new ArrayList<>();
        
        // Buscar en áreas donde es más probable encontrar barcos grandes
        for (int i = 2; i < 8; i++) {
            for (int j = 2; j < 8; j++) {
                Coordinate coord = new Coordinate(i, j);
                if (!
shotsFired.contains(coord)) {
                    candidates.add(coord);
                }
            }
        }
        
        if (!candidates.isEmpty()) {
            return candidates.get((int) (Math.random() * candidates.size()));
        }
        
        return generateRandomCoordinate();
    }
    
    private Coordinate generateShotDestruction() {
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        // Generar disparos alrededor del último impacto
        return generateShotAround(lastImpact);
    }
    
    private Coordinate generateShotDestructionAgresive() {
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        // Estrategia más agresiva: probar en línea recta
        if (!establishedAddress) {
            return setDirection();
        } else {
            return continueInDirection();
        }
    }
    
    private Coordinate generateProbabilityShot() {
        // Encontrar la casilla con mayor probabilidad
        int maxProb = -1;
        List<Coordinate> betterOptions = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Coordinate coord = new Coordinate(i, j);
                if (!
      shotsFired.contains(coord)) {
                    if (mapOdds[i][j] > maxProb) {
                        maxProb = mapOdds[i][j];
                        betterOptions.clear();
                        betterOptions.add(coord);
                    } else if (mapOdds[i][j] == maxProb) {
                        betterOptions.add(coord);
                    }
                }
            }
        }
        
        if (!betterOptions.isEmpty()) {
            return betterOptions.get((int) (Math.random() * betterOptions.size()));
        }
        
        return generateRandomCoordinate();
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private void generateAdjacentShots(Coordinate impact) {
        int x = impact.x();
        int y = impact.y();
        
        // Generar disparos en las 4 direcciones
        addPendingShotIfValid(x - 1, y);
        addPendingShotIfValid(x + 1, y);
        addPendingShotIfValid(x, y - 1);
        addPendingShotIfValid(x, y + 1);
    }
    
    private void extendShot(Coordinate impact) {
        if (!establishedAddress) {
            setDirectionFromImpacts();
        }
        
        if (establishedAddress) {
            int x = impact.x();
            int y = impact.y();
            
            // Continuar en la dirección establecida
            switch (actualDirection) {
                case HORIZONTAL:
                    addPendingShotIfValid(x - 1, y);
                    addPendingShotIfValid(x + 1, y);
                    break;
                case VERTICAL:
                    addPendingShotIfValid(x, y - 1);
                    addPendingShotIfValid(x, y + 1);
                    break;
            }
        }
    }
    
    private void setDirectionFromImpacts() {
        // Lógica para determinar dirección basada en impactos consecutivos
        // Implementación simplificada
        actualDirection = Math.random() > 0.5 ? Direction.HORIZONTAL : Direction.VERTICAL;
        establishedAddress = true;
    }
    
    private Coordinate setDirection() {
        // Probar diferentes direcciones sistemáticamente
        List<Coordinate> options = new ArrayList<>();
        int x = lastImpact.x();
        int y = lastImpact.y();
        
        options.add(new Coordinate(x - 1, y));
        options.add(new Coordinate(x + 1, y));
        options.add(new Coordinate(x, y - 1));
        options.add(new Coordinate(x, y + 1));
        
        // Filtrar opciones válidas y no disparadas
        for (Coordinate coord : options) {
            if (isValidCoordinate(coord.x(), coord.y()) && 
                !
shotsFired.contains(coord)) {
                shotsPending.add(coord);
            }
        }
        
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        return generateRandomCoordinate();
    }
    
    private Coordinate continueInDirection() {
        int x = lastImpact.x();
        int y = lastImpact.y();
        
        // Continuar en la dirección actual
        switch (actualDirection) {
            case HORIZONTAL:
                // Probar ambos lados
                addPendingShotIfValid(x - 1, y);
                addPendingShotIfValid(x + 1, y);
                break;
            case VERTICAL:
                addPendingShotIfValid(x, y - 1);
                addPendingShotIfValid(x, y + 1);
                break;
        }
        
        if (!shotsPending.isEmpty()) {
            return shotsPending.remove(0);
        }
        
        return generateRandomCoordinate();
    }
    
    private void changeDirection() {
        if (actualDirection == Direction.HORIZONTAL) {
            actualDirection = Direction.VERTICAL;
        } else {
            actualDirection = Direction.HORIZONTAL;
        }
        
        // Regenerar disparos pendientes en nueva dirección
        shotsPending.clear();
        generateAdjacentShots(lastImpact);
    }
    
    private Coordinate generateShotAround(Coordinate center) {
        List<Coordinate> around = new ArrayList<>();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Saltar el centro
                if (dx != 0 && dy != 0) continue; // Saltar diagonales
                
                int nx = center.x() + dx;
                int ny = center.y() + dy;
                
                if (isValidCoordinate(nx, ny)) {
                    Coordinate coord = new Coordinate(nx, ny);
                    if (!
shotsFired.contains(coord)) {
                        around.add(coord);
                    }
                }
            }
        }
        
        if (!around.isEmpty()) {
            return around.get((int) (Math.random() * around.size()));
        }
        
        return generateRandomCoordinate();
    }
    
    // ========== MÉTODOS PARA MODO EXPERT ==========
    
    private void initializeMapProbabilities() {
        // Inicializar con distribución uniforme
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                mapOdds[i][j] = 1;
            }
        }
    }
    
    private void updateProbabilitiesMap(Coordinate shot, ShotResult result) {
        // Reducir probabilidad en casillas ya disparadas
        mapOdds[shot.x()][shot.y()] = 0;
        
        if (result.impact() && !result.sunk()) {
            // Aumentar probabilidad en casillas adyacentes
            increaseProbabilityAround(shot, 5);
        }
    }
    
    private void updateSunkenProbabilitiesMap() {
        // Cuando se hunde un barco, reducir probabilidad en el área circundante
        // ya que es menos probable que haya otro barco muy cerca
        if (lastImpact != null) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    int nx = lastImpact.x() + dx;
                    int ny = lastImpact.y() + dy;
                    
                    if (isValidCoordinate(nx, ny) && mapOdds[nx][ny] > 0) {
                        mapOdds[nx][ny] = Math.max(1, mapOdds[nx][ny] - 1);
                    }
                }
            }
        }
    }
    
    private void increaseProbabilityAround(Coordinate center, int incremento) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = center.x() + dx;
                int ny = center.y() + dy;
                
                if (isValidCoordinate(nx, ny)) {
                    mapOdds[nx][ny] += incremento;
                }
            }
        }
    }
    
    private void ordenarDisparosPendientes() {
        if (difficulty == Difficulty.EXPERT) {
            shotsPending.sort((c1, c2) -> 
                Integer.compare(mapOdds[c2.x()][c2.y()], 
                              mapOdds[c1.x()][c1.y()]));
        }
    }
    
    // ========== MÉTODOS UTILITARIOS ==========
    
    private Coordinate generateRandomCoordinate() {
        int x, y;
        Coordinate coord;
        
        do {
            x = (int) (Math.random() * 10);
            y = (int) (Math.random() * 10);
            coord = new Coordinate(x, y);
        } while (
shotsFired.contains(coord));
        
        return coord;
    }
    
    private void addPendingShotIfValid(int x, int y) {
        if (isValidCoordinate(x, y)) {
            Coordinate coord = new Coordinate(x, y);
            if (!
shotsFired.contains(coord) && !shotsPending.contains(coord)) {
                shotsPending.add(coord);
            }
        }
    }
    
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public void setDificultad(Difficulty dificulty) {
        this.difficulty = dificulty;
        if (dificulty == Difficulty.EXPERT && mapOdds == null) {
            initializeMapProbabilities();
        }
    }
    
    public Difficulty getDificulty() {
        return difficulty;
    }
    
    public void reset() {
        
shotsFired.clear();
        shotsPending.clear();
        inSearchMode = true;
        lastImpact = null;
        establishedAddress = false;
        
        if (mapOdds != null) {
            initializeMapProbabilities();
        }
    }
}
