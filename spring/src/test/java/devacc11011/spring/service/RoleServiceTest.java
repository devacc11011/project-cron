package devacc11011.spring.service;

import devacc11011.spring.entity.Role;
import devacc11011.spring.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private RoleService roleService;

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
	}

	@Test
	@DisplayName("모든 역할 조회 성공")
	void findAllRoles_Success() {
		// given
		given(roleRepository.findAll()).willReturn(Arrays.asList(adminRole, userRole));

		// when
		List<Role> roles = roleService.findAllRoles();

		// then
		assertThat(roles).hasSize(2);
		assertThat(roles).containsExactly(adminRole, userRole);
	}

	@Test
	@DisplayName("역할 이름으로 조회 성공")
	void findByName_Success() {
		// given
		given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(adminRole));

		// when
		Role role = roleService.findByName("ADMIN");

		// then
		assertThat(role.getName()).isEqualTo("ADMIN");
		assertThat(role.getDescription()).isEqualTo("관리자");
	}

	@Test
	@DisplayName("존재하지 않는 역할 조회 시 예외 발생")
	void findByName_NotFound_ThrowsException() {
		// given
		given(roleRepository.findByName("INVALID")).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> roleService.findByName("INVALID"))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Role not found");
	}

	@Test
	@DisplayName("새로운 역할 생성 성공")
	void createRole_Success() {
		// given
		Role newRole = Role.builder()
			.name("APPROVE")
			.description("승인 권한")
			.build();

		given(roleRepository.existsByName("APPROVE")).willReturn(false);
		given(roleRepository.save(any(Role.class))).willReturn(newRole);

		// when
		Role createdRole = roleService.createRole("APPROVE", "승인 권한");

		// then
		assertThat(createdRole.getName()).isEqualTo("APPROVE");
		verify(roleRepository, times(1)).save(any(Role.class));
	}

	@Test
	@DisplayName("중복된 역할 생성 시 예외 발생")
	void createRole_Duplicate_ThrowsException() {
		// given
		given(roleRepository.existsByName("ADMIN")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> roleService.createRole("ADMIN", "관리자"))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Role already exists");
	}

	@Test
	@DisplayName("기본 역할 초기화")
	void initializeDefaultRoles_Success() {
		// given
		given(roleRepository.existsByName("ADMIN")).willReturn(false);
		given(roleRepository.existsByName("USER")).willReturn(false);
		given(roleRepository.existsByName("APPROVE")).willReturn(false);
		given(roleRepository.save(any(Role.class))).willReturn(new Role());

		// when
		roleService.initializeDefaultRoles();

		// then
		verify(roleRepository, times(3)).save(any(Role.class));
	}
}
