
package com.minigta;

import javafx.geometry.Point2D;
import java.util.List;

public class TrafficController {

    private TrafficCar car;
    private List<Point2D> route;

    private static final double TARGET_SPEED = 100;
    private static final double WAYPOINT_DISTANCE = 40;

    public TrafficController(TrafficCar car, List<Point2D> route) {
        this.car = car;
        this.route = route;
    }

    public void update(double deltaTime) {

        // Make sure the route has enough waypoints
        if (route == null || route.size() < 3) {
            return;
        }

        // Get the current target waypoint
        int targetIndex = car.getTargetIndex();

        if (targetIndex >= route.size() || targetIndex < 0) {
            targetIndex = 0;
            car.setTargetIndex(0);
        }

        Point2D target = route.get(targetIndex);

        // Calculate the car's center position
        double carX = car.getWorldX() + 23.0 / 2;
        double carY = car.getWorldY() + 40.0 / 2;

        // Calculate the distance to the target
        double dx = target.getX() - carX;
        double dy = target.getY() - carY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Move to the next waypoint when close enough
        if (distance < WAYPOINT_DISTANCE) {

            targetIndex++;

            if (targetIndex >= route.size()) {
                targetIndex = 0;
            }

            car.setTargetIndex(targetIndex);

            target = route.get(targetIndex);

            dx = target.getX() - carX;
            dy = target.getY() - carY;
        }

        // Calculate the desired angle
        double desiredAngle = Math.toDegrees(Math.atan2(-dx, dy));

        // Calculate the difference between the two angles
        double angleDifference = desiredAngle - car.getAngle();

        // Normalize the angle difference to -180 through 180
        while (angleDifference > 180) {
            angleDifference -= 360;
        }

        while (angleDifference < -180) {
            angleDifference += 360;
        }

        // Accelerate or brake to maintain the target speed
        if (car.getSpeed() < TARGET_SPEED) {
            car.accelerate(deltaTime);
        } else if (car.getSpeed() > TARGET_SPEED) {
            car.brake(deltaTime);
        }

        // Steer towards the target
        if (angleDifference > 2) {
            car.steerRight(deltaTime, false);
        } else if (angleDifference < -2) {
            car.steerLeft(deltaTime, false);
        }

        // Move the car
        car.move(deltaTime);
    }
}
