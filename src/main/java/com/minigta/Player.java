package com.minigta;

public class Player {

    private double worldX;
    private double worldY;
    private double speed;
    private String direction;

    private int frame;
    private double animationTimer;

    public Player(double worldX, double worldY, double speed) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = speed;
        this.direction = "down";
        this.frame = 0;
        this.animationTimer = 0;
    }

    public void animate(double deltaTime, boolean moving) {
        if (moving) {
            animationTimer += deltaTime;

            if (animationTimer >= 0.12) {
                frame++;
                animationTimer = 0;

                if (frame >= 5) {
                    frame = 0;
                }
            }
        } else {
            frame = 0;
            animationTimer = 0;
        }
    }

    public void updateDirection(double dx, double dy) {
        if (dx != 0 || dy != 0) {
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    direction = "right";
                } else {
                    direction = "left";
                }
            } else {
                if (dy > 0) {
                    direction = "down";
                } else {
                    direction = "up";
                }
            }
        }
    }

    public void moveX(double amount) {
        worldX += amount;
    }

    public void moveY(double amount) {
        worldY += amount;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public void setWorldX(double worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(double worldY) {
        this.worldY = worldY;
    }

    public String getDirection() {
        return direction;
    }

    public int getFrame() {
        return frame;
    }
}
