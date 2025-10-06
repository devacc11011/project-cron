package devacc11011.spring.dto;

import devacc11011.spring.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

	private Long id;
	private Long taskId;  // nullable (관리자용만)
	private String title;
	private String prompt;
	private String aiProvider;
	private Boolean enableWebSearch;
	private String cronExpression;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Boolean enabled;
	private LocalDateTime lastExecutedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ScheduleResponse from(Schedule schedule) {
		return ScheduleResponse.builder()
			.id(schedule.getId())
			.taskId(schedule.getTask() != null ? schedule.getTask().getId() : null)
			.title(schedule.getTitle())
			.prompt(schedule.getPrompt())
			.aiProvider(schedule.getAiProvider())
			.enableWebSearch(schedule.getEnableWebSearch())
			.cronExpression(schedule.getCronExpression())
			.startDate(schedule.getStartDate())
			.endDate(schedule.getEndDate())
			.enabled(schedule.getEnabled())
			.lastExecutedAt(schedule.getLastExecutedAt())
			.createdAt(schedule.getCreatedAt())
			.updatedAt(schedule.getUpdatedAt())
			.build();
	}
}
