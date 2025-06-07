package sac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class); // Added logger
    private Socket socket;
    private String pseudo;
    private DataInputStream input;
    private DataOutputStream output;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            pseudo = input.readUTF();
            ServerController.addClient(pseudo, this);
            String command;
            while ((command = input.readUTF()) != null) {
                switch (command) {
                    case "MESSAGE":
                        String target = input.readUTF();
                        String message = input.readUTF();
                        ServerController.broadcastMessage(message, pseudo, target);
                        break;
                    case "FILE":
                        target = input.readUTF();
                        String fileName = input.readUTF();
                        long fileSize = input.readLong();
                        byte[] fileData = new byte[(int) fileSize];
                        input.readFully(fileData);
                        ServerController.broadcastFile(fileName, fileData, pseudo, target);
                        break;
                    case "CREATE_GROUP":
                        String groupName = input.readUTF();
                        int memberCount = input.readInt();
                        Set<String> members = new HashSet<>();
                        for (int i = 0; i < memberCount; i++) {
                            members.add(input.readUTF());
                        }
                        ServerController.createGroup(groupName, members);
                        break;
                    case "CALL_REQUEST":
                        String targetUser = input.readUTF();
                        boolean withVideo = input.readBoolean();
                        String callerIp = input.readUTF();
                        int callerPort = input.readInt();
                        ServerController.forwardCallRequest(pseudo, targetUser, withVideo, callerIp, callerPort);
                        break;
                    case "CALL_ACCEPT":
                        String caller = input.readUTF();
                        String accepterIp = input.readUTF();
                        int accepterPort = input.readInt();
                        ServerController.forwardCallAccept(pseudo, caller, accepterIp, accepterPort);
                        break;
                    case "CALL_REJECT":
                        caller = input.readUTF();
                        ServerController.forwardCallReject(pseudo, caller);
                        break;
                    case "CALL_END":
                        String otherUser = input.readUTF();
                        ServerController.forwardCallEnd(pseudo, otherUser);
                        break;
                }
            }
        } catch (IOException e) {
            logger.info("{} disconnected: {}", pseudo, e.getMessage()); // Modified logging
        } finally {
            ServerController.removeClient(pseudo);
            closeResources();
        }
    }

    public void sendClientList(List<String> clientList) {
        try {
            output.writeUTF("CLIENT_LIST");
            output.writeInt(clientList.size());
            for (String client : clientList) {
                output.writeUTF(client);
            }
        } catch (IOException e) {
            logger.error("Error sending client list to {}: {}", pseudo, e.getMessage(), e); // Modified logging
        }
    }

    public void sendGroupNotification(String groupName) {
        try {
            output.writeUTF("ADD_TO_GROUP");
            output.writeUTF(groupName);
        } catch (IOException e) {
            logger.error("Error sending group notification to {}: {}", pseudo, e.getMessage(), e); // Modified logging
        }
    }

    public void sendCallRequest(String caller, String target, boolean withVideo, String callerIp, int callerPort) {
        try {
            output.writeUTF("CALL_REQUEST");
            output.writeUTF(caller);
            output.writeBoolean(withVideo);
            output.writeUTF(callerIp);
            output.writeInt(callerPort);
            logger.info("Sent CALL_REQUEST to {} from {} at {}:{}", target, caller, callerIp, callerPort); // Added logging
        } catch (IOException e) {
            logger.error("Error sending CALL_REQUEST to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendCallAccept(String accepter, String target, String accepterIp, int accepterPort) {
        try {
            output.writeUTF("CALL_ACCEPT");
            output.writeUTF(accepter);
            output.writeUTF(accepterIp);
            output.writeInt(accepterPort);
            logger.info("Sent CALL_ACCEPT to {} from {} at {}:{}", target, accepter, accepterIp, accepterPort); // Added logging
        } catch (IOException e) {
            logger.error("Error sending CALL_ACCEPT to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendCallReject(String rejecter, String target) {
        try {
            output.writeUTF("CALL_REJECT");
            output.writeUTF(rejecter);
        } catch (IOException e) {
            logger.error("Error sending CALL_REJECT to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendCallEnd(String ender, String target) {
        try {
            output.writeUTF("CALL_END");
            output.writeUTF(ender);
        } catch (IOException e) {
            logger.error("Error sending CALL_END to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendVideoFrame(String sender, String target, byte[] frameData) {
        try {
            synchronized (output) {
                output.writeUTF("VIDEO_FRAME");
                output.writeUTF(sender);
                output.writeInt(frameData.length);
                output.write(frameData);
                output.flush();
            }
        } catch (IOException e) {
            logger.error("Error sending video frame to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendAudioData(String sender, String target, byte[] audioData, int length) {
        try {
            synchronized (output) {
                output.writeUTF("AUDIO_DATA");
                output.writeUTF(sender);
                output.writeInt(length);
                output.write(audioData, 0, length);
                output.flush();
            }
        } catch (IOException e) {
            logger.error("Error sending audio data to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendMessage(String sender, String target, String message) {
        try {
            output.writeUTF("MESSAGE");
            output.writeUTF(sender);
            output.writeUTF(target);
            output.writeUTF(message);
        } catch (IOException e) {
            logger.error("Error sending message to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    public void sendFile(String fileName, byte[] fileData, String sender, String target) {
        try {
            output.writeUTF("FILE");
            output.writeUTF(sender);
            output.writeUTF(target);
            output.writeUTF(fileName);
            output.writeLong(fileData.length);
            output.write(fileData);
        } catch (IOException e) {
            logger.error("Error sending file to {}: {}", target, e.getMessage(), e); // Modified logging
        }
    }

    private void closeResources() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.error("Error closing resources for {}: {}", pseudo, e.getMessage(), e); // Modified logging
        }
    }
}