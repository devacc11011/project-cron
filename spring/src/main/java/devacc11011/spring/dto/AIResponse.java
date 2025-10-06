package devacc11011.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

	private String text;
	private Long tokensUsed;

	public static AIResponse of(String text, Long tokensUsed) {
		return AIResponse.builder()
			.text(text)
			.tokensUsed(tokensUsed)
			.build();
	}

	public static AIResponse ofTextOnly(String text) {
		return AIResponse.builder()
			.text(text)
			.tokensUsed(0L)
			.build();
	}
}
