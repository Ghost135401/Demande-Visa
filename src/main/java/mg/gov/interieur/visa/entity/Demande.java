package mg.gov.interieur.visa.entity;

import jakarta.persistence.*;
import lombok.*;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité principale : demande de transformation de visa transformable.
 *
 * Workflow statut :
 *   DOSSIER_CREE ──► SCAN_TERMINE ──► VISA_ACCEPTE
 *
 * Types de demande :
 *   - NOUVEAU_TITRE   : transformation initiale
 *   - DUPLICATA       : duplicata de la carte de résident
 *   - TRANSFERT_VISA  : transfert vers un nouveau passeport
 *
 * Sprint 2 : sansDonneesAnterieures = true → statut directement VISA_ACCEPTE
 */
@Entity
@Table(name = "demande")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence unique générée automatiquement ex: DEM-2024-00001 */
    @Column(name = "numero_demande", nullable = false, unique = true, length = 50)
    private String numeroDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private CategorieDemande categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutDemande statut = StatutDemande.DOSSIER_CREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_demande", nullable = false, length = 30)
    private TypeDemande typeDemande;

    /**
     * Sprint 2 : true si la personne possédait déjà une carte de résident
     * avant la mise en place du système (pas de données historiques).
     * Dans ce cas, le statut passe directement à VISA_ACCEPTE.
     */
    @Column(name = "sans_donnees_anterieures", nullable = false)
    @Builder.Default
    private boolean sansDonneesAnterieures = false;

    /** Indique si le demandeur est resident (carte de resident) ou etranger */
    @Column(name = "resident", nullable = false)
    @Builder.Default
    private boolean resident = false;

    /** État civil du demandeur (colonnes intégrées dans la table demande). */
    @Embedded
    private EtatCivil etatCivil;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "passeport_id")
    private Passeport passeport;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "visa_transformable_id")
    private VisaTransformable visaTransformable;

    /** Générée lors du passage au statut VISA_ACCEPTE. */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "carte_resident_id")
    private CarteResident carteResident;

    /** Pour DUPLICATA et TRANSFERT_VISA : référence à la demande d'origine. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_parente_id")
    @ToString.Exclude
    private Demande demandeParente;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<PieceJustificative> piecesJustificatives = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── helpers ───────────────────────────────────────────────────────────────

    public boolean isModifiable() {
        return StatutDemande.DOSSIER_CREE.equals(this.statut);
    }

    public boolean toutesLesPiecesObligatoiresUploadees() {
        return piecesJustificatives.stream()
            .filter(PieceJustificative::isObligatoire)
            .allMatch(piece ->
                // Considérer la pièce fournie si : uploadée, cochée par l'agent,
                // ou si un contenu/chemin de fichier est présent en base.
                piece.isEstUploadee()
                    || piece.isEstCochee()
                    || (piece.getContenuFichier() != null && piece.getContenuFichier().length > 0)
                    || (piece.getCheminFichier() != null && !piece.getCheminFichier().isBlank())
            );
    }
}