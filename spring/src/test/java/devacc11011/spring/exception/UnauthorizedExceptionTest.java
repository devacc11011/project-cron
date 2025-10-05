package devacc11011.spring.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UnauthorizedExceptionTest {

	@Test
	@DisplayName("UnauthorizedException은 401 상태 코드를 가져야 함")
	void shouldHaveUnauthorizedStatus() {
		ResponseStatus responseStatus = UnauthorizedException.class.getAnnotation(ResponseStatus.class);
		assertThat(responseStatus).isNotNull();
		assertThat(responseStatus.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DisplayName("기본 메시지로 예외 생성")
	void createWithDefaultMessage() {
		UnauthorizedException exception = new UnauthorizedException();
		assertThat(exception.getMessage()).isEqualTo("Unauthorized");
	}

	@Test
	@DisplayName("커스텀 메시지로 예외 생성")
	void createWithCustomMessage() {
		String customMessage = "Custom unauthorized message";
		UnauthorizedException exception = new UnauthorizedException(customMessage);
		assertThat(exception.getMessage()).isEqualTo(customMessage);
	}
}
