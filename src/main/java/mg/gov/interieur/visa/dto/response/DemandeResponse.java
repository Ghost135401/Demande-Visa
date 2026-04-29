package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.SituationFamiliale;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DemandeResponse {

    private Long id;
    private String numeroDemande;
    private CategorieDemande categorie;
    private StatutDemande statut;
    private String statutLibelle;
    private TypeDemande typeDemande;
    private boolean sansDonneesAnterieures;
    private boolean modifiable;

    private String nom;
    private String prenoms;
    private String nomJeuneFille;
    private SituationFamiliale situationFamiliale;
    private String nationalite;
    private String profession;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresseMadagascar;
    private String email;
    private String telephone;

    private PasseportResponse passeport;
    private VisaTransformableResponse visaTransformable;
    private CarteResidentResponse carteResident;
    private List<PieceJustificativeResponse> piecesJustificatives;

    private Long demandeParenteId;
    private String demandeParenteNumero;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean resident;
}
