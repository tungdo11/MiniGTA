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
    import javafx.scene.shape.Rectangle;
    import javafx.scene.shape.Circle;
    import javafx.scene.shape.Line;
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

        @Override
        public void start(Stage stage) {
            Pane root = new Pane();
            root.setStyle("-fx-background-color: green;");
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
                }
            };

            timer.start();

            Image logoImage = new Image(getClass().getResource("/logo.png").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Mini GTA");
            stage.setResizable(false);
            stage.getIcons().add(logoImage);
            stage.show();

        }
    }