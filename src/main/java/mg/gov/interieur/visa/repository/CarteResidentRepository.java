package mg.gov.interieur.visa.repository;

import mg.gov.interieur.visa.entity.CarteResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarteResidentRepository extends JpaRepository<CarteResident, Long> {

    Optional<CarteResident> findByNumeroCarte(String numeroCarte);
}
