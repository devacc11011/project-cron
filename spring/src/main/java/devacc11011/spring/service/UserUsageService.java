package devacc11011.spring.service;

import devacc11011.spring.entity.User;
import devacc11011.spring.entity.UserUsage;
import devacc11011.spring.repository.UserUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUsageService {

	private final UserUsageRepository userUsageRepository;

	private static final long DEFAULT_TOKEN_LIMIT = 120000L; // 월 12만 토큰

	@Transactional
	public UserUsage getOrCreateCurrentMonthUsage(User user) {
		String currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

		return userUsageRepository.findByUserAndYearMonth(user, currentYearMonth)
			.orElseGet(() -> {
				UserUsage newUsage = UserUsage.builder()
					.user(user)
					.yearMonth(currentYearMonth)
					.totalTokensUsed(0L)
					.tokenLimit(DEFAULT_TOKEN_LIMIT)
					.build();
				return userUsageRepository.save(newUsage);
			});
	}

	@Transactional(readOnly = true)
	public boolean hasTokensAvailable(User user, long tokensNeeded) {
		UserUsage usage = getOrCreateCurrentMonthUsage(user);
		return usage.hasTokensAvailable(tokensNeeded);
	}

	@Transactional(readOnly = true)
	public long getRemainingTokens(User user) {
		UserUsage usage = getOrCreateCurrentMonthUsage(user);
		return usage.getRemainingTokens();
	}

	@Transactional
	public void addTokensUsed(User user, long tokensUsed) {
		UserUsage usage = getOrCreateCurrentMonthUsage(user);
		usage.addTokensUsed(tokensUsed);
		userUsageRepository.save(usage);
		log.info("User {} used {} tokens. Total: {}/{}",
			user.getUsername(), tokensUsed, usage.getTotalTokensUsed(), usage.getTokenLimit());
	}

	@Transactional
	public void resetMonthlyUsage(String yearMonth) {
		userUsageRepository.deleteByYearMonth(yearMonth);
		log.info("Reset usage for yearMonth: {}", yearMonth);
	}

	@Transactional(readOnly = true)
	public UserUsage getCurrentMonthUsage(User user) {
		return getOrCreateCurrentMonthUsage(user);
	}
}
