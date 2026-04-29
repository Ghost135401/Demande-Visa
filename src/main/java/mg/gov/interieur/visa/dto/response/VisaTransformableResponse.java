package mg.gov.interieur.visa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VisaTransformableResponse {

    private Long id;
    private String refVisa;
    private LocalDate dateEntree;
    private String lieuEntree;
    private LocalDate dateSortie;
}
