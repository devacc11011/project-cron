package devacc11011.spring.repository;

import devacc11011.spring.entity.User;
import devacc11011.spring.entity.UserTokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenUsageRepository extends JpaRepository<UserTokenUsage, Long> {

	Optional<UserTokenUsage> findByUserAndYearMonth(User user, String yearMonth);

	void deleteByYearMonth(String yearMonth);
}
