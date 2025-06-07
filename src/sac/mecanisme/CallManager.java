package sac.mecanisme;

import javafx.scene.image.ImageView;
import javax.sound.sampled.LineUnavailableException;

public class CallManager {
    private VideoHandler videoHandler;
    private AudioHandler audioHandler;
    private String targetUser;
    private boolean isCallActive = false;
    private boolean isVideoEnabled = false;
    private boolean isAudioEnabled = false;

    public CallManager(String targetIp, int targetPort, ImageView localVideoView, ImageView remoteVideoView) {
        this.videoHandler = new VideoHandler(localVideoView, remoteVideoView, targetIp, targetPort);
        this.audioHandler = new AudioHandler(targetIp, targetPort);
        System.out.println("CallManager: Initialized with target " + targetIp + ":" + targetPort);
    }

    public void startCall(String targetUser, boolean withVideo) {
        this.targetUser = targetUser;
        isCallActive = true;

        System.out.println("CallManager: Starting call with " + targetUser + ", video=" + withVideo);

        // Configure handlers with target user
        videoHandler.setTargetUser(targetUser);
        audioHandler.setTargetUser(targetUser);

        try {
            // Start audio
            startAudio();
            if (withVideo) {
                startVideo();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("CallManager: Failed to start audio: " + e.getMessage());
        }
    }

    public void endCall() {
        isCallActive = false;
        System.out.println("CallManager: Ending call with " + targetUser);

        // Stop audio and video streams
        if (isAudioEnabled) {
            stopAudio();
        }

        if (isVideoEnabled) {
            stopVideo();
        }
    }

    public void startAudio() throws LineUnavailableException {
        System.out.println("CallManager: Starting audio for target: " + targetUser);
        if (isCallActive && !isAudioEnabled) {
            audioHandler.startAudioCapture();
            isAudioEnabled = true;
            System.out.println("CallManager: Audio started successfully");
        }
    }

    public void startVideo() {
        System.out.println("CallManager: Starting video for target: " + targetUser);
        if (isCallActive && !isVideoEnabled) {
            videoHandler.startVideoCapture();
            isVideoEnabled = true;
            System.out.println("CallManager: Video started successfully");
        }
    }

    public void stopVideo() {
        if (isVideoEnabled) {
            videoHandler.stopVideoCapture();
            isVideoEnabled = false;
            System.out.println("CallManager: Video stopped");
        }
    }

    public void stopAudio() {
        if (isAudioEnabled) {
            audioHandler.stopAudioCapture();
            isAudioEnabled = false;
            System.out.println("CallManager: Audio stopped");
        }
    }

    public void toggleVideo() {
        if (isCallActive) {
            if (isVideoEnabled) {
                stopVideo();
            } else {
                startVideo();
            }
        }
    }

    public void toggleAudio() {
        if (isCallActive) {
            if (isAudioEnabled) {
                stopAudio();
            } else {
                try {
                    startAudio();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                    System.err.println("CallManager: Failed to toggle audio: " + e.getMessage());
                }
            }
        }
    }

    public void receiveVideoFrame(byte[] frameData) {
        try {
            videoHandler.receiveVideoFrame(frameData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CallManager: Error receiving video: " + e.getMessage());
        }
    }

    public void receiveAudioData(byte[] audioData, int length) {
        try {
            audioHandler.receiveAudioData(audioData, length);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CallManager: Error receiving audio: " + e.getMessage());
        }
    }

    public boolean isCallActive() {
        return isCallActive;
    }

    public boolean isVideoEnabled() {
        return isVideoEnabled;
    }

    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    public String getTargetUser() {
        return targetUser;
    }
}