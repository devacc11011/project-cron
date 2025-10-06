package devacc11011.spring.controller;

import devacc11011.spring.dto.UserTokenUsageResponse;
import devacc11011.spring.entity.UserTokenUsage;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.UserTokenUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UserTokenUsageController {

	private final UserTokenUsageService userTokenUsageService;

	@GetMapping("/current")
	public UserTokenUsageResponse getCurrentUsage(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		UserTokenUsage usage = userTokenUsageService.getCurrentMonthUsage(oAuth2User.getUser());
		return UserTokenUsageResponse.from(usage);
	}
}
