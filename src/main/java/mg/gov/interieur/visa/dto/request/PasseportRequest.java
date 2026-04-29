package mg.gov.interieur.visa.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PasseportRequest {

    @NotBlank(message = "Le numéro de passeport est obligatoire")
    @Size(max = 50)
    private String numero;

    @NotNull(message = "La date de délivrance est obligatoire")
    private LocalDate dateDelivrance;

    @NotNull(message = "La date d'expiration est obligatoire")
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDate dateExpiration;
}