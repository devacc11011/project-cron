package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

	@Override
	public boolean isEnabled() {
		return apiKey != null && !apiKey.isEmpty();
	}

	@Override
	public String executeTask(String prompt) {
		if (!isEnabled()) {
			return "ChatGPT API is disabled (API key not configured)";
		}

		try {
			WebClient webClient = webClientBuilder.build();

			Map<String, Object> requestBody = Map.of(
				"model", MODEL,
				"messages", List.of(
					Map.of(
						"role", "user",
						"content", prompt
					)
				)
			);

			String response = webClient.post()
				.uri(OPENAI_API_URL)
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Map.class)
				.map(this::extractTextFromResponse)
				.block();

			return response != null ? response : "Failed to get response from OpenAI API";
		} catch (Exception e) {
			log.error("Error calling OpenAI API", e);
			return "Error: " + e.getMessage();
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
}
