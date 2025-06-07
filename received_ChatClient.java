package sac.client;

import javafx.application.Platform;
import sac.Interface.CallUI;
import sac.Interface.ClientUI;
import sac.mecanisme.NotificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String pseudo;
    private Map<String, CallUI> activeCalls = new HashMap<>();
    private Map<String, List<String>> chatHistory = new HashMap<>();
    private Map<String, File> receivedFiles = new HashMap<>();
    private Map<String, File> sentFiles = new HashMap<>();
    private Map<String, Boolean> pendingCallTypes = new HashMap<>();
    private boolean isRunning = true;
    private ClientUI clientUI;
    private NotificationManager notificationManager;
    private DatagramSocket udpSocket;
    private String localIpAddress;
    private int localUdpPort;
    private Map<String, CallEndpoint> callEndpoints = new HashMap<>();

    private static class CallEndpoint {
        String ip;
        int port;

        CallEndpoint(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CallEndpoint that = (CallEndpoint) o;
            return port == that.port && ip.equals(that.ip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }
    }

    public ChatClient(String pseudo) {
        this.pseudo = pseudo;
        this.notificationManager = new NotificationManager();
        try {
            udpSocket = new DatagramSocket(0);
            localUdpPort = udpSocket.getLocalPort();
            localIpAddress = InetAddress.getLocalHost().getHostAddress();
            logger.info("UDP socket initialized on {}:{}", localIpAddress, localUdpPort);
        } catch (SocketException | UnknownHostException e) {
            logger.error("Error initializing UDP socket: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void setClientUI(ClientUI clientUI) {
        this.clientUI = clientUI;
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 12340);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF(pseudo);

            new Thread(this::receiveMessages).start();
            startUdpReceiver();
        } catch (IOException e) {
            notificationManager.showNotification("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            isRunning = false;

            if (socket != null && !socket.isClosed()) {
                if (output != null) {
                    output.writeUTF("DISCONNECT");
                    output.flush();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (input != null) input.close();
                if (output != null) output.close();
                socket.close();
            }
            if (udpSocket != null) udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initiateCall(String targetUser, boolean withVideo) {
        try {
            output.writeUTF("CALL_REQUEST");
            output.writeUTF(targetUser);
            output.writeBoolean(withVideo);
            output.writeUTF(localIpAddress);
            output.writeInt(localUdpPort);
            pendingCallTypes.put(targetUser, withVideo);
            logger.info("Initiated {} call to {}", withVideo ? "video" : "audio", targetUser);
        } catch (IOException e) {
            logger.error("Error initiating call to {}: {}", targetUser, e.getMessage(), e);
            notificationManager.showNotification("Erreur lors de l'initiation de l'appel : " + e.getMessage());
        }
    }

    public void acceptCall(String caller, boolean withVideo, String callerIp, int callerPort) {
        try {
            output.writeUTF("CALL_ACCEPT");
            output.writeUTF(caller);
            output.writeUTF(localIpAddress);
            output.writeInt(localUdpPort);

            Platform.runLater(() -> {
                CallUI callUI = new CallUI(pseudo, caller, output, callerIp, callerPort);
                activeCalls.put(caller, callUI);
                callEndpoints.put(caller, new CallEndpoint(callerIp, callerPort));
                logger.info("Added call endpoint for {}: {}:{}", caller, callerIp, callerPort); // Added logging
                callUI.startCall(withVideo);
                logger.info("Accepted {} call from {} at {}:{}", withVideo ? "video" : "audio", caller, callerIp, callerPort);
            });
        } catch (IOException e) {
            logger.error("Error accepting call from {}: {}", caller, e.getMessage(), e);
            notificationManager.showNotification("Erreur lors de l'acceptation de l'appel : " + e.getMessage());
        }
    }

    private void handleCallAccept() throws IOException {
        String accepter = input.readUTF();
        String accepterIp = input.readUTF();
        int accepterPort = input.readInt();

        Platform.runLater(() -> {
            boolean isVideoCall = pendingCallTypes.getOrDefault(accepter, false);
            pendingCallTypes.remove(accepter);

            CallUI callUI = new CallUI(pseudo, accepter, output, accepterIp, accepterPort);
            activeCalls.put(accepter, callUI);
            callEndpoints.put(accepter, new CallEndpoint(accepterIp, accepterPort));
            logger.info("Added call endpoint for {}: {}:{}", accepter, accepterIp, accepterPort); // Added logging
            callUI.startCall(isVideoCall);

            logger.info("{} accepted {} call at {}:{}", accepter, isVideoCall ? "video" : "audio", accepterIp, accepterPort);
            notificationManager.showNotification(accepter + " a accepté votre " +
                    (isVideoCall ? "appel vidéo" : "appel audio"));
        });
    }

    private void handleCallEnd() throws IOException {
        String user = input.readUTF();

        Platform.runLater(() -> {
            CallUI callUI = activeCalls.get(user);
            if (callUI != null) {
                callUI.endCall();
                activeCalls.remove(user);
                callEndpoints.remove(user);
                logger.info("Call with {} ended", user);
                notificationManager.showNotification("Appel avec " + user + " terminé");
            }
        });
    }

    private void startUdpReceiver() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                logger.info("Started UDP receiver on port {}", localUdpPort);
                while (isRunning && !udpSocket.isClosed()) {
                    udpSocket.receive(packet);
                    String sourceIp = packet.getAddress().getHostAddress();
                    int sourcePort = packet.getPort();
                    logger.info("Received UDP packet from {}:{} of size {}", sourceIp, sourcePort, packet.getLength()); // Added logging
                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                    String sender = callEndpoints.entrySet().stream()
                            .filter(entry -> entry.getValue().ip.equals(sourceIp) && entry.getValue().port == sourcePort)
                            .findFirst()
                            .map(Map.Entry::getKey)
                            .orElse(null);
                    if (sender == null) {
                        logger.warn("No sender found for packet from {}:{}", sourceIp, sourcePort); // Added logging
                        continue;
                    }

                    if (data.length < 5) {
                        logger.warn("Packet too small from {}:{}", sourceIp, sourcePort); // Added logging
                        continue;
                    }

                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    DataInputStream dis = new DataInputStream(bais);
                    byte packetType = dis.readByte();
                    int frameSize = dis.readInt();
                    logger.info("Received packet type {}, frame size {}", packetType, frameSize); // Added logging
                    if (data.length - 5 != frameSize) {
                        logger.warn("Invalid frame size: expected {}, got {}", frameSize, data.length - 5); // Added logging
                        continue;
                    }

                    byte[] frameData = new byte[frameSize];
                    dis.readFully(frameData);

                    Platform.runLater(() -> {
                        CallUI callUI = activeCalls.get(sender);
                        if (callUI != null) {
                            if (packetType == 0) {
                                callUI.receiveAudioData(frameData, frameSize);
                            } else if (packetType == 1) {
                                callUI.receiveVideoFrame(frameData);
                                logger.info("Forwarded video frame to CallUI for {}", sender); // Added logging
                            } else {
                                logger.warn("Unknown packet type {} from {}", packetType, sender);
                            }
                        } else {
                            logger.warn("No active call for sender {}", sender);
                        }
                    });
                }
            } catch (IOException e) {
                if (isRunning) {
                    logger.error("UDP receiver error: {}", e.getMessage(), e);
                    Platform.runLater(() ->
                            notificationManager.showNotification("Erreur de réception UDP : " + e.getMessage()));
                }
            }
        }, "UdpReceiverThread").start();
    }

    public void rejectCall(String caller) {
        try {
            output.writeUTF("CALL_REJECT");
            output.writeUTF(caller);
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors du rejet de l'appel : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void endCall(String user) {
        try {
            output.writeUTF("CALL_END");
            output.writeUTF(user);

            Platform.runLater(() -> {
                CallUI callUI = activeCalls.get(user);
                if (callUI != null) {
                    callUI.endCall();
                    activeCalls.remove(user);
                }
            });
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors de la fin de l'appel : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(String targetChat, String message) {
        try {
            output.writeUTF("MESSAGE");
            output.writeUTF(targetChat);
            output.writeUTF(message);

            chatHistory.computeIfAbsent(targetChat, k -> new ArrayList<>()).add("Moi: " + message);
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors de l'envoi du message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendFile(String targetChat, File file) {
        try {
            output.writeUTF("FILE");
            output.writeUTF(targetChat);
            output.writeUTF(file.getName());

            byte[] fileData = new byte[(int) file.length()];
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                bis.read(fileData);
            }

            output.writeLong(file.length());
            output.write(fileData);

            sentFiles.put(file.getName(), file);
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors de l'envoi du fichier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessageReceived() throws IOException {
        String sender = input.readUTF();
        String targetChat = input.readUTF();
        String message = input.readUTF();

        Platform.runLater(() -> {
            String formattedMessage = sender + ": " + message;

            if (clientUI.getGroupList().getItems().contains(targetChat)) {
                chatHistory.computeIfAbsent(targetChat, k -> new ArrayList<>())
                        .add(formattedMessage);

                if (clientUI != null && targetChat.equals(clientUI.getCurrentChat())) {
                    clientUI.updateChatDisplay(targetChat, formattedMessage);
                } else {
                    notificationManager.showNotification("Nouveau message dans le groupe " + targetChat);
                }
            } else {
                chatHistory.computeIfAbsent(sender, k -> new ArrayList<>()).add(formattedMessage);

                if (clientUI != null) {
                    clientUI.updateChatDisplay(sender, formattedMessage);
                }
            }
        });
    }

    private void handleFileReceived() throws IOException {
        String sender = input.readUTF();
        String targetChat = input.readUTF();
        String fileName = input.readUTF();
        long fileSize = input.readLong();

        byte[] fileData = new byte[(int) fileSize];
        input.readFully(fileData);

        File receivedFile = new File("received_" + fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(receivedFile))) {
            bos.write(fileData);
        }

        Platform.runLater(() -> {
            receivedFiles.put(fileName, receivedFile);

            if (clientUI != null) {
                if (clientUI.getGroupList().getItems().contains(targetChat)) {
                    clientUI.updateFileList(fileName, sender, targetChat);
                    notificationManager.showNotification("Nouveau fichier dans le groupe " + targetChat + " de " + sender);
                } else {
                    clientUI.updateFileList(fileName, sender, sender);
                    notificationManager.showNotification("Nouveau fichier de " + sender);
                }
            }
        });
    }

    public void createGroup(String groupName, List<String> members) {
        try {
            output.writeUTF("CREATE_GROUP");
            output.writeUTF(groupName);
            output.writeInt(members.size());
            for (String member : members) {
                output.writeUTF(member);
            }
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors de la création du groupe : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void retrieveUnreadMessages(String contact) {
        try {
            output.writeUTF("RETRIEVE_UNREAD");
            output.writeUTF(contact);
        } catch (IOException e) {
            notificationManager.showNotification("Erreur lors de la récupération des messages non lus : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            while (isRunning && socket != null && !socket.isClosed()) {
                try {
                    String command = input.readUTF();
                    switch (command) {
                        case "MESSAGE":
                            handleMessageReceived();
                            break;
                        case "FILE":
                            handleFileReceived();
                            break;
                        case "CLIENT_LIST":
                            handleClientListUpdate();
                            break;
                        case "RETRIEVE_UNREAD":
                            handleUnreadMessages();
                            break;
                        case "ADD_TO_GROUP":
                            handleGroupAddition();
                            break;
                        case "CALL_REQUEST":
                            handleCallRequest();
                            break;
                        case "CALL_ACCEPT":
                            handleCallAccept();
                            break;
                        case "CALL_REJECT":
                            handleCallReject();
                            break;
                        case "CALL_END":
                            handleCallEnd();
                            break;
                    }
                } catch (SocketException se) {
                    if ("Socket closed".equals(se.getMessage()) || !isRunning) {
                        System.out.println("Socket fermé normalement.");
                        break;
                    } else {
                        throw se;
                    }
                }
            }
        } catch (IOException e) {
            Platform.runLater(() ->
                    notificationManager.showNotification("Connexion perdue : " + e.getMessage())
            );
            e.printStackTrace();
        }
    }

    private void handleCallRequest() throws IOException {
        String caller = input.readUTF();
        boolean withVideo = input.readBoolean();
        String callerIp = input.readUTF();
        int callerPort = input.readInt();

        Platform.runLater(() -> {
            notificationManager.showCallRequestDialog(caller, withVideo, this, callerIp, callerPort);
        });
    }

    private void handleCallReject() throws IOException {
        String rejecter = input.readUTF();

        Platform.runLater(() -> {
            notificationManager.showNotification(rejecter + " a rejeté votre appel");
        });
    }

    private void handleClientListUpdate() throws IOException {
        int size = input.readInt();
        List<String> clients = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            clients.add(input.readUTF());
        }

        Platform.runLater(() -> {
            if (clientUI != null) {
                clientUI.updateContactList(clients);
            }
        });
    }

    private void handleUnreadMessages() throws IOException {
        String sender = input.readUTF();
        int unreadCount = input.readInt();

        if (unreadCount > 0) {
            List<String> messages = new ArrayList<>();
            for (int i = 0; i < unreadCount; i++) {
                String unreadMessage = input.readUTF();
                messages.add(unreadMessage);
            }

            Platform.runLater(() -> {
                if (clientUI != null) {
                    for (String message : messages) {
                        clientUI.displayUnreadMessage(sender, message);
                    }
                }
            });
        }
    }

    private void handleGroupAddition() throws IOException {
        String groupName = input.readUTF();

        Platform.runLater(() -> {
            if (clientUI != null) {
                clientUI.addToGroupList(groupName);
            }
        });
    }

    public Map<String, List<String>> getChatHistory() {
        return chatHistory;
    }

    public Map<String, File> getReceivedFiles() {
        return receivedFiles;
    }

    public Map<String, File> getSentFiles() {
        return sentFiles;
    }

    public String getPseudo() {
        return pseudo;
    }
}