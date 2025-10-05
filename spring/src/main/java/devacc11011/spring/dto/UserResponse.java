package devacc11011.spring.dto;

import devacc11011.spring.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private String discordId;
	private String username;
	private String email;
	private String avatarUrl;
	private Set<String> roles;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static UserResponse from(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.discordId(user.getDiscordId())
			.username(user.getUsername())
			.email(user.getEmail())
			.avatarUrl(user.getAvatarUrl())
			.roles(user.getRoles().stream()
				.map(role -> role.getName())
				.collect(Collectors.toSet()))
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.build();
	}
}
