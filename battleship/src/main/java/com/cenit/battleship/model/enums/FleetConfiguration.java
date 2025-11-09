package com.cenit.battleship.model.enums;

import com.cenit.battleship.model.Ship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FleetConfiguration {
    private final List<Ship> ships;
    
    public FleetConfiguration(List<Ship> ships) {
        this.ships = ships;
    }
    
    public List<Ship> getShips() {
        return new ArrayList<>(ships); // Retorna copia para evitar modificaciones
    }
    
    public int getTotalShips() {
        return ships.size();
    }
    
    public int getTotalCells() {
        return ships.stream().mapToInt(Ship::getSize).sum();
    }
    
    // Configuraciones predefinidas de flotas
    public static final FleetConfiguration STANDARD = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.CARRIER),
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.SUBMARINE),
            new Ship(ShipType.DESTROYER)
        )
    );
    
    public static final FleetConfiguration SPECIAL = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.CARRIER),
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.SUBMARINE),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.SCOUT) // Barco especial adicional
        )
    );
    
    public static final FleetConfiguration TACTICAL = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.CARRIER),
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.SUBMARINE)
        )
    );
    
    public static final FleetConfiguration ASYMMETRIC = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.CARRIER),
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.DESTROYER)
        )
    );
    
    public static final FleetConfiguration MINIMAL = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.BATTLESHIP),
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.DESTROYER)
        )
    );
    
    public static final FleetConfiguration SWARM = new FleetConfiguration(
        Arrays.asList(
            new Ship(ShipType.CRUISER),
            new Ship(ShipType.SUBMARINE),
            new Ship(ShipType.SUBMARINE),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.DESTROYER),
            new Ship(ShipType.SCOUT),
            new Ship(ShipType.SCOUT)
        )
    );
}