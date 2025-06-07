package sac.Interface;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sac.client.ChatClient;
import sac.mecanisme.FileHandler;
import sac.mecanisme.NotificationManager;
import sac.EmojiData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientUI {
    private Stage primaryStage;
    private ChatClient chatClient;
    private String pseudo;

    private ScrollPane chatScrollPane;
    private VBox messagesContainer;

    private ListView<String> contactList;
    private ListView<String> groupList;
    private ListView<String> fileList;
    private ListView<String> currentListView;
    private String currentChat = "";

    private NotificationManager notificationManager;
    private FileHandler fileHandler;

    private HBox inputBox;
    private Button createGroupButton;
    private Button audioCallButton;
    private Button videoCallButton;

    private Label avatar;
    private Label fullName;
    private HBox contactBox;

    public ClientUI(Stage primaryStage, ChatClient chatClient, String pseudo) {
        this.primaryStage = primaryStage;
        this.chatClient = chatClient;
        this.pseudo = pseudo;
        this.notificationManager = new NotificationManager();
        this.fileHandler = new FileHandler();
        chatClient.setClientUI(this);
    }

    public void initializeUI() {
        Image img = new Image(getClass().getResourceAsStream("/logo2.png"));
        ImageView imgV = new ImageView(img);
        imgV.setFitHeight(20);
        imgV.setFitWidth(20);
        imgV.getStyleClass().add("Ima");
        primaryStage.getIcons().add(img);

        primaryStage.setTitle("ALANYA");
        BorderPane fenetre = new BorderPane();

        fenetre.setPadding(new Insets(10));
        fenetre.setStyle("-fx-background-color: #0d47a1;");

        messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(10));

        chatScrollPane = new ScrollPane(messagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background-color: white;");

        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(5));

        Region spacerContent = new Region();
        HBox.setHgrow(spacerContent, Priority.ALWAYS);

        Label sectionLabel = new Label("Contacts"); // Par défaut, on affiche "Contacts"
        sectionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        avatar = new Label("");
        avatar.setPrefSize(40, 40);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("""
            -fx-background-color: #64b5f6;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 50%;
            -fx-border-radius: 50%;
            -fx-font-size: 16px;
        """);

        fullName = new Label("Sélectionnez un chat");
        fullName.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        contactBox = new HBox(10, avatar, fullName);
        contactBox.setAlignment(Pos.CENTER_LEFT);

        Label appName = new Label("ALANYA");
        appName.setStyle("-fx-font-weight: bold; -fx-font-size: 17px;");
        contactList = new ListView<>();
        contactList.setCellFactory(param -> new ListCell<String>() {
            private final ImageView personIcon = new ImageView(new Image(getClass().getResourceAsStream("/person.jpg")));
            {
                personIcon.setFitHeight(30);
                personIcon.setFitWidth(30);
                setPrefHeight(60);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(personIcon);
                    setText(item);
                }
            }
        });

        groupList = new ListView<>();
        groupList.setCellFactory(param -> new ListCell<String>() {
            private final ImageView groupIcon = new ImageView(new Image(getClass().getResourceAsStream("/2person.jpg")));
            {
                groupIcon.setFitHeight(30);
                groupIcon.setFitWidth(30);
                setPrefHeight(60);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(groupIcon);
                    setText(item);
                }
            }
        });

        fileList = new ListView<>();
        fileList.setCellFactory(param -> new ListCell<String>() {
            private final ImageView fIcon = new ImageView(new Image(getClass().getResourceAsStream("/f.jpg")));
            {
                fIcon.setFitHeight(20);
                fIcon.setFitWidth(20);
                setPrefHeight(40);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(fIcon);
                    setText(item);
                }
            }
        });

        currentListView = new ListView<>();
        currentListView.setItems(contactList.getItems());
        currentListView.setCellFactory(contactList.getCellFactory());
        currentListView.setOnMouseClicked(e -> {
            String selected = currentListView.getSelectionModel().getSelectedItem();
            if (currentListView.getItems() == fileList.getItems()) {
                openFile(selected);
            } else {
                openChat(selected);
            }
        });

        Button contactsButton = new Button("📇");
        contactsButton.setOnAction(e -> {
            currentListView.setItems(contactList.getItems());
            currentListView.setCellFactory(contactList.getCellFactory());
            currentListView.setOnMouseClicked(ev -> openChat(currentListView.getSelectionModel().getSelectedItem()));
            sectionLabel.setText("Contacts");
        });

        Button groupsButton = new Button("👥");
        groupsButton.setOnAction(e -> {
            currentListView.setItems(groupList.getItems());
            currentListView.setCellFactory(groupList.getCellFactory());
            currentListView.setOnMouseClicked(ev -> openChat(currentListView.getSelectionModel().getSelectedItem()));
            sectionLabel.setText("Groupes");
        });

        Button filesButton = new Button("📁");
        filesButton.setOnAction(e -> {
            currentListView.setItems(fileList.getItems());
            currentListView.setCellFactory(fileList.getCellFactory());
            currentListView.setOnMouseClicked(ev -> openFile(currentListView.getSelectionModel().getSelectedItem()));
            sectionLabel.setText("Fichiers");
        });
        contactsButton.setStyle(
                "-fx-background-color: #1976d2; -fx-text-fill: white;"
        );
        contactsButton.setOnMousePressed(event -> {
            contactsButton.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #1976d2;"
            );
        });
        contactsButton.setOnMouseReleased(event -> {
            contactsButton.setStyle(
                    "-fx-background-color: #1976d2; -fx-text-fill: white;"
            );
        });
        groupsButton.setStyle(
                "-fx-background-color: #1976d2; -fx-text-fill: white;"
        );
        groupsButton.setOnMousePressed(event -> {
            groupsButton.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #1976d2;"
            );
        });
        groupsButton.setOnMouseReleased(event -> {
            groupsButton.setStyle(
                    "-fx-background-color: #1976d2; -fx-text-fill: white;"
            );
        });
        filesButton.setStyle(
                "-fx-background-color: #1976d2; -fx-text-fill: white;"
        );
        filesButton.setOnMousePressed(event -> {
            filesButton.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #1976d2;"
            );
        });
        filesButton.setOnMouseReleased(event -> {
            filesButton.setStyle(
                    "-fx-background-color: #1976d2; -fx-text-fill: white;"
            );
        });


        HBox buttonBox = new HBox(10, contactsButton, groupsButton, filesButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(currentListView, Priority.ALWAYS);
        
        HBox nameArea = new HBox(10, imgV, appName);
        VBox leftPane = new VBox(10, nameArea, sectionLabel, spacer, currentListView, buttonBox);

        leftPane.setPadding(new Insets(10));
        leftPane.getStyleClass().add("left-pane");

        TextField messageField = new TextField();
        messageField.setPromptText("Entrer votre message...");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("📤");
        sendButton.setOnAction(e -> sendMessage(messageField));

        messageField.setOnAction(e -> sendMessage(messageField));

        Button fileButton = new Button("📎");
        fileButton.setOnAction(e -> sendFile());

        Button emojiButton = new Button("😊");
        emojiButton.setOnAction(e -> showEmojiPicker(messageField));

        audioCallButton = new Button("📞");
        videoCallButton = new Button("📹");

        audioCallButton.setOnAction(e -> initiateAudioCall());
        videoCallButton.setOnAction(e -> initiateVideoCall());

        createGroupButton = new Button("➕👥");
        createGroupButton.setOnAction(e -> createGroup());

        inputBox = new HBox(10, emojiButton, messageField, fileButton, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setVisible(false);

        createGroupButton.setVisible(false);
        audioCallButton.setVisible(false);
        videoCallButton.setVisible(false);

        mainContent.getChildren().addAll(contactBox, spacerContent, createGroupButton, audioCallButton, videoCallButton);

        BorderPane messageArea = new BorderPane();
        messageArea.setTop(mainContent);
        messageArea.setCenter(chatScrollPane);
        messageArea.setBottom(inputBox);

        fenetre.setCenter(messageArea);
        fenetre.setLeft(leftPane);

        fenetre.getStylesheets().add("style.css");

        primaryStage.setOnCloseRequest(event -> {
            chatClient.disconnect(); // Disconnect the client (closes socket and UDP socket)
            System.exit(0); // Terminate the JVM
        });

        Scene scene = new Scene(fenetre, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showEmojiPicker(TextField messageField) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choisir un emoji");
        dialog.getDialogPane().getStylesheets().add("style.css");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Map<String, List<String>> emojiCategories = EmojiData.getEmojis();
        if (emojiCategories.isEmpty()) {
            notificationManager.showNotification("Aucun emoji disponible");
            return;
        }

        for (Map.Entry<String, List<String>> entry : emojiCategories.entrySet()) {
            String category = entry.getKey();
            List<String> emojis = entry.getValue();

            GridPane emojiGrid = new GridPane();
            emojiGrid.setHgap(8);
            emojiGrid.setVgap(8);
            emojiGrid.setPadding(new Insets(10));
            emojiGrid.getStyleClass().add("emoji-grid");

            int row = 0;
            int col = 0;
            for (String emoji : emojis) {
                Button emojiButton = new Button(emoji);
                emojiButton.getStyleClass().add("emoji-button");
                emojiButton.setOnAction(e -> {
                    messageField.appendText(emoji);
                });
                emojiGrid.add(emojiButton, col, row);
                col++;
                if (col > 7) {
                    col = 0;
                    row++;
                }
            }

            ScrollPane scrollPane = new ScrollPane(emojiGrid);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f0f0f0;");

            Tab tab = new Tab(category, scrollPane);
            tabPane.getTabs().add(tab);
        }

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(400, 300);
        dialog.showAndWait();
    }

    private void initiateAudioCall() {
        if (currentChat.isEmpty()) {
            notificationManager.showNotification("Veuillez sélectionner un contact pour appeler");
            return;
        }
        chatClient.initiateCall(currentChat, false);
    }

    private void initiateVideoCall() {
        if (currentChat.isEmpty()) {
            notificationManager.showNotification("Veuillez sélectionner un contact pour appeler");
            return;
        }
        chatClient.initiateCall(currentChat, true);
    }

    private void sendMessage(TextField messageField) {
        String message = messageField.getText().trim();
        if (message.isEmpty() || currentChat.isEmpty()) return;

        chatClient.sendMessage(currentChat, message);

        Platform.runLater(() -> {
            addMessageBubble("Moi", message, true);
            messageField.clear();
            scrollToBottom();
        });
    }

    private void addMessageBubble(String sender, String messageText, boolean isMe) {
        BorderPane messageBubble = new BorderPane();

        Label messageLabel = new Label(messageText);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(8));
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-background-color: " + (isMe ? "#DCF8C6" : "#FFA07A") +
                "; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-font-family:'Segoe UI Emoji';");

        if (isMe) {
            messageBubble.setRight(messageLabel);
        } else {
            Label senderLabel = new Label(sender);
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");

            VBox messageBox = new VBox(5, senderLabel, messageLabel);
            messageBubble.setLeft(messageBox);
        }

        messageBubble.setPadding(new Insets(5));
        messagesContainer.getChildren().add(messageBubble);
    }

    private void scrollToBottom() {
        chatScrollPane.setVvalue(1.0);
    }

    private void sendFile() {
        if (currentChat.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            chatClient.sendFile(currentChat, file);

            Platform.runLater(() -> {
                if (!fileList.getItems().contains(file.getName())) {
                    fileList.getItems().add(file.getName());
                }
                notificationManager.showNotification("Fichier envoyé: " + file.getName());
            });
        }
    }

    private void createGroup() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Créer un groupe");

        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Nom du groupe");

        ListView<String> membersList = new ListView<>();
        membersList.getItems().addAll(contactList.getItems());
        membersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        VBox content = new VBox(10,
                new Label("Nom du groupe:"),
                groupNameField,
                new Label("Membres:"),
                membersList
        );
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String groupName = groupNameField.getText().trim();
                if (groupName.isEmpty()) {
                    notificationManager.showNotification("Le nom du groupe ne peut pas être vide");
                    return null;
                }

                List<String> selectedMembers = new ArrayList<>(membersList.getSelectionModel().getSelectedItems());
                if (selectedMembers.isEmpty()) {
                    notificationManager.showNotification("Veuillez sélectionner au moins un membre");
                    return null;
                }

                if (!selectedMembers.contains(pseudo)) {
                    selectedMembers.add(pseudo);
                }

                System.out.println("createGroup: Creating group " + groupName + " with members " + selectedMembers);
                chatClient.createGroup(groupName, selectedMembers);
                return selectedMembers;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openChat(String contact) {
        if (contact == null || contact.isEmpty()) {
            System.out.println("openChat: No contact selected");
            return;
        }

        System.out.println("openChat: Opening chat for " + contact);
        currentChat = contact;
        messagesContainer.getChildren().clear();

        Platform.runLater(() -> {
            try {
                avatar.setText(contact.substring(0, 1).toUpperCase());
                fullName.setText(" " + contact);
                inputBox.setVisible(true);
                createGroupButton.setVisible(true);
                audioCallButton.setVisible(true);
                videoCallButton.setVisible(true);
                System.out.println("openChat: UI updated for " + contact);
            } catch (Exception e) {
                System.err.println("openChat: Error updating UI - " + e.getMessage());
            }
        });

        List<String> history = chatClient.getChatHistory().get(contact);
        if (history != null) {
            for (String line : history) {
                processHistoryMessage(line);
            }
        }

        chatClient.retrieveUnreadMessages(contact);
        scrollToBottom();
    }

    private void processHistoryMessage(String message) {
        if (message.startsWith("Moi: ")) {
            String content = message.substring(5);
            addMessageBubble("Moi", content, true);
        } else {
            int colonIndex = message.indexOf(": ");
            if (colonIndex > 0) {
                String sender = message.substring(0, colonIndex);
                String content = message.substring(colonIndex + 2);
                addMessageBubble(sender, content, false);
            } else {
                Label systemMessage = new Label(message);
                systemMessage.setStyle("-fx-font-style: italic; -fx-text-fill: #777777;");
                systemMessage.setAlignment(Pos.CENTER);
                systemMessage.setMaxWidth(Double.MAX_VALUE);
                messagesContainer.getChildren().add(systemMessage);
            }
        }
    }

    public void updateChatDisplay(String targetChat, String formattedMessage) {
        Platform.runLater(() -> {
            if (currentChat.equals(targetChat)) {
                int colonIndex = formattedMessage.indexOf(": ");
                if (colonIndex > 0) {
                    String sender = formattedMessage.substring(0, colonIndex);
                    String content = formattedMessage.substring(colonIndex + 2);
                    addMessageBubble(sender, content, false);
                    scrollToBottom();
                }
            } else {
                if (groupList.getItems().contains(targetChat)) {
                    notificationManager.showNotification("Nouveau message dans le groupe " + targetChat);
                } else {
                    notificationManager.showNotification("Nouveau message de " + targetChat);
                }
            }
        });
    }

    public void updateFileList(String fileName, String sender, String targetChat) {
        Platform.runLater(() -> {
            if (!fileList.getItems().contains(fileName)) {
                fileList.getItems().add(fileName);
            }
            if (currentListView.getItems() == fileList.getItems()) {
                if (!currentListView.getItems().contains(fileName)) {
                    currentListView.getItems().add(fileName);
                }
            }
            if (groupList.getItems().contains(targetChat)) {
                notificationManager.showNotification("Nouveau fichier dans le groupe " + targetChat + " de " + sender);
            } else {
                notificationManager.showNotification("Nouveau fichier de " + sender);
            }
        });
    }

    private void openFile(String fileName) {
        if (fileName != null) {
            File file = chatClient.getReceivedFiles().get(fileName);
            if (file == null) {
                file = chatClient.getSentFiles().get(fileName);
            }

            if (file != null) {
                fileHandler.openFile(file);
            } else {
                notificationManager.showNotification("Fichier non trouvé : " + fileName);
            }
        }
    }

    public void updateContactList(List<String> clients) {
        Platform.runLater(() -> {
            contactList.getItems().clear();
            contactList.getItems().addAll(clients);
            if (currentListView.getItems() == contactList.getItems()) {
                currentListView.getItems().clear();
                currentListView.getItems().addAll(clients);
            }
        });
    }

    public void addToGroupList(String groupName) {
        Platform.runLater(() -> {
            System.out.println("addToGroupList: Attempting to add group " + groupName);
            if (!groupList.getItems().contains(groupName)) {
                groupList.getItems().add(groupName);
                System.out.println("addToGroupList: Added " + groupName + " to groupList");
            } else {
                System.out.println("addToGroupList: " + groupName + " already in groupList");
            }
            if (currentListView.getItems() == groupList.getItems()) {
                if (!currentListView.getItems().contains(groupName)) {
                    currentListView.getItems().add(groupName);
                    System.out.println("addToGroupList: Added " + groupName + " to currentListView");
                } else {
                    System.out.println("addToGroupList: " + groupName + " already in currentListView");
                }
            }
        });
    }

    public void displayUnreadMessage(String sender, String unreadMessage) {
        Platform.runLater(() -> {
            addMessageBubble(sender + " (non lu)", unreadMessage, false);
            scrollToBottom();
        });
    }

    public String getCurrentChat() {
        return currentChat;
    }

    public ListView<String> getGroupList() {
        return groupList;
    }
}