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

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		Map<String, Object> attributes = oAuth2User.getAttributes();

		User user;
		if ("google".equals(registrationId)) {
			user = processGoogleUser(attributes);
		} else if ("discord".equals(registrationId)) {
			user = processDiscordUser(attributes);
		} else {
			throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
		}

		return new CustomOAuth2User(user, attributes, registrationId);
	}

	private User processGoogleUser(Map<String, Object> attributes) {
		String googleId = attributes.get("sub").toString();
		String email = attributes.get("email") != null ? attributes.get("email").toString() : null;
		String name = attributes.get("name") != null ? attributes.get("name").toString() : "Google User";
		String picture = attributes.get("picture") != null ? attributes.get("picture").toString() : null;

		User user = userRepository.findByGoogleId(googleId)
			.orElseGet(() -> createNewUser(null, googleId, name, email, picture));

		// 기존 유저 정보 업데이트
		user.setUsername(name);
		user.setEmail(email);
		user.setAvatarUrl(picture);
		userRepository.save(user);

		return user;
	}

	private User processDiscordUser(Map<String, Object> attributes) {
		String discordId = attributes.get("id").toString();
		String username = attributes.get("username").toString();
		String email = attributes.get("email") != null ? attributes.get("email").toString() : null;
		String avatarUrl = buildDiscordAvatarUrl(discordId, attributes.get("avatar"));

		User user = userRepository.findByDiscordId(discordId)
			.orElseGet(() -> createNewUser(discordId, null, username, email, avatarUrl));

		// 기존 유저 정보 업데이트
		user.setUsername(username);
		user.setEmail(email);
		user.setAvatarUrl(avatarUrl);
		userRepository.save(user);

		return user;
	}

	private User createNewUser(String discordId, String googleId, String username, String email, String avatarUrl) {
		// 첫 번째 사용자인지 확인
		boolean isFirstUser = userRepository.count() == 0;

		Role userRole = roleRepository.findByName("USER")
			.orElseGet(() -> roleRepository.save(Role.builder().name("USER").description("일반 사용자").build()));

		Set<Role> roles = Set.of(userRole);

		// 첫 번째 사용자라면 ADMIN 권한 추가
		if (isFirstUser) {
			Role adminRole = roleRepository.findByName("ADMIN")
				.orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").description("관리자").build()));
			roles = Set.of(userRole, adminRole);
		}

		User user = User.builder()
			.discordId(discordId)
			.googleId(googleId)
			.username(username)
			.email(email)
			.avatarUrl(avatarUrl)
			.roles(roles)
			.build();

		return userRepository.save(user);
	}

	private String buildDiscordAvatarUrl(String userId, Object avatar) {
		if (avatar == null) {
			return null;
		}
		return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", userId, avatar);
	}
}
