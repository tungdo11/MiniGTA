package com.minigta;

import javafx.scene.shape.Rectangle;

public class Car extends Interactable {

    private double speed;
    private String direction;
    private String vehicleType;
    private double angle;
    private double acceleration;
    private double maxSpeed;
    private double turnRate;
    private double lateralSpeed = 0;
    private double grip = 1.0;
    private boolean handbrakeActive = false;

    private static final double SPRITE_WIDTH = 23;
    private static final double SPRITE_HEIGHT = 40;
    private static final double SPRITE_SCALE = 1.8;

    // Hitbox dimensions in original sprite pixels
    private static final double HITBOX_WIDTH = 17;
    private static final double HITBOX_HEIGHT = 32;

    public Car(double worldX, double worldY, double speed,
               String direction, String vehicleType) {

        super(worldX, worldY, "car");

        this.speed = speed;
        this.direction = direction;
        this.vehicleType = vehicleType;
        this.angle = 0;
        this.acceleration = 100;
        this.maxSpeed = 300;
        this.turnRate = 120;
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

    public double getAngle() {
        return angle;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getTurnRate() {
        return turnRate;
    }

    public void accelerate(double deltaTime) {
        speed += acceleration * grip * deltaTime;

        if (speed > maxSpeed) {
            speed = maxSpeed;
        }
    }

    public void steerLeft(double deltaTime, boolean braking) {
        if (speed != 0) {
            double effectiveTurnRate =
                    turnRate / (1 + Math.abs(speed) / 150);

            effectiveTurnRate *= grip;

            if (braking && speed > 0) {
                effectiveTurnRate *= 1.25;
            }

            angle -= effectiveTurnRate * deltaTime;

            if (Math.abs(speed) > 60) {
                double slideFactor = handbrakeActive ? 0.35 : 0.12;
                lateralSpeed -= Math.abs(speed) * slideFactor * deltaTime;
            }
        }
    }

    public void steerRight(double deltaTime, boolean braking) {
        if (speed != 0) {
            double effectiveTurnRate =
                    turnRate / (1 + Math.abs(speed) / 150);

            effectiveTurnRate *= grip;

            if (braking && speed > 0) {
                effectiveTurnRate *= 1.25;
            }

            angle += effectiveTurnRate * deltaTime;

            if (Math.abs(speed) > 60) {
                double slideFactor = handbrakeActive ? 0.35 : 0.12;
                lateralSpeed += Math.abs(speed) * slideFactor * deltaTime;
            }
        }
    }

    public void move(double deltaTime) {
        double radians = Math.toRadians(angle);

        double forwardX = -Math.sin(radians);
        double forwardY = Math.cos(radians);

        double sidewaysX = Math.cos(radians);
        double sidewaysY = Math.sin(radians);

        double newX = getWorldX()
                + (forwardX * speed + sidewaysX * lateralSpeed) * deltaTime;

        double newY = getWorldY()
                + (forwardY * speed + sidewaysY * lateralSpeed) * deltaTime;

        setWorldX(newX);
        setWorldY(newY);

        // Grip gradually reduces sideways sliding.
        double slideResistance = handbrakeActive ? 0.5 : 4.0;
        lateralSpeed *= Math.max(0, 1 - slideResistance * grip * deltaTime);
    }

    public void brake(double deltaTime) {
        speed -= 200 * deltaTime;

        if (speed < 0) {
            speed = 0;
        }
    }

    public void applyFriction(double deltaTime) {
        double friction = 30;

        if (speed > 0) {
            speed -= friction * deltaTime;

            if (speed < 0) speed = 0;
        } else if (speed < 0) {
            speed += friction * deltaTime;

            if (speed > 0) speed = 0;
        }
    }

    public void reverse(double deltaTime) {
        if (speed > 0) {
            speed -= 200 * deltaTime;

            if (speed < 0) {
                speed = 0;
            }
        } else {
            speed -= acceleration * deltaTime;

            if (speed < -100) {
                speed = -100;
            }
        }
    }

    public void setGrip(double grip) {
        this.grip = grip;
    }

    public void collide() {
        speed *= -0.15;
        lateralSpeed *= 0.2;
    }

    private double[] project(double[][] corners, double axisX, double axisY) {

        double min = corners[0][0] * axisX + corners[0][1] * axisY;

        double max = min;

        for (int i = 1; i < corners.length; i++) {
            double projection = corners[i][0] * axisX + corners[i][1] * axisY;

            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }

        return new double[]{min, max};
    }

    public boolean resolveCollision(Rectangle building) {

        // Hitbox dimensions after scaling
        double halfWidth = HITBOX_WIDTH * SPRITE_SCALE / 2;
        double halfHeight = HITBOX_HEIGHT * SPRITE_SCALE / 2;

        // ImageView scales around the original sprite's centre
        double centerX = getWorldX() + SPRITE_WIDTH / 2;
        double centerY = getWorldY() + SPRITE_HEIGHT / 2;

        double radians = Math.toRadians(angle);

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // Car's four rotated corners
        double[][] carCorners = new double[4][2];

        carCorners[0][0] = centerX - cos * halfWidth + sin * halfHeight;
        carCorners[0][1] = centerY - sin * halfWidth - cos * halfHeight;

        carCorners[1][0] = centerX + cos * halfWidth + sin * halfHeight;
        carCorners[1][1] = centerY + sin * halfWidth - cos * halfHeight;

        carCorners[2][0] = centerX + cos * halfWidth - sin * halfHeight;
        carCorners[2][1] = centerY + sin * halfWidth + cos * halfHeight;

        carCorners[3][0] = centerX - cos * halfWidth - sin * halfHeight;
        carCorners[3][1] = centerY - sin * halfWidth + cos * halfHeight;

        // Building's four corners
        double left = building.getX();
        double right = left + building.getWidth();
        double top = building.getY();
        double bottom = top + building.getHeight();

        double[][] buildingCorners = {
                {left, top},
                {right, top},
                {right, bottom},
                {left, bottom}
        };

        // Four axes: two from the car and two from the building
        double[][] axes = {
                {cos, sin},
                {-sin, cos},
                {1, 0},
                {0, 1}
        };

        double minimumOverlap = Double.MAX_VALUE;
        double smallestAxisX = 0;
        double smallestAxisY = 0;

        // Test every separating axis
        for (double[] axis : axes) {

            double[] carProjection =
                    project(carCorners, axis[0], axis[1]);

            double[] buildingProjection =
                    project(buildingCorners, axis[0], axis[1]);

            double overlap = Math.min(
                    carProjection[1],
                    buildingProjection[1]
            ) - Math.max(
                    carProjection[0],
                    buildingProjection[0]
            );

            // A separating axis means no collision
            if (overlap <= 0) {
                return false;
            }

            // Find the axis with the smallest overlap
            if (overlap < minimumOverlap) {
                minimumOverlap = overlap;
                smallestAxisX = axis[0];
                smallestAxisY = axis[1];
            }
        }

        // Orient the MTV axis from the car toward the building
        double buildingCenterX =
                building.getX() + building.getWidth() / 2;

        double buildingCenterY =
                building.getY() + building.getHeight() / 2;

        double directionX = buildingCenterX - centerX;
        double directionY = buildingCenterY - centerY;

        if (directionX * smallestAxisX
                + directionY * smallestAxisY < 0) {

            smallestAxisX = -smallestAxisX;
            smallestAxisY = -smallestAxisY;
        }

        // MTV: move the car away from the building
        double separation = minimumOverlap + 0.5;

        setWorldX(getWorldX() - smallestAxisX * separation);
        setWorldY(getWorldY() - smallestAxisY * separation);

        // Remove velocity directed into the building
        double forwardX = -sin;
        double forwardY = cos;

        double sidewaysX = cos;
        double sidewaysY = sin;

        double velocityX =
                forwardX * speed + sidewaysX * lateralSpeed;

        double velocityY =
                forwardY * speed + sidewaysY * lateralSpeed;

        double velocityIntoBuilding =
                velocityX * smallestAxisX
                        + velocityY * smallestAxisY;

        if (velocityIntoBuilding > 0) {
            velocityX -= velocityIntoBuilding * smallestAxisX;
            velocityY -= velocityIntoBuilding * smallestAxisY;
        }

        // Convert the corrected velocity back into car movement
        speed = velocityX * forwardX + velocityY * forwardY;

        lateralSpeed =
                velocityX * sidewaysX + velocityY * sidewaysY;

        return true;
    }

    public double[] getExitPosition(boolean leftSide) {
        double radians = Math.toRadians(angle);

        // Car's sideways direction
        double sideX = Math.cos(radians);
        double sideY = Math.sin(radians);

        double direction = leftSide ? -1 : 1;

        double exitX = getWorldX() + 23.0 / 2
                + sideX * direction * 45;

        double exitY = getWorldY() + 40.0 / 2
                + sideY * direction * 45;

        return new double[]{exitX, exitY};
    }

    public void handbrake(double deltaTime) {
        speed -= 350 * deltaTime;

        if (speed < 0) {
            speed = 0;
        }
    }

    public void setHandbrakeActive(boolean active) {
        handbrakeActive = active;
    }
}