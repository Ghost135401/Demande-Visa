package mg.gov.interieur.visa.enums;

/**
 * Types de pièces justificatives.
 * Les pièces communes s'appliquent aux deux catégories.
 * Les pièces spécifiques dépendent de la catégorie (TRAVAILLEUR ou INVESTISSEUR).
 */
public enum TypePiece {

    // ── Pièces communes (TRAVAILLEUR + INVESTISSEUR) ──────────────────────────
    FORMULAIRE_DEMANDE(
            "Formulaire de demande rempli et signé", true, true, true),

    PHOTOS_IDENTITE(
            "4 photos d'identité récentes (format 4x4)", true, true, true),

    COPIE_PASSEPORT(
            "Copie du passeport (pages d'identité + visa)", true, true, true),

    COPIE_VISA_TRANSFORMABLE(
            "Copie du visa transformable", true, true, true),

    CERTIFICAT_MEDICAL(
            "Certificat médical délivré par un médecin agréé", true, true, true),

    CASIER_JUDICIAIRE(
            "Casier judiciaire du pays d'origine (moins de 3 mois)", true, true, true),

    EXTRAIT_NAISSANCE(
            "Extrait d'acte de naissance légalisé", true, true, true),

    JUSTIFICATIF_DOMICILE(
            "Justificatif de domicile à Madagascar", true, true, true),

    QUITTANCE_TAXES(
            "Quittance de paiement des taxes et droits", true, true, true),

    // ── Pièces spécifiques TRAVAILLEUR ────────────────────────────────────────
    CONTRAT_TRAVAIL(
            "Contrat de travail signé par l'employeur et l'employé", true, false, true),

    ATTESTATION_EMPLOYEUR(
            "Attestation de l'employeur indiquant le poste et le salaire", true, false, true),

    AUTORISATION_TRAVAIL(
            "Autorisation de travail délivrée par le Ministère du Travail", true, false, true),

    // ── Pièces spécifiques INVESTISSEUR ───────────────────────────────────────
    BUSINESS_PLAN(
            "Plan d'affaires / Business plan", false, true, true),

    JUSTIFICATIF_INVESTISSEMENT(
            "Justificatif d'investissement (preuve de fonds disponibles)", false, true, true),

    STATUTS_SOCIETE(
            "Statuts de la société enregistrés (ou en cours d'enregistrement)", false, true, true),

    NIF(
            "Numéro d'Identification Fiscale (NIF) ou demande en cours", false, true, false);

    /**
     * @param libelle         Intitulé affiché
     * @param travailleur     Applicable pour la catégorie TRAVAILLEUR
     * @param investisseur    Applicable pour la catégorie INVESTISSEUR
     * @param obligatoire     Pièce obligatoire
     */
    private final String libelle;
    private final boolean travailleur;
    private final boolean investisseur;
    private final boolean obligatoire;

    TypePiece(String libelle, boolean travailleur, boolean investisseur, boolean obligatoire) {
        this.libelle      = libelle;
        this.travailleur  = travailleur;
        this.investisseur = investisseur;
        this.obligatoire  = obligatoire;
    }

    public String getLibelle()      { return libelle; }
    public boolean isTravailleur()  { return travailleur; }
    public boolean isInvestisseur() { return investisseur; }
    public boolean isObligatoire()  { return obligatoire; }
}