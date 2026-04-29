package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PasseportResponse {

    private Long id;
    private String numero;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
}
