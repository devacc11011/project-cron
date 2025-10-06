package devacc11011.spring.config;

import devacc11011.spring.job.MonthlyUsageResetJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class QuartzConfig {

	private final Scheduler scheduler;

	@PostConstruct
	public void init() {
		try {
			// 월별 사용량 리셋 스케줄러 (매월 1일 00:00)
			JobDetail monthlyResetJob = JobBuilder.newJob(MonthlyUsageResetJob.class)
				.withIdentity("monthlyUsageResetJob", "usage")
				.storeDurably()
				.build();

			Trigger monthlyResetTrigger = TriggerBuilder.newTrigger()
				.withIdentity("monthlyUsageResetTrigger", "usage")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 * ?"))
				.build();

			if (!scheduler.checkExists(monthlyResetJob.getKey())) {
				scheduler.scheduleJob(monthlyResetJob, monthlyResetTrigger);
				log.info("Monthly usage reset scheduler initialized");
			}
		} catch (SchedulerException e) {
			log.error("Failed to initialize Quartz schedulers", e);
		}
	}
}
