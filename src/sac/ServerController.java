package sac;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerController {
    private static final int PORT = 12340;
    private static Map<String, ClientHandler> clients = new HashMap<>();
    private static Map<String, Set<String>> groups = new HashMap<>();
    private static ExecutorService clientPool = Executors.newFixedThreadPool(20);
    private static Map<String, List<String>> groupMessages = new HashMap<>();
    private static Map<String, List<String>> groupFiles = new HashMap<>();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void broadcastMessage(String message, String sender, String target) {
        if (groups.containsKey(target)) {
            // Vérifier si l'expéditeur est membre du groupe
            if (!groups.get(target).contains(sender)) {
                return;
            }

            // Envoyer le message à tous les membres du groupe SAUF l'expéditeur
            for (String member : groups.get(target)) {
                if (!member.equals(sender) && clients.containsKey(member)) {
                    clients.get(member).sendMessage(sender, target, message);
                }
            }

            // Stocker le message dans l'historique du groupe
            groupMessages.computeIfAbsent(target, k -> new ArrayList<>())
                    .add(sender + ": " + message);
        } else if (clients.containsKey(target)) {
            // Message privé
            clients.get(target).sendMessage(sender, target, message);
        }
    }

    static synchronized void broadcastFile(String fileName, byte[] fileData, String sender, String target) {
        if (groups.containsKey(target)) {
            // Vérifier si l'expéditeur est membre du groupe
            if (!groups.get(target).contains(sender)) {
                return;
            }

            // Envoyer le fichier à tous les membres du groupe SAUF l'expéditeur
            for (String member : groups.get(target)) {
                if (!member.equals(sender) && clients.containsKey(member)) {
                    clients.get(member).sendFile(fileName, fileData, sender, target);
                }
            }

            // Stocker le nom du fichier dans l'historique des fichiers du groupe
            groupFiles.computeIfAbsent(target, k -> new ArrayList<>())
                    .add(fileName);
        } else if (clients.containsKey(target)) {
            // Fichier privé
            clients.get(target).sendFile(fileName, fileData, sender, target);
        }
    }

    static synchronized void broadcastClientList() {
        try {
            List<String> clientList = new ArrayList<>(clients.keySet());
            for (ClientHandler client : clients.values()) {
                client.sendClientList(clientList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static synchronized void addClient(String pseudo, ClientHandler handler) {
        clients.put(pseudo, handler);
        broadcastMessage(pseudo + " a rejoint le chat.", "Serveur", "");
        broadcastClientList();
    }

    static synchronized void removeClient(String pseudo) {
        clients.remove(pseudo);
        broadcastMessage(pseudo + " a quitté le chat.", "Serveur", "");
        broadcastClientList();
    }

    static synchronized void forwardCallRequest(String caller, String targetUser, boolean withVideo, String callerIp, int callerPort) {
        if (clients.containsKey(targetUser)) {
            clients.get(targetUser).sendCallRequest(caller, targetUser, withVideo, callerIp, callerPort);
        }
    }

    static synchronized void forwardCallAccept(String accepter, String caller, String accepterIp, int accepterPort) {
        if (clients.containsKey(caller)) {
            clients.get(caller).sendCallAccept(accepter, caller, accepterIp, accepterPort);
        }
    }

    static synchronized void forwardCallReject(String rejecter, String caller) {
        if (clients.containsKey(caller)) {
            clients.get(caller).sendCallReject(rejecter, caller);
        }
    }

    static synchronized void forwardCallEnd(String ender, String otherUser) {
        if (clients.containsKey(otherUser)) {
            clients.get(otherUser).sendCallEnd(ender, otherUser);
        }
    }


    static synchronized void createGroup(String groupName, Set<String> members) {
        groups.put(groupName, members);
        groupMessages.put(groupName, new ArrayList<>());
        groupFiles.put(groupName, new ArrayList<>());

        for (String member : members) {
            if (clients.containsKey(member)) {
                clients.get(member).sendGroupNotification(groupName);
            }
        }
    }
}