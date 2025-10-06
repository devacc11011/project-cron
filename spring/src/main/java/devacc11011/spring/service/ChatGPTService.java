package devacc11011.spring.service;

import devacc11011.spring.dto.AIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("chatGPTService")
@RequiredArgsConstructor
@Slf4j
public class ChatGPTService implements AIService {

	private final WebClient.Builder webClientBuilder;

	@Value("${openai.api.key:}")
	private String apiKey;

	private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
	private static final String MODEL = "gpt-4o";
	private static final String MODEL_WITH_SEARCH = "gpt-4o-with-browsing";

	@Override
	public boolean isEnabled() {
		return apiKey != null && !apiKey.isEmpty();
	}

	@Override
	public AIResponse executeTask(String prompt) {
		return executeChatGPTRequest(prompt, false);
	}

	@Override
	public AIResponse executeTaskWithWebSearch(String prompt) {
		return executeChatGPTRequest(prompt, true);
	}

	private AIResponse executeChatGPTRequest(String prompt, boolean enableWebSearch) {
		if (!isEnabled()) {
			return AIResponse.ofTextOnly("ChatGPT API is disabled (API key not configured)");
		}

		try {
			WebClient webClient = webClientBuilder.build();

			// Web search는 system message로 지시
			List<Map<String, Object>> messages;
			if (enableWebSearch) {
				String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				messages = List.of(
					Map.of(
						"role", "system",
						"content", String.format("Today is %s. You have access to web search. Use it to find the most recent and accurate information. Focus on events from %s or the most recent available data.", today, today)
					),
					Map.of(
						"role", "user",
						"content", prompt
					)
				);
			} else {
				messages = List.of(
					Map.of(
						"role", "user",
						"content", prompt
					)
				);
			}

			Map<String, Object> requestBody = Map.of(
				"model", MODEL,
				"messages", messages
			);

			Map<String, Object> responseMap = webClient.post()
				.uri(OPENAI_API_URL)
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

			if (responseMap != null) {
				String text = extractTextFromResponse(responseMap);
				Long tokensUsed = extractTokenUsage(responseMap);
				return AIResponse.of(text, tokensUsed);
			}

			return AIResponse.ofTextOnly("Failed to get response from OpenAI API");
		} catch (Exception e) {
			log.error("Error calling OpenAI API", e);
			return AIResponse.ofTextOnly("Error: " + e.getMessage());
		}
	}

	@Override
	public String getProviderName() {
		return "ChatGPT";
	}

	private String extractTextFromResponse(Map<String, Object> response) {
		try {
			List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
			if (choices != null && !choices.isEmpty()) {
				Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
				return (String) message.get("content");
			}
		} catch (Exception e) {
			log.error("Error extracting text from response", e);
		}
		return "Failed to extract text from response";
	}

	private Long extractTokenUsage(Map<String, Object> response) {
		try {
			Map<String, Object> usage = (Map<String, Object>) response.get("usage");
			if (usage != null) {
				Object totalTokens = usage.get("total_tokens");
				if (totalTokens instanceof Integer) {
					return ((Integer) totalTokens).longValue();
				} else if (totalTokens instanceof Long) {
					return (Long) totalTokens;
				}
			}
		} catch (Exception e) {
			log.error("Error extracting token usage from response", e);
		}
		return 0L;
	}
}
