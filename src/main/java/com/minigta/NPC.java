package com.minigta;

public class NPC {

    private double worldX;
    private double worldY;
    private double speed;
    private String direction;
    private String movementType;

    private int frame;
    private double animationTimer;

    private double patrolStart;
    private double patrolEnd;

    public NPC(double worldX, double worldY, double speed,
               double patrolStart, double patrolEnd, String movementType) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = speed;
        this.patrolStart = patrolStart;
        this.patrolEnd = patrolEnd;
        this.movementType = movementType;

        if (movementType.equals("vertical")) {
            this.direction = "down";
        } else {
            this.direction = "right";
        }

        this.frame = 0;
        this.frame = 0;
        this.animationTimer = 0;
    }

    public void update(double deltaTime) {
        double movement = speed * deltaTime;

        if (movementType.equals("horizontal")) {

            if (direction.equals("right")) {
                worldX += movement;

                if (worldX >= patrolEnd) {
                    direction = "left";
                }
            } else if (direction.equals("left")) {
                worldX -= movement;

                if (worldX <= patrolStart) {
                    direction = "right";
                }
            }

        } else if (movementType.equals("vertical")) {

            if (direction.equals("down")) {
                worldY += movement;

                if (worldY >= patrolEnd) {
                    direction = "up";
                }
            } else if (direction.equals("up")) {
                worldY -= movement;

                if (worldY <= patrolStart) {
                    direction = "down";
                }
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
