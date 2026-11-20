package com.minigta;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.geometry.Point2D;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

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

    private boolean editorMode = false;
    private boolean f1Down = false;
    private boolean oDown = false;

    private boolean nDown = false;
    private boolean tabDown = false;

    private boolean hasDragged = false;

    private boolean isDriving = false;
    private int currentCar = -1;

    private boolean fPressed = false;
    private boolean fWasPressed = false;

    private long lastTime = 0;
    private double deltaTime;

    private Rectangle[] buildingHitbox = new Rectangle[2000];
    private int buildingCount = 0;

    private Rectangle[] buildingHitboxVisual = new Rectangle[2000];
    private int selectedHitboxIndex = -1;

    private Rectangle hoverTile;

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

    // Occlusion zones
    private final List<Rectangle> occlusionZones = new ArrayList<>();
    private final List<Rectangle> occlusionZoneVisuals = new ArrayList<>();

    private boolean occlusionEditorMode = false;
    private int selectedOcclusionZoneIndex = -1;

    private static double cameraX = 0;
    private static double cameraY = 0;
    private double tileX = 0;
    private double tileY = 0;

    private ImageView player;

    private Image playerUpIdle;
    private Image playerDownIdle;
    private Image playerLeftIdle;
    private Image playerRightIdle;

    private Image[] playerUpWalk = new Image[5];
    private Image[] playerDownWalk = new Image[5];
    private Image[] playerLeftWalk = new Image[5];
    private Image[] playerRightWalk = new Image[5];

    private ImageView[] npcs = new ImageView[50];
    private NPC[] npcObjects = new NPC[50];

    private NPC frankie;
    private ImageView frankieView;
    private boolean frankieActive = false;

    private boolean prologueStarted = false;

    private Circle[] npcMarkers = new Circle[50];
    private double[] npcMarkerTimers = new double[50];

    // Driver NPCs
    private final List<NPC> driverNPCs = new ArrayList<>();
    private final List<ImageView> driverNPCViews = new ArrayList<>();

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
    private Text coordinateText;
    private Text capturedCoordinateText;

    private Polygon carHitboxVisual;

    private boolean drawingHitbox = false;

    private double hitboxStartX;
    private double hitboxStartY;

    private Rectangle hitboxPreview;

    // Traffic route editor
    private boolean trafficEditorMode = false;

    // Multiple traffic routes
    private final List<List<Point2D>> trafficRoutes = new ArrayList<>();

    private final List<List<Circle>> trafficRouteWaypointVisuals = new ArrayList<>();

    private final List<List<Line>> trafficRouteLineVisuals = new ArrayList<>();

    // Colors assigned to traffic routes
    private final Color[] trafficRouteColors = {Color.LIMEGREEN, Color.DODGERBLUE, Color.ORANGE, Color.MAGENTA, Color.YELLOW};

    // Index of the currently selected route
    private int selectedTrafficRoute = 0;
    private int nextTrafficRouteIndex = 0;

    // Traffic AI
    private final List<TrafficCar> trafficCars = new ArrayList<>();
    private final List<TrafficController> trafficControllers = new ArrayList<>();
    private final List<ImageView> trafficCarViews = new ArrayList<>();

    private final List<TrafficCar> parkedCars = new ArrayList<>();
    private final List<ImageView> parkedCarViews = new ArrayList<>();

    // Traffic vehicle sprites
    private Image redCarImage, blueCarImage, blueTruckImage, pinkCarImage, turquoiseCarImage, carBlueImage,
            carBlueDarkImage, carGrayImage, carGreenImage, carOrangeImage, carRedImage, carSportsRedImage,
            carTealImage, carWhiteImage, carYellowImage, constructionVehImage, vanImage, helicopterImage,
            fourXFourImage, breakdownTruckImage, bugImage, coachImage, cossieImage, counthashImage, italiImage,
            jugularImage, mundanoImage, penetratorImage, pickUpImage, porkaTurboImage, repairVanImage, stallionImage,
            trainImage, tramImage;

    private double trafficSpawnTimer = 0;

    private static final double TRAFFIC_SPAWN_INTERVAL = 3.0;
    private static final int MAX_TRAFFIC_CARS = 100;

    private DialogueSystem dialogueSystem;
    private boolean cutsceneActive = false;
    private boolean gameStarted = false;
    private boolean gamePaused = false;
    private int destinationMarker = 0;

    private boolean drugDealerReached = false;
    private boolean dealerDialogueActive = false;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        // Create the first traffic route
        trafficRoutes.add(new ArrayList<>());
        trafficRouteWaypointVisuals.add(new ArrayList<>());
        trafficRouteLineVisuals.add(new ArrayList<>());

        Group world = new Group();
        Group foregroundWorld = new Group();

        Group baseLayer = new Group();
        Group backgroundLayer = new Group();
        Group objectLayer = new Group();
        Group foregroundLayer = new Group();
        Group buildingOverlay = new Group();

        Group gridLayer = new Group();
        gridLayer.setVisible(false);

        world.getChildren().addAll(
                baseLayer,
                backgroundLayer,
                objectLayer,
                buildingOverlay,
                gridLayer
        );

        root.getChildren().add(world);

        root.setOnMouseMoved(event -> {
            if (!editorMode) {
                coordinateText.setVisible(false);
                hoverTile.setVisible(false);
                return;
            }

            // Converts the mouse's Scene coordinates (screen position) into local coordinates relative to world
            Point2D mouseWorld = world.sceneToLocal(
                    event.getSceneX(),
                    event.getSceneY()
            );

            double mouseWorldX = mouseWorld.getX();
            double mouseWorldY = mouseWorld.getY();

            int gridX = (int) Math.floor(mouseWorldX / 4);
            int gridY = (int) Math.floor(mouseWorldY / 4);

            coordinateText.setText(
                    String.format(
                            "World: (%.1f, %.1f) | Grid: (%d, %d)",
                            mouseWorldX * 2, mouseWorldY * 2, gridX, gridY
                    )
            );

            coordinateText.setX(20);
            coordinateText.setY(60);
            coordinateText.setVisible(true);

            tileX = Math.floor(mouseWorld.getX() / 4) * 4;
            tileY = Math.floor(mouseWorld.getY() / 4) * 4;

            hoverTile.setX(tileX);
            hoverTile.setY(tileY);
            hoverTile.setVisible(true);
        });

        root.setOnMouseClicked(event -> {
            if (!editorMode) {
                return;
            }

            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (hasDragged) {
                hasDragged = false;
                return;
            }

            // Converts the mouse's Scene coordinates (screen position) into local coordinates relative to world
            Point2D mouseMap = world.sceneToLocal(
                    event.getSceneX(),
                    event.getSceneY()
            );

            // Traffic waypoint placement
            if (trafficEditorMode) {

                // Get the currently selected route and its visual lists
                // This retrieves the currently selected traffic route.
                List<Point2D> currentRoute = trafficRoutes.get(selectedTrafficRoute);

                List<Circle> currentMarkers = trafficRouteWaypointVisuals.get(selectedTrafficRoute);

                List<Line> currentLines = trafficRouteLineVisuals.get(selectedTrafficRoute);

                // Get the color assigned to this route
                Color routeColor = trafficRouteColors[selectedTrafficRoute % trafficRouteColors.length];

                double gridSpacing = 4;

                double snappedX =
                        Math.floor(mouseMap.getX() / gridSpacing) * gridSpacing;

                double snappedY =
                        Math.floor(mouseMap.getY() / gridSpacing) * gridSpacing;

                // Creates a waypoint using world coordinates.
                Point2D waypoint = new Point2D(
                        snappedX * 2 + gridSpacing,
                        snappedY * 2 + gridSpacing
                );

                // Connect this waypoint to the previous one
                if (!currentRoute.isEmpty()) {

                    Point2D previous =
                            currentRoute.get(currentRoute.size() - 1);

                    Line routeLine = new Line(
                            previous.getX() / 2,
                            previous.getY() / 2,
                            waypoint.getX() / 2,
                            waypoint.getY() / 2
                    );

                    routeLine.setStroke(routeColor);
                    routeLine.setStrokeWidth(1.5);

                    currentLines.add(routeLine);
                    gridLayer.getChildren().add(routeLine);
                }

                // Save the stuffs
                // It adds the waypoint to the route and saves the route to a file.
                //It also creates a Circle to visually represent the waypoint
                currentRoute.add(waypoint);
                saveTrafficRoutes();

                // Create its visual marker
                Circle marker = new Circle(
                        snappedX + gridSpacing / 2,
                        snappedY + gridSpacing / 2,
                        3
                );

                marker.setFill(routeColor);
                marker.setStroke(Color.WHITE);
                marker.setStrokeWidth(0.8);

                currentMarkers.add(marker);
                gridLayer.getChildren().add(marker);

                capturedCoordinateText.setText(
                        "Route " + (selectedTrafficRoute + 1)
                                + ": waypoint #" + currentRoute.size()
                );

                capturedCoordinateText.setVisible(true);
                return;
            }

            if (hasDragged) {
                hasDragged = false;
                return;
            }

            double mouseWorldX = mouseMap.getX() * 2;
            double mouseWorldY = mouseMap.getY() * 2;

            // Handle occlusion-zone editing
            if (occlusionEditorMode) {

                selectedOcclusionZoneIndex = -1;

                // Check whether the click selects an existing occlusion zone
                for (int i = 0; i < occlusionZoneVisuals.size(); i++) {

                    if (occlusionZoneVisuals.get(i).contains(mouseMap.getX(), mouseMap.getY())) {

                        selectedOcclusionZoneIndex = i;
                        break;
                    }
                }

                // Highlight the selected zone
                for (int i = 0; i < occlusionZoneVisuals.size(); i++) {

                    if (i == selectedOcclusionZoneIndex) {
                        occlusionZoneVisuals.get(i).setStroke(Color.YELLOW);
                        occlusionZoneVisuals.get(i).setStrokeWidth(2);
                    } else {
                        occlusionZoneVisuals.get(i).setStroke(Color.DEEPSKYBLUE);
                        occlusionZoneVisuals.get(i).setStrokeWidth(1);
                    }
                }

                if (selectedOcclusionZoneIndex != -1) {

                    capturedCoordinateText.setText(
                            "Selected occlusion zone #" +
                                    (selectedOcclusionZoneIndex + 1) +
                                    " | Press DELETE to remove"
                    );

                    capturedCoordinateText.setVisible(true);
                    return;
                }

                // Create a small zone when the user performs a normal click
                double gridSpacing = 4;

                double snappedX = Math.floor(mouseMap.getX() / gridSpacing) * gridSpacing;
                double snappedY = Math.floor(mouseMap.getY() / gridSpacing) * gridSpacing;

                Rectangle visual = new Rectangle(
                        snappedX,
                        snappedY,
                        gridSpacing,
                        gridSpacing
                );

                visual.setFill(Color.rgb(0, 180, 255, 0.25));
                visual.setStroke(Color.DEEPSKYBLUE);
                visual.setStrokeWidth(1);

                Rectangle zone = new Rectangle(
                        snappedX * 2,
                        snappedY * 2,
                        gridSpacing * 2,
                        gridSpacing * 2
                );

                gridLayer.getChildren().add(visual);

                occlusionZoneVisuals.add(visual);
                occlusionZones.add(zone);

                saveOcclusionZones();

                capturedCoordinateText.setText(
                        "Created occlusion zone #" + occlusionZones.size()
                );

                capturedCoordinateText.setVisible(true);
                return;
            }

            // Check whether the click hits an existing hitbox
            selectedHitboxIndex = -1;

            for (int i = 0; i < buildingCount; i++) {
                if (buildingHitboxVisual[i].contains(
                        mouseMap.getX(), mouseMap.getY())) {

                    selectedHitboxIndex = i;
                    break;
                }
            }

            // Highlight the selected hitbox
            for (int i = 0; i < buildingCount; i++) {
                if (i == selectedHitboxIndex) {
                    buildingHitboxVisual[i].setStroke(Color.YELLOW);
                    buildingHitboxVisual[i].setStrokeWidth(2);
                } else {
                    buildingHitboxVisual[i].setStroke(Color.RED);
                    buildingHitboxVisual[i].setStrokeWidth(1);
                }
            }

            if (selectedHitboxIndex != -1) {
                capturedCoordinateText.setText(
                        "Selected hitbox #" + (selectedHitboxIndex + 1)
                                + " | Press DELETE to remove"
                );
                capturedCoordinateText.setVisible(true);
                return;
            }

            // No existing hitbox was selected, so create a new one.
            if (buildingCount >= buildingHitbox.length) {
                capturedCoordinateText.setText("Hitbox limit reached!");
                return;
            }

            double gridSpacing = 4;

            // Snap the position to the top-left corner of the grid cell
            double snappedX = Math.floor(mouseMap.getX() / gridSpacing) * gridSpacing;
            double snappedY = Math.floor(mouseMap.getY() / gridSpacing) * gridSpacing;

            Rectangle visual = new Rectangle(
                    snappedX,
                    snappedY,
                    gridSpacing,
                    gridSpacing
            );

            visual.setFill(Color.rgb(255, 0, 0, 0.25));
            visual.setStroke(Color.RED);
            visual.setStrokeWidth(1);

            Rectangle collision = new Rectangle(
                    visual.getX() * 2,
                    visual.getY() * 2,
                    visual.getWidth() * 2,
                    visual.getHeight() * 2
            );

            gridLayer.getChildren().add(visual);

            buildingHitbox[buildingCount] = collision;
            buildingHitboxVisual[buildingCount] = visual;

            buildingCount++;

            saveHitboxes();

            capturedCoordinateText.setText("Created hitbox #" + buildingCount);
        });

        root.setOnMousePressed(event -> {
            hasDragged = false;

            if (!editorMode || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (trafficEditorMode) {
                drawingHitbox = false;
                hitboxPreview.setVisible(false);
                return;
            }

            Point2D mouseWorld = world.sceneToLocal(
                    event.getSceneX(),
                    event.getSceneY()
            );

            hitboxStartX = mouseWorld.getX();
            hitboxStartY = mouseWorld.getY();

            drawingHitbox = true;

            hitboxPreview.setX(hitboxStartX);
            hitboxPreview.setY(hitboxStartY);
            hitboxPreview.setWidth(0);
            hitboxPreview.setHeight(0);

            // Change preview color depending on editor mode
            if (occlusionEditorMode) {
                hitboxPreview.setFill(Color.rgb(0, 180, 255, 0.25));
                hitboxPreview.setStroke(Color.DEEPSKYBLUE);
            } else {
                hitboxPreview.setFill(Color.rgb(255, 0, 0, 0.25));
                hitboxPreview.setStroke(Color.RED);
            }

            hitboxPreview.setVisible(true);
        });

        root.setOnMouseDragged(event -> {
            hasDragged = true;

            if (!editorMode || !drawingHitbox) {
                return;
            }

            Point2D mouseWorld = world.sceneToLocal(
                    event.getSceneX(),
                    event.getSceneY()
            );

            double currentX = mouseWorld.getX();
            double currentY = mouseWorld.getY();


            // These calculations ensure that the rectangle always has a valid top-left position and
            // positive width and height.
            double left = Math.min(hitboxStartX, currentX);
            double top = Math.min(hitboxStartY, currentY);

            double width = Math.abs(currentX - hitboxStartX);
            double height = Math.abs(currentY - hitboxStartY);

            hitboxPreview.setX(left);
            hitboxPreview.setY(top);
            hitboxPreview.setWidth(width);
            hitboxPreview.setHeight(height);
        });

        root.setOnMouseReleased(event -> {
            if (!drawingHitbox) {
                return;
            }

            drawingHitbox = false;

            // Ignore a normal click or an extremely small drag
            if (hitboxPreview.getWidth() < 4 || hitboxPreview.getHeight() < 4) {

                hitboxPreview.setVisible(false);
                return;
            }

            // Create an occlusion zone instead of a building hitbox
            if (occlusionEditorMode) {

                if (occlusionZones.size() >= 2000) {
                    capturedCoordinateText.setText("Occlusion zone limit reached!");
                    capturedCoordinateText.setVisible(true);
                    hitboxPreview.setVisible(false);
                    return;
                }

                Rectangle savedZoneVisual = new Rectangle(
                        hitboxPreview.getX(),
                        hitboxPreview.getY(),
                        hitboxPreview.getWidth(),
                        hitboxPreview.getHeight()
                );

                savedZoneVisual.setFill(Color.rgb(0, 180, 255, 0.25));
                savedZoneVisual.setStroke(Color.DEEPSKYBLUE);
                savedZoneVisual.setStrokeWidth(1);

                // Convert map coordinates into world coordinates
                Rectangle savedZone = new Rectangle(
                        savedZoneVisual.getX() * 2,
                        savedZoneVisual.getY() * 2,
                        savedZoneVisual.getWidth() * 2,
                        savedZoneVisual.getHeight() * 2
                );

                gridLayer.getChildren().add(savedZoneVisual);

                occlusionZoneVisuals.add(savedZoneVisual);
                occlusionZones.add(savedZone);

                saveOcclusionZones();

                capturedCoordinateText.setText(
                        "Created occlusion zone #" + occlusionZones.size()
                );

                capturedCoordinateText.setVisible(true);

                hitboxPreview.setVisible(false);
                return;
            }

            // Check capacity before creating the permanent hitbox
            if (buildingCount >= buildingHitbox.length) {
                capturedCoordinateText.setText("Hitbox limit reached!");
                capturedCoordinateText.setVisible(true);
                hitboxPreview.setVisible(false);
                return;
            }

            // Create a permanent copy of the preview
            Rectangle savedHitbox = new Rectangle(
                    hitboxPreview.getX(),
                    hitboxPreview.getY(),
                    hitboxPreview.getWidth(),
                    hitboxPreview.getHeight()
            );

            savedHitbox.setFill(Color.rgb(255, 0, 0, 0.25));
            savedHitbox.setStroke(Color.RED);
            savedHitbox.setStrokeWidth(1);

            Rectangle collisionHitbox = new Rectangle(
                    savedHitbox.getX() * 2,
                    savedHitbox.getY() * 2,
                    savedHitbox.getWidth() * 2,
                    savedHitbox.getHeight() * 2
            );

            gridLayer.getChildren().add(savedHitbox);

            buildingHitbox[buildingCount] = collisionHitbox;
            buildingHitboxVisual[buildingCount] = savedHitbox;

            buildingCount++;

            saveHitboxes();

            // Hide the reusable preview
            hitboxPreview.setVisible(false);
        });

        Image cityMapImage = new Image(
                getClass().getResource("/map/base/city_map.png").toExternalForm()
        );

        ImageView cityMapView = new ImageView(cityMapImage);

        cityMapView.setFitWidth(2880);
        cityMapView.setFitHeight(1920);
        cityMapView.setPreserveRatio(true);
        cityMapView.setSmooth(false);

        baseLayer.getChildren().add(cityMapView);

        Image apartmentBlue = new Image(
                getClass().getResource(
                        "/map/objects/buildings/apartment_blue.png"
                ).toExternalForm()
        );

        // Create editor grid
        double gridSpacing = 4; // 8 world units after the 2x world scaling

        double mapWidth = 2880;
        double mapHeight = 1920;

        // Vertical lines
        for (double x = 0; x <= mapWidth; x += gridSpacing) {
            Line line = new Line(x, 0, x, mapHeight);
            line.setStroke(Color.rgb(255, 255, 255, 0.25));
            line.setStrokeWidth(0.5);

            gridLayer.getChildren().add(line);
        }

        // Horizontal lines
        for (double y = 0; y <= mapHeight; y += gridSpacing) {
            Line line = new Line(0, y, mapWidth, y);
            line.setStroke(Color.rgb(255, 255, 255, 0.25));
            line.setStrokeWidth(0.5);

            gridLayer.getChildren().add(line);
        }

        root.setStyle("-fx-background-color: #ffffff;");
        Scene scene = new Scene(root, 1280, 720);

        interactionText = new Text("Press F to interact");
        interactionText.setVisible(false);
        interactionText.setX(20);
        interactionText.setY(40);
        root.getChildren().add(interactionText);

        coordinateText = new Text();
        coordinateText.setFill(Color.WHITE);
        coordinateText.setVisible(false);
        root.getChildren().add(coordinateText);

        capturedCoordinateText = new Text("Last clicked: None");
        capturedCoordinateText.setFill(Color.YELLOW);
        capturedCoordinateText.setVisible(false);
        root.getChildren().add(capturedCoordinateText);

        hoverTile = new Rectangle(4, 4);
        hoverTile.setFill(Color.rgb(255, 0, 0, 0.35));
        hoverTile.setStroke(Color.RED);
        hoverTile.setStrokeWidth(0.5);
        hoverTile.setVisible(false);

        gridLayer.getChildren().add(hoverTile);

        hitboxPreview = new Rectangle();

        hitboxPreview.setFill(Color.rgb(255, 0, 0, 0.25));
        hitboxPreview.setStroke(Color.RED);
        hitboxPreview.setStrokeWidth(1);

        hitboxPreview.setVisible(false);

        gridLayer.getChildren().add(hitboxPreview);

        Text modeText = new Text("PLAY MODE");
        modeText.setLayoutX(20);
        modeText.setLayoutY(30);
        modeText.setStyle("-fx-font-size: 18px; -fx-fill: white;");
        modeText.setVisible(false);

        root.getChildren().add(modeText);

        // ==================== MINIMAP RADAR ====================

        double radarWidth = 250;
        double radarHeight = 150;
        double radarMargin = 20;

        // Main radar panel
        Pane radarPane = new Pane();

        radarPane.setPrefSize(radarWidth, radarHeight);

        radarPane.setLayoutX(radarMargin);
        radarPane.setLayoutY(scene.getHeight() - radarHeight - radarMargin);

        radarPane.setMouseTransparent(true);

        // Radar background
        Rectangle radarBackground = new Rectangle(radarWidth, radarHeight);

        radarBackground.setArcWidth(12);
        radarBackground.setArcHeight(12);

        radarBackground.setFill(Color.rgb(15, 20, 25, 0.9));
        radarBackground.setStroke(Color.WHITE);
        radarBackground.setStrokeWidth(1.5);

        // Radar world layer
        Pane radarWorld = new Pane();

        radarWorld.setPrefSize(radarWidth, radarHeight);

        // Keep radar content inside its boundaries
        radarWorld.setClip(new Rectangle(radarWidth, radarHeight));

        // Player marker
        Circle radarPlayer = new Circle(radarWidth / 2, radarHeight / 2, 5);

        radarPlayer.setFill(Color.LIMEGREEN);
        radarPlayer.setStroke(Color.WHITE);
        radarPlayer.setStrokeWidth(1);

        // Add radar elements
        radarPane.getChildren().addAll(
                radarBackground,
                radarWorld,
                radarPlayer
        );

        // Add radar above the game world
        root.getChildren().add(radarPane);

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
                case F1 -> {
                    if (!f1Down) {
                        editorMode = !editorMode;
                        f1Down = true;

                        if (editorMode) {
                            if (trafficEditorMode) {
                                modeText.setText("EDITOR MODE - TRAFFIC WAYPOINTS");
                            } else if (occlusionEditorMode) {
                                modeText.setText("EDITOR MODE - OCCLUSION ZONES");
                            } else {
                                modeText.setText("EDITOR MODE - BUILDING HITBOXES");
                            }
                            modeText.setVisible(true);
                        } else {
                            modeText.setText("PLAY MODE");
                            modeText.setVisible(false);
                        }
                    }
                }
                case O -> {
                    if (editorMode && !oDown) {
                        oDown = true;

                        // Cycle: Building -> Occlusion -> Traffic -> Building
                        if (!occlusionEditorMode && !trafficEditorMode) {
                            occlusionEditorMode = true;
                        } else if (occlusionEditorMode) {
                            occlusionEditorMode = false;
                            trafficEditorMode = true;
                        } else {
                            trafficEditorMode = false;
                        }

                        selectedHitboxIndex = -1;
                        selectedOcclusionZoneIndex = -1;

                        // Restore the normal appearance of existing editor objects
                        for (int i = 0; i < buildingCount; i++) {
                            buildingHitboxVisual[i].setStroke(Color.RED);
                            buildingHitboxVisual[i].setStrokeWidth(1);
                        }

                        for (Rectangle visual : occlusionZoneVisuals) {
                            visual.setStroke(Color.DEEPSKYBLUE);
                            visual.setStrokeWidth(1);
                        }

                        // Update the hover tile and mode label
                        if (trafficEditorMode) {
                            hoverTile.setFill(Color.rgb(0, 255, 100, 0.35));
                            hoverTile.setStroke(Color.LIMEGREEN);

                            modeText.setText("EDITOR MODE - TRAFFIC WAYPOINTS");
                            capturedCoordinateText.setText(
                                    "Traffic editing: GREEN markers | Left-click to add"
                            );

                        } else if (occlusionEditorMode) {
                            hoverTile.setFill(Color.rgb(0, 180, 255, 0.35));
                            hoverTile.setStroke(Color.DEEPSKYBLUE);

                            modeText.setText("EDITOR MODE - OCCLUSION ZONES");
                            capturedCoordinateText.setText(
                                    "Occlusion editing: BLUE rectangles"
                            );

                        } else {
                            hoverTile.setFill(Color.rgb(255, 0, 0, 0.35));
                            hoverTile.setStroke(Color.RED);

                            modeText.setText("EDITOR MODE - BUILDING HITBOXES");
                            capturedCoordinateText.setText(
                                    "Building hitbox editing: RED rectangles"
                            );
                        }

                        capturedCoordinateText.setVisible(true);
                    }
                }
                case T -> {
                    System.out.println("T KEY DETECTED!");
                    System.out.println("Editor mode: " + editorMode);
                    System.out.println(
                            "Selected route waypoints: " +
                                    trafficRoutes.get(selectedTrafficRoute).size()
                    );

                    if (!editorMode) {
                        System.out.println("Calling spawnTrafficCar...");
                        spawnTrafficCar(root, false);
                    } else {
                        System.out.println("Cannot spawn: currently in editor mode.");
                    }
                }
                case N -> {
                    if (editorMode && trafficEditorMode && !nDown) {
                        nDown = true;

                        // Create a new route with its own three lists
                        trafficRoutes.add(new ArrayList<>());
                        trafficRouteWaypointVisuals.add(new ArrayList<>());
                        trafficRouteLineVisuals.add(new ArrayList<>());

                        // Select the newly created route
                        selectedTrafficRoute = trafficRoutes.size() - 1;

                        capturedCoordinateText.setText(
                                "Created Route " + (selectedTrafficRoute + 1)
                                        + " | Left-click to add waypoints"
                        );

                        capturedCoordinateText.setVisible(true);

                        System.out.println("Created traffic route: " + (selectedTrafficRoute + 1));

                        updateTrafficRouteAppearance();

                        saveTrafficRoutes();
                    }
                }

                case TAB -> {
                    if (editorMode && trafficEditorMode && !tabDown) {
                        tabDown = true;

                        // Move to the next route
                        selectedTrafficRoute++;

                        // Return to Route 1 after the final route
                        if (selectedTrafficRoute >= trafficRoutes.size()) {
                            selectedTrafficRoute = 0;
                        }

                        List<Point2D> currentRoute =
                                trafficRoutes.get(selectedTrafficRoute);

                        capturedCoordinateText.setText("Selected Route " + (selectedTrafficRoute + 1) + " | Waypoints: " + currentRoute.size());

                        capturedCoordinateText.setVisible(true);

                        System.out.println("Switched to traffic route: " + (selectedTrafficRoute + 1));

                        event.consume();
                        updateTrafficRouteAppearance();
                    }
                }
                case DELETE -> {
                    if (editorMode) {
                        if (trafficEditorMode) {

                            // Get the currently selected route and its visual lists
                            List<Point2D> currentRoute = trafficRoutes.get(selectedTrafficRoute);

                            List<Circle> currentMarkers = trafficRouteWaypointVisuals.get(selectedTrafficRoute);

                            List<Line> currentLines = trafficRouteLineVisuals.get(selectedTrafficRoute);

                            // Only delete if the route contains waypoints
                            if (!currentRoute.isEmpty()) {

                                // Remove the last waypoint
                                currentRoute.remove(currentRoute.size() - 1);

                                // Remove its visual marker
                                if (!currentMarkers.isEmpty()) {
                                    Circle lastMarker =
                                            currentMarkers.remove(currentMarkers.size() - 1);

                                    gridLayer.getChildren().remove(lastMarker);
                                }

                                // Remove the connecting line
                                if (!currentLines.isEmpty()) {
                                    Line lastLine = currentLines.remove(currentLines.size() - 1);

                                    gridLayer.getChildren().remove(lastLine);
                                }

                                capturedCoordinateText.setText("Route " + (selectedTrafficRoute + 1) + ": waypoint removed");

                                saveTrafficRoutes();

                                capturedCoordinateText.setVisible(true);
                            }

                            return;
                        }
                    }
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
                case O -> oDown = false;
                case N -> nDown = false;
                case TAB -> tabDown = false;
                case SPACE -> handbrakeDown = false;
                case F1 -> f1Down = false;
            }
        });

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (!gameStarted) {
                    return;
                }

                if (gamePaused) {
                    return;
                }

                // `now` is the current time in nanoseconds
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Automatically spawn traffic cars
                if (!editorMode && getActiveTrafficCarCount() < MAX_TRAFFIC_CARS) {
                    trafficSpawnTimer += deltaTime;

                    if (trafficSpawnTimer >= TRAFFIC_SPAWN_INTERVAL) {
                        int carsToSpawn = MAX_TRAFFIC_CARS - getActiveTrafficCarCount();

                        for (int i = 0; i < carsToSpawn; i++) {
                            spawnTrafficCar(root, true);
                        }

                        trafficSpawnTimer = 0;
                    }
                }

                gridLayer.setVisible(editorMode);

                if (!isDriving) {
                    double dx = 0;
                    double dy = 0;
                    double speed = editorMode ? 600 : 200;

                    if (!cutsceneActive || editorMode) {
                        if (wDown) dy--;
                        if (sDown) dy++;
                        if (aDown) dx--;
                        if (dDown) dx++;
                    }

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

                    double movement = speed * deltaTime;

                    double length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    double oldX = playerObject.getWorldX();

                    playerObject.moveX(dx * movement);

                    playerHitbox.setX(playerObject.getWorldX() + HITBOX_OFFSET_X);
                    playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);

                    if (!editorMode) {
                        for (Rectangle building : buildingHitbox) {
                            if (building != null && checkCollision(playerHitbox, building)) {
                                playerObject.setWorldX(oldX);

                                playerHitbox.setX(playerObject.getWorldX() + HITBOX_OFFSET_X);
                                playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);

                                break;
                            }
                        }
                    }

                    double oldY = playerObject.getWorldY();

                    playerObject.moveY(dy * movement);

                    playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);

                    if (!editorMode) {
                        for (Rectangle building : buildingHitbox) {
                            if (building != null && checkCollision(playerHitbox, building)) {

                                playerObject.setWorldY(oldY);
                                playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);
                                break;
                            }
                        }
                    }
                }

                // Hide the player when inside a building's occlusion zone
                if (!isDriving) {
                    player.setVisible(editorMode || !isPlayerInsideOcclusionZone());
                }

                if (!editorMode && isDriving && !cutsceneActive) {
                    Car car = getCurrentCar();
                    car.setHandbrakeActive(handbrakeDown);

                    boolean braking = drivingS && car.getSpeed() > 0;

                    if (drivingW) car.accelerate(deltaTime);
                    else if (drivingS) car.reverse(deltaTime);
                    else if (handbrakeDown) car.handbrake(deltaTime);
                    else car.applyFriction(deltaTime);

                    if (drivingA) car.steerLeft(deltaTime, braking);

                    if (drivingD) car.steerRight(deltaTime, braking);

                    double previousX = car.getWorldX();
                    double previousY = car.getWorldY();

                    car.move(deltaTime);

                    // Check collisions with AI traffic cars
                    for (TrafficCar trafficCar : trafficCars) {

                        // Ignore the controlled car and parked vehicles
                        if (trafficCar == car || trafficCar.isParked() || trafficCar.isPlayerControlled()) {
                            continue;
                        }

                        Rectangle trafficHitbox = new Rectangle(
                                trafficCar.getWorldX(),
                                trafficCar.getWorldY(),
                                23,
                                40
                        );

                        if (car.intersects(trafficHitbox)) {

                            double oldDX = previousX - trafficCar.getWorldX();
                            double oldDY = previousY - trafficCar.getWorldY();

                            double newDX = car.getWorldX() - trafficCar.getWorldX();
                            double newDY = car.getWorldY() - trafficCar.getWorldY();

                            double oldDistance = oldDX * oldDX + oldDY * oldDY;
                            double newDistance = newDX * newDX + newDY * newDY;

                            // Only block movement if it makes the overlap worse
                            if (newDistance < oldDistance) {
                                car.setWorldX(previousX);
                                car.setWorldY(previousY);
                            }

                            break;
                        }
                    }

                    for (Rectangle building : buildingHitbox) {
                        if (building != null && car.resolveCollision(building)) {
                            break;
                        }
                    }

                    getCurrentCarView().setRotate(car.getAngle());
                }

                double cameraTargetX;
                double cameraTargetY;

                if (isDriving) {
                    Car car = getCurrentCar();

                    cameraTargetX = car.getWorldX();
                    cameraTargetY = car.getWorldY();
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

                // ==================== UPDATE MINIMAP RADAR ====================

                // Clear previous radar objects
                radarWorld.getChildren().clear();

                // Find the player's current world position
                double focusX;
                double focusY;

                if (isDriving) {
                    Car currentVehicle = getCurrentCar();

                    focusX = currentVehicle.getWorldX() + 11.5;
                    focusY = currentVehicle.getWorldY() + 20;
                } else {
                    focusX = playerObject.getWorldX() + 24;
                    focusY = playerObject.getWorldY() + 24;
                }

                // Convert world distances into radar distances
                double radarScale = 0.12;

                // -------------------- BUILDINGS --------------------

                for (int i = 0; i < buildingCount; i++) {

                    Rectangle building = buildingHitbox[i];

                    if (building == null) {
                        continue;
                    }

                    double radarX = radarWidth / 2 + (building.getX() - focusX) * radarScale;

                    double radarY = radarHeight / 2 + (building.getY() - focusY) * radarScale;

                    double radarBuildingWidth = building.getWidth() * radarScale;

                    double radarBuildingHeight = building.getHeight() * radarScale;

                    // Skip buildings outside the radar
                    if (radarX + radarBuildingWidth < 0
                            || radarX > radarWidth
                            || radarY + radarBuildingHeight < 0
                            || radarY > radarHeight) {

                        continue;
                    }

                    Rectangle radarBuilding = new Rectangle(
                            radarX,
                            radarY,
                            radarBuildingWidth,
                            radarBuildingHeight
                    );

                    radarBuilding.setFill(Color.rgb(120, 130, 140));

                    radarWorld.getChildren().add(radarBuilding);
                }

                // -------------------- AI TRAFFIC CARS --------------------

                for (TrafficCar trafficCar : trafficCars) {

                    // Don't draw the vehicle currently controlled by the player
                    if (isDriving && getCurrentCar() == trafficCar) {
                        continue;
                    }

                    double radarX = radarWidth / 2
                            + (trafficCar.getWorldX() + 11.5 - focusX)
                            * radarScale;

                    double radarY = radarHeight / 2
                            + (trafficCar.getWorldY() + 20 - focusY)
                            * radarScale;

                    Rectangle radarCar = new Rectangle(
                            radarX - 2,
                            radarY - 3,
                            4,
                            6
                    );

                    radarCar.setFill(Color.YELLOW);

                    radarWorld.getChildren().add(radarCar);
                }

                // -------------------- PARKED CARS --------------------

                for (TrafficCar parkedCar : parkedCars) {

                    if (isDriving && getCurrentCar() == parkedCar) {
                        continue;
                    }

                    double radarX = radarWidth / 2
                            + (parkedCar.getWorldX() + 11.5 - focusX)
                            * radarScale;

                    double radarY = radarHeight / 2
                            + (parkedCar.getWorldY() + 20 - focusY)
                            * radarScale;

                    Rectangle radarCar = new Rectangle(
                            radarX - 2,
                            radarY - 3,
                            4,
                            6
                    );

                    radarCar.setFill(Color.LIGHTBLUE);

                    radarWorld.getChildren().add(radarCar);
                }

                // -------------------- DESTINATION MARKER --------------------
                // The drug dealer place, coordinate of (4190, 1244)
                // For the Prologue, Mission 1, and Mission 2

                if (destinationMarker != 0) {

                    double destinationX = 0;
                    double destinationY = 0;

                    // 1 = Frankie
                    if (destinationMarker == 1) {
                        destinationX = frankie.getWorldX();
                        destinationY = frankie.getWorldY();
                    }

                    // 2 = Drug dealer
                    else if (destinationMarker == 2) {
                        destinationX = 4190;
                        destinationY = 1244;
                    }

                    // Calculate destination position relative to the player
                    double dx = (destinationX - focusX) * radarScale;
                    double dy = (destinationY - focusY) * radarScale;

                    // Keep marker inside radar boundaries
                    double halfWidth = radarWidth / 2 - 8;
                    double halfHeight = radarHeight / 2 - 8;

                    double markerScale = Math.min(1.0, Math.min(halfWidth / Math.max(Math.abs(dx), 0.0001), halfHeight / Math.max(Math.abs(dy), 0.0001)));

                    double missionX = radarWidth / 2 + dx * markerScale;
                    double missionY = radarHeight / 2 + dy * markerScale;

                    Circle missionMarker = new Circle(missionX, missionY, 5);

                    if (destinationMarker == 1) {
                        missionMarker.setFill(Color.YELLOW); // Frankie, also where the mission starts
                    } else if (destinationMarker == 2) {
                        missionMarker.setFill(Color.RED); // Drug dealer, or the destination
                    }

                    missionMarker.setStroke(Color.WHITE);
                    missionMarker.setStrokeWidth(1.5);

                    radarWorld.getChildren().add(missionMarker);
                }

                // Draw AI traffic cars
                for (TrafficCar trafficCar : trafficCars) {

                    // Don't draw the vehicle currently controlled by the player
                    if (isDriving && getCurrentCar() == trafficCar) {
                        continue;
                    }

                    double radarX = radarWidth / 2
                            + (trafficCar.getWorldX() + 11.5 - focusX) * radarScale;

                    double radarY = radarHeight / 2
                            + (trafficCar.getWorldY() + 20 - focusY) * radarScale;

                    Rectangle radarCar = new Rectangle(
                            radarX - 2,
                            radarY - 3,
                            4,
                            6
                    );

                    radarCar.setFill(Color.YELLOW);
                    radarWorld.getChildren().add(radarCar);
                }

                // Draw parked cars
                for (TrafficCar parkedCar : parkedCars) {

                    if (isDriving && getCurrentCar() == parkedCar) {
                        continue;
                    }

                    double radarX = radarWidth / 2
                            + (parkedCar.getWorldX() + 11.5 - focusX) * radarScale;

                    double radarY = radarHeight / 2
                            + (parkedCar.getWorldY() + 20 - focusY) * radarScale;

                    Rectangle radarCar = new Rectangle(
                            radarX - 2,
                            radarY - 3,
                            4,
                            6
                    );

                    radarCar.setFill(Color.LIGHTBLUE);
                    radarWorld.getChildren().add(radarCar);
                }

                // Update traffic AI
                if (!editorMode) {
                    for (int i = 0; i < trafficControllers.size(); i++) {
                        trafficControllers.get(i).update(deltaTime);
                    }
                }

                // Detect collisions between AI traffic and the walking player
                handleTrafficPlayerCollisions(deltaTime);
                handleTrafficCarCollisions(deltaTime);

                foregroundWorld.setTranslateX(-cameraX);
                foregroundWorld.setTranslateY(-cameraY);

                // Update traffic car screen positions
                for (int i = 0; i < trafficCars.size(); i++) {

                    TrafficCar trafficCar = trafficCars.get(i);
                    ImageView trafficView = trafficCarViews.get(i);

                    trafficView.setX(trafficCar.getWorldX() - cameraX);
                    trafficView.setY(trafficCar.getWorldY() - cameraY);

                    trafficView.setRotate(trafficCar.getAngle());

                    trafficView.setVisible(
                            editorMode || !trafficCar.isPlayerControlled()
                                    || !isCarInsideOcclusionZone(trafficCar)
                    );
                }


                // Update parked car screen positions
                for (int i = 0; i < parkedCars.size(); i++) {
                    TrafficCar parkedCar = parkedCars.get(i);
                    ImageView parkedView = parkedCarViews.get(i);

                    parkedView.setX(parkedCar.getWorldX() - cameraX);
                    parkedView.setY(parkedCar.getWorldY() - cameraY);
                    parkedView.setRotate(parkedCar.getAngle());

                    parkedView.setVisible(editorMode || !isCarInsideOcclusionZone(parkedCar));
                }

                player.setX(playerObject.getWorldX() + 8 - cameraX);
                player.setY(playerObject.getWorldY() + 12 - cameraY);

                // Update Frankie's screen position
                if (frankieActive) {
                    frankieView.setX(frankie.getWorldX() - cameraX);
                    frankieView.setY(frankie.getWorldY() - cameraY);
                }

                // Check whether the player has approached Frankie
                if (!prologueStarted && frankieActive && !isDriving) {
                    double dx = playerObject.getWorldX() - frankie.getWorldX();
                    double dy = playerObject.getWorldY() - frankie.getWorldY();

                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 70) {
                        prologueStarted = true;
                        cutsceneActive = true;

                        dialogueSystem.startSequence(
                                new String[][]{
                                        {"FRANKIE", "Well, look who finally showed up! Alex! Man, I thought the cops " +
                                                "caught you back in Liberty City. Look at you—still dressing like you're" +
                                                " going to a funeral."},
                                        {"ALEX", "Save the jokes, Frankie. I didn't drive all the way across the country " +
                                                "in a car with no air conditioning just to talk about my clothes. You " +
                                                "said you had a real job for me. Big money. Not stealing cheap cars for " +
                                                "pocket change."},
                                        {"FRANKIE", "Whoa, whoa, take it easy, man! Relax. Take a deep breath. Smell " +
                                                "that? That’s Los Santos, baby. Fast money, bright sunshine, and a whole " +
                                                "lot of dangerous people who will take you out if you aren't careful. " +
                                                "You need to lose that bad attitude before you get yourself into real trouble."},
                                        {"ALEX", "I’m listening. Talk."},
                                        {"FRANKIE", "Alright, listen up. Word got out you’re back in the mix, and " +
                                                "some heavy hitters took notice. Ever hear of Hector Vargas?"},
                                        {"ALEX", "The guy running illegal business down at the docks? I thought he was in prison."},
                                        {"FRANKIE", "He got out. Nobody was brave enough to testify against him in court—funny" +
                                                " how that works, right? Anyway, Hector is back, he’s angry, and he’s taking " +
                                                "over the city fast. Right now, he has plenty of tough guys and plenty of illegal " +
                                                "goods to move. What he doesn't have is a getaway driver who stays calm when the " +
                                                "police helicopters start chasing him."},
                                        {"ALEX", "And that’s why you called me."},
                                        {"FRANKIE", "Exactly. Everybody knows you're the best behind the wheel, Alex. Hector needs someone " +
                                                "who can outrun the police with a trunk full of valuable cargo and make zero mistakes. " +
                                                "The pay is crazy good. Huge cash, no questions asked."},
                                        {"ALEX", "What’s the catch? Nobody pays that much money just for a normal driver."},
                                        {"FRANKIE", "The catch is, if you fail, Hector doesn't just fire you—you're finished " +
                                                "for good. Plus, rival gangs are trying to take over his territory right now, " +
                                                "and undercover agents are watching the docks closely. If you take this job, you " +
                                                "become a huge target."},
                                        {"ALEX", "Sounds like a normal day at work. Where do I find him?"},
                                        {"FRANKIE", "I just sent the location to your GPS. It's an old factory down in Cypress Flats. " +
                                                "Knock three times on the metal door by the back entrance, and tell them Frankie " +
                                                "sent you. And hey—stay sharp out there, alright? This city is brutal to newcomers. " +
                                                "Don’t make me look bad for trusting you."},
                                        {"ALEX", "Just make sure my share of the money is ready when I get back, Frankie."},
                                        {"FRANKIE", "Yeah, yeah, get out of here before somebody sees us talking!"}
                                },
                                DialogueMode.CUTSCENE
                        );
                    }
                }

                // Check whether Alex has reached the drug dealer
                if (destinationMarker == 2 && !drugDealerReached) {

                    double dx;
                    double dy;

                    if (isDriving) {
                        Car currentVehicle = getCurrentCar();

                        dx = currentVehicle.getWorldX() + 11.5 - 4190;
                        dy = currentVehicle.getWorldY() + 20 - 1244;

                    } else {
                        dx = playerObject.getWorldX() + 24 - 4190;
                        dy = playerObject.getWorldY() + 24 - 1244;
                    }

                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 70) {
                        drugDealerReached = true;
                        dealerDialogueActive = true;
                        cutsceneActive = true;

                        dialogueSystem.startSequence(
                                new String[][]{
                                        {"HECTOR", "So. You’re the famous driver from Liberty City. Frankie told me you were " +
                                                "fast. He didn't tell me you looked so quiet."},
                                        {"ALEX", "I don’t get paid to talk. I get paid to drive. Frankie said you needed " +
                                                "someone who knows how to handle a wheel."},
                                        {"HECTOR", "Fair enough. Let’s get straight to business, then. Look at this map, Alex. " +
                                                "For the last five years, I built an empire in this city. I move high-value " +
                                                "shipments across every highway, port, and back alley in Los Santos. Gold, " +
                                                "electronics, rare goods—if it’s worth millions, it moves through my warehouses."},
                                        {"ALEX", "Sounds like you already have everything under control. Why do you need me?"},
                                        {"HECTOR", "Because success attracts enemies. Rival gangs are trying to hijack my trucks, " +
                                                "and the police are blocking the main roads every single night. Last week, I " +
                                                "lost two shipments because my drivers panicked when the sirens started wailing. " +
                                                "They crashed, they got caught, and they cost me a fortune."},
                                        {"ALEX", "They didn't know the streets. Or how to stay cool under pressure."},
                                        {"HECTOR", "Exactly. Anyone can drive fast on an empty highway. I need someone who can " +
                                                "lose five police cars in rush-hour traffic without scratching the paint. I need " +
                                                "someone who treats a high-speed chase like a Sunday drive."},
                                        {"ALEX", "And what’s in it for me?"},
                                        {"HECTOR", "Ten thousand dollars for every successful delivery. Plus a bonus if you " +
                                                "keep the cargo completely untouched. You do the job right, you’ll be rich " +
                                                "before the end of the month."},
                                        {"ALEX", "And if things go wrong?"},
                                        {"HECTOR", "In my business, failure is not an option. If you get trapped, you find a way out. " +
                                                "You never lead the cops back here, and you never talk. Simple rules. Do we have a deal?"},
                                        {"ALEX", "You have a deal. Just point me to the car and tell me where the first drop is."},
                                        {"HECTOR", "Waite outside. Your first job starts in ten minutes. Don’t disappoint me, Alex."}
                                },
                                DialogueMode.CUTSCENE
                        );
                    }
                }

                carViews[0].setX(cars[0].getWorldX() - cameraX);
                carViews[0].setY(cars[0].getWorldY() - cameraY);

                carViews[0].setVisible(editorMode || !isCarInsideOcclusionZone(cars[0]));

                Car car = isDriving ? getCurrentCar() : cars[0];


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

                if (!editorMode) {

                    if (!isDriving) {

                        Car nearestCar = null;
                        int nearestCarIndex = -1;
                        double nearestDistance = CAR_INTERACTION_DISTANCE;

                        // Check the original taxi
                        double dx = playerObject.getWorldX() -
                                (cars[0].getWorldX() + 23.0 / 2);

                        double dy = playerObject.getWorldY() -
                                (cars[0].getWorldY() + 40.0 / 2);

                        double distance = Math.sqrt(dx * dx + dy * dy);

                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestCar = cars[0];
                            nearestCarIndex = 0;
                        }

                        // Check every traffic car
                        for (int i = 0; i < trafficCars.size(); i++) {

                            TrafficCar trafficCar = trafficCars.get(i);

                            dx = playerObject.getWorldX() -
                                    (trafficCar.getWorldX() + 23.0 / 2);

                            dy = playerObject.getWorldY() -
                                    (trafficCar.getWorldY() + 40.0 / 2);

                            distance = Math.sqrt(dx * dx + dy * dy);

                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearestCar = trafficCar;
                                nearestCarIndex = i + 1;
                            }
                        }

                        // Check every parked car
                        for (int i = 0; i < parkedCars.size(); i++) {

                            TrafficCar parkedCar = parkedCars.get(i);

                            dx = playerObject.getWorldX() - (parkedCar.getWorldX() + 23.0 / 2);

                            dy = playerObject.getWorldY() - (parkedCar.getWorldY() + 40.0 / 2);

                            distance = Math.sqrt(dx * dx + dy * dy);

                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearestCar = parkedCar;
                                nearestCarIndex = -(i + 1);
                            }
                        }

                        // Show interaction when a vehicle is nearby
                        if (nearestCar != null) {

                            interactionText.setText("Press F to enter the vehicle");
                            interactionText.setVisible(true);

                            if (fPressed && !fWasPressed) {

                                isDriving = true;
                                currentCar = nearestCarIndex;

                                // Entering an AI traffic car
                                if (currentCar > 0) {

                                    TrafficCar takenCar = trafficCars.get(currentCar - 1);

                                    takenCar.setPlayerControlled(true);

                                    if (!takenCar.isDriverSpawned()) {
                                        spawnDriverNPC(takenCar, root);
                                        takenCar.setDriverSpawned(true);
                                    }
                                }

                                // Entering a parked car
                                else if (currentCar < 0) {

                                    TrafficCar takenCar = parkedCars.get(-currentCar - 1);

                                    takenCar.setPlayerControlled(true);
                                    takenCar.setParked(false);
                                }

                                handbrakeDown = false;
                                player.setVisible(false);
                            }

                        } else {
                            interactionText.setVisible(false);
                        }

                    } else {

                        interactionText.setText("Press F to exit the vehicle");
                        interactionText.setVisible(true);

                        if (fPressed && !fWasPressed) {

                            double[] leftExit = car.getExitPosition(true);
                            double[] rightExit = car.getExitPosition(false);

                            boolean leftSafe = isSafeExit(leftExit[0], leftExit[1]);
                            boolean rightSafe = isSafeExit(rightExit[0], rightExit[1]);

                            if (leftSafe || rightSafe) {

                                double[] exitPosition =
                                        leftSafe ? leftExit : rightExit;

                                // Permanently park the abandoned traffic car
                                if (currentCar > 0) {

                                    TrafficCar abandonedCar =
                                            trafficCars.get(currentCar - 1);

                                    abandonedCar.setPlayerControlled(false);
                                    abandonedCar.setParked(true);
                                }

                                // Park the vehicle if it was originally a parked car
                                else if (currentCar < 0) {

                                    TrafficCar abandonedCar =
                                            parkedCars.get(-currentCar - 1);

                                    abandonedCar.setPlayerControlled(false);
                                    abandonedCar.setParked(true);
                                }

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

                                playerHitbox.setX(
                                        playerObject.getWorldX() + HITBOX_OFFSET_X
                                );

                                playerHitbox.setY(
                                        playerObject.getWorldY() + HITBOX_OFFSET_Y
                                );

                            } else {
                                interactionText.setText("No safe place to exit!");
                            }
                        }
                    }

                    fWasPressed = fPressed;
                }

                playerHitboxVisual.setX(playerObject.getWorldX() + HITBOX_OFFSET_X - cameraX);
                playerHitboxVisual.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y - cameraY);

                for (int i = 0; i < npcObjects.length; i++) {

                    if (npcObjects[i] == null) {
                        continue;
                    }

                    if (!editorMode) {
                        if (i != 2) {
                            npcObjects[i].update(deltaTime);
                        }

                        if (!npcObjects[i].isKnockedDown()) {
                            npcObjects[i].animate(deltaTime);
                        }
                    }

                    if (npcObjects[i].isKnockedDown()) {

                        npcMarkerTimers[i] += deltaTime;

                        double progress = Math.min(npcMarkerTimers[i] / 1.5, 1.0);

                        npcMarkers[i].setCenterX(
                                npcObjects[i].getWorldX() + 16 - cameraX
                        );

                        npcMarkers[i].setCenterY(
                                npcObjects[i].getWorldY() + 24 - cameraY
                        );

                        npcMarkers[i].setRadius(12 + 6 * progress);
                        npcMarkers[i].setOpacity(0.65 * progress);
                    }

                    npcs[i].setX(npcObjects[i].getWorldX() - cameraX);
                    npcs[i].setY(npcObjects[i].getWorldY() - cameraY);

                    if (npcObjects[i].isKnockedDown()) {

                        switch (npcObjects[i].getDirection()) {
                            case "right":
                                npcs[i].setImage(npcRightIdle);
                                break;
                            case "left":
                                npcs[i].setImage(npcLeftIdle);
                                break;
                            case "up":
                                npcs[i].setImage(npcUpIdle);
                                break;
                            case "down":
                                npcs[i].setImage(npcDownIdle);
                                break;
                        }

                    } else {

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
                }

                // Update and render driver NPCs
                for (int i = 0; i < driverNPCs.size(); i++) {

                    NPC driver = driverNPCs.get(i);
                    ImageView driverView = driverNPCViews.get(i);

                    if (!editorMode) {
                        driver.update(deltaTime);
                        driver.animate(deltaTime);
                    }

                    driverView.setX(driver.getWorldX() - cameraX);
                    driverView.setY(driver.getWorldY() - cameraY);

                    if (!driver.isEscaping() || !driver.isMoving()) {

                        // Standing still during the pause
                        driverView.setImage(npcDownIdle);

                    } else {

                        // Walking away after the pause
                        if (driver.getDirection().equals("right")) {
                            driverView.setImage(npcRightWalk[driver.getFrame()]);
                        } else if (driver.getDirection().equals("left")) {
                            driverView.setImage(npcLeftWalk[driver.getFrame()]);
                        } else if (driver.getDirection().equals("up")) {
                            driverView.setImage(npcUpWalk[driver.getFrame()]);
                        } else if (driver.getDirection().equals("down")) {
                            driverView.setImage(npcDownWalk[driver.getFrame()]);
                        }
                    }
                }

                // NPC follower system
                if (!editorMode && npcObjects[1] != null && npcObjects[2] != null && npcObjects[1].getGroupId() == npcObjects[2].getGroupId()) {
                    npcObjects[2].follow(npcObjects[1], 32, 32);
                }

                handlePlayerCarNPCCollisions();
            }
        };

        Image logoImage = new Image(
                getClass().getResource("/logo.png").toExternalForm()
        );

        playerObject = new Player(985, 3095, 200);

        cars[0] = new Car(700, 2950, 0, "right", "redCar");

        // Setup NPCs
        // new NPC(worldX, worldY, speed, patrolStart, patrolEnd, movementType, groupId);
//        npcObjects[0] = new NPC(560, 2730, 80, 26 * 16, 42 * 16, "horizontal", 1);
//        npcObjects[1] = new NPC(850, 2730, 70, 30 * 16, 40 * 16, "vertical", 2);
//        npcObjects[2] = new NPC(1100, 2820, 60, 40 * 16, 46 * 16, "vertical", 2);

        redCarImage = new Image(getClass().getResource("/legacy_tiles/vehicles/red_car_frame_01.png").toExternalForm());
        blueCarImage = new Image(getClass().getResource("/legacy_tiles/vehicles/blue_car_frame_01.png").toExternalForm());
        blueTruckImage = new Image(getClass().getResource("/legacy_tiles/vehicles/blue_truck_frame_01.png").toExternalForm());
        pinkCarImage = new Image(getClass().getResource("/legacy_tiles/vehicles/pink_car_frame_01.png").toExternalForm());
        turquoiseCarImage = new Image(getClass().getResource("/legacy_tiles/vehicles/turquoise_car_frame_01.png").toExternalForm());
        carBlueImage = new Image(getClass().getResource("/map/objects/vehicles/car_blue.png").toExternalForm());
        carBlueDarkImage = new Image(getClass().getResource("/map/objects/vehicles/car_blue_dark.png").toExternalForm());
        carGrayImage = new Image(getClass().getResource("/map/objects/vehicles/car_gray.png").toExternalForm());
        carGreenImage = new Image(getClass().getResource("/map/objects/vehicles/car_green.png").toExternalForm());
        carOrangeImage = new Image(getClass().getResource("/map/objects/vehicles/car_orange.png").toExternalForm());
        carRedImage = new Image(getClass().getResource("/map/objects/vehicles/car_red.png").toExternalForm());
        carSportsRedImage = new Image(getClass().getResource("/map/objects/vehicles/car_sports_red.png").toExternalForm());
        carTealImage = new Image(getClass().getResource("/map/objects/vehicles/car_teal.png").toExternalForm());
        carWhiteImage = new Image(getClass().getResource("/map/objects/vehicles/car_white.png").toExternalForm());
        carYellowImage = new Image(getClass().getResource("/map/objects/vehicles/car_yellow.png").toExternalForm());
        constructionVehImage = new Image(getClass().getResource("/map/objects/vehicles/car_blue.png").toExternalForm());
        helicopterImage = new Image(getClass().getResource("/map/objects/vehicles/helicopter.png").toExternalForm());
        breakdownTruckImage = new Image(getClass().getResource("/legacy_tiles/vehicles/breakdown_truck_frame_01.png").toExternalForm());
        bugImage = new Image(getClass().getResource("/legacy_tiles/vehicles/bug_frame_01.png").toExternalForm());
        coachImage = new Image(getClass().getResource("/legacy_tiles/vehicles/coach_frame_01.png").toExternalForm());
        cossieImage = new Image(getClass().getResource("/legacy_tiles/vehicles/cossie_frame_01.png").toExternalForm());
        fourXFourImage = new Image(getClass().getResource("/legacy_tiles/vehicles/four_x_four_frame_01.png").toExternalForm());
        counthashImage = new Image(getClass().getResource("/legacy_tiles/vehicles/counthash_frame_01.png").toExternalForm());
        jugularImage = new Image(getClass().getResource("/legacy_tiles/vehicles/jugular_frame_01.png").toExternalForm());
        mundanoImage = new Image(getClass().getResource("/legacy_tiles/vehicles/mundano_frame_01.png").toExternalForm());
        penetratorImage = new Image(getClass().getResource("/legacy_tiles/vehicles/penetrator_frame_01.png").toExternalForm());
        pickUpImage = new Image(getClass().getResource("/legacy_tiles/vehicles/pickup_frame_01.png").toExternalForm());
        porkaTurboImage = new Image(getClass().getResource("/legacy_tiles/vehicles/porka_turbo_frame_01.png").toExternalForm());
        repairVanImage = new Image(getClass().getResource("/legacy_tiles/vehicles/repair_van_frame_01.png").toExternalForm());
        stallionImage = new Image(getClass().getResource("/legacy_tiles/vehicles/stallion_frame_01.png").toExternalForm());
        trainImage = new Image(getClass().getResource("/legacy_tiles/vehicles/train_frame_01.png").toExternalForm());
        tramImage = new Image(getClass().getResource("/legacy_tiles/vehicles/tram_frame_01.png").toExternalForm());

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

        System.out.println("Red: " + redCarImage.isError());
        System.out.println("Blue: " + blueCarImage.isError());
        System.out.println("Truck: " + blueTruckImage.isError());
        System.out.println("Pink: " + pinkCarImage.isError());
        System.out.println("Turquoise: " + turquoiseCarImage.isError());

        carViews[0] = new ImageView(redCarImage);
        carViews[0].setSmooth(false);

        player = new ImageView(playerDownIdle);
        player.setSmooth(false);

        for (int i = 0; i < npcs.length; i++) {

            npcMarkers[i] = new Circle(12);
            npcMarkers[i].setFill(Color.rgb(180, 25, 25));
            npcMarkers[i].setOpacity(0);
            npcMarkers[i].setMouseTransparent(true);

            root.getChildren().add(npcMarkers[i]);

            npcs[i] = new ImageView(npcDownIdle);
            npcs[i].setSmooth(false);
            root.getChildren().add(npcs[i]);
        }

        // ******** FRANKIE ********
        // Create Frankie for the prologue
        frankie = new NPC(1070, 3110, 0, 1075, 1075, "horizontal", -1);
        destinationMarker = 1;

        frankieView = new ImageView(npcLeftIdle);
        frankieView.setSmooth(false);
        frankieView.setX(frankie.getWorldX());
        frankieView.setY(frankie.getWorldY());

        scale(frankieView, 2);

        root.getChildren().add(frankieView);
        frankieView.setVisible(frankieActive);

        // Activate Frankie for the prologue
        frankieActive = true;
        frankieView.setVisible(frankieActive);

        root.getChildren().add(carViews[0]);

        root.getChildren().add(player);
        root.getChildren().add(foregroundWorld);

        // Add parked cars
        spawnParkedCar(root, 373, 1475, blueCarImage, 0, 1.8);
        spawnParkedCar(root, 403, 1475, pinkCarImage, 0, 1.8);
        spawnParkedCar(root, 433, 1475, turquoiseCarImage, 0, 1.8);
        spawnParkedCar(root, 463, 1475, blueTruckImage, 0, 1.8);

        spawnParkedCar(root, 603, 1678, carBlueImage, 0, 1.2);
        spawnParkedCar(root, 633, 1678, carBlueDarkImage, 0, 1.2);
        spawnParkedCar(root, 663, 1678, carGreenImage, 0, 1.2);
        spawnParkedCar(root, 693, 1678, carOrangeImage, 0, 1.2);
        spawnParkedCar(root, 723, 1678, carRedImage, 0, 1.2);
        spawnParkedCar(root, 753, 1678, carSportsRedImage, 0, 1.2);
        spawnParkedCar(root, 783, 1678, carTealImage, 0, 1.2);
        spawnParkedCar(root, 813, 1678, carYellowImage, 0, 1.2);
        spawnParkedCar(root, 843, 1678, constructionVehImage, 0, 1.2);

        spawnParkedCar(root, 233, 1050, turquoiseCarImage, 0, 1.8);
        spawnParkedCar(root, 645, 1185, carGreenImage, 0, 1.2);
        spawnParkedCar(root, 675, 1185, carOrangeImage, 0, 1.2);

        spawnParkedCar(root, 1152, 1660, carRedImage, 0, 1.2);
        spawnParkedCar(root, 1195, 1660, pinkCarImage, 0, 1.8);

        spawnParkedCar(root, 2569, 1118, carWhiteImage, 0, 1.2);

        spawnParkedCar(root, 2478, 960, constructionVehImage, 0, 1.2);

        spawnParkedCar(root, 2086, 982, carSportsRedImage, 0, 1.2);

        spawnParkedCar(root, 2288, 1208, turquoiseCarImage, 0, 1.8);
        spawnParkedCar(root, 2288, 1268, carGreenImage, 0, 1.2);
        spawnParkedCar(root, 2288, 1422, carOrangeImage, 0, 1.2);

        spawnParkedCar(root, 2541, 1240, pinkCarImage, 0, 1.8);
        spawnParkedCar(root, 2541, 1300, turquoiseCarImage, 0, 1.8);
        spawnParkedCar(root, 2541, 1360, blueTruckImage, 0, 1.8);

        spawnParkedCar(root, 936, 180, carYellowImage, 0, 1.2);

        spawnParkedCar(root, 2362, 479, blueTruckImage, 0, 1.8);

        spawnParkedCar(root, 81, 1509, carTealImage, 0, 1.2);

        spawnParkedCar(root, 1688, 719, helicopterImage, 0, 1.2);

        playerHitboxVisual.setFill(javafx.scene.paint.Color.rgb(128, 0, 128, 0.5));
        root.getChildren().add(playerHitboxVisual);

        if (editorMode) {
            playerHitboxVisual.setVisible(true);
        } else {
            playerHitboxVisual.setVisible(false);
        }

        player.setX(playerObject.getWorldX());
        player.setY(playerObject.getWorldY());

        for (int i = 0; i < npcObjects.length; i++) {

            if (npcObjects[i] == null) {
                continue;
            }

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
        foregroundWorld.getTransforms().add(new Scale(2, 2, 0, 0));

        loadHitboxes(gridLayer);
        loadOcclusionZones(gridLayer);
        loadTrafficRoutes(gridLayer);

        stage.setScene(scene);
        stage.setTitle("Mini GTA");
        stage.setResizable(false);
        stage.getIcons().add(logoImage);

        // Keep the radar above all other UI elements
        radarPane.toFront();
        stage.show();
        radarPane.toFront();

        Text instructionText = new Text("Get to the ");
        instructionText.setFill(Color.WHITE);
        instructionText.setStroke(Color.rgb(0, 0, 0));
        instructionText.setStrokeWidth(0.5);
        instructionText.setStyle("-fx-font-size: 20px;");

        Text destinationText = new Text("drug dealer's place.");
        destinationText.setFill(Color.RED);
        destinationText.setStroke(Color.rgb(0, 0, 0));
        destinationText.setStrokeWidth(0.5);
        destinationText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextFlow missionInstruction = new TextFlow(
                instructionText,
                destinationText
        );

        missionInstruction.setLayoutX(scene.getWidth() / 2 - 110);
        missionInstruction.setLayoutY(scene.getHeight() - 30);
        missionInstruction.setVisible(false);

        root.getChildren().add(missionInstruction);

        // Start Menu
        Pane startMenu = new Pane();

        // Pause Menu
        Pane pauseMenu = new Pane();
        pauseMenu.setPrefSize(1280, 720);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Text pauseTitle = new Text("PAUSED");
        pauseTitle.setFill(Color.WHITE);
        pauseTitle.setStyle("-fx-font-size: 64px; -fx-font-weight: bold;");
        pauseTitle.setX(520);
        pauseTitle.setY(250);

        pauseMenu.getChildren().add(pauseTitle);

        Button resumeButton = new Button("RESUME");
        resumeButton.setLayoutX(590);
        resumeButton.setLayoutY(380);
        resumeButton.setPrefSize(120, 45);

        resumeButton.setStyle("-fx-font-size: 18px;" + "-fx-font-weight: bold;" + "-fx-background-color: #ffffff;" + "-fx-text-fill: black;");

        resumeButton.setOnAction(event -> {
            gamePaused = false;
            lastTime = 0;
            pauseMenu.setVisible(false);
        });

        pauseMenu.getChildren().add(resumeButton);

        pauseMenu.setVisible(false);

        root.getChildren().add(pauseMenu);

        startMenu.setPrefSize(1280, 720);
        startMenu.setStyle("-fx-background-color: #151515;");

        Text gameTitle = new Text("MINI GTA");
        gameTitle.setFill(Color.WHITE);
        gameTitle.setStyle("-fx-font-size: 64px; -fx-font-weight: bold;");
        gameTitle.setX(490);
        gameTitle.setY(250);

        Text gameSubtitle = new Text("A TOP-DOWN OPEN WORLD");
        gameSubtitle.setFill(Color.LIGHTGRAY);
        gameSubtitle.setStyle("-fx-font-size: 18px;");
        gameSubtitle.setX(490);
        gameSubtitle.setY(290);

        startMenu.getChildren().addAll(gameTitle, gameSubtitle);

        Button playButton = new Button("PLAY");

        playButton.setLayoutX(590);
        playButton.setLayoutY(380);
        playButton.setPrefSize(120, 45);

        playButton.setStyle("-fx-font-size: 18px;" + "-fx-font-weight: bold;" + "-fx-background-color: #ffffff;" + "-fx-text-fill: black;");

        playButton.setOnAction(event -> {
            gameStarted = true;
            startMenu.setVisible(false);
        });

        startMenu.getChildren().add(playButton);

        // Add menu above the game
        root.getChildren().add(startMenu);

        // Creating dialogues
        dialogueSystem = new DialogueSystem(root);

        Image missionPassedImage = new Image(getClass().getResource("/mission_passed.png").toExternalForm());

        ImageView missionPassedView = new ImageView(missionPassedImage);

        missionPassedView.setPreserveRatio(true);
        missionPassedView.setFitWidth(400);
        scale(missionPassedView, 2);
        missionPassedView.setVisible(false);
        missionPassedView.setMouseTransparent(true);

        missionPassedView.setX((scene.getWidth() - 400) / 2);

        missionPassedView.setY(scene.getHeight() * 0.35);

        root.getChildren().add(missionPassedView);

        scene.addEventHandler(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {

                        boolean sequenceFinished = dialogueSystem.advanceDialogue();

                        if (sequenceFinished && cutsceneActive) {
                            cutsceneActive = false;

                            if (dealerDialogueActive) {
                                dealerDialogueActive = false;
                                destinationMarker = 0;

                                missionPassedView.setVisible(true);
                                missionPassedView.toFront();

                                missionInstruction.setVisible(false);

                                PauseTransition pause = new PauseTransition(Duration.seconds(5));

                                pause.setOnFinished(e -> {
                                    missionPassedView.setVisible(false);
                                });

                                pause.play();
                            } else {
                                destinationMarker = 2;
                                missionInstruction.setVisible(true);
                            }
                        }
                    }
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE && gameStarted && !cutsceneActive) {

                        gamePaused = !gamePaused;
                        pauseMenu.setVisible(gamePaused);
                    }
                }
        );

        timer.start();
    }

    // ******** METHODS ********
    private Car getCurrentCar() {

        if (currentCar == 0) {
            return cars[0];
        }

        if (currentCar > 0) {
            return trafficCars.get(currentCar - 1);
        }

        return parkedCars.get(-currentCar - 1);
    }

    private ImageView getCurrentCarView() {

        if (currentCar == 0) {
            return carViews[0];
        }

        if (currentCar > 0) {
            return trafficCarViews.get(currentCar - 1);
        }

        return parkedCarViews.get(-currentCar - 1);
    }

    public static void scale(ImageView object, double ratio) {
        object.setScaleX(ratio);
        object.setScaleY(ratio);
    }

    public static void scale(Group name, double ratio) {
        name.setScaleX(ratio);
        name.setScaleY(ratio);
    }

    private void spawnTrafficCar(Pane root, boolean automatic) {

        int routeCount = trafficRoutes.size();

        if (routeCount == 0) {
            return;
        }

        int routeIndex = selectedTrafficRoute;
        List<Point2D> route = null;
        int spawnIndex = -1;

        int routesToCheck = automatic ? routeCount : 1;

        for (int r = 0; r < routesToCheck; r++) {

            int candidateRouteIndex = automatic ? (nextTrafficRouteIndex + r) % routeCount : selectedTrafficRoute;

            List<Point2D> candidateRoute = trafficRoutes.get(candidateRouteIndex);

            if (candidateRoute.size() < 3) {
                continue;
            }

            for (int i = 0; i < candidateRoute.size(); i++) {

                int candidateIndex = (trafficCars.size() + i)
                        % candidateRoute.size();

                Point2D candidate = candidateRoute.get(candidateIndex);

                if (isTrafficSpawnPositionAvailable(candidate)) {
                    routeIndex = candidateRouteIndex;
                    route = new ArrayList<>(candidateRoute);
                    spawnIndex = candidateIndex;
                    break;
                }
            }

            if (route != null) {
                break;
            }
        }

        if (route == null) {
            return;
        }

        if (automatic) {
            nextTrafficRouteIndex = (routeIndex + 1) % routeCount;
        }

        Point2D start = route.get(spawnIndex);

        // Available traffic vehicle sprites
        Image[] vehicleImages = {
                redCarImage,
                blueCarImage,
                blueTruckImage,
                pinkCarImage,
                breakdownTruckImage,
                bugImage,
                coachImage,
                cossieImage,
                fourXFourImage,
                counthashImage,
                jugularImage,
                mundanoImage,
                penetratorImage,
                pickUpImage,
                porkaTurboImage,
                repairVanImage,
                stallionImage,
                trainImage,
                tramImage
        };

        // Select one random vehicle
        int randomIndex = (int) (Math.random() * vehicleImages.length);
        Image selectedVehicleImage = vehicleImages[randomIndex];

        System.out.println("Selected vehicle index: " + randomIndex);

        // Calculate the scaled dimensions
        double vehicleWidth = selectedVehicleImage.getWidth() * 1.8;
        double vehicleHeight = selectedVehicleImage.getHeight() * 1.8;

        // Position the car so its center aligns with the waypoint
        TrafficCar trafficCar = new TrafficCar(
                start.getX() - vehicleWidth / 2,
                start.getY() - vehicleHeight / 2,
                0,
                "right",
                "traffic"
        );

        trafficCar.setTargetIndex(spawnIndex);

        // Create the AI controller
        TrafficController controller =
                new TrafficController(trafficCar, route);

        // Create the vehicle's visual representation
        ImageView trafficView = new ImageView(selectedVehicleImage);
        trafficView.setSmooth(false);

        scale(trafficView, 1.8);

        trafficView.setX(trafficCar.getWorldX());
        trafficView.setY(trafficCar.getWorldY());
        trafficView.setRotate(trafficCar.getAngle());

        // Add the vehicle behind the player
        root.getChildren().add(
                root.getChildren().indexOf(player),
                trafficView
        );

        // Store the objects
        trafficCars.add(trafficCar);
        trafficControllers.add(controller);
        trafficCarViews.add(trafficView);

        capturedCoordinateText.setText("Traffic car spawned!");
        capturedCoordinateText.setVisible(true);

        System.out.println("TRAFFIC CAR SUCCESSFULLY CREATED!");
    }

    private void spawnParkedCar(Pane root, double x, double y, Image vehicleImage, double angle, double scaleFactor) {

        TrafficCar car = new TrafficCar(x, y, 0, "right", "parked");
        car.setAngle(angle);
        car.setParked(true);

        ImageView carView = new ImageView(vehicleImage);
        carView.setSmooth(false);
        scale(carView, scaleFactor);

        x *= 2;
        y *= 2;

        car = new TrafficCar(
                x, y, 0, "right", "parked"
        );

        car.setAngle(angle);
        car.setParked(true);

        carView = new ImageView(vehicleImage);
        carView.setSmooth(false);

        scale(carView, scaleFactor);

        carView.setX(x);
        carView.setY(y);
        carView.setRotate(angle);

        root.getChildren().add(
                root.getChildren().indexOf(player), carView
        );

        parkedCars.add(car);
        parkedCarViews.add(carView);
    }

    private void spawnDriverNPC(TrafficCar car, Pane root) {

        // Get the center of the abandoned car
        double carCenterX = car.getWorldX() + 23.0 / 2;
        double carCenterY = car.getWorldY() + 40.0 / 2;

        // Place the driver beside the car
        double spawnX = carCenterX + 30;
        double spawnY = carCenterY;

        // Set a destination away from the car
        double destinationX = spawnX + 150;
        double destinationY = spawnY + 100;

        NPC driver = new NPC(
                spawnX, spawnY, 80,
                0, 0, "escape", -1
        );

        driver.setDestination(destinationX, destinationY);

        // Create the driver's visual representation
        ImageView driverView = new ImageView(npcDownIdle);
        driverView.setSmooth(false);
        scale(driverView, 2);

        driverView.setX(spawnX);
        driverView.setY(spawnY);

        // Add the sprite behind the player
        root.getChildren().add(
                root.getChildren().indexOf(player),
                driverView
        );

        // Store the NPC and its sprite
        driverNPCs.add(driver);
        driverNPCViews.add(driverView);
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

    private boolean isSafeExit(double x, double y) {
        if (x + HITBOX_OFFSET_X < 0
                || y + HITBOX_OFFSET_Y < 0
                || x + HITBOX_OFFSET_X + HITBOX_WIDTH > 5760
                || y + HITBOX_OFFSET_Y + HITBOX_HEIGHT > 3840) {
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

    private void saveHitboxes() {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < buildingCount; i++) {
            Rectangle hitbox = buildingHitbox[i];

            lines.add(
                    hitbox.getX() + "," +
                            hitbox.getY() + "," +
                            hitbox.getWidth() + "," +
                            hitbox.getHeight()
            );
        }

        Path file = Path.of(System.getProperty("user.dir"), "hitboxes.txt");

        try {
            Files.write(file, lines);

            System.out.println("Hitboxes saved: " + buildingCount);
            System.out.println("Save location: " + file.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("Could not save hitboxes.");
            e.printStackTrace();
        }
    }

    private void loadHitboxes(Group gridLayer) {
        Path file = Path.of(System.getProperty("user.dir"), "hitboxes.txt");

        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file);

            for (String line : lines) {
                String[] values = line.split(",");

                if (values.length != 4 || buildingCount >= buildingHitbox.length) {
                    continue;
                }

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);
                double width = Double.parseDouble(values[2]);
                double height = Double.parseDouble(values[3]);

                // Restore the collision hitbox
                buildingHitbox[buildingCount] =
                        new Rectangle(x, y, width, height);

                buildingCount++;

                // Restore its editor visualization
                Rectangle visual = new Rectangle(
                        x / 2, y / 2, width / 2, height / 2
                );

                visual.setFill(Color.rgb(255, 0, 0, 0.25));
                visual.setStroke(Color.RED);
                visual.setStrokeWidth(1);

                buildingHitboxVisual[buildingCount - 1] = visual;
                gridLayer.getChildren().add(visual);
            }

            System.out.println("Hitboxes loaded: " + buildingCount);

        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not load hitboxes.");
            e.printStackTrace();
        }
    }

    private boolean isPlayerInsideOcclusionZone() {

        for (Rectangle zone : occlusionZones) {

            if (checkCollision(playerHitbox, zone)) {
                return true;
            }
        }

        return false;
    }

    private void handleTrafficPlayerCollisions(double deltaTime) {

        if (isDriving || editorMode) {
            return;
        }

        for (TrafficCar trafficCar : trafficCars) {

            // Ignore cars that are not controlled by traffic AI
            if (trafficCar.isPlayerControlled() || trafficCar.isParked()) {
                continue;
            }

            // Check whether the rotated car overlaps the player
            if (trafficCar.intersects(playerHitbox)) {

                // Calculate the center of the car
                double carCenterX = trafficCar.getWorldX() + 23.0 / 2;
                double carCenterY = trafficCar.getWorldY() + 40.0 / 2;

                // Calculate the center of the player
                double playerCenterX =
                        playerHitbox.getX() + playerHitbox.getWidth() / 2;
                double playerCenterY =
                        playerHitbox.getY() + playerHitbox.getHeight() / 2;

                // Calculate the direction away from the car
                double dx = playerCenterX - carCenterX;
                double dy = playerCenterY - carCenterY;

                double distance = Math.sqrt(dx * dx + dy * dy);

                // Prevent division by zero
                if (distance == 0) {
                    dx = 1;
                    dy = 0;
                    distance = 1;
                }

                // Push the player away
                double pushDistance = 140 * deltaTime;

                playerObject.setWorldX(
                        playerObject.getWorldX() + (dx / distance) * pushDistance
                );

                playerObject.setWorldY(
                        playerObject.getWorldY() + (dy / distance) * pushDistance
                );

                // Keep the hitbox synchronized with the player
                playerHitbox.setX(
                        playerObject.getWorldX() + HITBOX_OFFSET_X
                );

                playerHitbox.setY(
                        playerObject.getWorldY() + HITBOX_OFFSET_Y
                );

                // Slow down the traffic car
                trafficCar.brake(deltaTime);
            }
        }
    }

    private void handleTrafficCarCollisions(double deltaTime) {

        for (int i = 0; i < trafficCars.size(); i++) {

            TrafficCar car1 = trafficCars.get(i);

            if (car1.isParked() || !car1.isPlayerControlled()) {
                continue;
            }

            for (int j = i + 1; j < trafficCars.size(); j++) {

                TrafficCar car2 = trafficCars.get(j);

                if (car2.isParked() || car2.isPlayerControlled()) {
                    continue;
                }

                // Approximate car2 with a rectangle.
                Rectangle car2Hitbox = new Rectangle(
                        car2.getWorldX(),
                        car2.getWorldY(),
                        23,
                        40
                );

                if (car1.intersects(car2Hitbox)) {
                    car2.brake(deltaTime);
                }
            }
        }

        // Handle the original player taxi separately.
        if (isDriving && currentCar == 0) {

            TrafficCar playerTaxi = null;

            for (TrafficCar trafficCar : trafficCars) {
                if (!trafficCar.isParked()
                        && trafficCar.intersects(
                        new Rectangle(
                                cars[0].getWorldX(),
                                cars[0].getWorldY(),
                                23,
                                40
                        ))) {

                    trafficCar.brake(deltaTime);
                }
            }
        }
    }

    private boolean isCarInsideOcclusionZone(Car car) {

        // Car hitbox dimensions
        double halfWidth = 17 * 1.8 / 2;
        double halfHeight = 32 * 1.8 / 2;

        // Car center in world coordinates
        double centerX = car.getWorldX() + 23.0 / 2;
        double centerY = car.getWorldY() + 40.0 / 2;

        double radians = Math.toRadians(car.getAngle());

        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));

        // Calculate the bounding box of the rotated car
        double extentX = cos * halfWidth + sin * halfHeight;
        double extentY = sin * halfWidth + cos * halfHeight;

        Rectangle carHitbox = new Rectangle(
                centerX - extentX,
                centerY - extentY,
                extentX * 2,
                extentY * 2
        );

        for (Rectangle zone : occlusionZones) {

            if (checkCollision(carHitbox, zone)) {
                return true;
            }
        }

        return false;
    }

    private void saveOcclusionZones() {

        List<String> lines = new ArrayList<>();

        for (Rectangle zone : occlusionZones) {

            lines.add(
                    zone.getX() + "," +
                            zone.getY() + "," +
                            zone.getWidth() + "," +
                            zone.getHeight()
            );
        }

        Path file = Path.of(
                System.getProperty("user.dir"),
                "occlusion_zones.txt"
        );

        try {
            Files.write(file, lines);

            System.out.println(
                    "Occlusion zones saved: " + occlusionZones.size()
            );

        } catch (IOException e) {
            System.out.println("Could not save occlusion zones.");
            e.printStackTrace();
        }
    }

    private void saveTrafficRoutes() {

        List<String> lines = new ArrayList<>();

        // Save the total number of routes
        lines.add(String.valueOf(trafficRoutes.size()));

        // Save each route on a separate line
        for (List<Point2D> route : trafficRoutes) {

            // Represent an empty route with "-"
            if (route.isEmpty()) {
                lines.add("-");
                continue;
            }

            String routeData = "";

            // Save every waypoint's coordinates
            for (int i = 0; i < route.size(); i++) {

                Point2D waypoint = route.get(i);

                if (i > 0) {
                    routeData += ";";
                }

                routeData += waypoint.getX() + "," + waypoint.getY();
            }

            lines.add(routeData);
        }

        Path file = Path.of(
                System.getProperty("user.dir"),
                "traffic_routes.txt"
        );

        try {
            Files.write(file, lines);

            System.out.println(
                    "Traffic routes saved: " + trafficRoutes.size()
            );

        } catch (IOException e) {
            System.out.println("Could not save traffic routes.");
            e.printStackTrace();
        }
    }


    private void loadTrafficRoutes(Group gridLayer) {

        Path file = Path.of(
                System.getProperty("user.dir"),
                "traffic_routes.txt"
        );

        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file);

            if (lines.isEmpty()) {
                return;
            }

            int routeCount = Integer.parseInt(lines.get(0));

            // Check whether the file contains enough route data
            if (routeCount < 1 || lines.size() < routeCount + 1) {
                System.out.println("Invalid traffic route file.");
                return;
            }

            // Clear the initial empty route
            trafficRoutes.clear();
            trafficRouteWaypointVisuals.clear();
            trafficRouteLineVisuals.clear();

            // Reconstruct every route
            for (int i = 0; i < routeCount; i++) {

                List<Point2D> route = new ArrayList<>();
                List<Circle> markers = new ArrayList<>();
                List<Line> routeLines = new ArrayList<>();

                Color routeColor = trafficRouteColors[i % trafficRouteColors.length];

                String routeData = lines.get(i + 1);

                // Skip empty routes
                if (!routeData.equals("-")) {

                    String[] waypoints = routeData.split(";");

                    for (String waypointData : waypoints) {

                        String[] coordinates = waypointData.split(",");

                        if (coordinates.length != 2) {
                            continue;
                        }

                        double x = Double.parseDouble(coordinates[0]);
                        double y = Double.parseDouble(coordinates[1]);

                        Point2D waypoint = new Point2D(x, y);

                        // Recreate the connecting line
                        if (!route.isEmpty()) {

                            Point2D previous =
                                    route.get(route.size() - 1);

                            Line routeLine = new Line(
                                    previous.getX() / 2,
                                    previous.getY() / 2,
                                    waypoint.getX() / 2,
                                    waypoint.getY() / 2
                            );

                            routeLine.setStroke(routeColor);
                            routeLine.setStrokeWidth(1.5);

                            routeLines.add(routeLine);
                            gridLayer.getChildren().add(routeLine);
                        }

                        route.add(waypoint);

                        // Recreate the waypoint marker
                        Circle marker = new Circle(
                                waypoint.getX() / 2,
                                waypoint.getY() / 2,
                                3
                        );

                        marker.setFill(routeColor);
                        marker.setStroke(Color.WHITE);
                        marker.setStrokeWidth(0.8);

                        markers.add(marker);
                        gridLayer.getChildren().add(marker);
                    }
                }

                trafficRoutes.add(route);
                trafficRouteWaypointVisuals.add(markers);
                trafficRouteLineVisuals.add(routeLines);
            }

            selectedTrafficRoute = 0;
            updateTrafficRouteAppearance();

            System.out.println("Traffic routes loaded: " + trafficRoutes.size());

        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not load traffic routes.");
            e.printStackTrace();
        }
    }


    private void loadOcclusionZones(Group gridLayer) {

        Path file = Path.of(
                System.getProperty("user.dir"),
                "occlusion_zones.txt"
        );

        if (!Files.exists(file)) {
            return;
        }

        try {

            List<String> lines = Files.readAllLines(file);

            for (String line : lines) {

                String[] values = line.split(",");

                if (values.length != 4 || occlusionZones.size() >= 2000) {
                    continue;
                }

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);
                double width = Double.parseDouble(values[2]);
                double height = Double.parseDouble(values[3]);

                // Restore the actual occlusion zone
                Rectangle zone = new Rectangle(
                        x, y, width, height
                );

                occlusionZones.add(zone);

                // Restore its editor visualization
                Rectangle visual = new Rectangle(
                        x / 2,
                        y / 2,
                        width / 2,
                        height / 2
                );

                visual.setFill(Color.rgb(0, 180, 255, 0.25));
                visual.setStroke(Color.DEEPSKYBLUE);
                visual.setStrokeWidth(1);

                occlusionZoneVisuals.add(visual);
                gridLayer.getChildren().add(visual);
            }

            System.out.println(
                    "Occlusion zones loaded: " + occlusionZones.size()
            );

        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not load occlusion zones.");
            e.printStackTrace();
        }
    }

    private void updateTrafficRouteAppearance() {

        for (int i = 0; i < trafficRouteWaypointVisuals.size(); i++) {

            // Keep the selected route bright
            double opacity = (i == selectedTrafficRoute) ? 1.0 : 0.25;

            // Update waypoint markers
            for (Circle marker : trafficRouteWaypointVisuals.get(i)) {
                marker.setOpacity(opacity);
            }

            // Update route lines
            for (Line line : trafficRouteLineVisuals.get(i)) {
                line.setOpacity(opacity);
            }
        }
    }

    private boolean isTrafficSpawnPositionAvailable(Point2D position) {

        // Minimum distance between traffic cars
        double minimumDistance = 100;

        // Check the position against every existing traffic car
        for (TrafficCar existingCar : trafficCars) {

            double existingX = existingCar.getWorldX() + 23.0 / 2;
            double existingY = existingCar.getWorldY() + 40.0 / 2;

            double dx = position.getX() - existingX;
            double dy = position.getY() - existingY;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minimumDistance) {
                return false;
            }
        }

        return true;
    }

    private int getActiveTrafficCarCount() {

        int count = 0;

        for (TrafficCar car : trafficCars) {

            if (!car.isPlayerControlled() && !car.isParked()) {
                count++;
            }
        }

        return count;
    }

    private void handlePlayerCarNPCCollisions() {
        if (!isDriving || editorMode) {
            return;
        }

        Car car = getCurrentCar();

        double carCenterX = car.getWorldX() + 23.0 / 2;
        double carCenterY = car.getWorldY() + 40.0 / 2;

        for (NPC npc : npcObjects) {

            if (npc == null || npc.isKnockedDown()) {
                continue;
            }

            Rectangle npcHitbox = new Rectangle(
                    npc.getWorldX(),
                    npc.getWorldY(),
                    32,
                    32
            );

            if (car.intersects(npcHitbox)) {

                double npcCenterX = npc.getWorldX() + 16;
                double npcCenterY = npc.getWorldY() + 16;

                double directionX = npcCenterX - carCenterX;
                double directionY = npcCenterY - carCenterY;

                npc.hitByCar(directionX, directionY);
            }
        }
    }
}