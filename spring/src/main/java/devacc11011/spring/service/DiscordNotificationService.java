package devacc11011.spring.service;

import devacc11011.spring.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("discordNotificationService")
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationService implements NotificationService {

	private final WebClient.Builder webClientBuilder;

	@Value("${discord.webhook.url:}")
	private String webhookUrl;

	@Override
	public boolean isEnabled() {
		return webhookUrl != null && !webhookUrl.isEmpty();
	}

	@Override
	public String getNotificationType() {
		return "discord";
	}

	@Override
	public void sendTaskCompletionNotification(Task task) {
		if (!isEnabled()) {
			log.warn("Discord webhook URL is not configured");
			return;
		}

		try {
			WebClient webClient = webClientBuilder.build();

			String statusEmoji = getStatusEmoji(task.getStatus());
			String statusColor = getStatusColor(task.getStatus());

			Map<String, Object> embed = Map.of(
				"title", statusEmoji + " Task Completed: " + task.getTitle(),
				"description", "Your scheduled task has been executed.",
				"color", Integer.parseInt(statusColor, 16),
				"fields", List.of(
					Map.of("name", "Status", "value", task.getStatus().name(), "inline", true),
					Map.of("name", "AI Provider", "value", task.getAiProvider().toUpperCase(), "inline", true),
					Map.of("name", "Tokens Used", "value", task.getTokensUsed() != null ? task.getTokensUsed().toString() : "N/A", "inline", true),
					Map.of("name", "Executed At", "value", task.getExecutedAt() != null ?
						task.getExecutedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A", "inline", false),
					Map.of("name", "Result Preview", "value", getResultPreview(task.getResult()), "inline", false)
				),
				"footer", Map.of("text", "Project Cron"),
				"timestamp", task.getExecutedAt() != null ? task.getExecutedAt().toString() : null
			);

			Map<String, Object> requestBody = Map.of(
				"username", "Project Cron",
				"embeds", List.of(embed)
			);

			webClient.post()
				.uri(webhookUrl)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			log.info("Discord notification sent for task: {}", task.getId());
		} catch (Exception e) {
			log.error("Failed to send Discord notification for task: {}", task.getId(), e);
		}
	}

	private String getStatusEmoji(Task.TaskStatus status) {
		return switch (status) {
			case COMPLETED -> "✅";
			case FAILED -> "❌";
			case PROCESSING -> "⏳";
			case PENDING -> "⏸️";
		};
	}

	private String getStatusColor(Task.TaskStatus status) {
		return switch (status) {
			case COMPLETED -> "00FF00"; // Green
			case FAILED -> "FF0000"; // Red
			case PROCESSING -> "0000FF"; // Blue
			case PENDING -> "FFFF00"; // Yellow
		};
	}

	private String getResultPreview(String result) {
		if (result == null || result.isEmpty()) {
			return "No result available";
		}

		// 최대 200자까지만 표시
		if (result.length() > 200) {
			return result.substring(0, 200) + "...";
		}

		return result;
	}
}
