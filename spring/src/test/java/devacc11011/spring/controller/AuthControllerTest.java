package devacc11011.spring.controller;

import devacc11011.spring.entity.Role;
import devacc11011.spring.entity.User;
import devacc11011.spring.security.CustomOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private User testUser;
	private CustomOAuth2User customOAuth2User;

	@BeforeEach
	void setUp() {
		Role userRole = Role.builder()
			.id(1L)
			.name("USER")
			.description("일반 사용자")
			.build();

		testUser = User.builder()
			.id(1L)
			.discordId("123456789")
			.username("testuser")
			.email("test@example.com")
			.avatarUrl("https://example.com/avatar.png")
			.roles(Set.of(userRole))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", "123456789");
		attributes.put("username", "testuser");
		attributes.put("email", "test@example.com");

		customOAuth2User = new CustomOAuth2User(testUser, attributes);
	}

	@Test
	@DisplayName("인증된 사용자 정보 조회 성공")
	void getCurrentUser_Authenticated_Success() throws Exception {
		mockMvc.perform(get("/api/auth/me")
				.with(oauth2Login().oauth2User(customOAuth2User)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.discordId").value("123456789"))
			.andExpect(jsonPath("$.username").value("testuser"))
			.andExpect(jsonPath("$.email").value("test@example.com"));
	}

	@Test
	@DisplayName("인증되지 않은 사용자 정보 조회 시 401")
	void getCurrentUser_NotAuthenticated_Returns401() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("인증 상태 확인 - 로그인됨")
	void checkAuthStatus_Authenticated_ReturnsTrue() throws Exception {
		mockMvc.perform(get("/api/auth/status")
				.with(oauth2Login().oauth2User(customOAuth2User)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}

	@Test
	@DisplayName("인증 상태 확인 - 로그인 안됨")
	void checkAuthStatus_NotAuthenticated_ReturnsFalse() throws Exception {
		mockMvc.perform(get("/api/auth/status"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(false));
	}
}
