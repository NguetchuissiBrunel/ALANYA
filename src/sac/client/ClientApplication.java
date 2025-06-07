package sac.client;


import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sac.mecanisme.AuthService;
import sac.Interface.AcceuilUI;
import sac.Interface.ClientUI;

public class ClientApplication extends Application {

    private AcceuilUI acceuilUI;
    private ClientUI clientUI;
    private ChatClient chatClient;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        acceuilUI=new AcceuilUI(primaryStage);
        acceuilUI.initialiseUI();

    }



    public void handleLogin(String username, String password,Stage primaryStage) {
        AuthService.AuthResult result = AuthService.login(username, password);

        switch (result) {
            case SUCCESS:
                showAlert("Connexion réussie", "Bienvenue " + username);
                // Ouvrir l'application principale ici
                chatClient = new ChatClient(username);
                clientUI = new ClientUI(primaryStage, chatClient, username);

                // Démarrer la connexion et l'interface
                clientUI.initializeUI();
                chatClient.connect();
                break;
            case INVALID_CREDENTIALS:
                showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect");
                break;
            case DATABASE_ERROR:
                showAlert("Erreur", "Problème de connexion à la base de données");
                break;
        }
    }

    public void handleRegister(String username, String password,Stage primaryStage) {
        AuthService.AuthResult result = AuthService.register(username, password);


        switch (result) {
            case SUCCESS:
                showAlert("Inscription réussie", "Compte créé pour " + username);
                chatClient = new ChatClient(username);
                clientUI = new ClientUI(primaryStage, chatClient, username);

                // Démarrer la connexion et l'interface
                clientUI.initializeUI();
                chatClient.connect();
                break;
            case USERNAME_TAKEN:
                showAlert("Erreur", "Ce nom d'utilisateur est déjà pris");
                break;
            case DATABASE_ERROR:
                showAlert("Erreur", "Problème de création du compte");
                break;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public void stop() {
        // Fermeture propre de la connexion
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }


}