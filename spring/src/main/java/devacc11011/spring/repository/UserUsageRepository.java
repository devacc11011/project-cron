package devacc11011.spring.repository;

import devacc11011.spring.entity.User;
import devacc11011.spring.entity.UserUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserUsageRepository extends JpaRepository<UserUsage, Long> {

	Optional<UserUsage> findByUserAndYearMonth(User user, String yearMonth);

	void deleteByYearMonth(String yearMonth);
}
