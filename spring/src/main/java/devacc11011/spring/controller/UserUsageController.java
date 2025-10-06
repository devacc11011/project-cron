package devacc11011.spring.controller;

import devacc11011.spring.dto.UserUsageResponse;
import devacc11011.spring.entity.UserUsage;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.UserUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UserUsageController {

	private final UserUsageService userUsageService;

	@GetMapping("/current")
	public UserUsageResponse getCurrentUsage(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		UserUsage usage = userUsageService.getCurrentMonthUsage(oAuth2User.getUser());
		return UserUsageResponse.from(usage);
	}
}
