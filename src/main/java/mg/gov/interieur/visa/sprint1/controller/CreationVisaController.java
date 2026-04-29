package mg.gov.interieur.visa.sprint1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.dto.request.CreateDemandeRequest;
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
public class CreationVisaController {

    private final DemandeService demandeService;

    @PostMapping
    public ResponseEntity<ApiResponse<DemandeResponse>> creer(
            @Valid @RequestBody CreateDemandeRequest req
    ) {
        DemandeResponse response = demandeService.creerDemande(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Demande creee avec succes", response));
    }
}
