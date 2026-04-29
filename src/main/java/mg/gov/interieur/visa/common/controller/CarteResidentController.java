package mg.gov.interieur.visa.common.controller;

import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.CarteResidentResponse;
import mg.gov.interieur.visa.entity.CarteResident;
import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.exception.ResourceNotFoundException;
import mg.gov.interieur.visa.repository.DemandeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class CarteResidentController {

    private final DemandeRepository demandeRepository;

    @GetMapping("/{id}/carte-resident")
    public ResponseEntity<ApiResponse<CarteResidentResponse>> getCarte(@PathVariable Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));

        if (!StatutDemande.VISA_ACCEPTE.equals(demande.getStatut())
                || demande.getCarteResident() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Aucune carte de resident pour cette demande"));
        }

        CarteResident carte = demande.getCarteResident();
        return ResponseEntity.ok(ApiResponse.ok(CarteResidentResponse.builder()
                .id(carte.getId())
                .numeroCarte(carte.getNumeroCarte())
                .dateDelivrance(carte.getDateDelivrance())
                .dateExpiration(carte.getDateExpiration())
                .createdAt(carte.getCreatedAt())
                .build()));
    }
}
