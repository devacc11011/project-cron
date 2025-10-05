package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AIServiceFactory {

	private final Map<String, AIService> aiServices;

	public AIService getAIService(String providerName) {
		AIService service = switch (providerName.toLowerCase()) {
			case "gemini" -> aiServices.get("geminiService");
			case "claude" -> aiServices.get("claudeService");
			case "chatgpt", "openai" -> aiServices.get("chatGPTService");
			default -> aiServices.get("geminiService");
		};

		// If requested service is disabled, find the first enabled service
		if (service != null && !service.isEnabled()) {
			return aiServices.values().stream()
				.filter(AIService::isEnabled)
				.findFirst()
				.orElse(service); // Return disabled service if none are enabled
		}

		return service;
	}

	public AIService getFirstEnabledService() {
		return aiServices.values().stream()
			.filter(AIService::isEnabled)
			.findFirst()
			.orElse(aiServices.get("geminiService")); // Fallback to gemini
	}
}
