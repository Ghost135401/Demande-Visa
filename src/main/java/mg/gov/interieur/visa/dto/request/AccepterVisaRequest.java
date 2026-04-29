package mg.gov.interieur.visa.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccepterVisaRequest {

    /**
     * Pour un nouveau titre et un duplicata, ces champs sont verifies dans le service.
     * Pour un transfert de visa, la carte existante est reprise depuis la demande parente.
     */
    private String numeroCarte;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
}
