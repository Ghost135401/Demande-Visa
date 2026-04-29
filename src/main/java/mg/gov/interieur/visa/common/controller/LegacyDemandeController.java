package mg.gov.interieur.visa.common.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.dto.request.CreateDemandeAvecVisaRequest;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class LegacyDemandeController {

    private final DemandeService demandeService;

    @PostMapping("/sans-donnees-anterieures")
    public ResponseEntity<ApiResponse<DemandeResponse>> creerAvecVisa(
            @Valid @RequestBody CreateDemandeAvecVisaRequest req
    ) {
        DemandeResponse response = demandeService.creerDemandeAvecVisa(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Demande creee en reprise legacy (sans donnees anterieures)",
                        response));
    }
}
