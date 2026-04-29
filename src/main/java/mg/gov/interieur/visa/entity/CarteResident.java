package mg.gov.interieur.visa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carte de résident générée lorsque le visa est accepté.
 * Sa date d'expiration correspond à celle du visa de séjour apposé sur le passeport.
 */
@Entity
@Table(name = "carte_resident")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_carte", nullable = false, unique = true, length = 100)
    private String numeroCarte;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    /**
     * Même date d'expiration que le visa de séjour apposé sur le passeport.
     */
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}