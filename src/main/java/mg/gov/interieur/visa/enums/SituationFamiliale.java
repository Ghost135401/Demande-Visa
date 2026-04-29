package mg.gov.interieur.visa.enums;

public enum SituationFamiliale {
    CELIBATAIRE("Célibataire"),
    MARIE("Marié(e)"),
    DIVORCE("Divorcé(e)"),
    VEUF("Veuf/Veuve"),
    SEPARE("Séparé(e)");

    private final String libelle;

    SituationFamiliale(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}