package com.cenit.battleship.model;

/**
 * Representa una coordenada en el tablero de batalla naval
 *
 * @author Usuario
 */
public record Coordinate(int x, int y) {

    public Coordinate  {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            throw new IllegalArgumentException("Coordenada fuera del tablero: (" + x + ", " + y + ")");
        }
    }

    // Los records generan automáticamente:
    // public int x() { return x; }
    // public int y() { return y; }
    // Si necesitas métodos getX() y getY() explícitamente:
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Convierte notación de batalla naval (A1, B5, etc.) a Coordinate
     *
     * @param notacion La notación en formato letra+número
     * @return Coordinate correspondiente
     * @throws IllegalArgumentException si la notación es inválida
     */
    public static Coordinate desdeNotacion(String notacion) {
        if (notacion == null || notacion.length() < 2) {
            throw new IllegalArgumentException("Notación inválida: " + notacion);
        }

        try {
            char letra = Character.toUpperCase(notacion.charAt(0));
            String numeroStr = notacion.substring(1).replaceAll("[^0-9]", "");

            if (numeroStr.isEmpty()) {
                throw new IllegalArgumentException("No se encontró número en: " + notacion);
            }

            int x = letra - 'A';
            int y = Integer.parseInt(numeroStr) - 1;

            // Validar rango
            if (x < 0 || x >= 10 || y < 0 || y >= 10) {
                throw new IllegalArgumentException("Coordenada fuera de rango: " + notacion);
            }

            return new Coordinate(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inválido en notación: " + notacion, e);
        }
    }

    /**
     * Convierte la coordenada a notación de batalla naval
     *
     * @return Notación en formato letra+número (ej: "A1", "B5")
     */
    public String aNotacion() {
        char letra = (char) ('A' + x);
        return letra + String.valueOf(y + 1);
    }

    /**
     * Verifica si esta coordenada es adyacente a otra
     *
     * @param otra La otra coordenada
     * @return true si son adyacentes horizontal o verticalmente
     */
    public boolean esAdyacente(Coordinate otra) {
        int diffX = Math.abs(this.x - otra.x);
        int diffY = Math.abs(this.y - otra.y);
        return (diffX == 1 && diffY == 0) || (diffX == 0 && diffY == 1);
    }

    /**
     * Obtiene una coordenada desplazada en una dirección
     *
     * @param dx Desplazamiento en X
     * @param dy Desplazamiento en Y
     * @return Nueva coordenada desplazada
     */
    public Coordinate desplazar(int dx, int dy) {
        try {
            return new Coordinate(x + dx, y + dy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Desplazamiento fuera del tablero: (" + dx + ", " + dy + ")", e);
        }
    }

    @Override
    public String toString() {
        return "Coordinate{" + "x=" + x + ", y=" + y + "}";
    }
}
