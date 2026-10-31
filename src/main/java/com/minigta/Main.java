package com.minigta;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.Group;

import java.lang.Math;

public class Main extends Application {

    /**
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */
    private boolean wDown = false;
    private boolean aDown = false;
    private boolean sDown = false;
    private boolean dDown = false;
    private long lastTime = 0;
    private double deltaTime;
    private ImageView playerIdle;

    private Rectangle[] buildingHitbox = new Rectangle[50];
    private int buildingCount = 0;

    private static final double HITBOX_OFFSET_X = 8;
    private static final double HITBOX_OFFSET_Y = 18;
    private static final double HITBOX_WIDTH = 16;
    private static final double HITBOX_HEIGHT = 18;

    private static Rectangle playerHitbox =
            new Rectangle(
                    HITBOX_OFFSET_X,
                    HITBOX_OFFSET_Y,
                    HITBOX_WIDTH,
                    HITBOX_HEIGHT
            );

    private static Rectangle playerHitboxVisual =
            new Rectangle(
                    HITBOX_OFFSET_X,
                    HITBOX_OFFSET_Y,
                    HITBOX_WIDTH,
                    HITBOX_HEIGHT
            );

    private double playerWorldX;
    private double playerWorldY;

    private static double cameraX = 0;
    private static double cameraY = 0;

    private static final int GRASS = 0;
    private static final int ROAD_HORIZONTAL = 1;
    private static final int ROAD_VERTICAL = 2;
    private static final int SIDEWALK = 3;
    private static final int INTERSECTION = 4;
    private static final int BUILDING = 5;

    private int[][] map = new int[48][64];

    private ImageView player;

    private Image playerUpIdle;
    private Image playerDownIdle;
    private Image playerLeftIdle;
    private Image playerRightIdle;

    private String playerDirection = "down";

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Group world = new Group();
        Group buildingOverlay = new Group();

        root.getChildren().add(world);
        root.setStyle("-fx-background-color: #ffffff;");
        Scene scene = new Scene(root, 1280, 720);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> wDown = true;
                case A -> aDown = true;
                case S -> sDown = true;
                case D -> dDown = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W -> wDown = false;
                case A -> aDown = false;
                case S -> sDown = false;
                case D -> dDown = false;
            }
        });

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                double dx = 0;
                double dy = 0;
                double speed = 200;

                if (wDown) dy--;
                if (sDown) dy++;
                if (aDown) dx--;
                if (dDown) dx++;

                if (dx != 0 || dy != 0) {
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            playerDirection = "right";
                        } else {
                            playerDirection = "left";
                        }
                    } else {
                        if (dy > 0) {
                            playerDirection = "down";
                        } else {
                            playerDirection = "up";
                        }
                    }
                }

                if (playerDirection.equals("up")) {
                    player.setImage(playerUpIdle);
                } else if (playerDirection.equals("down")) {
                    player.setImage(playerDownIdle);
                } else if (playerDirection.equals("right")) {
                    player.setImage(playerRightIdle);
                } else if (playerDirection.equals("left")) {
                    player.setImage(playerLeftIdle);
                }

                double movement = speed * deltaTime;

                double length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                if (length > 0) {
                    dx /= length;
                    dy /= length;
                }

                double newX = playerWorldX + dx * movement;
                double oldX = playerWorldX;

                playerWorldX = newX;
                playerHitbox.setX(playerWorldX + HITBOX_OFFSET_X);

                for (Rectangle building : buildingHitbox) {
                    if (building != null &&
                            checkCollision(playerHitbox, building)) {

                        playerWorldX = oldX;
                        playerHitbox.setX(playerWorldX + 8);
                        break;
                    }
                }


                double newY = playerWorldY + dy * movement;
                double oldY = playerWorldY;

                playerWorldY = newY;
                playerHitbox.setY(playerWorldY + HITBOX_OFFSET_Y);

                for (Rectangle building : buildingHitbox) {
                    if (building != null &&
                            checkCollision(playerHitbox, building)) {

                        playerWorldY = oldY;
                        playerHitbox.setY(playerWorldY + 12);
                        break;
                    }
                }

                cameraX =  playerWorldX - scene.getWidth() / 2 + 24;
                cameraY =  playerWorldY - scene.getHeight() / 2 + 24;

                double worldWidth = map[0].length * 16 * 2;
                double worldHeight = map.length * 16 * 2;

                cameraX = Math.max(0, Math.min(cameraX, worldWidth - scene.getWidth()));
                cameraY = Math.max(0, Math.min(cameraY, worldHeight - scene.getHeight()));

                world.setTranslateX(-cameraX);
                world.setTranslateY(-cameraY);

                buildingOverlay.setTranslateX(-cameraX);
                buildingOverlay.setTranslateY(-cameraY);

                player.setX(playerWorldX + 8 - cameraX);
                player.setY(playerWorldY + 12 - cameraY);

                playerHitboxVisual.setX(playerWorldX + HITBOX_OFFSET_X - cameraX);
                playerHitboxVisual.setY(playerWorldY + HITBOX_OFFSET_Y - cameraY);
            }
        };

        Image logoImage = new Image(getClass().getResource("/logo.png").toExternalForm());

        playerWorldX = scene.getWidth() / 2 - 24;
        playerWorldY = scene.getHeight() / 2 - 24;

        Image grassImage = new Image(
                getClass().getResource("/tiles/ground/grass.png").toExternalForm());

        Image roadHorizontalImage = new Image(
                getClass().getResource("/tiles/roads/road_horizontal.png").toExternalForm()
        );

        Image sideWalkImage = new Image(
                getClass().getResource("/tiles/roads/sidewalk.png").toExternalForm()
        );

        Image intersectionImage = new Image(
                getClass().getResource("/tiles/roads/intersection.png").toExternalForm()
        );

        Image roadVerticalImage = new Image(
                getClass().getResource("/tiles/roads/road_vertical.png").toExternalForm()
        );

        Image roofImage = new Image(
                getClass().getResource("/tiles/buildings/roof.png").toExternalForm()
        );

        Image wallImage = new Image(
                getClass().getResource("/tiles/buildings/wall.png").toExternalForm()
        );

        Image treeImage = new Image(
                getClass().getResource("/tiles/props/tree.png").toExternalForm()
        );

        Image streetlightImage = new Image(
                getClass().getResource("/tiles/props/streetlight.png").toExternalForm()
        );

        Image hydrantImage = new Image(
                getClass().getResource("/tiles/props/hydrant.png").toExternalForm()
        );

        playerUpIdle = new Image(
                getClass().getResource("/tiles/player/up/player_up_idle_1.png").toExternalForm()
        );

        playerDownIdle = new Image(
                getClass().getResource("/tiles/player/down/player_down_idle_1.png").toExternalForm()
        );

        playerLeftIdle = new Image(
                getClass().getResource("/tiles/player/left/player_left_idle_1.png").toExternalForm()
        );

        playerRightIdle = new Image(
                getClass().getResource("/tiles/player/right/player_right_idle_1.png").toExternalForm()
        );

        player = new ImageView(playerDownIdle);
        player.setSmooth(false);

        root.getChildren().add(player);
        root.getChildren().add(buildingOverlay);

        // player's hitbox visuality
//        playerHitboxVisual.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 1));
//        root.getChildren().add(playerHitboxVisual);

        player.setX(playerWorldX);
        player.setY(playerWorldY);

        scale(player, 2);

        world.getTransforms().add(new Scale(2, 2, 0, 0));
        buildingOverlay.getTransforms().add(new Scale(2, 2, 0, 0));

        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {

                boolean horizontalRoad = row >= 12 && row < 16
                        || row >= 32 && row < 36;

                boolean verticalRoad = column >= 20 && column < 24
                        || column >= 44 && column < 48;

                boolean horizontalSidewalk = row >= 10 && row < 12
                        || row >= 16 && row < 18
                        || row >= 30 && row < 32
                        || row >= 36 && row < 38;

                boolean verticalSidewalk = column >= 18 && column < 20
                        || column >= 24 && column < 26
                        || column >= 42 && column < 44
                        || column >= 48 && column < 50;

                boolean buildingArea =
                        // Top-left district
                        (row >= 4 && row < 8 && column >= 4 && column < 8)
                                || (row >= 4 && row < 8 && column >= 12 && column < 16)

                                // Top-middle district
                                || (row >= 4 && row < 8 && column >= 28 && column < 32)
                                || (row >= 4 && row < 8 && column >= 36 && column < 40)

                                // Top-right district
                                || (row >= 4 && row < 8 && column >= 52 && column < 56)

                                // Middle-left district
                                || (row >= 22 && row < 26 && column >= 4 && column < 8)
                                || (row >= 22 && row < 26 && column >= 12 && column < 16)

                                // Middle-right district
                                || (row >= 22 && row < 26 && column >= 28 && column < 32)
                                || (row >= 22 && row < 26 && column >= 36 && column < 40)
                                || (row >= 22 && row < 26 && column >= 52 && column < 56)

                                // Bottom-left district
                                || (row >= 40 && row < 44 && column >= 4 && column < 8)
                                || (row >= 40 && row < 44 && column >= 12 && column < 16)

                                // Bottom-right district
                                || (row >= 40 && row < 44 && column >= 28 && column < 32)
                                || (row >= 40 && row < 44 && column >= 36 && column < 40)
                                || (row >= 40 && row < 44 && column >= 52 && column < 56);

                if (horizontalRoad && verticalRoad) {
                    map[row][column] = INTERSECTION;
                } else if (horizontalRoad) {
                    map[row][column] = ROAD_HORIZONTAL;
                } else if (verticalRoad) {
                    map[row][column] = ROAD_VERTICAL;
                } else if (horizontalSidewalk || verticalSidewalk) {
                    map[row][column] = SIDEWALK;
                } else if (buildingArea) {
                    map[row][column] = BUILDING;
                } else {
                    map[row][column] = GRASS;
                }
            }
        }

        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                if (map[row][column] == GRASS) {
                    Rectangle grassTile = new Rectangle(column * 16, row * 16, 16, 16);
                    grassTile.setFill(new ImagePattern(grassImage));
                    world.getChildren().add(grassTile);
                }

                if (map[row][column] == ROAD_HORIZONTAL) {

                    ImageView roadTile = new ImageView(roadHorizontalImage);

                    roadTile.setX(column * 16);
                    roadTile.setY(row * 16);
                    roadTile.setFitWidth(16);
                    roadTile.setFitHeight(16);
                    world.getChildren().add(roadTile);
                }

                if (map[row][column] == ROAD_VERTICAL) {

                    ImageView roadTile = new ImageView(roadVerticalImage);

                    roadTile.setX(column * 16);
                    roadTile.setY(row * 16);
                    roadTile.setFitWidth(16);
                    roadTile.setFitHeight(16);
                    world.getChildren().add(roadTile);
                }

                if (map[row][column] == SIDEWALK) {
                    ImageView sideWalkTile = new ImageView(sideWalkImage);
                    sideWalkTile.setX(column * 16);
                    sideWalkTile.setY(row * 16);
                    sideWalkTile.setFitWidth(16);
                    sideWalkTile.setFitHeight(16);
                    world.getChildren().add(sideWalkTile);
                }

                if (map[row][column] == INTERSECTION) {
                    ImageView intersectionTile = new ImageView(intersectionImage);
                    intersectionTile.setX(column * 16);
                    intersectionTile.setY(row * 16);
                    intersectionTile.setFitWidth(16);
                    intersectionTile.setFitHeight(16);
                    world.getChildren().add(intersectionTile);
                }

                if (map[row][column] == BUILDING
                        && (row == 0 || map[row - 1][column] != BUILDING)
                        && (column == 0 || map[row][column - 1] != BUILDING)) {
                    addBuilding(world, buildingOverlay, roofImage, wallImage, column, row);
                }
            }
        }

        // Trees
        addProp(world, treeImage, 2, 2, 32, 32);
        addProp(world, treeImage, 9, 9, 32, 32);
        addProp(world, treeImage, 17, 6, 32, 32);
        addProp(world, treeImage, 27, 9, 32, 32);
        addProp(world, treeImage, 34, 5, 32, 32);
        addProp(world, treeImage, 41, 9, 32, 32);
        addProp(world, treeImage, 53, 9, 32, 32);
        addProp(world, treeImage, 60, 5, 32, 32);

        addProp(world, treeImage, 2, 27, 32, 32);
        addProp(world, treeImage, 9, 29, 32, 32);
        addProp(world, treeImage, 17, 25, 32, 32);
        addProp(world, treeImage, 27, 28, 32, 32);
        addProp(world, treeImage, 34, 27, 32, 32);
        addProp(world, treeImage, 41, 24, 32, 32);
        addProp(world, treeImage, 53, 28, 32, 32);
        addProp(world, treeImage, 60, 24, 32, 32);

        addProp(world, treeImage, 2, 46, 32, 32);
        addProp(world, treeImage, 9, 42, 32, 32);
        addProp(world, treeImage, 17, 46, 32, 32);
        addProp(world, treeImage, 27, 47, 32, 32);
        addProp(world, treeImage, 34, 45, 32, 32);
        addProp(world, treeImage, 41, 47, 32, 32);
        addProp(world, treeImage, 53, 46, 32, 32);
        addProp(world, treeImage, 60, 42, 32, 32);

        // Streetlights
        addProp(world, streetlightImage, 18, 10, 16, 32);
        addProp(world, streetlightImage, 24, 10, 16, 32);
        addProp(world, streetlightImage, 42, 10, 16, 32);
        addProp(world, streetlightImage, 48, 10, 16, 32);

        addProp(world, streetlightImage, 18, 18, 16, 32);
        addProp(world, streetlightImage, 24, 18, 16, 32);
        addProp(world, streetlightImage, 42, 18, 16, 32);
        addProp(world, streetlightImage, 48, 18, 16, 32);

        addProp(world, streetlightImage, 18, 30, 16, 32);
        addProp(world, streetlightImage, 24, 30, 16, 32);
        addProp(world, streetlightImage, 42, 30, 16, 32);
        addProp(world, streetlightImage, 48, 30, 16, 32);

        addProp(world, streetlightImage, 18, 38, 16, 32);
        addProp(world, streetlightImage, 24, 38, 16, 32);
        addProp(world, streetlightImage, 42, 38, 16, 32);
        addProp(world, streetlightImage, 48, 38, 16, 32);

        // Hydrants
        addProp(world, hydrantImage, 19, 11, 16, 16);
        addProp(world, hydrantImage, 25, 17, 16, 16);
        addProp(world, hydrantImage, 43, 11, 16, 16);
        addProp(world, hydrantImage, 49, 17, 16, 16);

        addProp(world, hydrantImage, 19, 31, 16, 16);
        addProp(world, hydrantImage, 25, 37, 16, 16);
        addProp(world, hydrantImage, 43, 31, 16, 16);
        addProp(world, hydrantImage, 49, 37, 16, 16);

        stage.setScene(scene);
        stage.setTitle("Mini GTA");
        stage.setResizable(false);
        stage.getIcons().add(logoImage);
        stage.show();

        timer.start();
    }

    public static void scale(ImageView object, double ratio) {
        object.setScaleX(ratio);
        object.setScaleY(ratio);
    }

    public static void scale(Group name, double ratio) {
        name.setScaleX(ratio);
        name.setScaleY(ratio);
    }

    public boolean checkCollision(Rectangle player, Rectangle building) {
        double playerLeft = player.getX();
        double playerRight = player.getX() + player.getWidth();
        double playerTop = player.getY();
        double playerBottom = player.getY() + player.getHeight();

        double buildingLeft = building.getX();
        double buildingRight = building.getX() + building.getWidth();
        double buildingTop = building.getY();
        double buildingBottom = building.getY() + building.getHeight();

        return playerRight > buildingLeft
                && playerLeft < buildingRight
                && playerBottom > buildingTop
                && playerTop < buildingBottom;
    }

    public void addBuilding(Group world, Group buildingOverlay, Image roofImage, Image wallImage, int startColumn, int startRow) {
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {

                ImageView buildingTile;

                if (row < 2) {
                    buildingTile = new ImageView(roofImage);
                } else {
                    buildingTile = new ImageView(wallImage);
                }

                buildingTile.setX((startColumn + column) * 16);
                buildingTile.setY((startRow + row) * 16);
                buildingTile.setFitWidth(16);
                buildingTile.setFitHeight(16);

                if (row < 2) {
                    buildingOverlay.getChildren().add(buildingTile);
                } else {
                    world.getChildren().add(buildingTile);
                }
            }
        }

        Rectangle buildingCollision = new Rectangle(
                startColumn * 16 * 2,
                (startRow + 2) * 16 * 2,
                4 * 16 * 2,
                2 * 16 * 2
        );

        buildingHitbox[buildingCount] = buildingCollision;
        buildingCount++;
    }

    public void addProp(Group world, Image image, int column, int row, int width, int height) {
        ImageView prop = new ImageView(image);

        prop.setX(column * 16 - width / 2);
        prop.setY(row * 16 - height);
        prop.setFitWidth(width);
        prop.setFitHeight(height);

        world.getChildren().add(prop);
    }
}