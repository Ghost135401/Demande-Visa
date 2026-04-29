package mg.gov.interieur.visa.enums;

/**
 * Type de la demande effectuée.
 *
 * - NOUVEAU_TITRE   : transformation du visa transformable en visa de séjour.
 * - DUPLICATA       : duplicata de la carte de résident en cas de perte.
 * - TRANSFERT_VISA  : transfert du visa vers un nouveau passeport
 *                     (sans toucher à la carte de résident).
 *
 * NB : Pour DUPLICATA et TRANSFERT_VISA → "sans données antérieures" possible (Sprint 2).
 */
public enum TypeDemande {

    NOUVEAU_TITRE("Nouveau titre de séjour"),
    DUPLICATA("Duplicata de la carte de résident"),
    TRANSFERT_VISA("Transfert de visa vers nouveau passeport");

    private final String libelle;

    TypeDemande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}