package devacc11011.spring.service;

import devacc11011.spring.dto.AIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("geminiService")
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AIService {

	private final WebClient.Builder webClientBuilder;

	@Value("${gemini.api.key:}")
	private String apiKey;

	private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";

	@Override
	public boolean isEnabled() {
		return apiKey != null && !apiKey.isEmpty();
	}

	@Override
	public AIResponse executeTask(String prompt) {
		return executeGeminiRequest(prompt, false);
	}

	@Override
	public AIResponse executeTaskWithWebSearch(String prompt) {
		return executeGeminiRequest(prompt, true);
	}

	private AIResponse executeGeminiRequest(String prompt, boolean enableGrounding) {
		if (!isEnabled()) {
			return AIResponse.ofTextOnly("Gemini API is disabled (API key not configured)");
		}

		try {
			WebClient webClient = webClientBuilder.build();

			// Web search 활성화 시 현재 날짜 정보 추가
			String finalPrompt;
			if (enableGrounding) {
				String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				finalPrompt = String.format("Today is %s. %s", today, prompt);
			} else {
				finalPrompt = prompt;
			}

			Map<String, Object> requestBody;
			if (enableGrounding) {
				// Google Search Grounding 사용
				requestBody = Map.of(
					"contents", List.of(
						Map.of(
							"parts", List.of(
								Map.of("text", finalPrompt)
							)
						)
					),
					"tools", List.of(
						Map.of("google_search", Map.of())
					)
				);
			} else {
				requestBody = Map.of(
					"contents", List.of(
						Map.of(
							"parts", List.of(
								Map.of("text", finalPrompt)
							)
						)
					)
				);
			}

			Map<String, Object> responseMap = webClient.post()
				.uri(GEMINI_API_URL + "?key=" + apiKey)
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

			return AIResponse.ofTextOnly("Failed to get response from Gemini API");
		} catch (Exception e) {
			log.error("Error calling Gemini API", e);
			return AIResponse.ofTextOnly("Error: " + e.getMessage());
		}
	}

	@Override
	public String getProviderName() {
		return "Gemini";
	}

	private String extractTextFromResponse(Map<String, Object> response) {
		try {
			List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
			if (candidates != null && !candidates.isEmpty()) {
				Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
				List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
				if (parts != null && !parts.isEmpty()) {
					return (String) parts.get(0).get("text");
				}
			}
		} catch (Exception e) {
			log.error("Error extracting text from response", e);
		}
		return "Failed to extract text from response";
	}

	private Long extractTokenUsage(Map<String, Object> response) {
		try {
			Map<String, Object> usageMetadata = (Map<String, Object>) response.get("usageMetadata");
			if (usageMetadata != null) {
				Object totalTokenCount = usageMetadata.get("totalTokenCount");
				if (totalTokenCount instanceof Integer) {
					return ((Integer) totalTokenCount).longValue();
				} else if (totalTokenCount instanceof Long) {
					return (Long) totalTokenCount;
				}
			}
		} catch (Exception e) {
			log.error("Error extracting token usage from response", e);
		}
		return 0L;
	}
}
