package soyosa.userservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soyosa.userservice.Domain.Dto.VerifToken;

import java.util.Optional;

@Repository
public interface VerifTokenRepo extends JpaRepository<VerifToken, Long> {

    Optional<VerifToken> findByToken(String token);

}
