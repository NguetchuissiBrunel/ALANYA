package sac.mecanisme;
import sac.BD.DatabaseManager;

public class AuthService {
    public enum AuthResult {
        SUCCESS,
        USERNAME_TAKEN,
        INVALID_CREDENTIALS,
        DATABASE_ERROR
    }

    // Tentative de connexion
    public static AuthResult login(String nom, String mot_de_passe) {
        try {
            if (DatabaseManager.authenticate(nom, mot_de_passe)) {
                return AuthResult.SUCCESS;
            }
            return AuthResult.INVALID_CREDENTIALS;
        } catch (Exception e) {
            return AuthResult.DATABASE_ERROR;
        }
    }

    // Tentative d'inscription
    public static AuthResult register(String nom, String mot_de_passe) {
        try {
            if (!DatabaseManager.isUsernameAvailable(nom)) {
                return AuthResult.USERNAME_TAKEN;
            }

            if (DatabaseManager.register(nom, mot_de_passe)) {
                return AuthResult.SUCCESS;
            }
            return AuthResult.DATABASE_ERROR;
        } catch (Exception e) {
            return AuthResult.DATABASE_ERROR;
        }
    }
}