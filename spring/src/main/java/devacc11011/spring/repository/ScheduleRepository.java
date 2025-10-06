package devacc11011.spring.repository;

import devacc11011.spring.entity.Schedule;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.task WHERE s.enabled = true")
	List<Schedule> findByEnabledTrue();

	List<Schedule> findByTask(Task task);

	List<Schedule> findByUser(User user);

	List<Schedule> findByUserOrderByCreatedAtDesc(User user);

	Optional<Schedule> findByTaskId(Long taskId);

	@Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.task WHERE s.id = :id")
	Optional<Schedule> findByIdWithUser(@Param("id") Long id);
}
