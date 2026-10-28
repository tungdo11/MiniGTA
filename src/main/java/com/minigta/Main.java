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
         *              the application scene can be set.
         *              Applications may create other stages, if needed, but they will not be
         *              primary stages.
         */
        private boolean wDown = false;
        private boolean aDown = false;
        private boolean sDown = false;
        private boolean dDown = false;
        private long lastTime = 0;
        private double deltaTime;
        private ImageView playerIdle;

        private Rectangle[] buildingCollisions = new Rectangle[3];
        private int buildingCount = 0;
        private static Rectangle playerHitbox = new Rectangle(18, 24, 18, 20);

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

                    deltaTime =   (now - lastTime) / 1_000_000_000.0;
                    lastTime = now;

                    double dx = 0;
                    double dy = 0;
                    double speed = 200;

                    if (wDown) dy--;
                    if (sDown) dy++;
                    if (aDown) dx--;
                    if (dDown) dx++;

                    double movement = speed * deltaTime;

                    double length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    double newX = playerIdle.getX() + dx * movement;
                    double newY = playerIdle.getY() + dy * movement;

                    double oldX = playerIdle.getX();
                    double oldY = playerIdle.getY();

                    playerIdle.setX(newX);
                    playerIdle.setY(newY);

                    playerHitbox.setX(newX + 18);
                    playerHitbox.setY(newY + 24);

                    for (Rectangle buildingCollision : buildingCollisions) {
                        if (buildingCollision != null &&
                                playerHitbox.getBoundsInParent().intersects(buildingCollision.getBoundsInParent())) {

                            playerIdle.setX(oldX);
                            playerIdle.setY(oldY);

                            playerHitbox.setX(oldX + 18);
                            playerHitbox.setY(oldY + 24);

                            break;
                        }
                    }
                }
            };

            timer.start();

            Image logoImage = new Image(getClass().getResource("/logo.png").toExternalForm());

            Image playerIdleImage = new Image(
                    getClass().getResource("/playerIdle.png").toExternalForm()
            );
            playerIdle = new ImageView(playerIdleImage);

            root.getChildren().add(playerIdle);
//          root.getChildren().add(playerHitbox);   // player's hitbox visibility
            root.getChildren().add(buildingOverlay);
            playerIdle.setX(scene.getWidth() / 2 - 24);
            playerIdle.setY(scene.getHeight() / 2 - 24);
            scale(playerIdle, 1);

            Image grassImage = new Image(
                    getClass().getResource("/tiles/ground/grass.png").toExternalForm());

            world.getTransforms().add(new Scale(2, 2, 0, 0));
            buildingOverlay.getTransforms().add(new Scale(2, 2, 0, 0));

            for (int row = 0; row < 24; row++) {
                for (int column = 0; column < 32; column++) {
                    Rectangle grassTile = new Rectangle(column * 16, row * 16, 16, 16);
                    grassTile.setFill(new ImagePattern(grassImage));
                    world.getChildren().add(grassTile);
                }
            }

            Image roadHorizontalImage = new Image(
                    getClass().getResource("/tiles/roads/road_horizontal.png").toExternalForm()
            );

            for (int column = 0; column < 32; column++) {
                ImageView roadTile = new ImageView(roadHorizontalImage);
                roadTile.setX(column * 16);
                roadTile.setY(176);
                roadTile.setFitWidth(16);
                roadTile.setFitHeight(16);
                world.getChildren().add(roadTile);
            }


            Image roadVerticalImage = new Image(
                    getClass().getResource("/tiles/roads/road_vertical.png").toExternalForm()
            );

            for (int row = 0; row < 24; row++) {
                ImageView roadTile = new ImageView(roadVerticalImage);
                roadTile.setX(256);
                roadTile.setY(row * 16);
                roadTile.setFitWidth(16);
                roadTile.setFitHeight(16);
                world.getChildren().add(roadTile);
            }


            Image intersectionImage = new Image(
                    getClass().getResource("/tiles/roads/intersection.png").toExternalForm()
            );

            ImageView intersection = new ImageView(intersectionImage);
            intersection.setX(16 * 16);
            intersection.setY(11 * 16);
            intersection.setFitWidth(16);
            intersection.setFitHeight(16);
            world.getChildren().add(intersection);

            Image sideWalkImage = new Image(
                    getClass().getResource("/tiles/roads/sidewalk.png").toExternalForm());

            for (int column = 0; column < 32; column++) {
                ImageView sideWalkTop = new ImageView(sideWalkImage);
                sideWalkTop.setX(column * 16);
                sideWalkTop.setY(160);
                sideWalkTop.setFitWidth(16);
                sideWalkTop.setFitHeight(16);
                world.getChildren().add(sideWalkTop);

                ImageView sideWalkBottom = new ImageView(sideWalkImage);
                sideWalkBottom.setX(column * 16);
                sideWalkBottom.setY(191);
                sideWalkBottom.setFitWidth(16);
                sideWalkBottom.setFitHeight(16);
                world.getChildren().add(sideWalkBottom);
            }

            for (int row = 0; row < 24; row++) {
                ImageView sideWalkLeft = new ImageView(sideWalkImage);
                sideWalkLeft.setX(240);
                sideWalkLeft.setY(row * 16);
                sideWalkLeft.setFitWidth(16);
                sideWalkLeft.setFitHeight(16);
                world.getChildren().add(sideWalkLeft);

                ImageView sideWalkRight = new ImageView(sideWalkImage);
                sideWalkRight.setX(272);
                sideWalkRight.setY(row * 16);
                sideWalkRight.setFitWidth(16);
                sideWalkRight.setFitHeight(16);
                world.getChildren().add(sideWalkRight);
            }

            Image roofImage = new Image(
                    getClass().getResource("/tiles/buildings/roof.png").toExternalForm()
            );

            Image wallImage = new Image(
                    getClass().getResource("/tiles/buildings/wall.png").toExternalForm()
            );

            addBuilding(world, buildingOverlay, roofImage, wallImage, 5, 4);
            addBuilding(world, buildingOverlay, roofImage, wallImage, 10, 4);
            addBuilding(world, buildingOverlay, roofImage, wallImage, 20, 4);

            stage.setScene(scene);
            stage.setTitle("Mini GTA");
            stage.setResizable(false);
            stage.getIcons().add(logoImage);
            stage.show();
        }

        public static void scale(ImageView object, double ratio) {
            object.setScaleX(ratio);
            object.setScaleY(ratio);
        }

        public static void scale(Group name, double ratio) {
            name.setScaleX(ratio);
            name.setScaleY(ratio);
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

            buildingCollisions[buildingCount] = buildingCollision;
            buildingCount++;
        }
    }