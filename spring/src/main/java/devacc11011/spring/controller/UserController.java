package devacc11011.spring.controller;

import devacc11011.spring.dto.RoleRequest;
import devacc11011.spring.dto.UserResponse;
import devacc11011.spring.entity.User;
import devacc11011.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserResponse> getAllUsers() {
		List<User> users = userService.findAllUsers();
		return users.stream()
			.map(UserResponse::from)
			.collect(Collectors.toList());
	}

	@GetMapping("/{discordId}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse getUserByDiscordId(@PathVariable String discordId) {
		User user = userService.findByDiscordId(discordId);
		return UserResponse.from(user);
	}

	@PutMapping("/{discordId}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse updateUserRoles(
		@PathVariable String discordId,
		@RequestBody RoleRequest request
	) {
		User user = userService.updateUserRoles(discordId, request.getRoles());
		return UserResponse.from(user);
	}

	@PostMapping("/{discordId}/roles/{roleName}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse addRoleToUser(
		@PathVariable String discordId,
		@PathVariable String roleName
	) {
		User user = userService.addRoleToUser(discordId, roleName);
		return UserResponse.from(user);
	}

	@DeleteMapping("/{discordId}/roles/{roleName}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse removeRoleFromUser(
		@PathVariable String discordId,
		@PathVariable String roleName
	) {
		User user = userService.removeRoleFromUser(discordId, roleName);
		return UserResponse.from(user);
	}
}
