package mg.gov.interieur.visa.sprint3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.entity.PieceJustificative;
import mg.gov.interieur.visa.exception.ResourceNotFoundException;
import mg.gov.interieur.visa.repository.DemandeRepository;
import mg.gov.interieur.visa.repository.PieceJustificativeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanService {

    private final DemandeRepository demandeRepository;
    private final PieceJustificativeRepository pieceJustificativeRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> performCompleteScan(Long demandeId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, String>> details = new ArrayList<>();

        List<Demande> demandes = resolveDemandes(demandeId);
        int totalItems = demandes.size();
        int businessLogicValid = 0;

        log.info("Debut du scan - {} demande(s) a analyser (demandeId={})", totalItems, demandeId);

        for (Demande demande : demandes) {
            boolean isValid = true;

            Map<String, String> demandeDetails = new HashMap<>();
            demandeDetails.put("type", "Demande");
            demandeDetails.put(
                    "content",
                    String.format("Numero: %s, Type: %s", demande.getNumeroDemande(), demande.getTypeDemande())
            );
            details.add(demandeDetails);

            if (demande.getStatut() == null) {
                errors.add(createError(
                        "Statut manquant",
                        "La demande " + demande.getNumeroDemande() + " n'a pas de statut defini",
                        demande.getId(),
                        null
                ));
                isValid = false;
            }

            if (demande.getTypeDemande() == null) {
                errors.add(createError(
                        "Type de demande manquant",
                        "La demande " + demande.getNumeroDemande() + " n'a pas de type defini",
                        demande.getId(),
                        null
                ));
                isValid = false;
            }

            List<PieceJustificative> pieces = pieceJustificativeRepository.findByDemandeId(demande.getId());
            for (PieceJustificative piece : pieces) {
                Map<String, String> pieceDetails = new HashMap<>();
                pieceDetails.put("type", "Piece justificative");
                pieceDetails.put(
                        "content",
                        String.format(
                                "Type: %s, Cochee: %s, Uploadee: %s",
                                piece.getTypePiece(),
                                piece.isEstCochee() ? "Oui" : "Non",
                                piece.isEstUploadee() ? "Oui" : "Non"
                        )
                );
                details.add(pieceDetails);

                if (piece.getTypePiece() == null) {
                    errors.add(createError(
                            "Type de piece manquant",
                            "Une piece de la demande " + demande.getNumeroDemande() + " n'a pas de type defini",
                            demande.getId(),
                            piece.getId()
                    ));
                    isValid = false;
                }

                boolean hasFile = piece.isEstUploadee()
                        || piece.isEstCochee()
                        || (piece.getContenuFichier() != null && piece.getContenuFichier().length > 0)
                        || (piece.getCheminFichier() != null && !piece.getCheminFichier().isBlank());

                if (!hasFile && piece.isObligatoire()) {
                    errors.add(createError(
                            "Fichier manquant",
                            "La piece obligatoire \"" + piece.getTypePiece()
                                    + "\" de la demande " + demande.getNumeroDemande()
                                    + " n'a pas de fichier uploade",
                            demande.getId(),
                            piece.getId()
                    ));
                    isValid = false;
                }
            }

            if (isValid) {
                businessLogicValid++;
                log.debug("Demande {} valide", demande.getNumeroDemande());
            } else {
                log.debug("Demande {} invalide", demande.getNumeroDemande());
            }
        }

        result.put("totalItems", totalItems);
        result.put("businessLogicValid", businessLogicValid);
        result.put("invalidCount", totalItems - businessLogicValid);
        result.put("errors", errors);
        result.put("details", details);
        result.put("demandeId", demandeId);

        log.info(
                "Scan termine - {}/{} demande(s) valide(s) (demandeId={})",
                businessLogicValid,
                totalItems,
                demandeId
        );
        return result;
    }

    private List<Demande> resolveDemandes(Long demandeId) {
        if (demandeId == null) {
            return demandeRepository.findAll();
        }

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable pour le scan : " + demandeId));
        return List.of(demande);
    }

    private Map<String, Object> createError(String type, String message, Long demandeId, Long pieceId) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", type);
        error.put("message", message);
        if (demandeId != null) {
            error.put("demandeId", demandeId);
        }
        if (pieceId != null) {
            error.put("pieceId", pieceId);
        }
        return error;
    }
}
