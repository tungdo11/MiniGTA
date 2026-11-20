
package com.minigta;

import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

public class DialogueSystem {

    private Text speakerText;
    private Text dialogueText;
    private Text instructionText;

    private Text cutsceneSpeakerText;
    private Text cutsceneDialogueText;

    private VBox subtitleBox;
    private VBox cutsceneBox;
    private VBox bottomBox;

    private String[][] dialogueLines;
    private int currentLine;
    private DialogueMode currentMode;

    public DialogueSystem(Pane root) {

        // Subtitle display
        speakerText = new Text();
        speakerText.setFill(Color.WHITE);

        speakerText.setStroke(Color.BLACK);
        speakerText.setStrokeWidth(0.7);

        dialogueText = new Text();
        dialogueText.setFill(Color.WHITE);

        dialogueText.setStroke(Color.BLACK);
        dialogueText.setStrokeWidth(0.7);

        subtitleBox = new VBox(4, speakerText, dialogueText);
        subtitleBox.setAlignment(Pos.CENTER);
        subtitleBox.setVisible(false);

        subtitleBox.setTranslateY(45);

        // Cutscene dialogue display
        cutsceneSpeakerText = new Text();
        cutsceneSpeakerText.setFill(Color.WHITE);

        cutsceneSpeakerText.setStroke(Color.BLACK);
        cutsceneSpeakerText.setStrokeWidth(0.7);

        cutsceneDialogueText = new Text();
        cutsceneDialogueText.setFill(Color.WHITE);

        cutsceneDialogueText.setStroke(Color.BLACK);
        cutsceneDialogueText.setStrokeWidth(0.7);

        speakerText.setFont(javafx.scene.text.Font.font(
                "Arial", javafx.scene.text.FontWeight.BOLD, 15));

        dialogueText.setFont(javafx.scene.text.Font.font(
                "Arial", javafx.scene.text.FontWeight.BOLD, 16));

        cutsceneSpeakerText.setFont(javafx.scene.text.Font.font(
                "Arial", javafx.scene.text.FontWeight.BOLD, 15));

        cutsceneDialogueText.setFont(javafx.scene.text.Font.font(
                "Arial", javafx.scene.text.FontWeight.BOLD, 16));

        // Automatically wrap long dialogue onto multiple lines
        cutsceneDialogueText.setWrappingWidth(850);

        cutsceneBox = new VBox(
                8,
                cutsceneSpeakerText,
                cutsceneDialogueText
        );

        cutsceneBox.setAlignment(Pos.CENTER);
        cutsceneBox.setPrefWidth(880);
        cutsceneBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);" + "-fx-padding: 15;" + "-fx-background-radius: 8;");
        cutsceneBox.setVisible(false);

        cutsceneBox.setTranslateY(-23);

        // Mission instructions
        instructionText = new Text();
        instructionText.setFill(Color.WHITE);

        // Keep all bottom displays together
        bottomBox = new VBox(10, subtitleBox, cutsceneBox, instructionText);

        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setLayoutX(20);

        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            bottomBox.setLayoutY(newValue.doubleValue() - 220 + 70);
        });

        // Set the initial position as well
        bottomBox.setLayoutY(root.getHeight() - 210 + 70);

        bottomBox.setPrefWidth(1240);
        bottomBox.setMouseTransparent(true);

        root.getChildren().add(bottomBox);
    }

    public void showDialogue(String speaker, String line, DialogueMode mode) {

        if (mode == DialogueMode.SUBTITLE) {

            speakerText.setText(speaker);
            dialogueText.setText(line);

            subtitleBox.setVisible(true);
            cutsceneBox.setVisible(false);

        } else {

            cutsceneSpeakerText.setText(speaker);
            cutsceneDialogueText.setText(line);

            subtitleBox.setVisible(false);
            cutsceneBox.setVisible(true);
        }
    }

    public void startSequence(String[][] lines, DialogueMode mode) {

        if (lines == null || lines.length == 0) {
            hideDialogue();
            return;
        }

        dialogueLines = lines;
        currentLine = 0;
        currentMode = mode;

        showCurrentLine();
    }

    private void showCurrentLine() {

        showDialogue(
                dialogueLines[currentLine][0],
                dialogueLines[currentLine][1],
                currentMode
        );
    }

    public boolean advanceDialogue() {

        if (dialogueLines == null) {
            return false;
        }

        currentLine++;

        if (currentLine >= dialogueLines.length) {
            dialogueLines = null;
            hideDialogue();
            return true;
        } else {
            showCurrentLine();
            return false;
        }
    }

    public void hideDialogue() {

        subtitleBox.setVisible(false);
        cutsceneBox.setVisible(false);
    }

    public void setInstruction(String instruction) {

        instructionText.setText(instruction);
    }
}
