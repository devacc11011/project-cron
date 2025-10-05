package devacc11011.spring.service;

import devacc11011.spring.dto.TaskRequest;
import devacc11011.spring.entity.Task;
import devacc11011.spring.entity.User;
import devacc11011.spring.exception.NotFoundException;
import devacc11011.spring.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private AIServiceFactory aiServiceFactory;

	@Mock
	private AIService aiService;

	@InjectMocks
	private TaskService taskService;

	private User testUser;
	private Task testTask;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.id(1L)
			.discordId("123456789")
			.username("testuser")
			.build();

		testTask = Task.builder()
			.id(1L)
			.title("Test Task")
			.prompt("Test prompt")
			.status(Task.TaskStatus.PENDING)
			.aiProvider("gemini")
			.user(testUser)
			.build();
	}

	@Test
	@DisplayName("모든 작업 조회 성공")
	void findAllTasks_Success() {
		// given
		given(taskRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(testTask));

		// when
		List<Task> tasks = taskService.findAllTasks();

		// then
		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).getTitle()).isEqualTo("Test Task");
		verify(taskRepository).findAllByOrderByCreatedAtDesc();
	}

	@Test
	@DisplayName("사용자별 작업 조회 성공")
	void findUserTasks_Success() {
		// given
		given(taskRepository.findByUserOrderByCreatedAtDesc(testUser)).willReturn(List.of(testTask));

		// when
		List<Task> tasks = taskService.findUserTasks(testUser);

		// then
		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).getUser()).isEqualTo(testUser);
		verify(taskRepository).findByUserOrderByCreatedAtDesc(testUser);
	}

	@Test
	@DisplayName("ID로 작업 조회 성공")
	void findById_Success() {
		// given
		given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));

		// when
		Task task = taskService.findById(1L);

		// then
		assertThat(task).isNotNull();
		assertThat(task.getId()).isEqualTo(1L);
		verify(taskRepository).findById(1L);
	}

	@Test
	@DisplayName("ID로 작업 조회 실패 - 존재하지 않는 ID")
	void findById_NotFound() {
		// given
		given(taskRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> taskService.findById(999L))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("Task not found with id: 999");
	}

	@Test
	@DisplayName("작업 생성 성공")
	void createTask_Success() {
		// given
		TaskRequest request = TaskRequest.builder()
			.title("New Task")
			.prompt("New prompt")
			.aiProvider("claude")
			.build();

		Task newTask = Task.builder()
			.title(request.getTitle())
			.prompt(request.getPrompt())
			.status(Task.TaskStatus.PENDING)
			.aiProvider(request.getAiProvider())
			.user(testUser)
			.build();

		given(taskRepository.save(any(Task.class))).willReturn(newTask);

		// when
		Task createdTask = taskService.createTask(request, testUser);

		// then
		assertThat(createdTask).isNotNull();
		assertThat(createdTask.getTitle()).isEqualTo("New Task");
		assertThat(createdTask.getAiProvider()).isEqualTo("claude");
		assertThat(createdTask.getStatus()).isEqualTo(Task.TaskStatus.PENDING);
		verify(taskRepository).save(any(Task.class));
	}

	@Test
	@DisplayName("작업 삭제 성공")
	void deleteTask_Success() {
		// given
		given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
		willDoNothing().given(taskRepository).delete(testTask);

		// when
		taskService.deleteTask(1L);

		// then
		verify(taskRepository).findById(1L);
		verify(taskRepository).delete(testTask);
	}

	@Test
	@DisplayName("작업 삭제 실패 - 존재하지 않는 ID")
	void deleteTask_NotFound() {
		// given
		given(taskRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> taskService.deleteTask(999L))
			.isInstanceOf(NotFoundException.class);
		verify(taskRepository, never()).delete(any(Task.class));
	}
}
