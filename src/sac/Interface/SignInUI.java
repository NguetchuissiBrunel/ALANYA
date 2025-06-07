package sac.Interface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import sac.client.ClientApplication;

public class SignInUI extends Application {
    private ClientApplication clientApplication;
    private Stage primaryStage;
    private static final String PRIMARY_COLOR = "#1a73e8";
    private static final String ERROR_COLOR = "#d32f2f";

    // Définir le ratio de taille de la fenêtre par rapport à l'écran
    private final double WINDOW_WIDTH_RATIO = 0.7; // 70% de la largeur de l'écran
    private final double WINDOW_HEIGHT_RATIO = 0.8; // 80% de la hauteur de l'écran
    private final double MIN_WIDTH = 800;
    private final double MIN_HEIGHT = 600;


    public SignInUI(Stage primaryStage, ClientApplication clientApplication) {
        this.primaryStage = primaryStage;
        this.clientApplication = clientApplication;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }

    public void initializeUI() {
        // Obtenir les dimensions de l'écran
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();


        double windowWidth = Math.max(MIN_WIDTH, screenWidth * WINDOW_WIDTH_RATIO);
        double windowHeight = Math.max(MIN_HEIGHT, screenHeight * WINDOW_HEIGHT_RATIO);

        // Fond d'écran
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #e4e8f0);");

        // Conteneur principal
        VBox mainContainer = createMainContainer();


        Label titleLabel = new Label("Créer un compte ALANYA");
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        Label subtitleLabel = new Label("Rejoignez notre communauté");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web("#555555"));

        // Formulaire d'inscription
        GridPane form = createForm();

        // Ajouter tous les éléments au conteneur principal
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, form);

        // Ajouter le conteneur principal au centre avec padding pour assurer espace pour le footer
        VBox centerBox = new VBox(mainContainer);
        centerBox.setAlignment(Pos.CENTER);
        // Ajouter une marge en bas pour laisser de la place au footer
        centerBox.setPadding(new Insets(10, 0, 60, 0));
        root.setCenter(centerBox);

        // Pied de page
        Label footerLabel = new Label("© 2025 ALANYA - Communication sécurisée");
        footerLabel.setFont(Font.font("Segoe UI", 12));
        footerLabel.setTextFill(Color.web("#888888"));
        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));
        root.setBottom(footer);

        Image img = new Image(getClass().getResourceAsStream("/logo2.png"));
        ImageView imgV = new ImageView(img);
        imgV.setFitHeight(20);
        imgV.setFitWidth(20);
        imgV.getStyleClass().add("Ima");
        primaryStage.getIcons().add(img);

        // Créer et afficher la scène
        Scene scene = new Scene(root, windowWidth, windowHeight);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Création de compte - ALANYA");

        // Définir les dimensions minimales pour éviter des problèmes de mise en page
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Ne pas utiliser le mode plein écran
        primaryStage.setMaximized(false);

        // Centrer la fenêtre sur l'écran
        primaryStage.centerOnScreen();

        primaryStage.show();
    }

    private VBox createMainContainer() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(550);
        container.setMaxHeight(500);
        // Ne pas définir de hauteur maximum pour laisser le contenu s'ajuster naturellement
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        container.setEffect(new DropShadow(20, Color.gray(0.2)));
        container.setPadding(new Insets(20));
        return container;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(15);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));

        // Champs de saisie
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        styleTextField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        styleTextField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        styleTextField(confirmPasswordField);

        // Case à cocher pour les conditions
        CheckBox termsCheckBox = new CheckBox("J'accepte les conditions d'utilisation");
        termsCheckBox.setStyle("-fx-font-size: 14px;");

        // Bouton d'inscription
        Button registerButton = new Button("Créer mon compte");
        styleButton(registerButton, PRIMARY_COLOR);
        registerButton.setDisable(true);

        // Lien de connexion
        Hyperlink loginLink = createHyperlink("Se connecter");
        HBox loginBox = new HBox(5, new Label("Déjà un compte ?"), loginLink);
        loginBox.setAlignment(Pos.CENTER);

        // Message d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 14px;");

        // Ajouter les éléments au formulaire
        form.add(new Label("Nom d'utilisateur"), 0, 0);
        form.add(usernameField, 0, 1);
        form.add(new Label("Mot de passe"), 0, 2);
        form.add(passwordField, 0, 3);
        form.add(new Label("Confirmer le mot de passe"), 0, 4);
        form.add(confirmPasswordField, 0, 5);
        form.add(termsCheckBox, 0, 6);
        form.add(registerButton, 0, 7);
        form.add(errorLabel, 0, 8);
        form.add(new Separator(), 0, 9);
        form.add(loginBox, 0, 10);

        // Actions des boutons et liens
        termsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            registerButton.setDisable(!newVal);
        });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("Veuillez remplir tous les champs.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Les mots de passe ne correspondent pas.");
                return;
            }

            errorLabel.setText("");
            clientApplication.handleRegister(username, password, primaryStage);
        });

        loginLink.setOnAction(e -> {
            LoginUI loginUI = new LoginUI(primaryStage, clientApplication);
            loginUI.initializeUI();
        });

        return form;
    }

    private void styleTextField(Control field) {
        field.setStyle("-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0;");
        field.setPrefWidth(200);
    }

    private void styleButton(Button button, String color) {
        button.setStyle(String.format(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: %s; " +
                        "-fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;", color));
        button.setMaxWidth(Double.MAX_VALUE);

        // Effet hover
        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: %s; " +
                        "-fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;", darkenColor(color))));
        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: %s; " +
                        "-fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;", color)));
    }

    private Hyperlink createHyperlink(String text) {
        Hyperlink link = new Hyperlink(text);
        link.setStyle("-fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-size: 14px; -fx-border-color: transparent;");
        link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill: " + darkenColor(PRIMARY_COLOR) + "; -fx-underline: true; -fx-border-color: transparent;"));
        link.setOnMouseExited(e -> link.setStyle("-fx-text-fill: " + PRIMARY_COLOR + "; -fx-underline: false; -fx-border-color: transparent;"));
        return link;
    }

    // Utilitaire pour assombrir une couleur pour les effets hover
    private String darkenColor(String hexColor) {
        return hexColor.equals(PRIMARY_COLOR) ? "#1765cc" : "#5a4cad";
    }

    public static void main(String[] args) {
        launch(args);
    }
}