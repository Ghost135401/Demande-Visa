package mg.gov.interieur.visa.sprint4.controller;

import mg.gov.interieur.visa.common.service.DemandeService;
import mg.gov.interieur.visa.sprint4.service.QRCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final DemandeService demandeService;

    public QRCodeController(QRCodeService qrCodeService, DemandeService demandeService) {
        this.qrCodeService = qrCodeService;
        this.demandeService = demandeService;
    }

    @GetMapping("/demandes/{demandeId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForDemande(
            @PathVariable Long demandeId
    ) {
        return ResponseEntity.ok(qrCodeService.generateQRCodeForDemande(demandeService.getById(demandeId)));
    }

    @GetMapping("/piece/{pieceId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForPiece(
            @PathVariable String pieceId,
            @RequestParam String typePiece,
            @RequestParam String demandeId
    ) {
        return ResponseEntity.ok(qrCodeService.generateQRCodeForPiece(pieceId, typePiece, demandeId));
    }

    @GetMapping("/visa/{visaId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForVisa(
            @PathVariable String visaId,
            @RequestParam String numeroVisa,
            @RequestParam String typeVisa,
            @RequestParam(required = false) Boolean resident
    ) {
        return ResponseEntity.ok(qrCodeService.generateQRCodeForVisa(visaId, numeroVisa, typeVisa, resident));
    }

    @GetMapping("/transfert-visa/{transfertId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForTransfertVisa(
            @PathVariable String transfertId,
            @RequestParam String numeroTransfert,
            @RequestParam String visaOriginal
    ) {
        return ResponseEntity.ok(
                qrCodeService.generateQRCodeForTransfertVisa(transfertId, numeroTransfert, visaOriginal)
        );
    }

    @GetMapping("/duplicata/{duplicataId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForDuplicata(
            @PathVariable String duplicataId,
            @RequestParam String numeroDuplicata,
            @RequestParam String documentOriginal
    ) {
        return ResponseEntity.ok(
                qrCodeService.generateQRCodeForDuplicata(duplicataId, numeroDuplicata, documentOriginal)
        );
    }

    @GetMapping("/carte-residence/{carteId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForCarteResidence(
            @PathVariable String carteId,
            @RequestParam String numeroCarte,
            @RequestParam String typeCarte
    ) {
        return ResponseEntity.ok(qrCodeService.generateQRCodeForCarteResidence(carteId, numeroCarte, typeCarte));
    }

    @GetMapping("/passeport/{passeportId}")
    public ResponseEntity<Map<String, Object>> generateQRCodeForPasseport(
            @PathVariable String passeportId,
            @RequestParam String numeroPasseport,
            @RequestParam String dateExpiration
    ) {
        return ResponseEntity.ok(
                qrCodeService.generateQRCodeForPasseport(passeportId, numeroPasseport, dateExpiration)
        );
    }
}
