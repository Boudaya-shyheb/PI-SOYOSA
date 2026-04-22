package soyosa.userservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soyosa.userservice.Domain.Profile.Profile;
import soyosa.userservice.Domain.User.User;

import java.util.Optional;

@Repository
public interface ProfileRepo extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

    Optional<Profile> findByUserUsername(String username);

}
