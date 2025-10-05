package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service("geminiService")
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AIService {

	private final WebClient.Builder webClientBuilder;

	@Value("${gemini.api.key:}")
	private String apiKey;

	private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

	@Override
	public boolean isEnabled() {
		return apiKey != null && !apiKey.isEmpty();
	}

	@Override
	public String executeTask(String prompt) {
		if (!isEnabled()) {
			return "Gemini API is disabled (API key not configured)";
		}

		try {
			WebClient webClient = webClientBuilder.build();

			Map<String, Object> requestBody = Map.of(
				"contents", List.of(
					Map.of(
						"parts", List.of(
							Map.of("text", prompt)
						)
					)
				)
			);

			String response = webClient.post()
				.uri(GEMINI_API_URL + "?key=" + apiKey)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Map.class)
				.map(this::extractTextFromResponse)
				.block();

			return response != null ? response : "Failed to get response from Gemini API";
		} catch (Exception e) {
			log.error("Error calling Gemini API", e);
			return "Error: " + e.getMessage();
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
}
