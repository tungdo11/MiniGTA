package com.minigta;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import javafx.scene.paint.Color;

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

    private boolean drivingW = false;
    private boolean drivingA = false;
    private boolean drivingS = false;
    private boolean drivingD = false;
    private boolean handbrakeDown = false;

    private boolean isDriving = false;
    private int currentCar = -1;

    private boolean fPressed = false;
    private boolean fWasPressed = false;

    private long lastTime = 0;
    private double deltaTime;
    private ImageView playerIdle;

    private Rectangle[] buildingHitbox = new Rectangle[50];
    private int buildingCount = 0;

    private static final double HITBOX_OFFSET_X = 8;
    private static final double HITBOX_OFFSET_Y = 18;
    private static final double HITBOX_WIDTH = 16;
    private static final double HITBOX_HEIGHT = 18;

    private static final double CAR_INTERACTION_DISTANCE = 60;

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

    private Player playerObject;

    private static double cameraX = 0;
    private static double cameraY = 0;

    private static final int GRASS = 0;
    private static final int ROAD_HORIZONTAL = 1;
    private static final int ROAD_VERTICAL = 2;
    private static final int SIDEWALK = 3;
    private static final int INTERSECTION = 4;
    private static final int BUILDING = 5;

    private static final int ROAD_CORNER_BL = 6;
    private static final int ROAD_CORNER_BR = 7;
    private static final int ROAD_CORNER_TL = 8;
    private static final int ROAD_CORNER_TR = 9;

    private static final int SIDEWALK_CORNER_BL = 10;
    private static final int SIDEWALK_CORNER_BR = 11;
    private static final int SIDEWALK_CORNER_TL = 12;
    private static final int SIDEWALK_CORNER_TR = 13;

    private int[][] map = new int[96][144];

    private ImageView player;

    private Image playerUpIdle;
    private Image playerDownIdle;
    private Image playerLeftIdle;
    private Image playerRightIdle;

    private Image[] playerUpWalk = new Image[5];
    private Image[] playerDownWalk = new Image[5];
    private Image[] playerLeftWalk = new Image[5];
    private Image[] playerRightWalk = new Image[5];

    private ImageView[] npcs = new ImageView[3];
    private NPC[] npcObjects = new NPC[3];

    private Interactable[] interactables = new Interactable[3];
    private ImageView[] interactableViews = new ImageView[3];

    private Car[] cars = new Car[1];
    private ImageView[] carViews = new ImageView[1];

    private Image npcUpIdle;
    private Image npcDownIdle;
    private Image npcLeftIdle;
    private Image npcRightIdle;

    private Image[] npcUpWalk = new Image[4];
    private Image[] npcDownWalk = new Image[4];
    private Image[] npcLeftWalk = new Image[4];
    private Image[] npcRightWalk = new Image[4];

    private Text interactionText;

    private Polygon carHitboxVisual;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Group world = new Group();
        Group buildingOverlay = new Group();

        root.getChildren().add(world);

        Image cityMapImage = new Image(
                getClass().getResource("/map/base/city_map.png").toExternalForm()
        );

        ImageView cityMapView = new ImageView(cityMapImage);

        cityMapView.setFitWidth(2880);
        cityMapView.setFitHeight(1920);
        cityMapView.setPreserveRatio(true);
        cityMapView.setSmooth(false);

        world.getChildren().add(cityMapView);

        root.setStyle("-fx-background-color: #ffffff;");
        Scene scene = new Scene(root, 1280, 720);

        interactionText = new Text("Press F to interact");
        interactionText.setVisible(false);
        interactionText.setX(20);
        interactionText.setY(40);
        root.getChildren().add(interactionText);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> {
                    if (isDriving) drivingW = true;
                    else wDown = true;
                }
                case A -> {
                    if (isDriving) drivingA = true;
                    else aDown = true;
                }
                case S -> {
                    if (isDriving) drivingS = true;
                    else sDown = true;
                }
                case D -> {
                    if (isDriving) drivingD = true;
                    else dDown = true;
                }
                case F -> {
                    if (!fPressed) {
                        fPressed = true;
                    }
                }
                case SPACE -> {
                    if (isDriving) handbrakeDown = true;
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W -> {
                    if (isDriving) drivingW = false;
                    else wDown = false;
                }
                case A -> {
                    if (isDriving) drivingA = false;
                    else aDown = false;
                }
                case S -> {
                    if (isDriving) drivingS = false;
                    else sDown = false;
                }
                case D -> {
                    if (isDriving) drivingD = false;
                    else dDown = false;
                }
                case F -> fPressed = false;
                case SPACE -> handbrakeDown = false;
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

                if (!isDriving) {
                    double dx = 0;
                    double dy = 0;
                    double speed = 200;

                    if (wDown) dy--;
                    if (sDown) dy++;
                    if (aDown) dx--;
                    if (dDown) dx++;

                    playerObject.updateDirection(dx, dy);

                    playerObject.animate(deltaTime, dx != 0 || dy != 0);


                    if (dx != 0 || dy != 0) {
                        if (playerObject.getDirection().equals("up")) {
                            player.setImage(playerUpWalk[playerObject.getFrame()]);
                        } else if (playerObject.getDirection().equals("down")) {
                            player.setImage(playerDownWalk[playerObject.getFrame()]);
                        } else if (playerObject.getDirection().equals("right")) {
                            player.setImage(playerRightWalk[playerObject.getFrame()]);
                        } else if (playerObject.getDirection().equals("left")) {
                            player.setImage(playerLeftWalk[playerObject.getFrame()]);
                        }
                    } else {
                        if (playerObject.getDirection().equals("up")) {
                            player.setImage(playerUpIdle);
                        } else if (playerObject.getDirection().equals("down")) {
                            player.setImage(playerDownIdle);
                        } else if (playerObject.getDirection().equals("right")) {
                            player.setImage(playerRightIdle);
                        } else if (playerObject.getDirection().equals("left")) {
                            player.setImage(playerLeftIdle);
                        }
                    }

                    double movement = 200 * deltaTime;

                    double length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    double oldX = playerObject.getWorldX();

                    playerObject.moveX(dx * movement);
                    playerHitbox.setX(playerObject.getWorldX() + HITBOX_OFFSET_X);

                    for (Rectangle building : buildingHitbox) {
                        if (building != null &&
                                checkCollision(playerHitbox, building)) {

                            playerObject.setWorldX(oldX);
                            playerHitbox.setX(playerObject.getWorldX() + HITBOX_OFFSET_X);
                            break;
                        }
                    }

                    double oldY = playerObject.getWorldY();

                    playerObject.moveY(dy * movement);


                    playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);

                    for (Rectangle building : buildingHitbox) {
                        if (building != null &&
                                checkCollision(playerHitbox, building)) {

                            playerObject.setWorldY(oldY);
                            playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);
                            break;
                        }
                    }
                }

                if (isDriving) {
                    Car car = cars[currentCar];
                    car.setHandbrakeActive(handbrakeDown);

                    int tileColumn = (int) ((car.getWorldX() + 20) / 32);
                    int tileRow = (int) ((car.getWorldY() + 36) / 32);

                    double surfaceGrip = 1.0;

                    if (tileRow >= 0 && tileRow < map.length
                            && tileColumn >= 0 && tileColumn < map[0].length) {

                        int surface = map[tileRow][tileColumn];

                        if (surface == GRASS) {
                            surfaceGrip = 0.55;
                        } else if (surface == SIDEWALK) {
                            surfaceGrip = 0.85;
                        } else {
                            surfaceGrip = 1.0;
                        }
                    }

                    car.setGrip(surfaceGrip);

                    boolean braking = drivingS && car.getSpeed() > 0;

                    if (drivingW) car.accelerate(deltaTime);
                    else if (drivingS) car.reverse(deltaTime);
                    else if (handbrakeDown) car.handbrake(deltaTime);
                    else car.applyFriction(deltaTime);

                    if (drivingA) car.steerLeft(deltaTime, braking);

                    if (drivingD) car.steerRight(deltaTime, braking);

                    car.move(deltaTime);

                    for (Rectangle building : buildingHitbox) {
                        if (building != null && car.resolveCollision(building)) {
                            break;
                        }
                    }

                    carViews[currentCar].setRotate(car.getAngle());
                }

                double cameraTargetX;
                double cameraTargetY;

                if (isDriving) {
                    cameraTargetX = cars[currentCar].getWorldX();
                    cameraTargetY = cars[currentCar].getWorldY();
                } else {
                    cameraTargetX = playerObject.getWorldX() + 24;
                    cameraTargetY = playerObject.getWorldY() + 24;
                }

                cameraX = cameraTargetX - scene.getWidth() / 2;
                cameraY = cameraTargetY - scene.getHeight() / 2;

                double worldWidth = cityMapView.getBoundsInLocal().getWidth() * 2;
                double worldHeight = cityMapView.getBoundsInLocal().getHeight() * 2;

                cameraX = Math.max(0, Math.min(cameraX, worldWidth - scene.getWidth()));
                cameraY = Math.max(0, Math.min(cameraY, worldHeight - scene.getHeight()));

                world.setTranslateX(-cameraX);
                world.setTranslateY(-cameraY);

                buildingOverlay.setTranslateX(-cameraX);
                buildingOverlay.setTranslateY(-cameraY);

                player.setX(playerObject.getWorldX() + 8 - cameraX);
                player.setY(playerObject.getWorldY() + 12 - cameraY);

                for (int i = 0; i < interactableViews.length; i++) {
                    interactableViews[i].setX(interactables[i].getWorldX() - cameraX);
                    interactableViews[i].setY(interactables[i].getWorldY() - cameraY);
                }

                carViews[0].setX(cars[0].getWorldX() - cameraX);
                carViews[0].setY(cars[0].getWorldY() - cameraY);

                Car car = cars[0];

                // Adjust the hitbox dimensions
                double halfWidth = 17 * 1.8 / 2;
                double halfHeight = 32 * 1.8 / 2;

                double centerX = car.getWorldX() + 23.0 / 2 - cameraX;
                double centerY = car.getWorldY() + 40.0 / 2 - cameraY;

                double radians = Math.toRadians(car.getAngle());

                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                double[][] corners = {
                        {-halfWidth, -halfHeight},
                        {halfWidth, -halfHeight},
                        {halfWidth, halfHeight},
                        {-halfWidth, halfHeight}
                };

                carHitboxVisual.getPoints().clear();

                for (double[] corner : corners) {
                    double rotatedX = corner[0] * cos - corner[1] * sin;
                    double rotatedY = corner[0] * sin + corner[1] * cos;

                    carHitboxVisual.getPoints().addAll(
                            centerX + rotatedX,
                            centerY + rotatedY
                    );
                }

                double closestDistance = Double.MAX_VALUE;
                int closestInteractable = -1;

                for (int i = 0; i < interactables.length; i++) {
                    double distanceX = playerObject.getWorldX() - interactables[i].getWorldX();
                    double distanceY = playerObject.getWorldY() - interactables[i].getWorldY();

                    double distance = Math.sqrt(
                            Math.pow(distanceX, 2) + Math.pow(distanceY, 2)
                    );

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestInteractable = i;
                    }
                }

                if (closestDistance < 40) {
                    interactionText.setVisible(true);

                    if (fPressed && !fWasPressed) {
                        if (interactables[closestInteractable].getType().equals("hydrant")) {
                            System.out.println("You interacted with a hydrant.");
                        } else if (interactables[closestInteractable].getType().equals("sign")) {
                            System.out.println("You read the sign.");
                        } else if (interactables[closestInteractable].getType().equals("bench")) {
                            System.out.println("You found a bench.");
                        }
                    }
                } else {
                    interactionText.setVisible(false);
                }

                double carDistanceX = playerObject.getWorldX() - cars[0].getWorldX();
                double carDistanceY = playerObject.getWorldY() - cars[0].getWorldY();

                double carDistance = Math.sqrt(Math.pow(carDistanceX, 2) + Math.pow(carDistanceY, 2));

                if (!isDriving && carDistance < CAR_INTERACTION_DISTANCE) {
                    interactionText.setText("Press F to enter taxi");
                    interactionText.setVisible(true);

                    if (fPressed && !fWasPressed) {
                        isDriving = true;
                        currentCar = 0;
                        handbrakeDown = false;
                        player.setVisible(false);
                    }
                } else if (isDriving) {
                    interactionText.setText("Press F to exit the vehicle");
                    interactionText.setVisible(true);

                    if (fPressed && !fWasPressed) {
//                        Car car = cars[currentCar];

                        double[] leftExit = car.getExitPosition(true);
                        double[] rightExit = car.getExitPosition(false);

                        boolean leftSafe = isSafeExit(leftExit[0], leftExit[1]);
                        boolean rightSafe = isSafeExit(rightExit[0], rightExit[1]);

                        if (leftSafe || rightSafe) {
                            double[] exitPosition = leftSafe ? leftExit : rightExit;

                            playerObject.setWorldX(exitPosition[0]);
                            playerObject.setWorldY(exitPosition[1]);

                            isDriving = false;
                            currentCar = -1;

                            handbrakeDown = false;

                            wDown = false;
                            aDown = false;
                            sDown = false;
                            dDown = false;

                            drivingW = false;
                            drivingA = false;
                            drivingS = false;
                            drivingD = false;

                            player.setVisible(true);

                            playerHitbox.setX(playerObject.getWorldX() + HITBOX_OFFSET_X);
                            playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);
                        } else {
                            interactionText.setText("No safe place to exit!");
                        }
                    }
                }

                fWasPressed = fPressed;

                playerHitboxVisual.setX(playerObject.getWorldX() + HITBOX_OFFSET_X - cameraX);
                playerHitboxVisual.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y - cameraY);

                for (int i = 0; i < npcObjects.length; i++) {
                    if (i != 2) {
                        npcObjects[i].update(deltaTime);
                    }

                    npcObjects[i].animate(deltaTime);

                    npcs[i].setX(npcObjects[i].getWorldX() - cameraX);
                    npcs[i].setY(npcObjects[i].getWorldY() - cameraY);

                    if (npcObjects[i].getDirection().equals("right")) {
                        npcs[i].setImage(npcRightWalk[npcObjects[i].getFrame()]);
                    } else if (npcObjects[i].getDirection().equals("left")) {
                        npcs[i].setImage(npcLeftWalk[npcObjects[i].getFrame()]);
                    } else if (npcObjects[i].getDirection().equals("up")) {
                        npcs[i].setImage(npcUpWalk[npcObjects[i].getFrame()]);
                    } else if (npcObjects[i].getDirection().equals("down")) {
                        npcs[i].setImage(npcDownWalk[npcObjects[i].getFrame()]);
                    }
                }

                if (npcObjects[1].getGroupId() == npcObjects[2].getGroupId()) {
                    npcObjects[2].follow(npcObjects[1], 32, 32);
                }
            }
        };

        Image logoImage = new Image(
                getClass().getResource("/logo.png").toExternalForm()
        );

        playerObject = new Player(985, 3095, 200);

        interactables[0] = new Interactable(450, 2700, "hydrant");
        interactables[1] = new Interactable(1050, 2700, "trashcan");
        interactables[2] = new Interactable(1250, 2900, "bench");

        cars[0] = new Car(700, 2950, 0, "right", "taxi");

        npcObjects[0] = new NPC(560, 2730, 80, 26 * 16, 42 * 16, "horizontal", 1);
        npcObjects[1] = new NPC(850, 2730, 70, 30 * 16, 40 * 16, "vertical", 2);
        npcObjects[2] = new NPC(1100, 2820, 60, 40 * 16, 46 * 16, "vertical", 2);


        Image grassImage = new Image(
                getClass().getResource("/legacy_tiles/ground/grass.png").toExternalForm());

        Image roadHorizontalImage = new Image(
                getClass().getResource("/legacy_tiles/roads/road_horizontal.png").toExternalForm()
        );

        Image sideWalkImage = new Image(
                getClass().getResource("/legacy_tiles/roads/sidewalk.png").toExternalForm()
        );

        Image intersectionImage = new Image(
                getClass().getResource("/legacy_tiles/roads/intersection.png").toExternalForm()
        );

        Image roadVerticalImage = new Image(
                getClass().getResource("/legacy_tiles/roads/road_vertical.png").toExternalForm()
        );

        Image roadCornerBLImage = new Image(
                getClass().getResource("/legacy_tiles/roads/corner_bl.png").toExternalForm()
        );

        Image roadCornerBRImage = new Image(
                getClass().getResource("/legacy_tiles/roads/corner_br.png").toExternalForm()
        );

        Image roadCornerTLImage = new Image(
                getClass().getResource("/legacy_tiles/roads/corner_tl.png").toExternalForm()
        );

        Image roadCornerTRImage = new Image(
                getClass().getResource("/legacy_tiles/roads/corner_tr.png").toExternalForm()
        );

        Image sidewalkCornerBLImage = new Image(
                getClass().getResource("/legacy_tiles/roads/sidewalk_corner_bl.png").toExternalForm()
        );

        Image sidewalkCornerBRImage = new Image(
                getClass().getResource("/legacy_tiles/roads/sidewalk_corner_br.png").toExternalForm()
        );

        Image sidewalkCornerTLImage = new Image(
                getClass().getResource("/legacy_tiles/roads/sidewalk_corner_tl.png").toExternalForm()
        );

        Image sidewalkCornerTRImage = new Image(
                getClass().getResource("/legacy_tiles/roads/sidewalk_corner_tr.png").toExternalForm()
        );

        Image roofImage = new Image(
                getClass().getResource("/legacy_tiles/buildings/roof.png").toExternalForm()
        );

        Image wallImage = new Image(
                getClass().getResource("/legacy_tiles/buildings/wall.png").toExternalForm()
        );

        Image treeImage = new Image(
                getClass().getResource("/legacy_tiles/props/tree.png").toExternalForm()
        );

        Image streetlightImage = new Image(
                getClass().getResource("/legacy_tiles/props/streetlight.png").toExternalForm()
        );

        Image hydrantImage = new Image(
                getClass().getResource("/legacy_tiles/props/hydrant.png").toExternalForm()
        );

        Image benchImage = new Image(
                getClass().getResource("/legacy_tiles/props/bench.png").toExternalForm()
        );

        Image trashcanImage = new Image(
                getClass().getResource("/legacy_tiles/props/trashcan.png").toExternalForm()
        );

        Image taxiImage = new Image(
                getClass().getResource("/legacy_tiles/vehicles/taxi/taxi_frame_01.png").toExternalForm()
        );

        playerUpIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/player/up/player_up_idle_1.png").toExternalForm()
        );

        playerDownIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/player/down/player_down_idle_1.png").toExternalForm()
        );

        playerLeftIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/player/left/player_left_idle_1.png").toExternalForm()
        );

        playerRightIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/player/right/player_right_idle_1.png").toExternalForm()
        );

        for (int i = 0; i < 5; i++) {
            playerUpWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/player/up/player_up_walk_" + (i + 1) + ".png").toExternalForm()
            );

            playerDownWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/player/down/player_down_walk_" + (i + 1) + ".png").toExternalForm()
            );

            playerLeftWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/player/left/player_left_walk_" + (i + 1) + ".png").toExternalForm()
            );

            playerRightWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/player/right/player_right_walk_" + (i + 1) + ".png").toExternalForm()
            );
        }

        npcUpIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/npc/civilian/up/civilian_up_idle_1.png").toExternalForm()
        );

        npcDownIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/npc/civilian/down/civilian_down_idle_1.png").toExternalForm()
        );

        npcLeftIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/npc/civilian/left/civilian_left_idle_1.png").toExternalForm()
        );

        npcRightIdle = new Image(
                getClass().getResource("/legacy_tiles/characters/npc/civilian/right/civilian_right_idle_1.png").toExternalForm()
        );

        for (int i = 0; i < 4; i++) {
            npcUpWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/npc/civilian/up/civilian_up_walk_" + (i + 1) + ".png").toExternalForm()
            );

            npcDownWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/npc/civilian/down/civilian_down_walk_" + (i + 1) + ".png").toExternalForm()
            );

            npcLeftWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/npc/civilian/left/civilian_left_walk_" + (i + 1) + ".png").toExternalForm()
            );

            npcRightWalk[i] = new Image(
                    getClass().getResource("/legacy_tiles/characters/npc/civilian/right/civilian_right_walk_" + (i + 1) + ".png").toExternalForm()
            );
        }

        carViews[0] = new ImageView(taxiImage);
        carViews[0].setSmooth(false);

        player = new ImageView(playerDownIdle);
        player.setSmooth(false);

        for (int i = 0; i < interactableViews.length; i++) {

            if (interactables[i].getType().equals("hydrant")) {
                interactableViews[i] = new ImageView(hydrantImage);
            } else if (interactables[i].getType().equals("trashcan")) {
                interactableViews[i] = new ImageView(trashcanImage);
            } else if (interactables[i].getType().equals("bench")) {
                interactableViews[i] = new ImageView(benchImage);
            }

            interactableViews[i].setSmooth(false);
            root.getChildren().add(interactableViews[i]);
        }

        for (int i = 0; i < npcs.length; i++) {
            npcs[i] = new ImageView(npcDownIdle);
            npcs[i].setSmooth(false);
            root.getChildren().add(npcs[i]);
        }

        root.getChildren().add(carViews[0]);

        root.getChildren().add(player);
        root.getChildren().add(buildingOverlay);

        // player's hitbox visuality
//        playerHitboxVisual.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 1));
//        root.getChildren().add(playerHitboxVisual);

        player.setX(playerObject.getWorldX());
        player.setY(playerObject.getWorldY());

        for (int i = 0; i < interactableViews.length; i++) {
            interactableViews[i].setX(interactables[i].getWorldX());
            interactableViews[i].setY(interactables[i].getWorldY());
            scale(interactableViews[i], 2);
        }

        for (int i = 0; i < npcs.length; i++) {
            npcs[i].setX(npcObjects[i].getWorldX());
            npcs[i].setY(npcObjects[i].getWorldY());
        }

        carViews[0].setX(cars[0].getWorldX());
        carViews[0].setY(cars[0].getWorldY());

        scale(player, 2);
        for (int i = 0; i < npcs.length; i++) {
            scale(npcs[i], 2);
        }

        scale(carViews[0], 1.8);

        carHitboxVisual = new Polygon();

        // Hitbox visibility
        carHitboxVisual.setVisible(false);

        carHitboxVisual.setFill(Color.rgb(255, 0, 0, 0.20));
        carHitboxVisual.setStroke(Color.RED);
        carHitboxVisual.setStrokeWidth(1.5);

        root.getChildren().add(carHitboxVisual);

        world.getTransforms().add(new Scale(2, 2, 0, 0));
        buildingOverlay.getTransforms().add(new Scale(2, 2, 0, 0));


//// 1. Fill the map with grass first
//        for (int row = 0; row < map.length; row++) {
//            for (int column = 0; column < map[row].length; column++) {
//                map[row][column] = GRASS;
//            }
//        }
//
//// 2. Main horizontal avenue
//        for (int row = 38; row < 42; row++) {
//            for (int column = 0; column < 96; column++) {
//                map[row][column] = ROAD_HORIZONTAL;
//            }
//        }
//
//// 3. Main vertical avenue
//        for (int row = 0; row < 80; row++) {
//            for (int column = 46; column < 50; column++) {
//                map[row][column] = ROAD_VERTICAL;
//            }
//        }
//
//// 4. Secondary horizontal streets
//        for (int row = 18; row < 22; row++) {
//            for (int column = 4; column < 92; column++) {
//                map[row][column] = ROAD_HORIZONTAL;
//            }
//        }
//
//        for (int row = 60; row < 64; row++) {
//            for (int column = 4; column < 92; column++) {
//                map[row][column] = ROAD_HORIZONTAL;
//            }
//        }
//
//// 5. Secondary vertical streets
//        for (int row = 4; row < 76; row++) {
//            for (int column = 18; column < 22; column++) {
//                map[row][column] = ROAD_VERTICAL;
//            }
//        }
//
//        for (int row = 4; row < 76; row++) {
//            for (int column = 74; column < 78; column++) {
//                map[row][column] = ROAD_VERTICAL;
//            }
//        }
//
//// 6. Sidewalks beside the main avenues
//        for (int row = 36; row < 38; row++) {
//            for (int column = 0; column < 96; column++) {
//                map[row][column] = SIDEWALK;
//            }
//        }
//
//        for (int row = 42; row < 44; row++) {
//            for (int column = 0; column < 96; column++) {
//                map[row][column] = SIDEWALK;
//            }
//        }
//
//        for (int row = 0; row < 80; row++) {
//            for (int column = 44; column < 46; column++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int column = 50; column < 52; column++) {
//                map[row][column] = SIDEWALK;
//            }
//        }
//
//// 7. Sidewalks beside secondary streets
//        for (int column = 4; column < 92; column++) {
//            for (int row = 16; row < 18; row++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int row = 22; row < 24; row++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int row = 58; row < 60; row++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int row = 64; row < 66; row++) {
//                map[row][column] = SIDEWALK;
//            }
//        }
//
//        for (int row = 4; row < 76; row++) {
//            for (int column = 16; column < 18; column++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int column = 22; column < 24; column++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int column = 72; column < 74; column++) {
//                map[row][column] = SIDEWALK;
//            }
//
//            for (int column = 78; column < 80; column++) {
//                map[row][column] = SIDEWALK;
//            }
//        }
//
//// 8. Mark intersections
//        for (int row = 0; row < 80; row++) {
//            for (int column = 0; column < 96; column++) {
//                boolean horizontal =
//                        map[row][column] == ROAD_HORIZONTAL;
//
//                boolean vertical =
//                        map[row][column] == ROAD_VERTICAL;
//
//                if (horizontal && vertical) {
//                    map[row][column] = INTERSECTION;
//                }
//            }
//        }
//
//// 9. Central park
//        for (int row = 24; row < 36; row++) {
//            for (int column = 54; column < 72; column++) {
//                map[row][column] = GRASS;
//            }
//        }
//
//// 10. Place individual buildings (4 × 4 tiles each)
//        int[][] buildingPositions = {
//                // Northwest residential
//                {4, 4}, {9, 4}, {4, 10}, {9, 10},
//                {26, 4}, {31, 4}, {26, 10}, {31, 10},
//
//                // Southwest residential
//                {4, 26}, {9, 26}, {26, 26}, {31, 26},
//                {4, 46}, {9, 46}, {26, 46}, {31, 46},
//                {4, 68}, {9, 68}, {26, 68}, {31, 68},
//
//                // Eastern commercial district
//                {54, 4}, {63, 4}, {82, 4}, {87, 4},
//                {54, 10}, {63, 10}, {82, 10}, {87, 10},
//                {82, 26}, {87, 26},
//                {82, 46}, {87, 46},
//                {82, 68}, {87, 68}
//        };
//
//        for (int i = 0; i < buildingPositions.length; i++) {
//            int startColumn = buildingPositions[i][0];
//            int startRow = buildingPositions[i][1];
//
//            for (int row = 0; row < 4; row++) {
//                for (int column = 0; column < 4; column++) {
//                    map[startRow + row][startColumn + column] = BUILDING;
//                }
//            }
//        }
//
//
//        for (int row = 0; row < map.length; row++) {
//            for (int column = 0; column < map[row].length; column++) {
//                if (map[row][column] == GRASS) {
//                    Rectangle grassTile = new Rectangle(column * 16, row * 16, 16, 16);
//                    grassTile.setFill(new ImagePattern(grassImage));
//                    world.getChildren().add(grassTile);
//                }
//
//                if (map[row][column] == ROAD_HORIZONTAL) {
//
//                    ImageView roadTile = new ImageView(roadHorizontalImage);
//
//                    roadTile.setX(column * 16);
//                    roadTile.setY(row * 16);
//                    roadTile.setFitWidth(16);
//                    roadTile.setFitHeight(16);
//                    world.getChildren().add(roadTile);
//                }
//
//                if (map[row][column] == ROAD_VERTICAL) {
//
//                    ImageView roadTile = new ImageView(roadVerticalImage);
//
//                    roadTile.setX(column * 16);
//                    roadTile.setY(row * 16);
//                    roadTile.setFitWidth(16);
//                    roadTile.setFitHeight(16);
//                    world.getChildren().add(roadTile);
//                }
//
//                if (map[row][column] == SIDEWALK) {
//                    ImageView sideWalkTile = new ImageView(sideWalkImage);
//                    sideWalkTile.setX(column * 16);
//                    sideWalkTile.setY(row * 16);
//                    sideWalkTile.setFitWidth(16);
//                    sideWalkTile.setFitHeight(16);
//                    world.getChildren().add(sideWalkTile);
//                }
//
//                if (map[row][column] == INTERSECTION) {
//                    ImageView intersectionTile = new ImageView(intersectionImage);
//                    intersectionTile.setX(column * 16);
//                    intersectionTile.setY(row * 16);
//                    intersectionTile.setFitWidth(16);
//                    intersectionTile.setFitHeight(16);
//                    world.getChildren().add(intersectionTile);
//                }
//                else if (map[row][column] == ROAD_CORNER_BL) {
//                    ImageView tile = new ImageView(roadCornerBLImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == ROAD_CORNER_BR) {
//                    ImageView tile = new ImageView(roadCornerBRImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == ROAD_CORNER_TL) {
//                    ImageView tile = new ImageView(roadCornerTLImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == ROAD_CORNER_TR) {
//                    ImageView tile = new ImageView(roadCornerTRImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == SIDEWALK_CORNER_BL) {
//                    ImageView tile = new ImageView(sidewalkCornerBLImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == SIDEWALK_CORNER_BR) {
//                    ImageView tile = new ImageView(sidewalkCornerBRImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == SIDEWALK_CORNER_TL) {
//                    ImageView tile = new ImageView(sidewalkCornerTLImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//                else if (map[row][column] == SIDEWALK_CORNER_TR) {
//                    ImageView tile = new ImageView(sidewalkCornerTRImage);
//                    tile.setX(column * 16);
//                    tile.setY(row * 16);
//                    world.getChildren().add(tile);
//                }
//
//                if (map[row][column] == BUILDING
//                        && (row == 0 || map[row - 1][column] != BUILDING)
//                        && (column == 0 || map[row][column - 1] != BUILDING)) {
//                    addBuilding(world, buildingOverlay, roofImage, wallImage, column, row);
//                }
//            }
//        }
//
//        // Trees
//        addProp(world, treeImage, 2, 2, 32, 32);
//        addProp(world, treeImage, 9, 9, 32, 32);
//        addProp(world, treeImage, 17, 6, 32, 32);
//        addProp(world, treeImage, 27, 9, 32, 32);
//        addProp(world, treeImage, 34, 5, 32, 32);
//        addProp(world, treeImage, 41, 9, 32, 32);
//        addProp(world, treeImage, 53, 9, 32, 32);
//        addProp(world, treeImage, 60, 5, 32, 32);
//
//        addProp(world, treeImage, 2, 27, 32, 32);
//        addProp(world, treeImage, 9, 29, 32, 32);
//        addProp(world, treeImage, 17, 25, 32, 32);
//        addProp(world, treeImage, 27, 28, 32, 32);
//        addProp(world, treeImage, 34, 27, 32, 32);
//        addProp(world, treeImage, 41, 24, 32, 32);
//        addProp(world, treeImage, 53, 28, 32, 32);
//        addProp(world, treeImage, 60, 24, 32, 32);
//
//        addProp(world, treeImage, 2, 46, 32, 32);
//        addProp(world, treeImage, 9, 42, 32, 32);
//        addProp(world, treeImage, 17, 46, 32, 32);
//        addProp(world, treeImage, 27, 47, 32, 32);
//        addProp(world, treeImage, 34, 45, 32, 32);
//        addProp(world, treeImage, 41, 47, 32, 32);
//        addProp(world, treeImage, 53, 46, 32, 32);
//        addProp(world, treeImage, 60, 42, 32, 32);
//
//        // Streetlights
//        addProp(world, streetlightImage, 18, 10, 16, 32);
//        addProp(world, streetlightImage, 24, 10, 16, 32);
//        addProp(world, streetlightImage, 42, 10, 16, 32);
//        addProp(world, streetlightImage, 48, 10, 16, 32);
//
//        addProp(world, streetlightImage, 18, 18, 16, 32);
//        addProp(world, streetlightImage, 24, 18, 16, 32);
//        addProp(world, streetlightImage, 42, 18, 16, 32);
//        addProp(world, streetlightImage, 48, 18, 16, 32);
//
//        addProp(world, streetlightImage, 18, 30, 16, 32);
//        addProp(world, streetlightImage, 24, 30, 16, 32);
//        addProp(world, streetlightImage, 42, 30, 16, 32);
//        addProp(world, streetlightImage, 48, 30, 16, 32);
//
//        addProp(world, streetlightImage, 18, 38, 16, 32);
//        addProp(world, streetlightImage, 24, 38, 16, 32);
//        addProp(world, streetlightImage, 42, 38, 16, 32);
//        addProp(world, streetlightImage, 48, 38, 16, 32);
//
//        // Hydrants
//        addProp(world, hydrantImage, 19, 11, 16, 16);
//        addProp(world, hydrantImage, 25, 17, 16, 16);
//        addProp(world, hydrantImage, 43, 11, 16, 16);
//        addProp(world, hydrantImage, 49, 17, 16, 16);
//
//        addProp(world, hydrantImage, 19, 31, 16, 16);
//        addProp(world, hydrantImage, 25, 37, 16, 16);
//        addProp(world, hydrantImage, 43, 31, 16, 16);
//        addProp(world, hydrantImage, 49, 37, 16, 16);

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

    public boolean carCollidesWithBuilding(Car car, Rectangle building) {
        double centerX = car.getWorldX() + 20;
        double centerY = car.getWorldY() + 36;
        double radius = 24;

        double closestX = Math.max(building.getX(), Math.min(centerX, building.getX() + building.getWidth()));

        double closestY = Math.max(building.getY(), Math.min(centerY, building.getY() + building.getHeight()));

        double distanceX = centerX - closestX;
        double distanceY = centerY - closestY;

        return distanceX * distanceX + distanceY * distanceY < radius * radius;
    }

    private boolean isSafeExit(double x, double y) {
        if (x + HITBOX_OFFSET_X < 0
                || y + HITBOX_OFFSET_Y < 0
                || x + HITBOX_OFFSET_X + HITBOX_WIDTH > map[0].length * 32
                || y + HITBOX_OFFSET_Y + HITBOX_HEIGHT > map.length * 32) {
            return false;
        }

        Rectangle exitHitbox = new Rectangle(
                x + HITBOX_OFFSET_X,
                y + HITBOX_OFFSET_Y,
                HITBOX_WIDTH,
                HITBOX_HEIGHT
        );

        for (Rectangle building : buildingHitbox) {
            if (building != null && checkCollision(exitHitbox, building)) {
                return false;
            }
        }

        return true;
    }
}