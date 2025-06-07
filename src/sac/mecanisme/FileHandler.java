package sac.mecanisme;

import java.io.File;
import java.io.IOException;

public class FileHandler {
    public void openFile(File file) {
        if (file == null) return;

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", file.getAbsolutePath());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", file.getAbsolutePath());
            } else if (os.contains("nix") || os.contains("linux")) {
                pb = new ProcessBuilder("xdg-open", file.getAbsolutePath());
            } else {
                throw new UnsupportedOperationException("Système d'exploitation non pris en charge : " + os);
            }

            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveReceivedFile(byte[] fileData, String fileName) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("received_" + fileName);
            java.nio.file.Files.write(path, fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}