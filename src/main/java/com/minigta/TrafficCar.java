package com.minigta;

public class TrafficCar extends Interactable {

    private double speed;
    private String direction;
    private String vehicleType;

    public TrafficCar(double worldX, double worldY, double speed, String direction, String vehicleType) {

        super(worldX, worldY, "trafficCar");

        this.speed = speed;
        this.direction = direction;
        this.vehicleType = vehicleType;
    }

    public void move(double deltaTime) {

        if (direction.equals("right")) {
            setWorldX(getWorldX() + speed * deltaTime);

        } else if (direction.equals("left")) {
            setWorldX(getWorldX() - speed * deltaTime);
        }
    }

    public double getSpeed() {
        return speed;
    }

    public String getDirection() {
        return direction;
    }

    public String getVehicleType() {
        return vehicleType;
    }
}
