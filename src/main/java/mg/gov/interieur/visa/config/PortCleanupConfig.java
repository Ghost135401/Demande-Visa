package mg.gov.interieur.visa.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

/**
 * Configuration pour tuer le processus en cours sur le port de déploiement
 * avant le lancement de Spring Boot.
 *
 * Cette classe est exécutée au démarrage de l'application pour s'assurer
 * qu'aucun autre processus n'utilise le port configuré.
 */
public class PortCleanupConfig {

    /**
     * Méthode statique pour nettoyer le port avant le démarrage de Spring Boot.
     * Cette méthode est appelée directement dans la méthode main de l'application.
     * Fonctionne sur tous les ports, pas seulement 8080.
     */
    public static void cleanupPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            System.out.println("========================================");
            System.out.println("Vérification du port " + port + " avant démarrage...");

            if (isPortInUse(port)) {
                System.out.println("Le port " + port + " est déjà utilisé. Tentative de libération...");
                killProcessOnPort(port);

                // Attendre que le port soit libéré
                int maxAttempts = 10;
                int attempts = 0;
                while (isPortInUse(port) && attempts < maxAttempts) {
                    System.out.println("Attente de libération du port " + port + " (tentative " + (attempts + 1) + "/" + maxAttempts + ")...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    attempts++;
                }

                if (isPortInUse(port)) {
                    System.out.println("Impossible de libérer le port " + port + " après " + maxAttempts + " tentatives");
                    throw new RuntimeException("Le port " + port + " est toujours utilisé après tentative de libération");
                } else {
                    System.out.println("Port " + port + " libéré avec succès");
                }
            } else {
                System.out.println("Port " + port + " disponible");
            }
            System.out.println("========================================");
        } catch (NumberFormatException e) {
            System.err.println("Numéro de port invalide: " + portStr);
        }
    }

    /**
     * Vérifie si le port est déjà utilisé.
     */
    private static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Tue le processus utilisant le port spécifié.
     * Cette méthode fonctionne sur Windows et Linux.
     */
    private static void killProcessOnPort(int port) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                killProcessOnPortWindows(port);
            } else {
                killProcessOnPortUnix(port);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la tentative de terminaison du processus sur le port " + port + ": " + e.getMessage());
        }
    }

    /**
     * Tue le processus sur le port spécifié pour Windows.
     */
    private static void killProcessOnPortWindows(int port) throws IOException, InterruptedException {
        System.out.println("Recherche du processus sur le port " + port + " (Windows)...");

        // Trouver le PID du processus utilisant le port
        ProcessBuilder findProcessBuilder = new ProcessBuilder(
            "cmd", "/c", "netstat -ano | findstr :" + port
        );

        Process findProcess = findProcessBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));

        String line;
        String pid = null;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 5 && parts[1].endsWith(":" + port)) {
                pid = parts[parts.length - 1];
                break;
            }
        }
        reader.close();
        findProcess.waitFor();

        if (pid != null) {
            System.out.println("Processus trouvé avec PID: " + pid);

            // Tuer le processus
            ProcessBuilder killProcessBuilder = new ProcessBuilder("cmd", "/c", "taskkill /F /PID " + pid);
            Process killProcess = killProcessBuilder.start();
            killProcess.waitFor();

            System.out.println("Processus " + pid + " terminé");
        } else {
            System.out.println("Aucun processus trouvé sur le port " + port);
        }
    }

    /**
     * Tue le processus sur le port spécifié pour Unix/Linux/Mac.
     */
    private static void killProcessOnPortUnix(int port) throws IOException, InterruptedException {
        System.out.println("Recherche du processus sur le port " + port + " (Unix)...");

        // Trouver le PID du processus utilisant le port avec lsof
        ProcessBuilder lsofBuilder = new ProcessBuilder("lsof", "-ti:" + port);
        Process lsofProcess = lsofBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(lsofProcess.getInputStream()));

        String pid = reader.readLine();
        reader.close();
        lsofProcess.waitFor();

        if (pid != null && !pid.isEmpty()) {
            System.out.println("Processus trouvé avec PID: " + pid);

            // Tuer le processus
            ProcessBuilder killProcessBuilder = new ProcessBuilder("kill", "-9", pid);
            Process killProcess = killProcessBuilder.start();
            killProcess.waitFor();

            System.out.println("Processus " + pid + " terminé");
        } else {
            System.out.println("Aucun processus trouvé sur le port " + port);
        }
    }
}
