package sac.Interface;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import sac.client.ClientApplication;

import java.util.Arrays;
import java.util.List;

public class AcceuilUI {

    private Stage primaryStage;
    private ClientApplication clientApplication;
    private Text emojiText;
    private int currentEmojiIndex = 0;
    private final List<String> messageEmojis = Arrays.asList(
            "💬", "📱", "✉️", "📨", "🗨️", "📲", "📤", "📥", "👥", "🔔"
    );

    // Définir le ratio de taille de la fenêtre par rapport à l'écran
    private final double WINDOW_WIDTH_RATIO = 0.7; // 70% de la largeur de l'écran
    private final double WINDOW_HEIGHT_RATIO = 0.8; // 80% de la hauteur de l'écran
    private final double MIN_WIDTH = 800;
    private final double MIN_HEIGHT = 600;

    public AcceuilUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public void initialiseUI() {
        // Obtenir les dimensions de l'écran
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // Calculer les dimensions de la fenêtre avec des valeurs minimales
        double windowWidth = Math.max(MIN_WIDTH, screenWidth * WINDOW_WIDTH_RATIO);
        double windowHeight = Math.max(MIN_HEIGHT, screenHeight * WINDOW_HEIGHT_RATIO);

        // conteneur principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #e4e8f0);");

        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(400);
        mainContainer.setMaxHeight(500);
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        mainContainer.setEffect(new DropShadow(20, Color.gray(0.2)));
        mainContainer.setPadding(new Insets(40));

        // Logo et titre
        Image logo = new Image(getClass().getResource("/Logo2.png").toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(100);
        logoView.setPreserveRatio(true);

        Label titleLabel = new Label("ALANYA");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web("#1a73e8"));
        titleLabel.setEffect(new DropShadow(10, Color.LIGHTGRAY));

        Label subtitleLabel = new Label("Votre nouvelle expérience de messagerie");
        subtitleLabel.setFont(Font.font("Segoe UI", 18));
        subtitleLabel.setTextFill(Color.web("#555555"));

        // Emoji animé
        emojiText = new Text(messageEmojis.get(0));
        emojiText.setFont(Font.font("Segoe UI", 60));

        // Timeline pour changer l'emoji toutes les 10 secondes
        Timeline emojiTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    currentEmojiIndex = (currentEmojiIndex + 1) % messageEmojis.size();
                    emojiText.setText(messageEmojis.get(currentEmojiIndex));
                })
        );
        emojiTimeline.setCycleCount(Timeline.INDEFINITE);
        emojiTimeline.play();

        // Bouton pour commencer
        Button startButton = new Button("Commencer l'expérience");
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        startButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; " +
                "-fx-padding: 15 30; -fx-background-radius: 30; -fx-cursor: hand;");

        // Effet hover
        startButton.setOnMouseEntered(e ->
                startButton.setStyle("-fx-background-color: #1765cc; -fx-text-fill: white; " +
                        "-fx-padding: 15 30; -fx-background-radius: 30; -fx-cursor: hand;"));
        startButton.setOnMouseExited(e ->
                startButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; " +
                        "-fx-padding: 15 30; -fx-background-radius: 30; -fx-cursor: hand;"));

        // Action du bouton
        startButton.setOnAction(e -> {
            if (clientApplication != null) {
                LoginUI loginUI = new LoginUI(primaryStage, clientApplication);
                loginUI.initializeUI();
            } else {
                LoginUI loginUI = new LoginUI();
                try {
                    loginUI.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Pied de page
        Label footerLabel = new Label("© 2025 ALANYA - 3GI");
        footerLabel.setFont(Font.font("Segoe UI", 12));
        footerLabel.setTextFill(Color.web("#888888"));

        // Ajout des éléments au conteneur principal
        mainContainer.getChildren().addAll(logoView, titleLabel, subtitleLabel, emojiText, startButton, footerLabel);

        // Placer le conteneur au centre
        root.setCenter(mainContainer);

        Image img = new Image(getClass().getResourceAsStream("/logo2.png"));
        ImageView imgV = new ImageView(img);
        imgV.setFitHeight(20);
        imgV.setFitWidth(20);
        imgV.getStyleClass().add("Ima");
        primaryStage.getIcons().add(img);

        // Créer la scène avec une taille adaptative
        Scene scene = new Scene(root, windowWidth, windowHeight);
        primaryStage.setTitle("ALANYA - Messagerie");
        primaryStage.setScene(scene);

        // Définir les dimensions minimales pour éviter des problèmes de mise en page
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Ne plus utiliser le mode plein écran
        primaryStage.setMaximized(false);

        // Centrer la fenêtre sur l'écran
        primaryStage.centerOnScreen();

        primaryStage.show();
    }
}