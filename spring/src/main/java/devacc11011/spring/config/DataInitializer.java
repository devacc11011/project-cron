package devacc11011.spring.config;

import devacc11011.spring.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final RoleService roleService;

	@Override
	public void run(String... args) {
		roleService.initializeDefaultRoles();
	}
}
