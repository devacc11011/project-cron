package devacc11011.spring.service;

import devacc11011.spring.dto.AIResponse;
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
	private final UserUsageService userUsageService;

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

		Boolean enableWebSearch = request.getEnableWebSearch();
		if (enableWebSearch == null) {
			enableWebSearch = false;
		}

		Task task = Task.builder()
			.title(request.getTitle())
			.prompt(request.getPrompt())
			.status(Task.TaskStatus.PENDING)
			.aiProvider(provider)
			.enableWebSearch(enableWebSearch)
			.user(user)
			.build();

		return taskRepository.save(task);
	}

	@Transactional
	@Async
	public void executeTask(Long taskId) {
		Task task = findById(taskId);

		// 사용량 체크 (예상 최대 토큰: 5000)
		long estimatedTokens = 5000L;
		if (!userUsageService.hasTokensAvailable(task.getUser(), estimatedTokens)) {
			log.warn("User {} exceeded token limit", task.getUser().getUsername());
			task.setStatus(Task.TaskStatus.FAILED);
			task.setResult("Error: Monthly token limit exceeded. Please upgrade your plan or wait for next month.");
			taskRepository.save(task);
			return;
		}

		try {
			task.setStatus(Task.TaskStatus.PROCESSING);
			taskRepository.save(task);

			AIService aiService = aiServiceFactory.getAIService(task.getAiProvider());

			// 웹 검색 옵션에 따라 다른 메서드 호출
			AIResponse response;
			if (Boolean.TRUE.equals(task.getEnableWebSearch())) {
				response = aiService.executeTaskWithWebSearch(task.getPrompt());
				log.info("Task {} executing with web search enabled", taskId);
			} else {
				response = aiService.executeTask(task.getPrompt());
			}

			task.setResult(response.getText());
			task.setTokensUsed(response.getTokensUsed());
			task.setStatus(Task.TaskStatus.COMPLETED);
			task.setExecutedAt(LocalDateTime.now());
			taskRepository.save(task);

			// 실제 사용된 토큰 업데이트
			userUsageService.addTokensUsed(task.getUser(), response.getTokensUsed());

			log.info("Task {} completed successfully with {} ({} tokens used)",
				taskId, aiService.getProviderName(), response.getTokensUsed());
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
