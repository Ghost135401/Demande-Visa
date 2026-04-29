package mg.gov.interieur.visa.sprint2.bis.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.dto.request.CreateDemandeRequest;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.enums.TypeDemande;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class TransfertVisaController {

    private final DemandeService demandeService;

    @PostMapping("/{id}/transfert-visa")
    public ResponseEntity<ApiResponse<DemandeResponse>> transfertVisa(
            @PathVariable Long id,
            @Valid @RequestBody CreateDemandeRequest req
    ) {
        req.setDemandeParenteId(id);
        if (!TypeDemande.TRANSFERT_VISA.equals(req.getTypeDemande())) {
            throw new IllegalArgumentException("Le type de demande doit etre TRANSFERT_VISA");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Demande de transfert de visa creee",
                        demandeService.creerDemande(req)));
    }
}
