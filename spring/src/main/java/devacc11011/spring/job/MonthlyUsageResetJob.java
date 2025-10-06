package devacc11011.spring.job;

import devacc11011.spring.service.UserTokenUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyUsageResetJob implements Job {

	private final UserTokenUsageService userTokenUsageService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String lastMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

		log.info("Starting monthly usage reset for yearMonth: {}", lastMonth);

		try {
			userTokenUsageService.resetMonthlyUsage(lastMonth);
			log.info("Successfully reset monthly usage for yearMonth: {}", lastMonth);
		} catch (Exception e) {
			log.error("Failed to reset monthly usage for yearMonth: {}", lastMonth, e);
			throw new JobExecutionException(e);
		}
	}
}
