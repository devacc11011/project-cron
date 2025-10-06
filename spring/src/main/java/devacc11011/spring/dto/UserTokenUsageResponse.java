package devacc11011.spring.dto;

import devacc11011.spring.entity.UserTokenUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenUsageResponse {

	private String yearMonth;
	private Long totalTokensUsed;
	private Long tokenLimit;
	private Long remainingTokens;
	private Double usagePercentage;

	public static UserTokenUsageResponse from(UserTokenUsage usage) {
		long remaining = usage.getRemainingTokens();
		double percentage = (usage.getTotalTokensUsed().doubleValue() / usage.getTokenLimit().doubleValue()) * 100;

		return UserTokenUsageResponse.builder()
			.yearMonth(usage.getYearMonth())
			.totalTokensUsed(usage.getTotalTokensUsed())
			.tokenLimit(usage.getTokenLimit())
			.remainingTokens(remaining)
			.usagePercentage(Math.round(percentage * 100.0) / 100.0)
			.build();
	}
}
