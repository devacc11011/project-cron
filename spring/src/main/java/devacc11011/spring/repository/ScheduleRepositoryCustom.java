package devacc11011.spring.repository;

import devacc11011.spring.entity.Schedule;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepositoryCustom {
    List<Schedule> findAllEnabledWithDetails();
    Optional<Schedule> findByIdWithDetails(Long id);
}
