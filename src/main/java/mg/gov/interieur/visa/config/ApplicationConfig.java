package mg.gov.interieur.visa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configuration de l'application pour gérer le démarrage et l'arrêt.
 * 
 * Cette classe s'assure que l'application démarre correctement
 * et que tous les processus sont gérés proprement.
 */
@Component
@Slf4j
public class ApplicationConfig {

    /**
     * Événement déclenché lorsque l'application est prête.
     * 
     * Cet événement est utilisé pour confirmer que l'application
     * a démarré avec succès et est prête à accepter des requêtes.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========================================");
        log.info("Application démarrée avec succès!");
        log.info("Accessible à: http://localhost:8080/back-office");
        log.info("========================================");
    }
}
