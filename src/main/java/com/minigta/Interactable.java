package com.minigta;

public class Interactable {

    private double worldX;
    private double worldY;

    public Interactable(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }
}