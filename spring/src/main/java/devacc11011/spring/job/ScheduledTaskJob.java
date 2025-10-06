package devacc11011.spring.job;

import devacc11011.spring.dto.TaskRequest;
import devacc11011.spring.entity.Schedule;
import devacc11011.spring.entity.Task;
import devacc11011.spring.repository.ScheduleRepository;
import devacc11011.spring.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTaskJob implements Job {

	private final TaskService taskService;
	private final ScheduleRepository scheduleRepository;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Long scheduleId = dataMap.getLong("scheduleId");

		log.info("Executing scheduled task for schedule ID: {}", scheduleId);

		Schedule schedule = scheduleRepository.findByIdWithUser(scheduleId).orElse(null);
		if (schedule == null) {
			log.error("Schedule not found: {}", scheduleId);
			return;
		}

		// 날짜 범위 체크
		LocalDateTime now = LocalDateTime.now();
		if (schedule.getStartDate() != null && now.isBefore(schedule.getStartDate())) {
			log.info("Schedule {} not yet started. Current: {}, Start: {}", scheduleId, now, schedule.getStartDate());
			return;
		}

		if (schedule.getEndDate() != null && now.isAfter(schedule.getEndDate())) {
			log.info("Schedule {} has ended. Disabling schedule.", scheduleId);
			schedule.setEnabled(false);
			scheduleRepository.save(schedule);
			return;
		}

		Task newTask;

		// Task 연결 여부에 따라 분기
		if (schedule.getTask() != null) {
			// 관리자용: 원본 Task 복제
			Task originalTask = schedule.getTask();
			TaskRequest taskRequest = TaskRequest.builder()
				.title(originalTask.getTitle() + " (Scheduled)")
				.prompt(originalTask.getPrompt())
				.aiProvider(originalTask.getAiProvider())
				.enableWebSearch(originalTask.getEnableWebSearch())
				.notificationType(originalTask.getNotificationType())
				.build();

			newTask = taskService.createTask(taskRequest, originalTask.getUser());
			log.info("Created task from linked Task ID {} for schedule {}", originalTask.getId(), scheduleId);
		} else {
			// 일반 사용자용: Schedule 정보로 직접 Task 생성
			TaskRequest taskRequest = TaskRequest.builder()
				.title(schedule.getTitle())
				.prompt(schedule.getPrompt())
				.aiProvider(schedule.getAiProvider())
				.enableWebSearch(schedule.getEnableWebSearch())
				.notificationType(schedule.getNotificationType())
				.build();

			newTask = taskService.createTask(taskRequest, schedule.getUser());
			log.info("Created task from schedule {} for user {}", scheduleId, schedule.getUser().getUsername());
		}

		// 비동기로 실행
		taskService.executeTask(newTask.getId());

		// 마지막 실행 시간 업데이트
		schedule.setLastExecutedAt(now);
		scheduleRepository.save(schedule);

		log.info("Created and executed task ID {} from schedule ID {}", newTask.getId(), scheduleId);
	}
}
