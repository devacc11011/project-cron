package devacc11011.spring.security;

import devacc11011.spring.entity.Role;
import devacc11011.spring.entity.User;
import devacc11011.spring.repository.RoleRepository;
import devacc11011.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String discordId = attributes.get("id").toString();
		String username = attributes.get("username").toString();
		String email = attributes.get("email") != null ? attributes.get("email").toString() : null;
		String avatarUrl = buildAvatarUrl(discordId, attributes.get("avatar"));

		User user = userRepository.findByDiscordId(discordId)
			.orElseGet(() -> createNewUser(discordId, username, email, avatarUrl));

		// 기존 유저 정보 업데이트
		user.setUsername(username);
		user.setEmail(email);
		user.setAvatarUrl(avatarUrl);
		userRepository.save(user);

		return new CustomOAuth2User(user, attributes);
	}

	private User createNewUser(String discordId, String username, String email, String avatarUrl) {
		Role userRole = roleRepository.findByName("USER")
			.orElseGet(() -> roleRepository.save(Role.builder().name("USER").description("일반 사용자").build()));

		User user = User.builder()
			.discordId(discordId)
			.username(username)
			.email(email)
			.avatarUrl(avatarUrl)
			.roles(Set.of(userRole))
			.build();

		return userRepository.save(user);
	}

	private String buildAvatarUrl(String userId, Object avatar) {
		if (avatar == null) {
			return null;
		}
		return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", userId, avatar);
	}
}
