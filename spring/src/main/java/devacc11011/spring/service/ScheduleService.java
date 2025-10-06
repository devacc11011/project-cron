package devacc11011.spring.service;

import devacc11011.spring.entity.Schedule;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import devacc11011.spring.exception.NotFoundException;
import devacc11011.spring.job.ScheduledTaskJob;
import devacc11011.spring.repository.ScheduleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final Scheduler scheduler;

	@PostConstruct
	public void init() {
		// 애플리케이션 시작 시 활성화된 모든 스케줄 등록
		List<Schedule> activeSchedules = scheduleRepository.findByEnabledTrue();
		for (Schedule schedule : activeSchedules) {
			try {
				registerQuartzJob(schedule);
				log.info("Registered schedule {} with cron: {}", schedule.getId(), schedule.getCronExpression());
			} catch (Exception e) {
				log.error("Failed to register schedule {}", schedule.getId(), e);
			}
		}
	}

	public List<Schedule> findAllSchedules() {
		return scheduleRepository.findAll();
	}

	public Schedule findById(Long id) {
		return scheduleRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Schedule not found with id: " + id));
	}

	public List<Schedule> findByTask(Task task) {
		return scheduleRepository.findByTask(task);
	}

	public List<Schedule> findByUser(User user) {
		return scheduleRepository.findByUserOrderByCreatedAtDesc(user);
	}

	// 관리자용: Task 연결 스케줄
	@Transactional
	public Schedule createSchedule(Task task, String cronExpression, LocalDateTime startDate, LocalDateTime endDate) {
		Schedule schedule = Schedule.builder()
			.task(task)
			.user(task.getUser())
			.cronExpression(cronExpression)
			.startDate(startDate)
			.endDate(endDate)
			.enabled(true)
			.build();

		schedule = scheduleRepository.save(schedule);

		// Quartz Job 등록
		try {
			registerQuartzJob(schedule);
		} catch (SchedulerException e) {
			log.error("Failed to register Quartz job for schedule {}", schedule.getId(), e);
			throw new RuntimeException("Failed to schedule task", e);
		}

		return schedule;
	}

	// 일반 사용자용: 독립 스케줄
	@Transactional
	public Schedule createUserSchedule(User user, String title, String prompt, String aiProvider, Boolean enableWebSearch,
	                                    String cronExpression, LocalDateTime startDate, LocalDateTime endDate) {
		Schedule schedule = Schedule.builder()
			.user(user)
			.title(title)
			.prompt(prompt)
			.aiProvider(aiProvider != null ? aiProvider : "gemini")
			.enableWebSearch(enableWebSearch != null ? enableWebSearch : false)
			.cronExpression(cronExpression)
			.startDate(startDate)
			.endDate(endDate)
			.enabled(true)
			.build();

		schedule = scheduleRepository.save(schedule);

		// Quartz Job 등록
		try {
			registerQuartzJob(schedule);
		} catch (SchedulerException e) {
			log.error("Failed to register Quartz job for schedule {}", schedule.getId(), e);
			throw new RuntimeException("Failed to schedule task", e);
		}

		return schedule;
	}

	@Transactional
	public Schedule updateSchedule(Long id, String cronExpression, LocalDateTime startDate, LocalDateTime endDate) {
		Schedule schedule = findById(id);

		// 기존 Job 삭제
		try {
			unregisterQuartzJob(schedule);
		} catch (SchedulerException e) {
			log.warn("Failed to unregister old job for schedule {}", id, e);
		}

		// 스케줄 정보 업데이트
		schedule.setCronExpression(cronExpression);
		schedule.setStartDate(startDate);
		schedule.setEndDate(endDate);
		scheduleRepository.save(schedule);

		// 새 Job 등록
		if (schedule.getEnabled()) {
			try {
				registerQuartzJob(schedule);
			} catch (SchedulerException e) {
				log.error("Failed to register updated Quartz job for schedule {}", id, e);
				throw new RuntimeException("Failed to reschedule task", e);
			}
		}

		return schedule;
	}

	@Transactional
	public Schedule toggleSchedule(Long id) {
		Schedule schedule = findById(id);
		schedule.setEnabled(!schedule.getEnabled());
		scheduleRepository.save(schedule);

		try {
			if (schedule.getEnabled()) {
				registerQuartzJob(schedule);
			} else {
				unregisterQuartzJob(schedule);
			}
		} catch (SchedulerException e) {
			log.error("Failed to toggle schedule {}", id, e);
			throw new RuntimeException("Failed to toggle schedule", e);
		}

		return schedule;
	}

	@Transactional
	public void deleteSchedule(Long id) {
		Schedule schedule = findById(id);

		try {
			unregisterQuartzJob(schedule);
		} catch (SchedulerException e) {
			log.warn("Failed to unregister job for schedule {}", id, e);
		}

		scheduleRepository.delete(schedule);
	}

	private void registerQuartzJob(Schedule schedule) throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob(ScheduledTaskJob.class)
			.withIdentity("task-" + schedule.getId(), "scheduled-tasks")
			.usingJobData("scheduleId", schedule.getId())
			.build();

		CronTrigger trigger = TriggerBuilder.newTrigger()
			.withIdentity("trigger-" + schedule.getId(), "scheduled-tasks")
			.withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression()))
			.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}

	private void unregisterQuartzJob(Schedule schedule) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("task-" + schedule.getId(), "scheduled-tasks");
		scheduler.deleteJob(jobKey);
	}
}
