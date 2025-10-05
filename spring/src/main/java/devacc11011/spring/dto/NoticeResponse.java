package devacc11011.spring.dto;

import devacc11011.spring.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {

	private Long id;
	private String title;
	private String content;
	private AuthorInfo author;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthorInfo {
		private String discordId;
		private String username;
		private String avatarUrl;
	}

	public static NoticeResponse from(Notice notice) {
		return NoticeResponse.builder()
			.id(notice.getId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.author(AuthorInfo.builder()
				.discordId(notice.getAuthor().getDiscordId())
				.username(notice.getAuthor().getUsername())
				.avatarUrl(notice.getAuthor().getAvatarUrl())
				.build())
			.createdAt(notice.getCreatedAt())
			.updatedAt(notice.getUpdatedAt())
			.build();
	}
}
