package com.minigta;

public class NPC {

    private double worldX;
    private double worldY;
    private double speed;
    private String direction;

    private int frame;
    private double animationTimer;

    public NPC(double worldX, double worldY, double speed) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = speed;
        this.direction = "right";
        this.frame = 0;
        this.animationTimer = 0;
    }

    public void update(double deltaTime) {
        double movement = speed * deltaTime;

        if (direction.equals("right")) {
            worldX += movement;

            if (worldX >= 35 * 16) {
                direction = "left";
            }
        } else if (direction.equals("left")) {
            worldX -= movement;

            if (worldX <= 30 * 16) {
                direction = "right";
            }
        }
    }

    public void animate(double deltaTime) {
        animationTimer += deltaTime;

        if (animationTimer >= 0.12) {
            frame++;
            animationTimer = 0;

            if (frame >= 4) {
                frame = 0;
            }
        }
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public String getDirection() {
        return direction;
    }

    public int getFrame() {
        return frame;
    }
}
