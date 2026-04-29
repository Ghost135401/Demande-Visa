package mg.gov.interieur.visa.common.controller;

import lombok.RequiredArgsConstructor;
import mg.gov.interieur.visa.dto.response.ApiResponse;
import mg.gov.interieur.visa.dto.response.PieceJustificativeResponse;
import mg.gov.interieur.visa.common.service.PieceJustificativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/demandes/{demandeId}/pieces")
@RequiredArgsConstructor
public class PieceJustificativeController {

    private final PieceJustificativeService pieceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PieceJustificativeResponse>>> getPieces(
            @PathVariable Long demandeId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(pieceService.getPieces(demandeId)));
    }

    @PatchMapping("/{pieceId}/cocher")
    public ResponseEntity<ApiResponse<PieceJustificativeResponse>> cocher(
            @PathVariable Long demandeId,
            @PathVariable Long pieceId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Piece mise a jour", pieceService.cocherPiece(demandeId, pieceId)));
    }

    @PostMapping("/{pieceId}/upload")
    public ResponseEntity<ApiResponse<PieceJustificativeResponse>> upload(
            @PathVariable Long demandeId,
            @PathVariable Long pieceId,
            @RequestParam("fichier") MultipartFile fichier
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Fichier upload avec succes",
                        pieceService.uploaderFichier(demandeId, pieceId, fichier)));
    }

    @DeleteMapping("/{pieceId}/fichier")
    public ResponseEntity<ApiResponse<Void>> supprimerFichier(
            @PathVariable Long demandeId,
            @PathVariable Long pieceId
    ) throws IOException {
        pieceService.supprimerFichier(demandeId, pieceId);
        return ResponseEntity.ok(ApiResponse.ok("Fichier supprime", null));
    }
}
