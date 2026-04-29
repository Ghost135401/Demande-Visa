package mg.gov.interieur.visa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "visa_transformable")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisaTransformable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_visa", nullable = false, length = 100)
    private String refVisa;

    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree;

    @Column(name = "lieu_entree", nullable = false, length = 200)
    private String lieuEntree;

    /**
     * Date de fin de validité du visa transformable.
     * C'est cette date qui détermine l'urgence de la demande.
     */
    @Column(name = "date_sortie", nullable = false)
    private LocalDate dateSortie;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}