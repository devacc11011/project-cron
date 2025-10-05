package devacc11011.spring.service;

import devacc11011.spring.entity.Role;
import devacc11011.spring.entity.User;
import devacc11011.spring.repository.RoleRepository;
import devacc11011.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public User findByDiscordId(String discordId) {
		return userRepository.findByDiscordId(discordId)
			.orElseThrow(() -> new RuntimeException("User not found with discordId: " + discordId));
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	@Transactional
	public User addRoleToUser(String discordId, String roleName) {
		User user = findByDiscordId(discordId);
		Role role = roleRepository.findByName(roleName)
			.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

		user.getRoles().add(role);
		return userRepository.save(user);
	}

	@Transactional
	public User removeRoleFromUser(String discordId, String roleName) {
		User user = findByDiscordId(discordId);
		Role role = roleRepository.findByName(roleName)
			.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

		user.getRoles().remove(role);
		return userRepository.save(user);
	}

	@Transactional
	public User updateUserRoles(String discordId, Set<String> roleNames) {
		User user = findByDiscordId(discordId);
		Set<Role> roles = roleRepository.findAllById(
			roleRepository.findAll().stream()
				.filter(role -> roleNames.contains(role.getName()))
				.map(Role::getId)
				.toList()
		).stream().collect(java.util.stream.Collectors.toSet());

		user.setRoles(roles);
		return userRepository.save(user);
	}
}
