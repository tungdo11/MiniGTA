package com.minigta;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class MapObject {

    private ImageView imageView;

    private double x;
    private double y;

    private double width;
    private double height;

    private double hitboxOffsetX;
    private double hitboxOffsetY;
    private double hitboxWidth;
    private double hitboxHeight;

    private int layer;

    private Rectangle hitbox;

    public MapObject(Image image, double x, double y,
                     double width, double height, int layer,
                     double hitboxOffsetX, double hitboxOffsetY,
                     double hitboxWidth, double hitboxHeight) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.layer = layer;

        this.hitboxOffsetX = hitboxOffsetX;
        this.hitboxOffsetY = hitboxOffsetY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;

        imageView = new ImageView(image);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        hitbox = new Rectangle(
                x + hitboxOffsetX,
                y + hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;

        imageView.setX(x);
        imageView.setY(y);

        hitbox.setX(x + hitboxOffsetX);
        hitbox.setY(y + hitboxOffsetY);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getLayer() {
        return layer;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}