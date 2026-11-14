
package com.minigta;

public class TrafficCar extends Car {

    // Index of the waypoint this car is currently targeting
    private int targetIndex;

    public TrafficCar(double worldX, double worldY,
                      double speed, String direction,
                      String vehicleType) {

        super(worldX, worldY, speed, direction, vehicleType);

        this.targetIndex = 0;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }
}
