package devacc11011.spring.repository;

import devacc11011.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByDiscordId(String discordId);

	Optional<User> findByGoogleId(String googleId);

	boolean existsByDiscordId(String discordId);

	boolean existsByGoogleId(String googleId);
}
