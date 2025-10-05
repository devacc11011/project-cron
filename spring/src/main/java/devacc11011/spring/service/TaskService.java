package devacc11011.spring.service;

import devacc11011.spring.dto.TaskRequest;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import devacc11011.spring.exception.NotFoundException;
import devacc11011.spring.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskService {

	private final TaskRepository taskRepository;
	private final AIServiceFactory aiServiceFactory;

	public List<Task> findAllTasks() {
		return taskRepository.findAllByOrderByCreatedAtDesc();
	}

	public List<Task> findUserTasks(User user) {
		return taskRepository.findByUserOrderByCreatedAtDesc(user);
	}

	public Task findById(Long id) {
		return taskRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
	}

	@Transactional
	public Task createTask(TaskRequest request, User user) {
		String provider = request.getAiProvider();
		if (provider == null || provider.isEmpty()) {
			provider = "gemini";
		}

		Task task = Task.builder()
			.title(request.getTitle())
			.prompt(request.getPrompt())
			.status(Task.TaskStatus.PENDING)
			.aiProvider(provider)
			.user(user)
			.build();

		return taskRepository.save(task);
	}

	@Transactional
	@Async
	public void executeTask(Long taskId) {
		Task task = findById(taskId);

		try {
			task.setStatus(Task.TaskStatus.PROCESSING);
			taskRepository.save(task);

			AIService aiService = aiServiceFactory.getAIService(task.getAiProvider());
			String result = aiService.executeTask(task.getPrompt());

			task.setResult(result);
			task.setStatus(Task.TaskStatus.COMPLETED);
			task.setExecutedAt(LocalDateTime.now());
			taskRepository.save(task);

			log.info("Task {} completed successfully with {}", taskId, aiService.getProviderName());
		} catch (Exception e) {
			log.error("Task {} failed", taskId, e);
			task.setStatus(Task.TaskStatus.FAILED);
			task.setResult("Error: " + e.getMessage());
			taskRepository.save(task);
		}
	}

	@Transactional
	public void deleteTask(Long id) {
		Task task = findById(id);
		taskRepository.delete(task);
	}
}
