package com.minigta;

public class Interactable {

    private double worldX;
    private double worldY;
    private String type;

    public Interactable(double worldX, double worldY, String type) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.type = type;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public String getType() {
        return type;
    }

    public void setWorldX(double worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(double worldY) {
        this.worldY = worldY;
    }
}