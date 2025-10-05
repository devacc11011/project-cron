package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("claudeService")
@RequiredArgsConstructor
@Slf4j
public class ClaudeService implements AIService {

	private final WebClient.Builder webClientBuilder;

	@Value("${claude.api.key:}")
	private String apiKey;

	private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
	private static final String CLAUDE_VERSION = "2023-06-01";
	private static final String MODEL = "claude-3-5-sonnet-20241022";

	@Override
	public boolean isEnabled() {
		return apiKey != null && !apiKey.isEmpty();
	}

	@Override
	public String executeTask(String prompt) {
		return executeClaudeRequest(prompt, false);
	}

	@Override
	public String executeTaskWithWebSearch(String prompt) {
		return executeClaudeRequest(prompt, true);
	}

	private String executeClaudeRequest(String prompt, boolean enableWebSearch) {
		if (!isEnabled()) {
			return "Claude API is disabled (API key not configured)";
		}

		try {
			WebClient webClient = webClientBuilder.build();

			// Web search는 system prompt로 지시
			Map<String, Object> requestBody;
			if (enableWebSearch) {
				String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				requestBody = Map.of(
					"model", MODEL,
					"max_tokens", 4096,
					"system", String.format("Today is %s. You have access to web search capabilities. Use them to find the most recent and accurate information. Focus on events from %s or the most recent available data.", today, today),
					"messages", List.of(
						Map.of(
							"role", "user",
							"content", prompt
						)
					)
				);
			} else {
				requestBody = Map.of(
					"model", MODEL,
					"max_tokens", 4096,
					"messages", List.of(
						Map.of(
							"role", "user",
							"content", prompt
						)
					)
				);
			}

			String response = webClient.post()
				.uri(CLAUDE_API_URL)
				.header("x-api-key", apiKey)
				.header("anthropic-version", CLAUDE_VERSION)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Map.class)
				.map(this::extractTextFromResponse)
				.block();

			return response != null ? response : "Failed to get response from Claude API";
		} catch (Exception e) {
			log.error("Error calling Claude API", e);
			return "Error: " + e.getMessage();
		}
	}

	@Override
	public String getProviderName() {
		return "Claude";
	}

	private String extractTextFromResponse(Map<String, Object> response) {
		try {
			List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
			if (content != null && !content.isEmpty()) {
				return (String) content.get(0).get("text");
			}
		} catch (Exception e) {
			log.error("Error extracting text from response", e);
		}
		return "Failed to extract text from response";
	}
}
