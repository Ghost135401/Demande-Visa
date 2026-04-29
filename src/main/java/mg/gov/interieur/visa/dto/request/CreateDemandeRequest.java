package mg.gov.interieur.visa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.TypeDemande;

/**
 * Corps de la requête pour créer une nouvelle demande (Sprint 1).
 */
@Data
public class CreateDemandeRequest {

    @NotNull(message = "La catégorie est obligatoire (TRAVAILLEUR ou INVESTISSEUR)")
    private CategorieDemande categorie;

    @NotNull(message = "Le type de demande est obligatoire")
    private TypeDemande typeDemande;

    @NotNull(message = "L'état civil est obligatoire")
    @Valid
    private EtatCivilRequest etatCivil;

    @Valid
    private PasseportRequest passeport;

    /**
     * Non obligatoire pour DUPLICATA et TRANSFERT_VISA.
     */
    @Valid
    private VisaTransformableRequest visaTransformable;

    /**
     * ID de la demande d'origine (obligatoire pour DUPLICATA et TRANSFERT_VISA).
     */
    private Long demandeParenteId;

    /**
     * true si la personne est resident (carte de resident) — false = etranger
     */
    private Boolean resident;
}
