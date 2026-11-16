
package com.minigta;

public class TrafficCar extends Car {

    // Index of the waypoint this car is currently targeting
    private int targetIndex;
    private boolean playerControlled = false;
    private boolean parked = false;
    private boolean driverSpawned = false;

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

    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public void setPlayerControlled(boolean playerControlled) {
        this.playerControlled = playerControlled;
    }

    public boolean isParked() {
        return parked;
    }

    public void setParked(boolean parked) {
        this.parked = parked;
    }

    public boolean isDriverSpawned() {
        return driverSpawned;
    }

    public void setDriverSpawned(boolean driverSpawned) {
        this.driverSpawned = driverSpawned;
    }
}
