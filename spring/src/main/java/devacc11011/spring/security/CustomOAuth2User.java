package devacc11011.spring.security;

import devacc11011.spring.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class CustomOAuth2User implements OAuth2User {

	private final User user;
	private final Map<String, Object> attributes;
	private final String registrationId;

	public CustomOAuth2User(User user, Map<String, Object> attributes, String registrationId) {
		this.user = user;
		this.attributes = attributes;
		this.registrationId = registrationId;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
			.collect(Collectors.toList());
	}

	@Override
	public String getName() {
		if ("google".equals(registrationId)) {
			return user.getGoogleId();
		} else if ("discord".equals(registrationId)) {
			return user.getDiscordId();
		}
		return user.getUsername();
	}
}
