package mg.gov.interieur.visa.enums;

/**
 * Statut d'avancement d'une demande de transformation de visa.
 *
 * Workflow :
 *  DOSSIER_CREE  ──► SCAN_TERMINE ──► VISA_ACCEPTE
 *
 * - DOSSIER_CREE  : informations saisies, pièces cochées, upload en cours.
 *                   Les données restent modifiables.
 * - SCAN_TERMINE  : toutes les pièces sont uploadées et validées.
 *                   Les données ne sont plus modifiables.
 * - VISA_ACCEPTE  : visa accordé, carte de résident générée.
 */
public enum StatutDemande {

    DOSSIER_CREE("Dossier créé"),
    SCAN_TERMINE("Scan terminé"),
    VISA_ACCEPTE("Visa accepté");

    private final String libelle;

    StatutDemande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}