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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
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

    private final List<MapObject> mapObjects = new ArrayList<>();

    // Occlusion zones
    private final List<Rectangle> occlusionZones = new ArrayList<>();
    private final List<Rectangle> occlusionZoneVisuals = new ArrayList<>();

    private boolean occlusionEditorMode = false;
    private int selectedOcclusionZoneIndex = -1;

    private static double cameraX = 0;
    private static double cameraY = 0;
    private double tileX = 0;
    private double tileY = 0;

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

    private final List<Point2D> trafficWaypoints = new ArrayList<>();
    private final List<Circle> trafficWaypointVisuals = new ArrayList<>();
    private final List<Line> trafficRouteLines = new ArrayList<>();

    // Multiple traffic routes
    private final List<List<Point2D>> trafficRoutes = new ArrayList<>();

    private final List<List<Circle>> trafficRouteWaypointVisuals = new ArrayList<>();

    private final List<List<Line>> trafficRouteLineVisuals = new ArrayList<>();

    // Colors assigned to traffic routes
    private final Color[] trafficRouteColors = {Color.LIMEGREEN, Color.DODGERBLUE, Color.ORANGE, Color.MAGENTA, Color.YELLOW};

    // Index of the currently selected route
    private int selectedTrafficRoute = 0;

    // Traffic AI
    private final List<TrafficCar> trafficCars = new ArrayList<>();
    private final List<TrafficController> trafficControllers = new ArrayList<>();
    private final List<ImageView> trafficCarViews = new ArrayList<>();

    private Image trafficCarImage;

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
                            mouseWorldX, mouseWorldY, gridX, gridY
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

            Point2D mouseMap = world.sceneToLocal(
                    event.getSceneX(),
                    event.getSceneY()
            );

            // Traffic waypoint placement
            if (trafficEditorMode) {

                // Get the currently selected route and its visual lists
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

                    if (occlusionZoneVisuals.get(i).contains(
                            mouseMap.getX(), mouseMap.getY())) {

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
                        spawnTrafficCar(root);
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

                        System.out.println(
                                "Created traffic route: " + (selectedTrafficRoute + 1)
                        );

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

                        capturedCoordinateText.setText(
                                "Selected Route " + (selectedTrafficRoute + 1)
                                        + " | Waypoints: " + currentRoute.size()
                        );

                        capturedCoordinateText.setVisible(true);

                        System.out.println(
                                "Switched to traffic route: " + (selectedTrafficRoute + 1)
                        );

                        event.consume();
                        updateTrafficRouteAppearance();
                    }
                }
                case DELETE -> {
                    if (editorMode) {
                        if (trafficEditorMode) {

                            // Get the currently selected route and its visual lists
                            List<Point2D> currentRoute =
                                    trafficRoutes.get(selectedTrafficRoute);

                            List<Circle> currentMarkers =
                                    trafficRouteWaypointVisuals.get(selectedTrafficRoute);

                            List<Line> currentLines =
                                    trafficRouteLineVisuals.get(selectedTrafficRoute);

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
                                    Line lastLine =
                                            currentLines.remove(currentLines.size() - 1);

                                    gridLayer.getChildren().remove(lastLine);
                                }

                                capturedCoordinateText.setText(
                                        "Route " + (selectedTrafficRoute + 1)
                                                + ": waypoint removed"
                                );

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
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                gridLayer.setVisible(editorMode);

                if (!isDriving) {
                    double dx = 0;
                    double dy = 0;
                    double speed = editorMode ? 600 : 200;

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
                            if (building != null &&
                                    checkCollision(playerHitbox, building)) {

                                playerObject.setWorldY(oldY);
                                playerHitbox.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y);
                                break;
                            }
                        }
                    }
                }

                // Hide the player when inside a building's occlusion zone
                if (!isDriving) {
                    player.setVisible(
                            editorMode || !isPlayerInsideOcclusionZone()
                    );
                }

                if (!editorMode && isDriving) {
                    Car car = cars[currentCar];
                    car.setHandbrakeActive(handbrakeDown);

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

                // Update traffic AI
                if (!editorMode) {
                    for (int i = 0; i < trafficControllers.size(); i++) {
                        trafficControllers.get(i).update(deltaTime);
                    }
                }

                foregroundWorld.setTranslateX(-cameraX);
                foregroundWorld.setTranslateY(-cameraY);

                // Update traffic car screen positions
                for (int i = 0; i < trafficCars.size(); i++) {

                    TrafficCar trafficCar = trafficCars.get(i);
                    ImageView trafficView = trafficCarViews.get(i);

                    trafficView.setX(trafficCar.getWorldX() - cameraX);
                    trafficView.setY(trafficCar.getWorldY() - cameraY);

                    trafficView.setRotate(trafficCar.getAngle());
                }

                player.setX(playerObject.getWorldX() + 8 - cameraX);
                player.setY(playerObject.getWorldY() + 12 - cameraY);

                carViews[0].setX(cars[0].getWorldX() - cameraX);
                carViews[0].setY(cars[0].getWorldY() - cameraY);

                carViews[0].setVisible(editorMode || !isCarInsideOcclusionZone(cars[0]));

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

                if (!editorMode) {
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
                }

                playerHitboxVisual.setX(playerObject.getWorldX() + HITBOX_OFFSET_X - cameraX);
                playerHitboxVisual.setY(playerObject.getWorldY() + HITBOX_OFFSET_Y - cameraY);

                for (int i = 0; i < npcObjects.length; i++) {
                    if (!editorMode) {
                        if (i != 2) {
                            npcObjects[i].update(deltaTime);
                        }

                        npcObjects[i].animate(deltaTime);
                    }

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

                if (!editorMode && npcObjects[1].getGroupId() == npcObjects[2].getGroupId()) {
                    npcObjects[2].follow(npcObjects[1], 32, 32);
                }
            }
        };

        Image logoImage = new Image(
                getClass().getResource("/logo.png").toExternalForm()
        );

        playerObject = new Player(985, 3095, 200);

        cars[0] = new Car(700, 2950, 0, "right", "taxi");

        npcObjects[0] = new NPC(560, 2730, 80, 26 * 16, 42 * 16, "horizontal", 1);
        npcObjects[1] = new NPC(850, 2730, 70, 30 * 16, 40 * 16, "vertical", 2);
        npcObjects[2] = new NPC(1100, 2820, 60, 40 * 16, 46 * 16, "vertical", 2);

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

        trafficCarImage = taxiImage;

        carViews[0] = new ImageView(taxiImage);
        carViews[0].setSmooth(false);

        player = new ImageView(playerDownIdle);
        player.setSmooth(false);

        for (int i = 0; i < npcs.length; i++) {
            npcs[i] = new ImageView(npcDownIdle);
            npcs[i].setSmooth(false);
            root.getChildren().add(npcs[i]);
        }

        root.getChildren().add(carViews[0]);

        root.getChildren().add(player);
        root.getChildren().add(foregroundWorld);

        // player's hitbox visuality
//        playerHitboxVisual.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 1));
//        root.getChildren().add(playerHitboxVisual);

        player.setX(playerObject.getWorldX());
        player.setY(playerObject.getWorldY());

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
        foregroundWorld.getTransforms().add(new Scale(2, 2, 0, 0));

        loadHitboxes(gridLayer);
        loadOcclusionZones(gridLayer);
        loadTrafficRoutes(gridLayer);

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

    private void spawnTrafficCar(Pane root) {

        // Get the currently selected route
        List<Point2D> currentRoute =
                trafficRoutes.get(selectedTrafficRoute);

        System.out.println("=== SPAWN TRAFFIC CAR ===");
        System.out.println("Existing traffic cars: " + trafficCars.size());
        System.out.println("Selected route: " + (selectedTrafficRoute + 1));
        System.out.println("Waypoint count: " + currentRoute.size());

        // Require at least three waypoints
        if (currentRoute.size() < 3) {
            capturedCoordinateText.setText(
                    "Place at least 3 waypoints on this route first!"
            );
            capturedCoordinateText.setVisible(true);
            return;
        }

        // Only allow one traffic car during our first test
        if (!trafficCars.isEmpty()) {
            capturedCoordinateText.setText(
                    "A traffic car already exists!"
            );
            capturedCoordinateText.setVisible(true);
            return;
        }

        // Copy the selected route
        List<Point2D> route = new ArrayList<>(currentRoute);

        // Get the first waypoint
        Point2D start = route.get(0);

        // Position the car so its center aligns with the waypoint
        TrafficCar trafficCar = new TrafficCar(
                start.getX() - 23.0 / 2,
                start.getY() - 40.0 / 2,
                0,
                "right",
                "traffic"
        );

        // Create the AI controller
        TrafficController controller =
                new TrafficController(trafficCar, route);

        // Create the visual representation
        ImageView trafficView = new ImageView(trafficCarImage);
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

    private void deleteSelectedHitbox(Group gridLayer) {
        if (selectedHitboxIndex == -1) {
            return;
        }

        int index = selectedHitboxIndex;

        // Remove the visual rectangle
        gridLayer.getChildren().remove(buildingHitboxVisual[index]);

        // Shift remaining hitboxes to fill the empty position
        for (int i = index; i < buildingCount - 1; i++) {
            buildingHitbox[i] = buildingHitbox[i + 1];
            buildingHitboxVisual[i] = buildingHitboxVisual[i + 1];
        }

        buildingCount--;

        // Clear the last occupied position
        buildingHitbox[buildingCount] = null;
        buildingHitboxVisual[buildingCount] = null;

        selectedHitboxIndex = -1;

        // Update the save file
        saveHitboxes();

        capturedCoordinateText.setText("Hitbox deleted!");
    }

    private void placeMapObject(
            Image image,
            double x,
            double y,
            double width,
            double height,
            Group targetLayer,
            double hitboxOffsetX,
            double hitboxOffsetY,
            double hitboxWidth,
            double hitboxHeight
    ) {
        MapObject mapObject = new MapObject(
                image,
                x,
                y,
                width,
                height,
                2,
                hitboxOffsetX,
                hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );

        mapObjects.add(mapObject);
        targetLayer.getChildren().add(mapObject.getImageView());
    }

    private boolean isPlayerInsideOcclusionZone() {

        for (Rectangle zone : occlusionZones) {

            if (checkCollision(playerHitbox, zone)) {
                return true;
            }
        }

        return false;
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

    private void deleteSelectedOcclusionZone(Group gridLayer) {

        if (selectedOcclusionZoneIndex == -1) {
            return;
        }

        int index = selectedOcclusionZoneIndex;

        gridLayer.getChildren().remove(
                occlusionZoneVisuals.get(index)
        );

        occlusionZoneVisuals.remove(index);
        occlusionZones.remove(index);

        selectedOcclusionZoneIndex = -1;

        saveOcclusionZones();

        capturedCoordinateText.setText("Occlusion zone deleted!");
        capturedCoordinateText.setVisible(true);
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
}