
package com.cenit.battleship.model;

import com.cenit.battleship.model.enums.ShipType;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class ShipState {

    private ShipType type;
    private int impactRecieved;
    private List<Coordinate> positions;

    // Getters y Setters
    public ShipType getType() {
        return type;
    }

    public void setType(ShipType type) {
        this.type = type;
    }

    public int getImpactsReceived() {
        return impactRecieved;
    }

    public void setImpactsReceived(int impactRecieved) {
        this.impactRecieved = impactRecieved;
    }

    public List<Coordinate> getPositions() {
        return positions;
    }

    public void setPositions(List<Coordinate> positions) {
        this.positions = positions;
    }

}
