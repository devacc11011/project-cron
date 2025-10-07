package devacc11011.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

	private String title;
	private String prompt;
	private String aiProvider;
	private Boolean enableWebSearch;
	private String notificationType;
}
