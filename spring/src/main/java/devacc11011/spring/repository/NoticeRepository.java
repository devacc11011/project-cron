package devacc11011.spring.repository;

import devacc11011.spring.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

	List<Notice> findAllByOrderByCreatedAtDesc();
}
