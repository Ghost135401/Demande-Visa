package mg.gov.interieur.visa.enums;

/**
 * Catégorie du visa demandé après transformation.
 * Détermine les pièces justificatives à fournir.
 */
public enum CategorieDemande {

    TRAVAILLEUR("Visa Travailleur"),
    INVESTISSEUR("Visa Investisseur");

    private final String libelle;

    CategorieDemande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}