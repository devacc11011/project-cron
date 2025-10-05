package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSearchService {

	private final WebClient.Builder webClientBuilder;

	@Value("${tavily.api.key:}")
	private String tavilyApiKey;

	private static final String TAVILY_API_URL = "https://api.tavily.com/search";

	public boolean isEnabled() {
		return tavilyApiKey != null && !tavilyApiKey.isEmpty();
	}

	public String search(String query) {
		if (!isEnabled()) {
			return "Web search is disabled (Tavily API key not configured)";
		}

		try {
			WebClient webClient = webClientBuilder.build();

			Map<String, Object> requestBody = Map.of(
				"api_key", tavilyApiKey,
				"query", query,
				"max_results", 5,
				"include_answer", true,
				"search_depth", "basic"
			);

			Map<String, Object> response = webClient.post()
				.uri(TAVILY_API_URL)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

			return formatSearchResults(response);
		} catch (Exception e) {
			log.error("Error calling Tavily Search API", e);
			return "Error searching the web: " + e.getMessage();
		}
	}

	private String formatSearchResults(Map<String, Object> response) {
		StringBuilder result = new StringBuilder();

		// Add answer if available
		if (response.containsKey("answer")) {
			result.append("Answer: ").append(response.get("answer")).append("\n\n");
		}

		// Add search results
		if (response.containsKey("results")) {
			List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
			result.append("Search Results:\n");

			for (int i = 0; i < results.size(); i++) {
				Map<String, Object> item = results.get(i);
				result.append("\n").append(i + 1).append(". ")
					.append(item.get("title")).append("\n")
					.append("URL: ").append(item.get("url")).append("\n")
					.append(item.get("content")).append("\n");
			}
		}

		return result.toString();
	}
}
