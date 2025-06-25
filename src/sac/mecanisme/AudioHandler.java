package sac.mecanisme;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sac.client.ChatClient;

public class AudioHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private boolean isStreaming = false;
    private DataOutputStream output;
    private String targetUser;
    private ScheduledExecutorService audioTimer;
    private final AudioFormat audioFormat;
    private final int bufferSize = 1500; // Réduire la taille du buffer
    private DatagramSocket udpSocket;
    private String targetIp;
    private int targetPort;

    public AudioHandler(String targetIp, int targetPort) {
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.audioFormat = new AudioFormat(44100.0f, 16, 1, true, false);
        try {
            this.udpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startAudioCapture() throws LineUnavailableException {
        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(micInfo)) {
            System.err.println("Format audio non supporté pour la capture");
            throw new LineUnavailableException("Format audio non supporté pour la capture");
        }

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(audioFormat);
            microphone.start();
        } catch (LineUnavailableException e) {
            System.err.println("Microphone non disponible : " + e.getMessage());
            throw new LineUnavailableException("Microphone non disponible");
        }

        // Configurer les haut-parleurs
        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speakers.open(audioFormat);
            speakers.start();
        } catch (LineUnavailableException e) {
            System.err.println("Haut-parleurs non disponibles : " + e.getMessage());
            throw new LineUnavailableException("Haut-parleurs non disponibles");
        }

        isStreaming = true;
        audioTimer = Executors.newScheduledThreadPool(2);
        audioTimer.scheduleAtFixedRate(this::captureAndSendAudio, 0, 20, TimeUnit.MILLISECONDS);
        startAudioReceiver();
    }

    private void startAudioReceiver() {
        ExecutorService receiverPool = Executors.newFixedThreadPool(2); // Pool pour la réception
        new Thread(() -> {
            try {
                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("AudioHandler: Starting audio receiver on port " + targetPort);
                while (isStreaming && !udpSocket.isClosed()) {
                    udpSocket.receive(packet);
                    byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
                    receiverPool.submit(() -> {
                        System.out.println("AudioHandler: Received audio packet of size " + packetData.length);
                        receiveAudioData(packetData, packetData.length);
                    });
                }
            } catch (IOException e) {
                if (isStreaming) {
                    e.printStackTrace();
                    System.err.println("AudioHandler: Error in audio receiver: " + e.getMessage());
                }
            } finally {
                receiverPool.shutdown();
            }
        }).start();
    }

    public void stopAudioCapture() {
        isStreaming = false;

        if (audioTimer != null) {
            audioTimer.shutdown();
            try {
                audioTimer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }

        if (speakers != null) {
            speakers.drain();
            speakers.stop();
            speakers.close();
        }

        if (udpSocket != null) {
            udpSocket.close();
        }
    }

    private void captureAndSendAudio() {
        if (!isStreaming || microphone == null) {
            System.err.println("Audio streaming stopped or microphone unavailable");
            return;
        }

        try {
            byte[] buffer = new byte[bufferSize];
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0 && targetIp != null && targetPort > 0) {
                ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(packetStream);
                dos.writeByte(0); // Type de paquet audio
                dos.writeInt(bytesRead);
                dos.write(buffer, 0, bytesRead);
                byte[] packetData = packetStream.toByteArray();

                DatagramPacket packet = new DatagramPacket(packetData, packetData.length,
                        InetAddress.getByName(targetIp), targetPort);
                udpSocket.send(packet);
                System.out.println("Sent audio packet to " + targetIp + ":" + targetPort + ", size: " + bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error sending audio: " + e.getMessage());
            e.printStackTrace();
            isStreaming = false;
        }
    }

    public void receiveAudioData(byte[] audioData, int length) {
        if (speakers == null || !speakers.isOpen()) {
            System.err.println("Speakers not open or null");
            logger.error("Speakers not open or null");
            return;
        }
        if (audioData == null || length <= 0) {
            System.err.println("Invalid audio data received: length=" + length);
            logger.warn("Invalid audio data received: length={}", length);
            return;
        }
        try {
            speakers.write(audioData, 0, length);
            logger.info("Wrote {} bytes of audio data to speakers", length);
        } catch (Exception e) {
            logger.error("Error writing audio data to speakers: {}", e.getMessage(), e);
        }
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}