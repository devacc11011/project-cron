package devacc11011.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActuatorHealthTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void 헬스체크_엔드포인트_정상_동작() throws Exception {
		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk());
	}

	@Test
	public void 헬스체크_엔드포인트_인증없이_접근_가능() throws Exception {
		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk());
	}
}
