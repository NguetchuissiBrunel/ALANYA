package sac.mecanisme;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoHandler {
    private static final Logger logger = LoggerFactory.getLogger(VideoHandler.class); // Added logger
    private Webcam webcam;
    private boolean isStreaming = false;
    private DataOutputStream output;
    private ImageView localVideoView;
    private ImageView remoteVideoView;
    private String targetUser;
    private ScheduledExecutorService videoTimer;
    private final int frameRate = 10;
    private final int width = 320;
    private final int height = 240;
    private DatagramSocket udpSocket;
    private String targetIp;
    private int targetPort;

    public VideoHandler(ImageView localVideoView, ImageView remoteVideoView, String targetIp, int targetPort) {
        this.localVideoView = localVideoView;
        this.remoteVideoView = remoteVideoView;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        try {
            this.udpSocket = new DatagramSocket();
            logger.info("Initialized UDP socket for video"); // Added logging
        } catch (SocketException e) {
            logger.error("Error initializing UDP socket: {}", e.getMessage(), e); // Modified logging
        }
    }

    public void startVideoCapture() {
        List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.isEmpty()) {
            logger.error("No webcam found"); // Modified logging
            return;
        }
        webcam = webcams.get(0);
        logger.info("Available webcams: {}", webcams); // Modified logging

        webcam.setViewSize(new java.awt.Dimension(width, height));

        try {
            webcam.open();
            logger.info("Selected webcam: {}", webcam.getName()); // Modified logging
            isStreaming = true;

            videoTimer = Executors.newScheduledThreadPool(4);
            videoTimer.scheduleAtFixedRate(this::captureAndSendFrame, 0, 1000 / frameRate, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Error opening webcam: {}", e.getMessage(), e); // Modified logging
        }
    }

    public void stopVideoCapture() {
        isStreaming = false;
        if (videoTimer != null) {
            videoTimer.shutdown();
            try {
                videoTimer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Error shutting down video timer: {}", e.getMessage(), e); // Modified logging
            }
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
        logger.info("Stopped video capture"); // Added logging
    }

    private void captureAndSendFrame() {
        if (!isStreaming || webcam == null || !webcam.isOpen()) {
            logger.warn("Cannot capture frame: streaming={}, webcam={}", isStreaming, webcam);
            return;
        }
        try {
            BufferedImage image = webcam.getImage();
            if (image == null) {
                logger.error("Webcam returned null image");
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            byte[] frameData = baos.toByteArray();
            if (frameData.length == 0) {
                logger.error("Encoded frame data is empty");
                return;
            }

            // Mettre à jour la vue locale
            updateLocalVideoView(frameData);

            // Envoyer les données sur un thread séparé
            videoTimer.execute(() -> sendFrame(frameData));
        } catch (IOException e) {
            logger.error("Error processing frame: {}", e.getMessage(), e);
            isStreaming = false;
        }
    }

    private void sendFrame(byte[] frameData) {
        try {
            ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(packetStream);
            dos.writeByte(1);
            dos.writeInt(frameData.length);
            dos.write(frameData);
            byte[] packetData = packetStream.toByteArray();

            if (targetIp != null && targetPort > 0) {
                DatagramPacket packet = new DatagramPacket(packetData, packetData.length,
                        InetAddress.getByName(targetIp), targetPort);
                udpSocket.send(packet);
                logger.info("Sent frame of size {} to {}:{}", frameData.length, targetIp, targetPort);
            }
        } catch (IOException e) {
            logger.error("Error sending frame: {}", e.getMessage(), e);
            isStreaming = false;
        }
    }
    public void receiveVideoFrame(byte[] frameData) {
        if (frameData == null || frameData.length == 0) {
            logger.warn("Received empty or null video frame data"); // Modified logging
            return;
        }
        try {
            updateRemoteVideoView(frameData);
        } catch (Exception e) {
            logger.error("Error receiving video frame: {}", e.getMessage(), e); // Modified logging
        }
    }

    public void updateLocalVideoView(byte[] frameData) {
        Platform.runLater(() -> {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(frameData);
                BufferedImage bufferedImage = ImageIO.read(bis);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                localVideoView.setImage(image);
                logger.info("Updated localVideoView with new frame"); // Added logging
            } catch (IOException e) {
                logger.error("Error displaying local video: {}", e.getMessage(), e); // Modified logging
            }
        });
    }

    public void updateRemoteVideoView(byte[] frameData) {
        Platform.runLater(() -> {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(frameData);
                BufferedImage bufferedImage = ImageIO.read(bis);
                if (bufferedImage == null) {
                    logger.warn("Failed to decode video frame: Invalid or corrupted image data"); // Modified logging
                    return;
                }
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                remoteVideoView.setImage(image);
                logger.info("Updated remoteVideoView with new frame"); // Added logging
            } catch (IOException e) {
                logger.error("Error displaying remote video: {}", e.getMessage(), e); // Modified logging
            }
        });
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}