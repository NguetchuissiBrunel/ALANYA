package sac.Interface;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import sac.mecanisme.CallManager;
import sac.mecanisme.NotificationManager;

import java.io.*;

public class CallUI {
    private Stage callStage;
    private CallManager callManager;
    private String targetUser;
    private String localUser;
    private NotificationManager notificationManager;
    private boolean isVideoCall;
    private ImageView localVideoView;
    private ImageView remoteVideoView;

    public CallUI(String localUser, String targetUser, DataOutputStream output, String targetIp, int targetPort) {
        this.localUser = localUser;
        this.targetUser = targetUser;
        this.notificationManager = new NotificationManager();

        callStage = new Stage();
        callStage.setTitle("Appel avec " + targetUser);

        // Initialize UI components
        localVideoView = new ImageView();
        localVideoView.setFitWidth(160); // Smaller for local view
        localVideoView.setFitHeight(120);
        localVideoView.setPreserveRatio(true);
        localVideoView.setStyle("-fx-border-color: #ffffff; -fx-border-width: 2; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 0);");
        localVideoView.setClip(new Circle(80, 60, 60));

        remoteVideoView = new ImageView();
        remoteVideoView.setFitWidth(640);
        remoteVideoView.setFitHeight(480);
        remoteVideoView.setPreserveRatio(true);
        remoteVideoView.setStyle("-fx-border-color: #ffffff; -fx-border-width: 2; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 0);");
        remoteVideoView.setClip(new Circle(320, 240, 240));

        callManager = new CallManager(targetIp, targetPort, localVideoView, remoteVideoView);

        // Set up the scene
        BorderPane root = new BorderPane();
        HBox controlBar = new HBox(10);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(10));
        controlBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");

        // Add end call button
        Button endCallButton = new Button("Raccrocher");
        endCallButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        endCallButton.setOnAction(e -> endCall());
        controlBar.getChildren().add(endCallButton);

        root.setBottom(controlBar);

        // Set background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("/fond.jpg"));
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        );
        root.setBackground(new Background(background));

        callStage.setOnCloseRequest(event -> {
            endCall(); // Terminate the call when the window is closed
        });

        Scene scene = new Scene(root, 800, 600);
        callStage.setScene(scene);
    }

    public void startCall(boolean withVideo) {
        this.isVideoCall = withVideo;
        callManager.startCall(targetUser, withVideo);
        configurerInterfaceSelonTypeAppel(withVideo);
        callStage.show();
    }

    private void configurerInterfaceSelonTypeAppel(boolean withVideo) {
        Scene scene = callStage.getScene();
        BorderPane root = (BorderPane) scene.getRoot();
        HBox controlBar = (HBox) root.getBottom();

        if (withVideo) {
            // Set up video layout
            StackPane videoContainer = new StackPane();
            videoContainer.setAlignment(Pos.CENTER);
            videoContainer.getChildren().add(remoteVideoView);

            // Add local video view in top-right corner
            VBox localVideoContainer = new VBox(localVideoView);
            localVideoContainer.setAlignment(Pos.BOTTOM_RIGHT);
            localVideoContainer.setPadding(new Insets(10));
            videoContainer.getChildren().add(localVideoContainer);

            root.setCenter(videoContainer);
            callStage.setTitle("Appel vidéo avec " + targetUser);

            // Add video toggle button if not already present
            boolean videoButtonExists = controlBar.getChildren().stream()
                    .filter(node -> node instanceof Button)
                    .map(node -> (Button) node)
                    .anyMatch(btn -> btn.getText().contains("Vidéo"));

            if (!videoButtonExists) {
                Button videoToggleButton = new Button("Désactiver Vidéo");
                videoToggleButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
                videoToggleButton.setOnMouseEntered(e -> videoToggleButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;"));
                videoToggleButton.setOnMouseExited(e -> videoToggleButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;"));
                videoToggleButton.setOnAction(e -> {
                    callManager.toggleVideo();
                    videoToggleButton.setText(callManager.isVideoEnabled() ? "Désactiver Vidéo" : "Activer Vidéo");
                });
                controlBar.getChildren().add(1, videoToggleButton);
            }
        } else {
            // Audio call setup
            VBox audioContainer = new VBox(20);
            audioContainer.setAlignment(Pos.CENTER);
            audioContainer.setPadding(new Insets(20));
            audioContainer.setBackground(root.getBackground());
            audioContainer.setStyle("-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");

            Image avatarImage = new Image(getClass().getResourceAsStream("/person.jpg"));
            ImageView avatarView = new ImageView(avatarImage);
            avatarView.setFitWidth(150);
            avatarView.setFitHeight(150);
            avatarView.setPreserveRatio(true);
            avatarView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
            avatarView.setClip(new Circle(75, 75, 75));

            Label audioLabel = new Label("Appel audio avec " + targetUser);
            audioLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);");

            audioContainer.getChildren().addAll(avatarView, audioLabel);
            root.setCenter(audioContainer);
            callStage.setTitle("Appel audio avec " + targetUser);

            // Remove video toggle button
            controlBar.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().contains("Vidéo"));
        }
    }

    public void endCall() {
        callManager.endCall();
        Platform.runLater(() -> callStage.close());
    }

    public void receiveVideoFrame(byte[] frameData) {
        if (isVideoCall) {
            callManager.receiveVideoFrame(frameData);
        }
    }

    public void receiveAudioData(byte[] audioData, int length) {
        callManager.receiveAudioData(audioData, length);
    }

    public String getTargetUser() {
        return targetUser;
    }
}