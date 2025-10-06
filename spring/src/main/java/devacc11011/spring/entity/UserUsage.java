package devacc11011.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUsage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "year_month", nullable = false, length = 7)
	private String yearMonth; // "2025-10" 형식

	@Column(name = "total_tokens_used", nullable = false)
	@Builder.Default
	private Long totalTokensUsed = 0L;

	@Column(name = "token_limit", nullable = false)
	@Builder.Default
	private Long tokenLimit = 120000L; // 기본 월 12만 토큰

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
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

	public boolean hasTokensAvailable(long tokensNeeded) {
		return totalTokensUsed + tokensNeeded <= tokenLimit;
	}

	public long getRemainingTokens() {
		return Math.max(0, tokenLimit - totalTokensUsed);
	}

	public void addTokensUsed(long tokens) {
		this.totalTokensUsed += tokens;
	}
}
