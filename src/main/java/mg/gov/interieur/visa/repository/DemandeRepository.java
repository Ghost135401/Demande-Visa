package mg.gov.interieur.visa.repository;

import mg.gov.interieur.visa.entity.Demande;
import mg.gov.interieur.visa.enums.CategorieDemande;
import mg.gov.interieur.visa.enums.StatutDemande;
import mg.gov.interieur.visa.enums.TypeDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    Optional<Demande> findByNumeroDemande(String numeroDemande);

    List<Demande> findByStatut(StatutDemande statut);

    List<Demande> findByCategorie(CategorieDemande categorie);

    List<Demande> findByTypeDemande(TypeDemande typeDemande);

    List<Demande> findBySansDonneesAnterieures(boolean sansDonneesAnterieures);

    List<Demande> findByStatutAndCategorie(StatutDemande statut, CategorieDemande categorie);

    List<Demande> findByDemandeParenteId(Long parentId);

    @Query("select max(d.id) from Demande d")
    Optional<Long> findMaxId();

    @Query("select d from Demande d left join fetch d.piecesJustificatives where d.id = :id")
    Optional<Demande> findByIdWithPieces(Long id);
}
