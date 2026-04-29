package mg.gov.interieur.visa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);

    private static final String DB_NAME   = "visa_management";
    private static final String APP_USER  = "visa_user";
    private static final String APP_PASS  = "robot123";

    public static void initialize(String adminUrl, String adminUser, String adminPass) {
        log.info("🔧 Bootstrap BDD — connexion admin : {}", adminUrl);
        try (Connection conn = DriverManager.getConnection(adminUrl, adminUser, adminPass)) {
            conn.setAutoCommit(true);
            creerUtilisateur(conn);
            creerBase(conn, adminUser, adminPass);
            octroierDroits(conn);
            log.info("✅ Bootstrap terminé — base '{}' prête", DB_NAME);
        } catch (Exception e) {
            throw new IllegalStateException(
                "❌ Impossible d'initialiser la base PostgreSQL : " + e.getMessage(), e);
        }
    }

    private static void creerUtilisateur(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM pg_roles WHERE rolname = ?")) {
            ps.setString(1, APP_USER);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                conn.createStatement().execute(
                    "CREATE USER " + APP_USER + " WITH PASSWORD '" + APP_PASS + "'");
                log.info("  ✔ Utilisateur '{}' créé", APP_USER);
            } else {
                // Resynchronise le mot de passe au cas où
                conn.createStatement().execute(
                    "ALTER USER " + APP_USER + " WITH PASSWORD '" + APP_PASS + "'");
                log.info("  ✔ Utilisateur '{}' existant — mot de passe resynchronisé", APP_USER);
            }
        }
    }

    private static void creerBase(Connection conn, String adminUser, String adminPass) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM pg_database WHERE datname = ?")) {
            ps.setString(1, DB_NAME);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                conn.createStatement().execute(
                    "CREATE DATABASE " + DB_NAME +
                    " OWNER " + APP_USER +
                    " ENCODING 'UTF8'");
                log.info("  ✔ Base '{}' créée", DB_NAME);
                
                // Se connecter à la nouvelle base pour accorder les droits sur le schéma public
                try (Connection newDbConn = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/" + DB_NAME, adminUser, adminPass)) {
                    newDbConn.createStatement().execute(
                        "GRANT ALL ON SCHEMA public TO " + APP_USER);
                    newDbConn.createStatement().execute(
                        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO " + APP_USER);
                    newDbConn.createStatement().execute(
                        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO " + APP_USER);
                    log.info("  ✔ Droits sur le schéma public accordés à '{}'", APP_USER);
                }
            } else {
                log.info("  ✔ Base '{}' déjà existante", DB_NAME);
                
                // Même si la base existe, s'assurer que les droits sont corrects
                try (Connection existingDbConn = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/" + DB_NAME, adminUser, adminPass)) {
                    existingDbConn.createStatement().execute(
                        "GRANT ALL ON SCHEMA public TO " + APP_USER);
                    existingDbConn.createStatement().execute(
                        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO " + APP_USER);
                    existingDbConn.createStatement().execute(
                        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO " + APP_USER);
                    log.info("  ✔ Droits sur le schéma public mis à jour pour '{}'", APP_USER);
                }
            }
        }
    }

    private static void octroierDroits(Connection conn) throws Exception {
        conn.createStatement().execute(
            "GRANT ALL PRIVILEGES ON DATABASE " + DB_NAME + " TO " + APP_USER);
        log.info("  ✔ Droits octroyés à '{}'", APP_USER);
    }
}