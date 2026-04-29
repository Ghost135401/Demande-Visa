package mg.gov.interieur.visa.dto.request;

import jakarta.validation.Valid;
import lombok.Data;
import mg.gov.interieur.visa.enums.CategorieDemande;

@Data
public class UpdateDemandeRequest {

    @Valid
    private EtatCivilRequest etatCivil;

    @Valid
    private PasseportRequest passeport;

    @Valid
    private VisaTransformableRequest visaTransformable;

    private CategorieDemande categorie;

    /** true si la personne est resident (carte de resident) — false = etranger */
    private Boolean resident;
}
