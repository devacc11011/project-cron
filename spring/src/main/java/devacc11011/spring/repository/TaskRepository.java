package devacc11011.spring.repository;

import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserOrderByCreatedAtDesc(User user);

	List<Task> findAllByOrderByCreatedAtDesc();
}
