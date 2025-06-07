package sac.mecanisme;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sac.client.ChatClient;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class NotificationManager {
    private Clip ringtoneClip;
    private Stage waitingStage;

    public void showNotification(String message) {
        Platform.runLater(() -> {
            // Create notification stage
            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.TRANSPARENT);
            notificationStage.setAlwaysOnTop(true);

            // Create root layout with gradient background
            VBox root = new VBox(10);
            root.setPadding(new Insets(15));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0d47a1, #1565c0); " +
                    "-fx-background-radius: 10; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
            root.setMaxWidth(300);
            root.setAlignment(Pos.CENTER_LEFT);

            // Avatar
            Image avatarImage = new Image(getClass().getResourceAsStream("/person.jpg"));
            ImageView avatarView = new ImageView(avatarImage);
            avatarView.setFitWidth(40);
            avatarView.setFitHeight(40);
            avatarView.setPreserveRatio(true);
            avatarView.setClip(new Circle(20, 20, 20));

            // Message label
            Label messageLabel = new Label(message);
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-font-size: 14px;");

            // Close button
            Button closeButton = new Button("✖");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
            closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;"));
            closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;"));
            closeButton.setOnAction(e -> notificationStage.close());

            // Layout
            HBox contentBox = new HBox(10, avatarView, messageLabel);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().addAll(contentBox, closeButton);

            // Set up scene
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            notificationStage.setScene(scene);

            // Position at top-right corner
            notificationStage.setX(notificationStage.getX() + notificationStage.getWidth() - 320);
            notificationStage.setY(50);

            // Fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Auto-dismiss after 5 seconds
            PauseTransition autoDismiss = new PauseTransition(Duration.seconds(5));
            autoDismiss.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> notificationStage.close());
                fadeOut.play();
            });
            autoDismiss.play();

            notificationStage.show();
        });
    }

    public void showCallRequestDialog(String caller, boolean withVideo, ChatClient client, String callerIp, int callerPort) {
        Platform.runLater(() -> {
            System.out.println("NotificationManager: Showing call request for " + caller + ", video=" + withVideo);

            // Initialize and play ringtone
            startRingtone();

            // Create custom stage for call interface
            Stage callRequestStage = new Stage();
            callRequestStage.initStyle(StageStyle.TRANSPARENT);
            callRequestStage.setAlwaysOnTop(true);
            Image img = new Image(getClass().getResourceAsStream("/logo2.png"));
            ImageView imgV = new ImageView(img);
            imgV.setFitHeight(20);
            imgV.setFitWidth(20);
            imgV.getStyleClass().add("Ima");
            callRequestStage.getIcons().add(img);

            // Create root layout with background image and gradient overlay
            StackPane root = new StackPane();
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(20));

            // Background image
            Image backgroundImage = new Image(getClass().getResourceAsStream("/fond.jpg"));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            root.setBackground(new Background(background));

            // Gradient overlay
            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);
            LinearGradient gradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0d47a1", 0.7)),
                    new Stop(1, Color.web("#1565c0", 0.7))
            );
            content.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(10), null)));
            content.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);");
            content.setMaxWidth(400);
            content.setMaxHeight(600);

            // Caller avatar
            Image avatarImage = new Image(getClass().getResourceAsStream("/person.jpg"));
            ImageView avatarView = new ImageView(avatarImage);
            avatarView.setFitWidth(150);
            avatarView.setFitHeight(150);
            avatarView.setPreserveRatio(true);
            avatarView.setClip(new Circle(75, 75, 75));
            avatarView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);");

            // Pulsing animation for avatar
            ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), avatarView);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(ScaleTransition.INDEFINITE);
            pulse.play();

            // Caller information
            Label callerLabel = new Label(caller);
            callerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label callTypeLabel = new Label(withVideo ? "Appel vidéo entrant" : "Appel audio entrant");
            callTypeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-opacity: 0.8;");

            Label ringingLabel = new Label("Ringing...");
            ringingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-opacity: 0.7;");

            // Accept and reject buttons with icons
            Button acceptButton = new Button("✔ Accepter");
            acceptButton.setStyle(
                    "-fx-background-color: #00FF00; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            );
            acceptButton.setOnMouseEntered(e -> acceptButton.setStyle(
                    "-fx-background-color: #00CC00; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            ));
            acceptButton.setOnMouseExited(e -> acceptButton.setStyle(
                    "-fx-background-color: #00FF00; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            ));

            Button rejectButton = new Button("✖ Rejeter");
            rejectButton.setStyle(
                    "-fx-background-color: #FF0000; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            );
            rejectButton.setOnMouseEntered(e -> rejectButton.setStyle(
                    "-fx-background-color: #CC0000; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            ));
            rejectButton.setOnMouseExited(e -> rejectButton.setStyle(
                    "-fx-background-color: #FF0000; -fx-text-fill: white; -fx-font-size: 16px; " +
                            "-fx-padding: 10 20; -fx-background-radius: 50; -fx-cursor: hand;"
            ));

            HBox buttonBox = new HBox(30, acceptButton, rejectButton);
            buttonBox.setAlignment(Pos.CENTER);

            // Add components to content
            content.getChildren().addAll(avatarView, callerLabel, callTypeLabel, ringingLabel, buttonBox);
            root.getChildren().add(content);

            // Slide-in animation
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), content);
            slideIn.setFromY(-600);
            slideIn.setToY(0);
            slideIn.play();

            // Set up scene
            Scene scene = new Scene(root, 400, 600);
            scene.setFill(Color.TRANSPARENT);
            callRequestStage.setScene(scene);

            // Timeout mechanism
            PauseTransition timeout = new PauseTransition(Duration.seconds(30));
            timeout.setOnFinished(e -> {
                if (callRequestStage.isShowing()) {
                    System.out.println("NotificationManager: Call timed out for " + caller);
                    stopRingtone();
                    pulse.stop();
                    callRequestStage.close();
                    client.rejectCall(caller);
                }
            });
            timeout.play();

            // Button actions
            acceptButton.setOnAction(e -> {
                System.out.println("NotificationManager: Accept button clicked for " + caller);
                stopRingtone();
                pulse.stop();
                callRequestStage.close();
                Platform.runLater(() -> {
                    try {
                        client.acceptCall(caller, withVideo, callerIp, callerPort);
                        System.out.println("NotificationManager: acceptCall invoked for " + caller);
                    } catch (Exception ex) {
                        System.err.println("NotificationManager: Error in acceptCall - " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            });

            rejectButton.setOnAction(e -> {
                System.out.println("NotificationManager: Reject button clicked for " + caller);
                stopRingtone();
                pulse.stop();
                callRequestStage.close();
                client.rejectCall(caller);
            });

            // Show the stage
            callRequestStage.show();
        });
    }

    public void showWaitingDialog(String targetUser, boolean withVideo, ChatClient client) {
        Platform.runLater(() -> {
            System.out.println("NotificationManager: Showing waiting dialog for " + targetUser);

            // Créer une fenêtre pour l'appel en attente
            waitingStage = new Stage();
            waitingStage.initStyle(StageStyle.TRANSPARENT);
            waitingStage.setAlwaysOnTop(true);
            Image img = new Image(getClass().getResourceAsStream("/logo2.png"));
            ImageView imgV = new ImageView(img);
            imgV.setFitHeight(20);
            imgV.setFitWidth(20);
            waitingStage.getIcons().add(img);

            // Créer le layout racine avec une image de fond et un overlay
            StackPane root = new StackPane();
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(20));

            // Image de fond
            Image backgroundImage = new Image(getClass().getResourceAsStream("/fond.jpg"));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            root.setBackground(new Background(background));

            // Overlay avec gradient
            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);
            LinearGradient gradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0d47a1", 0.7)),
                    new Stop(1, Color.web("#1565c0", 0.7))
            );
            content.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(10), null)));
            content.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);");
            content.setMaxWidth(400);
            content.setMaxHeight(600);

            // Avatar
            Image avatarImage = new Image(getClass().getResourceAsStream("/person.jpg"));
            ImageView avatarView = new ImageView(avatarImage);
            avatarView.setFitWidth(150);
            avatarView.setFitHeight(150);
            avatarView.setPreserveRatio(true);
            avatarView.setClip(new Circle(75, 75, 75));
            avatarView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);");

            // Animation de pulsation pour l'avatar
            ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), avatarView);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(ScaleTransition.INDEFINITE);
            pulse.play();

            // Informations sur l'appel
            Label targetLabel = new Label("Appel vers " + targetUser);
            targetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label callTypeLabel = new Label(withVideo ? "Appel vidéo en attente" : "Appel audio en attente");
            callTypeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-opacity: 0.8;");

            Label waitingLabel = new Label("En attente...");
            waitingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-opacity: 0.7;");

            // Ajouter les composants au contenu
            content.getChildren().addAll(avatarView, targetLabel, callTypeLabel, waitingLabel);
            root.getChildren().add(content);

            // Animation de glissement
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), content);
            slideIn.setFromY(-600);
            slideIn.setToY(0);
            slideIn.play();

            // Configurer la scène
            Scene scene = new Scene(root, 400, 600);
            scene.setFill(Color.TRANSPARENT);
            waitingStage.setScene(scene);

            // Timeout pour fermer la fenêtre après 30 secondes si pas de réponse
            PauseTransition timeout = new PauseTransition(Duration.seconds(30));
            timeout.setOnFinished(e -> {
                if (waitingStage.isShowing()) {
                    System.out.println("NotificationManager: Waiting dialog timed out for " + targetUser);
                    pulse.stop();
                    waitingStage.close();
                    client.rejectCall(targetUser);
                }
            });
            timeout.play();

            // Afficher la fenêtre
            waitingStage.show();
        });
    }

    public void stopWaitingDialog() {
        Platform.runLater(() -> {
            if (waitingStage != null && waitingStage.isShowing()) {
                waitingStage.close();
                waitingStage = null;
                System.out.println("NotificationManager: Waiting dialog closed");
            }
        });
    }

    public void updateChatDisplay(String sender, String message) {
        showNotification(sender + ": " + message);
    }

    private void startRingtone() {
        try {
            // Load ringtone from resources
            URL resource = getClass().getResource("/ringtone.wav");
            if (resource == null) {
                System.err.println("NotificationManager: Ringtone file not found");
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("NotificationManager: Audio line not supported for ringtone");
                audioStream.close();
                return;
            }
            ringtoneClip = (Clip) AudioSystem.getLine(info);
            ringtoneClip.open(audioStream);
            ringtoneClip.loop(Clip.LOOP_CONTINUOUSLY);
            ringtoneClip.start();
            audioStream.close();
            System.out.println("NotificationManager: Ringtone started");
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            System.err.println("NotificationManager: Error playing ringtone - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopRingtone() {
        try {
            if (ringtoneClip != null) {
                if (ringtoneClip.isRunning()) {
                    ringtoneClip.stop();
                    System.out.println("NotificationManager: Ringtone stopped");
                }
                if (ringtoneClip.isOpen()) {
                    ringtoneClip.close();
                    System.out.println("NotificationManager: Ringtone clip closed");
                }
                ringtoneClip = null;
            }
        } catch (Exception e) {
            System.err.println("NotificationManager: Error stopping ringtone - " + e.getMessage());
            e.printStackTrace();
        }
    }
}