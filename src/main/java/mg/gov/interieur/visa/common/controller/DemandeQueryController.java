package mg.gov.interieur.visa.common.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.dto.request.UpdateDemandeRequest;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.StatutDemande;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeQueryController {

    private final DemandeService demandeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandeResponse>>> getAll(
            @RequestParam(required = false) StatutDemande statut,
            @RequestParam(required = false) CategorieDemande categorie,
            @RequestParam(required = false) Boolean sansDonneesAnterieures
    ) {
        List<DemandeResponse> list;
        if (sansDonneesAnterieures != null) {
            list = demandeService.getBySansDonneesAnterieures(sansDonneesAnterieures);
            if (statut != null) {
                list = list.stream().filter(demande -> demande.getStatut().equals(statut)).toList();
            }
            if (categorie != null) {
                list = list.stream().filter(demande -> demande.getCategorie().equals(categorie)).toList();
            }
        } else if (statut != null && categorie != null) {
            list = demandeService.getByStatut(statut).stream()
                    .filter(demande -> demande.getCategorie().equals(categorie))
                    .toList();
        } else if (statut != null) {
            list = demandeService.getByStatut(statut);
        } else if (categorie != null) {
            list = demandeService.getByCategorie(categorie);
        } else {
            list = demandeService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(demandeService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandeResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDemandeRequest req
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Demande mise a jour", demandeService.modifier(id, req)));
    }
}
