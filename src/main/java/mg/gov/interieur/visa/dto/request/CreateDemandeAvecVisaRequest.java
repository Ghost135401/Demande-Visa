package mg.gov.interieur.visa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.TypeDemande;

import java.time.LocalDate;

/**
 * Sprint 2 : creation d'une demande sans donnees anterieures.
 * Le statut est directement mis a VISA_ACCEPTE.
 */
@Data
public class CreateDemandeAvecVisaRequest {

    @NotNull
    private CategorieDemande categorie;

    @NotNull
    private TypeDemande typeDemande;

    @Valid
    @NotNull
    private EtatCivilRequest etatCivil;

    @Valid
    private PasseportRequest passeport;

    @Valid
    private VisaTransformableRequest visaTransformable;

    @NotBlank
    private String numeroCarte;

    @NotNull
    private LocalDate dateDelivranceCarte;

    @NotNull
    private LocalDate dateExpirationCarte;

    private Long demandeParenteId;

    /** true si la personne est resident (carte de resident) — false = etranger */
    private Boolean resident;
}
