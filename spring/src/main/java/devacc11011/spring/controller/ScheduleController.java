package devacc11011.spring.controller;

import devacc11011.spring.dto.ScheduleRequest;
import devacc11011.spring.dto.ScheduleResponse;
import devacc11011.spring.entity.Schedule;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.ScheduleService;
import devacc11011.spring.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;
	private final TaskService taskService;

	@GetMapping
	public List<ScheduleResponse> getAllSchedules() {
		return scheduleService.findAllSchedules().stream()
			.map(ScheduleResponse::from)
			.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public ScheduleResponse getSchedule(@PathVariable Long id) {
		return ScheduleResponse.from(scheduleService.findById(id));
	}

	@GetMapping("/task/{taskId}")
	public List<ScheduleResponse> getSchedulesByTask(@PathVariable Long taskId) {
		Task task = taskService.findById(taskId);
		return scheduleService.findByTask(task).stream()
			.map(ScheduleResponse::from)
			.collect(Collectors.toList());
	}

	@PostMapping
	public ScheduleResponse createSchedule(@RequestBody ScheduleRequest request) {
		Task task = taskService.findById(request.getTaskId());

		LocalDateTime startDate = request.getStartDate() != null
			? LocalDateTime.parse(request.getStartDate())
			: null;

		LocalDateTime endDate = request.getEndDate() != null
			? LocalDateTime.parse(request.getEndDate())
			: null;

		Schedule schedule = scheduleService.createSchedule(
			task,
			request.getCronExpression(),
			startDate,
			endDate
		);

		return ScheduleResponse.from(schedule);
	}

	@PutMapping("/{id}")
	public ScheduleResponse updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
		LocalDateTime startDate = request.getStartDate() != null
			? LocalDateTime.parse(request.getStartDate())
			: null;

		LocalDateTime endDate = request.getEndDate() != null
			? LocalDateTime.parse(request.getEndDate())
			: null;

		Schedule schedule = scheduleService.updateSchedule(
			id,
			request.getCronExpression(),
			startDate,
			endDate
		);

		return ScheduleResponse.from(schedule);
	}

	@PostMapping("/{id}/toggle")
	public ScheduleResponse toggleSchedule(@PathVariable Long id) {
		Schedule schedule = scheduleService.toggleSchedule(id);
		return ScheduleResponse.from(schedule);
	}

	@DeleteMapping("/{id}")
	public void deleteSchedule(@PathVariable Long id) {
		scheduleService.deleteSchedule(id);
	}

	// 일반 사용자용 엔드포인트
	@GetMapping("/my")
	public List<ScheduleResponse> getMySchedules(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		User user = oAuth2User.getUser();
		return scheduleService.findByUser(user).stream()
			.map(ScheduleResponse::from)
			.collect(Collectors.toList());
	}

	@PostMapping("/my")
	public ScheduleResponse createMySchedule(
		@RequestBody ScheduleRequest request,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		User user = oAuth2User.getUser();

		LocalDateTime startDate = request.getStartDate() != null
			? LocalDateTime.parse(request.getStartDate())
			: null;

		LocalDateTime endDate = request.getEndDate() != null
			? LocalDateTime.parse(request.getEndDate())
			: null;

		Schedule schedule = scheduleService.createUserSchedule(
			user,
			request.getTitle(),
			request.getPrompt(),
			request.getAiProvider(),
			request.getEnableWebSearch(),
			request.getNotificationType(),
			request.getCronExpression(),
			startDate,
			endDate
		);

		return ScheduleResponse.from(schedule);
	}
}
