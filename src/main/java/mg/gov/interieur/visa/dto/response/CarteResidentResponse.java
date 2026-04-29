package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CarteResidentResponse {

    private Long id;
    private String numeroCarte;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
    private LocalDateTime createdAt;
}
