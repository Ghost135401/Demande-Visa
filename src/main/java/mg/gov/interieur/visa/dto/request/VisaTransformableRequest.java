package mg.gov.interieur.visa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VisaTransformableRequest {

    @NotBlank(message = "La référence du visa est obligatoire")
    @Size(max = 100)
    private String refVisa;

    @NotNull(message = "La date d'entrée est obligatoire")
    private LocalDate dateEntree;

    @NotBlank(message = "Le lieu d'entrée est obligatoire")
    @Size(max = 200)
    private String lieuEntree;

    @NotNull(message = "La date de sortie (fin de validité du visa) est obligatoire")
    private LocalDate dateSortie;
}