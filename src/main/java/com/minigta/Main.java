    package com.minigta;

    import javafx.animation.AnimationTimer;
    import javafx.application.Application;
    import javafx.scene.Scene;
    import javafx.scene.layout.Pane;
    import javafx.scene.paint.Color;
    import javafx.stage.Stage;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
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

        @Override
        public void start(Stage stage) {
            Pane root = new Pane();
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

                    if (wDown) {
                        dy -= 1;
                    }
                    if (sDown) {
                        dy += 1;
                    }
                    if (aDown) {
                        dx -= 1;
                    }
                    if (dDown) {
                        dx += 1;
                    }

                    double movement = speed * deltaTime;

                    double length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    playerIdle.setX(playerIdle.getX() + dx * movement);
                    playerIdle.setY(playerIdle.getY() + dy * movement);
                }
            };

            timer.start();

            Image logoImage = new Image(getClass().getResource("/logo.png").toExternalForm());

            Image playerIdleImage = new Image(getClass().getResource("/playerIdle.png").toExternalForm());
            playerIdle = new ImageView(playerIdleImage);

            root.getChildren().add(playerIdle);
            playerIdle.setX(scene.getWidth() / 2 - 24);
            playerIdle.setY(scene.getHeight() / 2 - 24);
            scale(playerIdle, 1);

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
    }