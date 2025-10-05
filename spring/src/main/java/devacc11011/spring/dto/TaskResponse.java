package devacc11011.spring.dto;

import devacc11011.spring.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

	private Long id;
	private String title;
	private String prompt;
	private String result;
	private String status;
	private String aiProvider;
	private Boolean enableWebSearch;
	private UserInfo user;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime executedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo {
		private String discordId;
		private String username;
	}

	public static TaskResponse from(Task task) {
		return TaskResponse.builder()
			.id(task.getId())
			.title(task.getTitle())
			.prompt(task.getPrompt())
			.result(task.getResult())
			.status(task.getStatus().name())
			.aiProvider(task.getAiProvider())
			.enableWebSearch(task.getEnableWebSearch())
			.user(UserInfo.builder()
				.discordId(task.getUser().getDiscordId())
				.username(task.getUser().getUsername())
				.build())
			.createdAt(task.getCreatedAt())
			.updatedAt(task.getUpdatedAt())
			.executedAt(task.getExecutedAt())
			.build();
	}
}
