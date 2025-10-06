package devacc11011.spring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String prompt;

	@Column(columnDefinition = "TEXT")
	private String result;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskStatus status;

	@Column(name = "ai_provider", nullable = false, length = 50)
	@Builder.Default
	private String aiProvider = "gemini";

	@Column(name = "enable_web_search")
	@Builder.Default
	private Boolean enableWebSearch = false;

	@Column(name = "tokens_used")
	private Long tokensUsed;

	@Column(name = "notification_type", length = 50)
	@Builder.Default
	private String notificationType = "discord";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "executed_at")
	private LocalDateTime executedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum TaskStatus {
		PENDING,
		PROCESSING,
		COMPLETED,
		FAILED
	}
}
