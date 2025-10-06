package devacc11011.spring.service;

import com.fasterxml.jackson.databind.JsonNode;
import devacc11011.spring.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("discordNotificationService")
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationService implements NotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${discord.bot-token:}")
    private String botToken;

    @Value("${discord.user-id:}")
    private String userId;

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    @Override
    public boolean isEnabled() {
        return botToken != null && !botToken.isEmpty() && userId != null && !userId.isEmpty();
    }

    @Override
    public String getNotificationType() {
        return "discord";
    }

    @Override
    public void sendTaskCompletionNotification(Task task) {
        if (!isEnabled()) {
            log.warn("Discord bot token or user ID is not configured.");
            return;
        }

        try {
            getDmChannelId()
                .flatMap(channelId -> sendMessage(channelId, task))
                .block();
            log.info("Discord DM notification sent for task: {}", task.getId());
        } catch (Exception e) {
            log.error("Failed to send Discord DM notification for task: {}", task.getId(), e);
        }
    }

    private Mono<String> getDmChannelId() {
        WebClient webClient = webClientBuilder.baseUrl(DISCORD_API_BASE).build();
        return webClient.post()
            .uri("/users/@me/channels")
            .header("Authorization", "Bot " + botToken)
            .header("Content-Type", "application/json")
            .bodyValue(Map.of("recipient_id", userId))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(jsonNode -> jsonNode.get("id").asText())
            .doOnError(e -> log.error("Failed to get DM channel ID", e));
    }

    private Mono<Void> sendMessage(String channelId, Task task) {
        WebClient webClient = webClientBuilder.baseUrl(DISCORD_API_BASE).build();

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
            "timestamp", task.getExecutedAt() != null ? task.getExecutedAt().toString() : ""
        );

        Map<String, Object> requestBody = Map.of(
            "embeds", List.of(embed)
        );

        return webClient.post()
            .uri("/channels/" + channelId + "/messages")
            .header("Authorization", "Bot " + botToken)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnError(e -> log.error("Failed to send message to channel {}", channelId, e));
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
        return result.length() > 200 ? result.substring(0, 200) + "..." : result;
    }
}