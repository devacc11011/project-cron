package devacc11011.spring.service;

import devacc11011.spring.entity.Role;
import devacc11011.spring.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

	private final RoleRepository roleRepository;

	public List<Role> findAllRoles() {
		return roleRepository.findAll();
	}

	public Role findByName(String name) {
		return roleRepository.findByName(name)
			.orElseThrow(() -> new RuntimeException("Role not found: " + name));
	}

	@Transactional
	public Role createRole(String name, String description) {
		if (roleRepository.existsByName(name)) {
			throw new RuntimeException("Role already exists: " + name);
		}

		Role role = Role.builder()
			.name(name)
			.description(description)
			.build();

		return roleRepository.save(role);
	}

	@Transactional
	public void initializeDefaultRoles() {
		if (!roleRepository.existsByName("ADMIN")) {
			createRole("ADMIN", "관리자");
		}
		if (!roleRepository.existsByName("USER")) {
			createRole("USER", "일반 사용자");
		}
		if (!roleRepository.existsByName("APPROVE")) {
			createRole("APPROVE", "승인 권한");
		}
	}
}
