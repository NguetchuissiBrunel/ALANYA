package sac.BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InitDatabase {
    private static final String DB_URL = "jdbc:sqlite:history.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS Utilisateur (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nom TEXT NOT NULL UNIQUE," +
                    "mot_de_passe TEXT NOT NULL" +
                    ");";

            stmt.execute(sql);
            System.out.println("✅ Table Utilisateur créée avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
