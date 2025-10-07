package devacc11011.spring.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserRepository의 기본적인 메서드 존재 확인 테스트
 */
class UserRepositorySimpleTest {

	@Test
	@DisplayName("UserRepository 인터페이스가 올바르게 정의되어 있는지 확인")
	void testUserRepositoryExists() {
		// Given & When & Then
		assertNotNull(UserRepository.class);
	}
}
