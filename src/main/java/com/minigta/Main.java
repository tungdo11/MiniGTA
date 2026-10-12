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
import java.lang.Math;

public class Main extends Application {

    /**
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set.
     *              Applications may create other stages, if needed, but they will not be
     *              primary stages.
     */
    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: green;");
        Scene scene = new Scene(root, 1280, 720);

        Image logoImage = new Image(getClass().getResource("/logo.png").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Mini GTA");
        stage.setResizable(false);
        stage.getIcons().add(logoImage);
        stage.show();

    }
}