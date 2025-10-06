package devacc11011.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

	// 관리자용 (Task 연결)
	private Long taskId;

	// 일반 사용자용 (독립 스케줄)
	private String title;
	private String prompt;
	private String aiProvider;
	private Boolean enableWebSearch;

	// 공통
	private String cronExpression;
	private String startDate; // ISO 8601 format
	private String endDate;   // ISO 8601 format
}
