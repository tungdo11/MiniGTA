package com.minigta;

public class NPC {

    private double worldX;
    private double worldY;
    private double speed;
    private String direction;
    private String movementType;
    private int groupId;

    private int frame;
    private double animationTimer;

    private double patrolStart;
    private double patrolEnd;

    private double destinationX;
    private double destinationY;

    private double pauseTimer = 0;
    private boolean escaping = false;

    private boolean knockedDown = false;
    private double knockbackX = 0;
    private double knockbackY = 0;

    public NPC(double worldX, double worldY, double speed,
               double patrolStart, double patrolEnd, String movementType, int groupId) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = speed;
        this.patrolStart = patrolStart;
        this.patrolEnd = patrolEnd;
        this.movementType = movementType;
        this.groupId = groupId;

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

        if (knockedDown) {
            worldX += knockbackX * deltaTime;
            worldY += knockbackY * deltaTime;

            double slowdown = 600 * deltaTime;

            if (knockbackX > 0) {
                knockbackX = Math.max(0, knockbackX - slowdown);
            } else {
                knockbackX = Math.min(0, knockbackX + slowdown);
            }

            if (knockbackY > 0) {
                knockbackY = Math.max(0, knockbackY - slowdown);
            } else {
                knockbackY = Math.min(0, knockbackY + slowdown);
            }

            return;
        }

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
        } else if (movementType.equals("escape")) {

            if (!escaping) {
                pauseTimer += deltaTime;

                if (pauseTimer >= 1.0) {
                    escaping = true;
                }

                return;
            }

            double dx = destinationX - worldX;
            double dy = destinationY - worldY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= movement) {
                worldX = destinationX;
                worldY = destinationY;
                speed = 0;
            } else {
                worldX += (dx / distance) * movement;
                worldY += (dy / distance) * movement;

                if (Math.abs(dx) > Math.abs(dy)) {
                    direction = dx > 0 ? "right" : "left";
                } else {
                    direction = dy > 0 ? "down" : "up";
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

    public int getGroupId() {
        return groupId;
    }

    public void setWorldX(double worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(double worldY) {
        this.worldY = worldY;
    }

    public void setDestination(double destinationX, double destinationY) {
        this.destinationX = destinationX;
        this.destinationY = destinationY;
    }

    public void follow(NPC leader, double offsetX, double offsetY) {
        if (knockedDown) {
            return;
        }

        worldX = leader.getWorldX() + offsetX;
        worldY = leader.getWorldY() + offsetY;
    }

    public boolean isEscaping() {
        return escaping;
    }

    public boolean isMoving() {
        return speed > 0;
    }

    public void hitByCar(double directionX, double directionY) {
        if (knockedDown) {
            return;
        }

        knockedDown = true;

        double length = Math.sqrt(directionX * directionX + directionY * directionY);

        if (length == 0) {
            length = 1;
        }

        knockbackX = (directionX / length) * 300;
        knockbackY = (directionY / length) * 300;
    }

    public boolean isKnockedDown() {
        return knockedDown;
    }
}
