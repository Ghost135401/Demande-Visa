package mg.gov.interieur.visa.sprint4.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.enums.TypeDemande;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    private final ObjectMapper objectMapper;
    private final ObjectMapper qrObjectMapper;

    public QRCodeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.qrObjectMapper = objectMapper.copy().findAndRegisterModules();
    }

    public Map<String, Object> generateQRCodeForPiece(String pieceId, String typePiece, String demandeId) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "PIECE_JUSTIFICATIVE");
        qrData.put("pieceId", pieceId);
        qrData.put("typePiece", typePiece);
        qrData.put("demandeId", demandeId);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForVisa(
            String visaId,
            String numeroVisa,
            String typeVisa,
            Boolean resident
    ) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "VISA");
        qrData.put("visaId", visaId);
        qrData.put("numeroVisa", numeroVisa);
        qrData.put("typeVisa", typeVisa);
        qrData.put("resident", resident != null && resident);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForTransfertVisa(
            String transfertId,
            String numeroTransfert,
            String visaOriginal
    ) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "TRANSFERT_VISA");
        qrData.put("transfertId", transfertId);
        qrData.put("numeroTransfert", numeroTransfert);
        qrData.put("visaOriginal", visaOriginal);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForDuplicata(
            String duplicataId,
            String numeroDuplicata,
            String documentOriginal
    ) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "DUPLICATA");
        qrData.put("duplicataId", duplicataId);
        qrData.put("numeroDuplicata", numeroDuplicata);
        qrData.put("documentOriginal", documentOriginal);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForCarteResidence(
            String carteId,
            String numeroCarte,
            String typeCarte
    ) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "CARTE_RESIDENT");
        qrData.put("carteId", carteId);
        qrData.put("numeroCarte", numeroCarte);
        qrData.put("typeCarte", typeCarte);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForPasseport(
            String passeportId,
            String numeroPasseport,
            String dateExpiration
    ) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "PASSEPORT");
        qrData.put("passeportId", passeportId);
        qrData.put("numeroPasseport", numeroPasseport);
        qrData.put("dateExpiration", dateExpiration);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    public Map<String, Object> generateQRCodeForDemande(DemandeResponse demande) {
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", "DOSSIER_VISA");
        qrData.put("demandeId", demande.getId());
        qrData.put("numeroDemande", demande.getNumeroDemande());
        qrData.put("parcours", buildParcoursLabel(demande));
        qrData.put("categorie", demande.getCategorie() != null ? demande.getCategorie().name() : null);
        qrData.put("statut", demande.getStatut() != null ? demande.getStatut().name() : null);
        qrData.put("statutLibelle", demande.getStatutLibelle());
        qrData.put("sansDonneesAnterieures", demande.isSansDonneesAnterieures());
        qrData.put("resident", Boolean.TRUE.equals(demande.getResident()));
        qrData.put("demandeur", String.format("%s %s", defaultValue(demande.getNom()), defaultValue(demande.getPrenoms())).trim());
        qrData.put("demandeParente", demande.getDemandeParenteNumero());
        qrData.put("numeroPasseport", demande.getPasseport() != null ? demande.getPasseport().getNumero() : null);
        qrData.put("referenceVisa", demande.getVisaTransformable() != null ? demande.getVisaTransformable().getRefVisa() : null);
        qrData.put("numeroCarteResident", demande.getCarteResident() != null ? demande.getCarteResident().getNumeroCarte() : null);
        qrData.put("expirationCarte", demande.getCarteResident() != null ? demande.getCarteResident().getDateExpiration() : null);
        qrData.put("timestamp", System.currentTimeMillis());
        return generateQRCodeResponse(qrData);
    }

    private String buildParcoursLabel(DemandeResponse demande) {
        if (demande.isSansDonneesAnterieures()) {
            return "Reprise sans donnees anterieures";
        }
        if (TypeDemande.DUPLICATA.equals(demande.getTypeDemande())) {
            return "Duplicata de carte de resident";
        }
        if (TypeDemande.TRANSFERT_VISA.equals(demande.getTypeDemande())) {
            return "Transfert de visa vers un nouveau passeport";
        }
        return "Nouvelle demande de visa";
    }

    private String defaultValue(String value) {
        return value != null ? value : "";
    }

    private Map<String, Object> generateQRCodeResponse(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String jsonData = qrObjectMapper.writeValueAsString(data);
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    jsonData,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_WIDTH,
                    QR_CODE_HEIGHT
            );

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            String base64Image = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

            response.put("success", true);
            response.put("qrCode", "data:image/png;base64," + base64Image);
            response.put("data", data);
        } catch (JsonProcessingException e) {
            log.error("Erreur de serialisation JSON pour le QR code", e);
            response.put("success", false);
            response.put("error", "Erreur de serialisation JSON : " + e.getMessage());
        } catch (WriterException e) {
            log.error("Erreur ZXing lors de la generation du QR code", e);
            response.put("success", false);
            response.put("error", "Erreur de generation du QR code : " + e.getMessage());
        } catch (IOException e) {
            log.error("Erreur d'ecriture du flux PNG pour le QR code", e);
            response.put("success", false);
            response.put("error", "Erreur d'ecriture de l'image PNG : " + e.getMessage());
        }

        return response;
    }
}
