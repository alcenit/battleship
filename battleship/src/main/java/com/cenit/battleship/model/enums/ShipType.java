
package com.cenit.battleship.model.enums;


public enum ShipType {

    CARRIER(5, "PORTAAVIONES"),
    BATTLESHIP(4, "ACORAZADO"),
    SUBMARINE(1, "SUBMARINO"),
    DESTROYER(2, "DESTRUCTOR"),
    CRUISER(3, "CRUCERO"),
    FRIGATE(4, "FRAGATA");

    private final int size;
    private final String name;

    ShipType(int size, String name) {
        this.size = size;
        this.name = name;
    }

    // Solo getters - inmutables
    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
