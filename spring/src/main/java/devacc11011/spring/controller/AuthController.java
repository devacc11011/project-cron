package devacc11011.spring.controller;

import devacc11011.spring.dto.UserResponse;
import devacc11011.spring.exception.UnauthorizedException;
import devacc11011.spring.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	@GetMapping("/me")
	public UserResponse getCurrentUser(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		if (oAuth2User == null) {
			throw new UnauthorizedException();
		}

		return UserResponse.from(oAuth2User.getUser());
	}

	@GetMapping("/status")
	public Boolean checkAuthStatus(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		return oAuth2User != null;
	}
}
