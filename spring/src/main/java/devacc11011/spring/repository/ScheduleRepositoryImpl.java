package devacc11011.spring.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import devacc11011.spring.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static devacc11011.spring.entity.QSchedule.schedule;
import static devacc11011.spring.entity.QTask.task;
import static devacc11011.spring.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Schedule> findAllEnabledWithDetails() {
        return queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.user, user).fetchJoin()
                .leftJoin(schedule.task, task).fetchJoin()
                .where(schedule.enabled.isTrue())
                .fetch();
    }

    @Override
    public Optional<Schedule> findByIdWithDetails(Long id) {
        Schedule result = queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.user, user).fetchJoin()
                .leftJoin(schedule.task, task).fetchJoin()
                .where(schedule.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
