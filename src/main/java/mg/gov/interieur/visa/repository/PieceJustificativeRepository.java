package mg.gov.interieur.visa.repository;

import mg.gov.interieur.visa.entity.PieceJustificative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJustificativeRepository extends JpaRepository<PieceJustificative, Long> {

    List<PieceJustificative> findByDemandeId(Long demandeId);

    List<PieceJustificative> findByDemandeIdAndObligatoire(Long demandeId, boolean obligatoire);
}
