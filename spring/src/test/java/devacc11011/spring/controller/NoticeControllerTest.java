package devacc11011.spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import devacc11011.spring.dto.NoticeRequest;
import devacc11011.spring.entity.Notice;
import devacc11011.spring.entity.Role;
import devacc11011.spring.entity.User;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
@Import(TestSecurityConfig.class)
class NoticeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NoticeService noticeService;

	private User testUser;
	private User adminUser;
	private Notice testNotice;
	private CustomOAuth2User adminOAuth2User;

	@BeforeEach
	void setUp() {
		Role userRole = Role.builder()
			.id(1L)
			.name("USER")
			.description("일반 사용자")
			.build();

		Role adminRole = Role.builder()
			.id(2L)
			.name("ADMIN")
			.description("관리자")
			.build();

		testUser = User.builder()
			.id(1L)
			.discordId("123456789")
			.username("testuser")
			.email("test@example.com")
			.avatarUrl("https://example.com/avatar.png")
			.roles(Set.of(userRole))
			.build();

		adminUser = User.builder()
			.id(2L)
			.discordId("987654321")
			.username("adminuser")
			.email("admin@example.com")
			.avatarUrl("https://example.com/admin-avatar.png")
			.roles(Set.of(adminRole))
			.build();

		testNotice = Notice.builder()
			.id(1L)
			.title("Test Notice")
			.content("Test Content")
			.author(adminUser)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", "987654321");
		attributes.put("username", "adminuser");
		attributes.put("email", "admin@example.com");

		adminOAuth2User = new CustomOAuth2User(adminUser, attributes, "discord");
	}

	@Test
	@DisplayName("모든 공지사항 조회")
	void getAllNotices() throws Exception {
		when(noticeService.findAllNotices()).thenReturn(List.of(testNotice));

		mockMvc.perform(get("/api/notices"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("Test Notice"))
			.andExpect(jsonPath("$[0].content").value("Test Content"));

		verify(noticeService, times(1)).findAllNotices();
	}

	@Test
	@DisplayName("ID로 공지사항 조회")
	void getNoticeById() throws Exception {
		when(noticeService.findById(1L)).thenReturn(testNotice);

		mockMvc.perform(get("/api/notices/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.title").value("Test Notice"));

		verify(noticeService, times(1)).findById(1L);
	}

	@Test
	@DisplayName("관리자 - 공지사항 생성")
	void createNotice_AsAdmin() throws Exception {
		NoticeRequest request = NoticeRequest.builder()
			.title("New Notice")
			.content("New Content")
			.build();

		when(noticeService.createNotice(any(NoticeRequest.class), any(User.class)))
			.thenReturn(testNotice);

		mockMvc.perform(post("/api/notices")
				.with(oauth2Login().oauth2User(adminOAuth2User))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("Test Notice"));

		verify(noticeService, times(1)).createNotice(any(NoticeRequest.class), any(User.class));
	}

	@Test
	@DisplayName("일반 사용자 - 공지사항 생성 시 403")
	void createNotice_AsUser_Returns403() throws Exception {
		NoticeRequest request = NoticeRequest.builder()
			.title("New Notice")
			.content("New Content")
			.build();

		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("id", "123456789");
		userAttributes.put("username", "testuser");
		userAttributes.put("email", "test@example.com");

		CustomOAuth2User userOAuth2User = new CustomOAuth2User(testUser, userAttributes, "discord");

		mockMvc.perform(post("/api/notices")
				.with(oauth2Login().oauth2User(userOAuth2User))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());

		verify(noticeService, never()).createNotice(any(NoticeRequest.class), any(User.class));
	}

	@Test
	@DisplayName("관리자 - 공지사항 수정")
	void updateNotice_AsAdmin() throws Exception {
		NoticeRequest request = NoticeRequest.builder()
			.title("Updated Notice")
			.content("Updated Content")
			.build();

		Notice updatedNotice = Notice.builder()
			.id(1L)
			.title("Updated Notice")
			.content("Updated Content")
			.author(adminUser)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		when(noticeService.updateNotice(eq(1L), any(NoticeRequest.class)))
			.thenReturn(updatedNotice);

		mockMvc.perform(put("/api/notices/1")
				.with(oauth2Login().oauth2User(adminOAuth2User))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Updated Notice"));

		verify(noticeService, times(1)).updateNotice(eq(1L), any(NoticeRequest.class));
	}

	@Test
	@DisplayName("관리자 - 공지사항 삭제")
	void deleteNotice_AsAdmin() throws Exception {
		doNothing().when(noticeService).deleteNotice(1L);

		mockMvc.perform(delete("/api/notices/1")
				.with(oauth2Login().oauth2User(adminOAuth2User)))
			.andExpect(status().isNoContent());

		verify(noticeService, times(1)).deleteNotice(1L);
	}

	@Test
	@DisplayName("일반 사용자 - 공지사항 삭제 시 403")
	void deleteNotice_AsUser_Returns403() throws Exception {
		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("id", "123456789");
		userAttributes.put("username", "testuser");
		userAttributes.put("email", "test@example.com");

		CustomOAuth2User userOAuth2User = new CustomOAuth2User(testUser, userAttributes, "discord");

		mockMvc.perform(delete("/api/notices/1")
				.with(oauth2Login().oauth2User(userOAuth2User)))
			.andExpect(status().isForbidden());

		verify(noticeService, never()).deleteNotice(1L);
	}
}
