package mg.gov.interieur.visa.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.dto.response.PieceJustificativeResponse;
import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.entity.PieceJustificative;
import mg.gov.interieur.visa.exception.DemandeNonModifiableException;
import mg.gov.interieur.visa.exception.ResourceNotFoundException;
import mg.gov.interieur.visa.repository.DemandeRepository;
import mg.gov.interieur.visa.repository.PieceJustificativeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PieceJustificativeService {

    private final PieceJustificativeRepository pieceRepo;
    private final DemandeRepository demandeRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<PieceJustificativeResponse> getPieces(Long demandeId) {
        return pieceRepo.findByDemandeId(demandeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PieceJustificativeResponse cocherPiece(Long demandeId, Long pieceId) {
        Demande demande = findDemande(demandeId);
        verifierModifiable(demande);
        PieceJustificative piece = findPiece(pieceId, demandeId);
        piece.setEstCochee(!piece.isEstCochee());
        return toResponse(pieceRepo.save(piece));
    }

    public PieceJustificativeResponse uploaderFichier(
            Long demandeId,
            Long pieceId,
            MultipartFile fichier
    ) throws IOException {
        Demande demande = findDemande(demandeId);
        verifierModifiable(demande);

        PieceJustificative piece = findPiece(pieceId, demandeId);

        // Stocker le fichier sur le disque dans le dossier uploads
        String cheminFichier = fileStorageService.stocker(fichier, demandeId, pieceId);

        // Stocker également le contenu en base de données pour sauvegarde
        piece.setCheminFichier(cheminFichier);
        piece.setNomFichier(fichier.getOriginalFilename());
        piece.setContenuFichier(fichier.getBytes());
        piece.setEstUploadee(true);
        piece.setEstCochee(true);
        piece.setDateUpload(LocalDateTime.now());

        log.info("Fichier uploade pour piece {} de la demande {} - Chemin: {}", pieceId, demandeId, cheminFichier);
        return toResponse(pieceRepo.save(piece));
    }

    public void supprimerFichier(Long demandeId, Long pieceId) throws IOException {
        Demande demande = findDemande(demandeId);
        verifierModifiable(demande);

        PieceJustificative piece = findPiece(pieceId, demandeId);
        // Supprimer le fichier sur disque seulement si un chemin existait.
        if (piece.getCheminFichier() != null) {
            fileStorageService.supprimer(piece.getCheminFichier());
        }
        piece.setCheminFichier(null);
        piece.setNomFichier(null);
        piece.setContenuFichier(null);
        piece.setEstUploadee(false);
        piece.setDateUpload(null);

        log.info("Fichier supprime pour piece {} de la demande {}", pieceId, demandeId);
        pieceRepo.save(piece);
    }

    private Demande findDemande(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }

    private PieceJustificative findPiece(Long pieceId, Long demandeId) {
        return pieceRepo.findById(pieceId)
                .filter(piece -> piece.getDemande().getId().equals(demandeId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Piece introuvable : " + pieceId + " pour demande " + demandeId));
    }

    private void verifierModifiable(Demande demande) {
        if (!demande.isModifiable()) {
            throw new DemandeNonModifiableException(
                    "La demande " + demande.getNumeroDemande() + " n'est plus modifiable");
        }
    }

    private PieceJustificativeResponse toResponse(PieceJustificative piece) {
        return PieceJustificativeResponse.builder()
                .id(piece.getId())
                .typePiece(piece.getTypePiece())
                .libelle(piece.getLibelle())
                .estCochee(piece.isEstCochee())
                .estUploadee(piece.isEstUploadee())
                .nomFichier(piece.getNomFichier())
                .dateUpload(piece.getDateUpload())
                .obligatoire(piece.isObligatoire())
                .build();
    }
}
