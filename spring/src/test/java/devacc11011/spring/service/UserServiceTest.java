package devacc11011.spring.service;

import devacc11011.spring.entity.Role;
import devacc11011.spring.entity.User;
import devacc11011.spring.repository.RoleRepository;
import devacc11011.spring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;
	private Role adminRole;
	private Role userRole;

	@BeforeEach
	void setUp() {
		adminRole = Role.builder()
			.id(1L)
			.name("ADMIN")
			.description("관리자")
			.build();

		userRole = Role.builder()
			.id(2L)
			.name("USER")
			.description("일반 사용자")
			.build();

		testUser = User.builder()
			.id(1L)
			.discordId("123456789")
			.username("testuser")
			.email("test@example.com")
			.avatarUrl("https://example.com/avatar.png")
			.roles(new HashSet<>(Set.of(userRole)))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	@Test
	@DisplayName("디스코드 ID로 사용자 조회 성공")
	void findByDiscordId_Success() {
		// given
		given(userRepository.findByDiscordId("123456789")).willReturn(Optional.of(testUser));

		// when
		User user = userService.findByDiscordId("123456789");

		// then
		assertThat(user.getDiscordId()).isEqualTo("123456789");
		assertThat(user.getUsername()).isEqualTo("testuser");
	}

	@Test
	@DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
	void findByDiscordId_NotFound_ThrowsException() {
		// given
		given(userRepository.findByDiscordId("invalid")).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.findByDiscordId("invalid"))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("User not found");
	}

	@Test
	@DisplayName("모든 사용자 조회 성공")
	void findAllUsers_Success() {
		// given
		given(userRepository.findAll()).willReturn(Arrays.asList(testUser));

		// when
		List<User> users = userService.findAllUsers();

		// then
		assertThat(users).hasSize(1);
		assertThat(users.get(0).getDiscordId()).isEqualTo("123456789");
	}

	@Test
	@DisplayName("사용자에게 역할 추가 성공")
	void addRoleToUser_Success() {
		// given
		given(userRepository.findByDiscordId("123456789")).willReturn(Optional.of(testUser));
		given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(adminRole));
		given(userRepository.save(any(User.class))).willReturn(testUser);

		// when
		User updatedUser = userService.addRoleToUser("123456789", "ADMIN");

		// then
		assertThat(updatedUser.getRoles()).contains(adminRole);
		verify(userRepository, times(1)).save(testUser);
	}

	@Test
	@DisplayName("사용자에게서 역할 제거 성공")
	void removeRoleFromUser_Success() {
		// given
		testUser.getRoles().add(adminRole);
		given(userRepository.findByDiscordId("123456789")).willReturn(Optional.of(testUser));
		given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(adminRole));
		given(userRepository.save(any(User.class))).willReturn(testUser);

		// when
		User updatedUser = userService.removeRoleFromUser("123456789", "ADMIN");

		// then
		assertThat(updatedUser.getRoles()).doesNotContain(adminRole);
		verify(userRepository, times(1)).save(testUser);
	}

	@Test
	@DisplayName("사용자 역할 일괄 업데이트 성공")
	void updateUserRoles_Success() {
		// given
		Set<String> newRoleNames = Set.of("ADMIN", "USER");
		given(userRepository.findByDiscordId("123456789")).willReturn(Optional.of(testUser));
		given(roleRepository.findAll()).willReturn(Arrays.asList(adminRole, userRole));
		given(roleRepository.findAllById(any())).willReturn(Arrays.asList(adminRole, userRole));
		given(userRepository.save(any(User.class))).willReturn(testUser);

		// when
		User updatedUser = userService.updateUserRoles("123456789", newRoleNames);

		// then
		verify(userRepository, times(1)).save(testUser);
	}
}
