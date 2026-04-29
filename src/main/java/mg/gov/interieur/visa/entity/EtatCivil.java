package mg.gov.interieur.visa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mg.gov.interieur.visa.enums.SituationFamiliale;

import java.time.LocalDate;

/**
 * État civil du demandeur — intégré directement dans la table demande.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtatCivil {

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenoms", nullable = false, length = 200)
    private String prenoms;

    @Column(name = "nom_jeune_fille", length = 100)
    private String nomJeuneFille;

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_familiale", nullable = false, length = 30)
    private SituationFamiliale situationFamiliale;

    @Column(name = "nationalite", nullable = false, length = 100)
    private String nationalite;

    @Column(name = "profession", nullable = false, length = 100)
    private String profession;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 200)
    private String lieuNaissance;

    @Column(name = "adresse_madagascar", nullable = false)
    private String adresseMadagascar;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", nullable = false, length = 30)
    private String telephone;
}