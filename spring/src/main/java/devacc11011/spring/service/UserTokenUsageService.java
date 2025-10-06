package devacc11011.spring.service;

import devacc11011.spring.entity.User;
import devacc11011.spring.entity.UserTokenUsage;
import devacc11011.spring.repository.UserTokenUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTokenUsageService {

	private final UserTokenUsageRepository userTokenUsageRepository;

	private static final long DEFAULT_TOKEN_LIMIT = 120000L; // 월 12만 토큰

	@Transactional
	public UserTokenUsage getOrCreateCurrentMonthUsage(User user) {
		String currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

		return userTokenUsageRepository.findByUserAndYearMonth(user, currentYearMonth)
			.orElseGet(() -> {
				UserTokenUsage newUsage = UserTokenUsage.builder()
					.user(user)
					.yearMonth(currentYearMonth)
					.totalTokensUsed(0L)
					.tokenLimit(DEFAULT_TOKEN_LIMIT)
					.build();
				return userTokenUsageRepository.save(newUsage);
			});
	}

	@Transactional
	public boolean hasTokensAvailable(User user, long tokensNeeded) {
		UserTokenUsage usage = getOrCreateCurrentMonthUsage(user);
		return usage.hasTokensAvailable(tokensNeeded);
	}

	@Transactional
	public long getRemainingTokens(User user) {
		UserTokenUsage usage = getOrCreateCurrentMonthUsage(user);
		return usage.getRemainingTokens();
	}

	@Transactional
	public void addTokensUsed(User user, long tokensUsed) {
		UserTokenUsage usage = getOrCreateCurrentMonthUsage(user);
		usage.addTokensUsed(tokensUsed);
		userTokenUsageRepository.save(usage);
		log.info("User {} used {} tokens. Total: {}/{}",
			user.getUsername(), tokensUsed, usage.getTotalTokensUsed(), usage.getTokenLimit());
	}

	@Transactional
	public void resetMonthlyUsage(String yearMonth) {
		userTokenUsageRepository.deleteByYearMonth(yearMonth);
		log.info("Reset usage for yearMonth: {}", yearMonth);
	}

	@Transactional
	public UserTokenUsage getCurrentMonthUsage(User user) {
		return getOrCreateCurrentMonthUsage(user);
	}
}
