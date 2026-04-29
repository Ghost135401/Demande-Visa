package mg.gov.interieur.visa.sprint3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.dto.request.AccepterVisaRequest;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeWorkflowController {

    private final DemandeService demandeService;

    @PostMapping("/{id}/scan-termine")
    public ResponseEntity<ApiResponse<DemandeResponse>> scanTermine(
            @PathVariable Long id,
            @RequestParam(name = "force", required = false, defaultValue = "false") boolean force
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Scan termine - la demande n'est plus modifiable",
                        demandeService.marquerScanTermine(id, force)));
    }

    @PostMapping("/{id}/accepter-visa")
    public ResponseEntity<ApiResponse<DemandeResponse>> accepterVisa(
            @PathVariable Long id,
            @Valid @RequestBody AccepterVisaRequest req
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Visa accepte - workflow finalise",
                        demandeService.accepterVisa(id, req)));
    }
}
