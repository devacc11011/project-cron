package devacc11011.spring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 관리자용: Task 연결 (nullable)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id")
	private Task task;

	// 일반 사용자용: 작업 정보 직접 저장
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String prompt;

	@Column(name = "ai_provider", length = 50)
	private String aiProvider;

	@Column(name = "enable_web_search")
	private Boolean enableWebSearch;

	@Column(name = "cron_expression", nullable = false, length = 100)
	private String cronExpression;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Column(nullable = false)
	@Builder.Default
	private Boolean enabled = true;

	@Column(name = "last_executed_at")
	private LocalDateTime lastExecutedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
