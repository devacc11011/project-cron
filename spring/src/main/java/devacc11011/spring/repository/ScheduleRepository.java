package devacc11011.spring.repository;

import devacc11011.spring.entity.Schedule;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByEnabledTrue();

	List<Schedule> findByTask(Task task);

	List<Schedule> findByUser(User user);

	List<Schedule> findByUserOrderByCreatedAtDesc(User user);

	Optional<Schedule> findByTaskId(Long taskId);
}
