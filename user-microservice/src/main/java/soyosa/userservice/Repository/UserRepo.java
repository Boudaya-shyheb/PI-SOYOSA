package soyosa.userservice.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soyosa.userservice.Domain.User.TypeRole;
import soyosa.userservice.Domain.User.User;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Page<User> findByRoleAndUsernameContainingIgnoreCase(TypeRole role, String username, Pageable pageable);

}
