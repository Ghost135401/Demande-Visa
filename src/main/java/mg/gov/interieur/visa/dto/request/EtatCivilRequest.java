package mg.gov.interieur.visa.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import mg.gov.interieur.visa.enums.SituationFamiliale;

import java.time.LocalDate;

@Data
public class EtatCivilRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Les prénoms sont obligatoires")
    @Size(max = 200)
    private String prenoms;

    @Size(max = 100)
    private String nomJeuneFille;

    @NotNull(message = "La situation familiale est obligatoire")
    private SituationFamiliale situationFamiliale;

    @NotBlank(message = "La nationalité est obligatoire")
    @Size(max = 100)
    private String nationalite;

    @NotBlank(message = "La profession est obligatoire")
    @Size(max = 100)
    private String profession;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @Size(max = 200)
    private String lieuNaissance;

    @NotBlank(message = "L'adresse à Madagascar est obligatoire")
    private String adresseMadagascar;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 30)
    private String telephone;
}