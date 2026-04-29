package mg.gov.interieur.visa.sprint4.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.gov.interieur.visa.dto.response.CarteResidentResponse;
import mg.gov.interieur.visa.dto.response.DemandeResponse;
import mg.gov.interieur.visa.dto.response.PasseportResponse;
import mg.gov.interieur.visa.dto.response.VisaTransformableResponse;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeServiceTest {

    private final QRCodeService service = new QRCodeService(new ObjectMapper());

    @Test
    void generateQRCodeForDemandeExposeLesInfosMetierDuDossier() {
        DemandeResponse demande = DemandeResponse.builder()
                .id(9L)
                .numeroDemande("DEM-2026-00009")
                .categorie(CategorieDemande.TRAVAILLEUR)
                .statut(StatutDemande.VISA_ACCEPTE)
                .statutLibelle("Visa accepte")
                .typeDemande(TypeDemande.TRANSFERT_VISA)
                .sansDonneesAnterieures(false)
                .resident(true)
                .nom("DOE")
                .prenoms("John")
                .demandeParenteNumero("DEM-2026-00001")
                .passeport(PasseportResponse.builder().numero("P-NEW-001").build())
                .visaTransformable(VisaTransformableResponse.builder().refVisa("VT-778").build())
                .carteResident(CarteResidentResponse.builder()
                        .numeroCarte("CR-778")
                        .dateExpiration(LocalDate.of(2027, 3, 1))
                        .build())
                .build();

        Map<String, Object> response = service.generateQRCodeForDemande(demande);

        assertEquals(true, response.get("success"));
        assertTrue(String.valueOf(response.get("qrCode")).startsWith("data:image/png;base64,"));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertEquals("DOSSIER_VISA", data.get("type"));
        assertEquals("DEM-2026-00009", data.get("numeroDemande"));
        assertEquals("Transfert de visa vers un nouveau passeport", data.get("parcours"));
        assertEquals("P-NEW-001", data.get("numeroPasseport"));
        assertEquals("CR-778", data.get("numeroCarteResident"));
        assertEquals("DOE John", data.get("demandeur"));
    }
}
