package devacc11011.spring.controller;

import devacc11011.spring.dto.TaskRequest;
import devacc11011.spring.dto.TaskResponse;
import devacc11011.spring.entity.Task;
import devacc11011.spring.exception.UnauthorizedException;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<TaskResponse> getAllTasks() {
		List<Task> tasks = taskService.findAllTasks();
		return tasks.stream()
			.map(TaskResponse::from)
			.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public TaskResponse getTaskById(@PathVariable Long id) {
		Task task = taskService.findById(id);
		return TaskResponse.from(task);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse createTask(
		@RequestBody TaskRequest request,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		if (oAuth2User == null) {
			throw new UnauthorizedException();
		}

		Task task = taskService.createTask(request, oAuth2User.getUser());
		return TaskResponse.from(task);
	}

	@PostMapping("/{id}/execute")
	@PreAuthorize("hasRole('ADMIN')")
	public TaskResponse executeTask(@PathVariable Long id) {
		Task task = taskService.findById(id);
		taskService.executeTask(id);
		return TaskResponse.from(task);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
	}
}
