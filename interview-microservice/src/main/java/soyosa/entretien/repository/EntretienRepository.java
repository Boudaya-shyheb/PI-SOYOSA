package soyosa.entretien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soyosa.entretien.Domain.Entretien;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {

    Entretien findByUser(String user);

}
