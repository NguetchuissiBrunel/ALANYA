

package sac.BD;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:history.db";

    // Connexion à la base SQLite
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Vérifie si l'utilisateur existe avec ce mot de passe
    public static boolean authenticate(String nom, String mot_de_passe) {
        String sql = "SELECT * FROM Utilisateur WHERE nom = ? AND mot_de_passe = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, mot_de_passe);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Crée un nouvel utilisateur
    public static boolean register(String nom, String mot_de_passe) {
        String sql = "INSERT INTO Utilisateur (nom, mot_de_passe) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, mot_de_passe);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Vérifie si le nom d'utilisateur est disponible
    public static boolean isUsernameAvailable(String nom) {
        String sql = "SELECT * FROM Utilisateur WHERE nom = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            return !stmt.executeQuery().next(); // true si nom pas encore utilisé
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Historique des URLs (optionnel)
    public static boolean insertURL(String url) {
        String sql = "INSERT INTO url_history (url, timestamp) VALUES (?, datetime('now'))";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, url);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void afficherHistorique() {
        String sql = "SELECT * FROM url_history ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getString("url") + " => " + rs.getString("timestamp"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
