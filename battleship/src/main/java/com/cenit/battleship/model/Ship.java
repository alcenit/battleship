package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.ShipType;
import com.cenit.battleship.model.enums.Direction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un barco en el juego de batalla naval con tracking de daño por
 * segmento
 *
 */
public class Ship {

    private final ShipType type;
    private final List<Cell> positions;
    private int impactsReceived;
    private List<Coordinate> coordinates;
    private Direction direction;

    // Array para trackear qué segmentos están dañados
    private List<Boolean> segmentDamage;

    private final GameConfiguration config = GameConfiguration.getInstance();

    public Ship(ShipType type) {
        this.type = type;
        this.positions = new ArrayList<>();
        this.coordinates = new ArrayList<>();
        this.impactsReceived = 0;
        this.direction = Direction.HORIZONTAL;
        this.segmentDamage = new ArrayList<>();
    }

    /**
     * Obtiene todos los segmentos (coordenadas) que ocupa el barco Este es el
     * método que necesitas para la validación de colocación
     */
    public List<Coordinate> getSegments() {
        return Collections.unmodifiableList(coordinates);
    }

    /**
     * Calcula y devuelve los segmentos basados en posición inicial, dirección y
     * tamaño Útil para validación antes de colocar el barco
     */
    public List<Coordinate> calculateSegments(Coordinate start, Direction dir, int size) {
        List<Coordinate> calculatedSegments = new ArrayList<>();
        int dx = dir == Direction.HORIZONTAL ? 0 : 1;
        int dy = dir == Direction.HORIZONTAL ? 1 : 0;

        for (int i = 0; i < size; i++) {
            int x = start.getX() + i * dx;
            int y = start.getY() + i * dy;

            // Validar que la coordenada esté dentro del tablero
            if (x >= 0 && x < config.getBoardSize()
                    && y >= 0 && y < config.getBoardSize()) {
                calculatedSegments.add(new Coordinate(x, y));
            } else {
                // Si alguna coordenada está fuera, devolver lista vacía
                return new ArrayList<>();
            }
        }

        return calculatedSegments;
    }

    /**
     * Coloca el barco en una posición específica con dirección
     */
    public boolean place(Coordinate start, Direction direction) {
        List<Coordinate> newSegments = calculateSegments(start, direction, type.getSize());

        if (newSegments.size() != type.getSize()) {
            System.err.println("❌ No se puede colocar " + type.getName()
                    + " en " + start.aNotacion() + " - coordenadas inválidas");
            return false;
        }

        this.coordinates = newSegments;
        this.direction = direction;
        initializeSegmentDamage();

        System.out.println("📍 " + type.getName() + " colocado en "
                + coordinates.get(0).aNotacion() + " a "
                + coordinates.get(coordinates.size() - 1).aNotacion()
                + " (" + direction + ")");
        return true;
    }

    /**
     * Sobrecarga para colocar con coordenadas x,y
     */
    public boolean place(int startX, int startY, Direction direction) {
        return place(new Coordinate(startX, startY), direction);
    }

    /**
     * Obtiene todas las coordenadas adyacentes al barco (para validación de no
     * contacto)
     */
    public List<Coordinate> getAdjacentCoordinates() {
        List<Coordinate> adjacent = new ArrayList<>();

        for (Coordinate segment : coordinates) {
            // Revisar las 8 direcciones alrededor de cada segmento
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue; // Saltar el segmento mismo
                    }
                    int adjX = segment.getX() + dx;
                    int adjY = segment.getY() + dy;

                    // Verificar límites del tablero
                    if (adjX >= 0 && adjX < config.getBoardSize()
                            && adjY >= 0 && adjY < config.getBoardSize()) {

                        Coordinate adjCoord = new Coordinate(adjX, adjY);

                        // Solo agregar si no es parte del barco y no está ya en la lista
                        if (!coordinates.contains(adjCoord) && !adjacent.contains(adjCoord)) {
                            adjacent.add(adjCoord);
                        }
                    }
                }
            }
        }

        return adjacent;
    }

    /**
     * Verifica si este barco se superpone con otro barco
     */
    public boolean overlapsWith(Ship other) {
        for (Coordinate myCoord : coordinates) {
            for (Coordinate otherCoord : other.getSegments()) {
                if (myCoord.equals(otherCoord)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si este barco está demasiado cerca de otro barco (adyacente)
     */
    public boolean isTooCloseTo(Ship other) {
        List<Coordinate> myAdjacent = getAdjacentCoordinates();
        for (Coordinate otherCoord : other.getSegments()) {
            if (myAdjacent.contains(otherCoord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el barco puede ser colocado en la posición especificada Sin
     * superponerse o estar demasiado cerca de otros barcos
     */
    public boolean canBePlacedAt(Coordinate start, Direction direction, List<Ship> otherShips) {
        // Calcular segmentos potenciales
        List<Coordinate> potentialSegments = calculateSegments(start, direction, type.getSize());

        // Verificar que todas las coordenadas sean válidas
        if (potentialSegments.size() != type.getSize()) {
            return false;
        }

        // Crear un barco temporal para validación
        Ship tempShip = new Ship(this.type);
        tempShip.coordinates = potentialSegments;
        tempShip.direction = direction;

        // Verificar contra otros barcos
        for (Ship otherShip : otherShips) {
            if (tempShip.overlapsWith(otherShip)) {
                return false;
            }
            if (tempShip.isTooCloseTo(otherShip)) {
                return false;
            }
        }

        return true;
    }

    // ========== POSICIONAMIENTO ==========
    /**
     * Establece la posición del barco y inicializa el tracking de daño
     *
     * @param coordinates Lista de coordenadas
     * @param direction Dirección explícita (opcional - si es null, se calcula
     * automáticamente)
     */
    public void setPosition(List<Coordinate> coordinates, Direction direction) {
        if (coordinates == null) {
            throw new IllegalArgumentException("La lista de coordenadas no puede ser nula");
        }

        if (coordinates.size() != type.getSize()) {
            throw new IllegalArgumentException(
                    "El barco " + type.getName() + " requiere " + type.getSize()
                    + " coordenadas, pero se proporcionaron " + coordinates.size()
            );
        }

        // Validar que las coordenadas sean consecutivas
        if (!areCoordinatesConsecutive(coordinates)) {
            throw new IllegalArgumentException("Las coordenadas del barco deben ser consecutivas");
        }

        this.coordinates = new ArrayList<>(coordinates);
        initializeSegmentDamage();

        // USAR LA DIRECCIÓN PROPORCIONADA O CALCULARLA SI ES NULL
        if (direction != null) {
            this.direction = direction;
        } else {
            determineDirection(coordinates);
        }

        System.out.println("📍 Barco " + type.getName() + " posicionado en "
                + coordinates.get(0).aNotacion() + " a "
                + coordinates.get(coordinates.size() - 1).aNotacion()
                + " (" + this.direction + ")");
    }

    // Sobrecarga para mantener compatibilidad
    public void setPosition(List<Coordinate> coordinates) {
        setPosition(coordinates, null); // Usar dirección calculada
    }

    /**
     * Establece la dirección del barco explícitamente
     */
    public void setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }
        this.direction = direction;
        System.out.println("🔄 Dirección de " + type.getName() + " establecida a: " + direction);
    }

    /**
     * Inicializa el array de daño por segmento (todos false inicialmente)
     */
    private void initializeSegmentDamage() {
        segmentDamage = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            segmentDamage.add(false);
        }
    }

    /**
     * Determina la dirección del barco basado en sus coordenadas
     */
    /**
     * Determina la dirección del barco basado en sus coordenadas
     */
    private void determineDirection(List<Coordinate> coordinates) {
        if (coordinates.size() <= 1) {
            this.direction = Direction.HORIZONTAL;
            return;
        }

        Coordinate first = coordinates.get(0);
        Coordinate second = coordinates.get(1);

        // LÓGICA CORREGIDA - debe coincidir con GameController
        if (first.getX() == second.getX()) {
            this.direction = Direction.HORIZONTAL; // Misma fila = HORIZONTAL
        } else if (first.getY() == second.getY()) {
            this.direction = Direction.VERTICAL;   // Misma columna = VERTICAL
        } else {
            throw new IllegalArgumentException("El barco debe estar en línea recta horizontal o vertical");
        }

        System.out.println("🧭 Dirección calculada para " + type.getName() + ": " + this.direction);
    }

    /**
     * Verifica si las coordenadas son consecutivas
     */
    private boolean areCoordinatesConsecutive(List<Coordinate> coordinates) {
        if (coordinates.size() <= 1) {
            return true;
        }

        boolean isHorizontal = true;
        boolean isVertical = true;

        int baseX = coordinates.get(0).getX();
        int baseY = coordinates.get(0).getY();

        for (int i = 1; i < coordinates.size(); i++) {
            Coordinate current = coordinates.get(i);

            // Para ser horizontal, todas las X deben ser iguales y las Y consecutivas
            if (isHorizontal) {
                if (current.getX() != baseX || current.getY() != baseY + i) {
                    isHorizontal = false;
                }
            }

            // Para ser vertical, todas las Y deben ser iguales y las X consecutivas
            if (isVertical) {
                if (current.getY() != baseY || current.getX() != baseX + i) {
                    isVertical = false;
                }
            }

            if (!isHorizontal && !isVertical) {
                return false;
            }
        }

        return true;
    }

    // ========== SISTEMA DE DAÑO Y REPARACIÓN ==========
    /**
     * Registra un impacto en una coordenada específica
     *
     * @param coord Coordenada impactada
     * @return true si el impacto fue registrado exitosamente
     */
    public boolean registerHitAtCoordinate(Coordinate coord) {
        for (int i = 0; i < coordinates.size(); i++) {
            if (coordinates.get(i).equals(coord)) {
                if (!segmentDamage.get(i)) {
                    segmentDamage.set(i, true);
                    impactsReceived++;

                    System.out.println("💥 Impacto en " + type.getName() + " en "
                            + coord.aNotacion() + " (" + (impactsReceived) + "/" + type.getSize() + ")");

                    if (isSunk()) {
                        System.out.println("💀 " + type.getName() + " HUNDIDO!");
                    }
                    return true;
                } else {
                    System.out.println("⚠️ Impacto repetido en " + type.getName() + " en " + coord.aNotacion());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Registra un impacto general (sin coordenada específica)
     */
    public void recordImpact() {
        if (!isSunk()) {
            // Encontrar el primer segmento no dañado
            for (int i = 0; i < segmentDamage.size(); i++) {
                if (!segmentDamage.get(i)) {
                    segmentDamage.set(i, true);
                    impactsReceived++;

                    System.out.println("💥 Impacto en " + type.getName()
                            + " (" + impactsReceived + "/" + type.getSize() + ")");

                    if (isSunk()) {
                        System.out.println("💀 " + type.getName() + " HUNDIDO!");
                    }
                    return;
                }
            }
        }
    }

    /**
     * Repara una parte específica del barco en la coordenada dada
     *
     * @param position Coordenada a reparar
     * @return true si la reparación fue exitosa
     */
    public boolean repairAtCoordinate(Coordinate position) {
        if (!isPlaced()) {
            System.err.println("❌ No se puede reparar: el barco no está colocado");
            return false;
        }

        if (!occupiesCoordinate(position)) {
            System.err.println("❌ No se puede reparar: la coordenada " + position.aNotacion()
                    + " no pertenece a este barco");
            return false;
        }

        // Buscar la posición en las coordenadas del barco
        for (int i = 0; i < coordinates.size(); i++) {
            if (coordinates.get(i).equals(position)) {
                // Verificar si esta parte está dañada
                if (segmentDamage.get(i)) {
                    // Reparar este segmento
                    segmentDamage.set(i, false);
                    impactsReceived--;

                    System.out.println("🔧 Parte reparada: " + type.getName() + " en "
                            + position.aNotacion() + " | Impactos restantes: "
                            + impactsReceived + "/" + type.getSize());
                    return true;
                } else {
                    System.out.println("ℹ️ La parte en " + position.aNotacion() + " ya está intacta");
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Repara completamente el barco (todas las partes)
     *
     * @return true si se reparó al menos una parte
     */
    public boolean fullRepair() {
        if (impactsReceived == 0) {
            System.out.println("ℹ️ " + type.getName() + " ya está completamente reparado");
            return false;
        }

        int repairedCount = impactsReceived;
        impactsReceived = 0;

        // Resetear todos los segmentos
        for (int i = 0; i < segmentDamage.size(); i++) {
            segmentDamage.set(i, false);
        }

        System.out.println("🔧 " + type.getName() + " completamente reparado: "
                + repairedCount + " partes restauradas");
        return true;
    }

    /**
     * Verifica si una parte específica del barco está dañada
     *
     * @param position Coordenada a verificar
     * @return true si esa parte está dañada
     */
    public boolean isPartDamaged(Coordinate position) {
        if (!occupiesCoordinate(position)) {
            return false;
        }

        for (int i = 0; i < coordinates.size(); i++) {
            if (coordinates.get(i).equals(position)) {
                return segmentDamage.get(i);
            }
        }
        return false;
    }

    /**
     * Verifica si el barco puede ser reparado (tiene partes dañadas)
     *
     * @return true si tiene al menos una parte dañada
     */
    public boolean canBeRepaired() {
        return impactsReceived > 0 && !isSunk();
    }

    // ========== MÉTODOS DE CONSULTA ==========
    /**
     * Verifica si el barco está hundido
     */
    public boolean isSunk() {
        return impactsReceived >= type.getSize();
    }

    /**
     * Verifica si el barco está colocado
     */
    public boolean isPlaced() {
        return !positions.isEmpty() || !coordinates.isEmpty();
    }

    /**
     * Verifica si el barco ocupa una coordenada específica
     */
    public boolean occupiesCoordinate(Coordinate coord) {
        return coordinates.contains(coord);
    }

    /**
     * Obtiene el porcentaje de daño del barco
     */
    public double getDamagePercentage() {
        return (double) impactsReceived / type.getSize();
    }

    /**
     * Obtiene el porcentaje de integridad del barco
     */
    public double getIntegrityPercentage() {
        return 1.0 - getDamagePercentage();
    }

    /**
     * Obtiene la cantidad de partes reparables (dañadas)
     */
    public int getRepairablePartsCount() {
        return impactsReceived;
    }

    /**
     * Obtiene la cantidad de partes intactas
     */
    public int getIntactPartsCount() {
        return type.getSize() - impactsReceived;
    }

    /**
     * Obtiene las coordenadas de las partes dañadas
     */
    public List<Coordinate> getDamagedCoordinates() {
        List<Coordinate> damaged = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            if (segmentDamage.get(i)) {
                damaged.add(coordinates.get(i));
            }
        }
        return Collections.unmodifiableList(damaged);
    }

    /**
     * Obtiene las coordenadas de las partes intactas
     */
    public List<Coordinate> getIntactCoordinates() {
        List<Coordinate> intact = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            if (!segmentDamage.get(i)) {
                intact.add(coordinates.get(i));
            }
        }
        return Collections.unmodifiableList(intact);
    }

    /**
     * Obtiene el estado visual de cada segmento del barco
     */
    public String[] getVisualState() {
        String[] visual = new String[type.getSize()];
        for (int i = 0; i < type.getSize(); i++) {
            if (i < segmentDamage.size() && segmentDamage.get(i)) {
                visual[i] = "💥"; // Segmento impactado
            } else {
                visual[i] = "🚢"; // Segmento intacto
            }
        }
        return visual;
    }

    /**
     * Obtiene información detallada del estado del barco
     */
    public String getDamageStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getName()).append(": ");

        for (int i = 0; i < type.getSize(); i++) {
            if (i < segmentDamage.size() && segmentDamage.get(i)) {
                sb.append("💥");
            } else {
                sb.append("🚢");
            }
        }

        sb.append(" (").append(impactsReceived).append("/").append(type.getSize()).append(")");
        sb.append(" | Integridad: ").append(String.format("%.0f", getIntegrityPercentage() * 100)).append("%");

        return sb.toString();
    }

    // ========== MÉTODOS DEL SISTEMA ORIGINAL ==========
    /**
     * Agrega una celda a la posición del barco (método original)
     */
    public void addPosition(Cell cell) {
        if (positions.size() >= type.getSize()) {
            throw new IllegalStateException("El barco ya tiene todas sus posiciones");
        }
        positions.add(cell);
        cell.setShip(this);
    }

    /**
     * Obtiene la coordenada de inicio del barco
     */
    public Coordinate getStartCoordinate() {
        if (coordinates.isEmpty()) {
            return null;
        }
        return coordinates.get(0);
    }

    /**
     * Obtiene la coordenada final del barco
     */
    public Coordinate getEndCoordinate() {
        if (coordinates.isEmpty()) {
            return null;
        }
        return coordinates.get(coordinates.size() - 1);
    }

    // ========== GETTERS ==========
    public ShipType getType() {
        return type;
    }

    public int getImpactsReceived() {
        return impactsReceived;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<Coordinate> getCoordinates() {
        return Collections.unmodifiableList(coordinates);
    }

    public List<Cell> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public int getSize() {
        return type.getSize();
    }

    public String getName() {
        return type.getName();
    }

    // ========== RESET ==========
    /**
     * Reinicia el barco a su estado inicial
     */
    public void reset() {
        for (Cell cell : positions) {
            cell.reset();
        }
        positions.clear();
        coordinates.clear();
        impactsReceived = 0;
        direction = Direction.HORIZONTAL;
        segmentDamage.clear();
    }

    // ========== TO STRING ==========
    @Override
    public String toString() {
        return String.format("%s [Tamaño: %d, Hundido: %s, Daño: %.0f%%, Dirección: %s]",
                type.getName(), type.getSize(), isSunk() ? "Sí" : "No",
                getDamagePercentage() * 100, direction);
    }

    /**
     * Obtiene información detallada del barco
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Barco: ").append(type.getName()).append("\n");
        sb.append("Tamaño: ").append(type.getSize()).append("\n");

        if (!coordinates.isEmpty()) {
            sb.append("Posición: ").append(getStartCoordinate().aNotacion())
                    .append(" a ").append(getEndCoordinate().aNotacion())
                    .append(" (").append(direction).append(")\n");
        } else {
            sb.append("Posición: No colocada\n");
        }

        sb.append("Estado: ");
        if (isSunk()) {
            sb.append("💀 HUNDIDO");
        } else {
            sb.append(String.format("%.0f%% daño", getDamagePercentage() * 100));
            sb.append(" - ").append(getIntactPartsCount())
                    .append("/").append(type.getSize()).append(" intactos");
        }

        sb.append("\nSegmentos: ").append(getDamageStatus());

        return sb.toString();
    }
}
