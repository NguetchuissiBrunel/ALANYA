package sac;

public class MainServer {
    public static void main(String[] args) {
        System.out.println("Serveur démarré...");
        try {
            ServerController server = new ServerController();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}