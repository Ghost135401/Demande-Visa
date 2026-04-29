package mg.gov.interieur.visa.entity;

import jakarta.persistence.*;
import lombok.*;
import mg.gov.interieur.visa.enums.TypePiece;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Pièce justificative associée à une demande.
 * Une pièce peut être :
 *  - cochée (case à cocher confirmant que la pièce est bien fournie)
 *  - uploadée (fichier numérisé stocké sur le serveur)
 *
 * L'upload est progressif : on peut uploader document par document.
 * Un fichier peut être supprimé et remplacé tant que le statut est DOSSIER_CREE.
 */
@Entity
@Table(name = "piece_justificative")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJustificative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    @ToString.Exclude
    private Demande demande;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", nullable = false, length = 80)
    private TypePiece typePiece;

    @Column(name = "libelle", nullable = false, length = 300)
    private String libelle;

    /** La case est cochée par l'agent lors de la vérification physique. */
    @Column(name = "est_cochee", nullable = false)
    private boolean estCochee;

    /** Le fichier numérique a été uploadé. */
    @Column(name = "est_uploadee", nullable = false)
    private boolean estUploadee;

    @Column(name = "nom_fichier", length = 300)
    private String nomFichier;

    @Column(name = "chemin_fichier", length = 500)
    private String cheminFichier;

    @Column(name = "contenu_fichier", columnDefinition = "bytea")
    private byte[] contenuFichier;
    @Column(name = "date_upload")
    private LocalDateTime dateUpload;

    /** Pièce obligatoire pour finaliser la demande. */
    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}