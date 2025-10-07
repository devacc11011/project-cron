package devacc11011.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);
		app.addInitializers(ctx -> {
			Environment env = ctx.getEnvironment();
			log.info("========================================");
			log.info("Loaded Environment Variables:");
			log.info("========================================");
			log.info("SPRING_PROFILES_ACTIVE: {}", env.getProperty("spring.profiles.active"));
			log.info("DB_HOST: {}", env.getProperty("DB_HOST"));
			log.info("DB_PORT: {}", env.getProperty("DB_PORT"));
			log.info("DB_NAME: {}", env.getProperty("DB_NAME"));
			log.info("DB_USERNAME: {}", env.getProperty("DB_USERNAME"));
			log.info("DISCORD_CLIENT_ID: {}", maskSecret(env.getProperty("DISCORD_CLIENT_ID")));
			log.info("DISCORD_CLIENT_SECRET: {}", maskSecret(env.getProperty("DISCORD_CLIENT_SECRET")));
			log.info("DISCORD_BOT_TOKEN: {}", maskSecret(env.getProperty("DISCORD_BOT_TOKEN")));
			log.info("GEMINI_API_KEY: {}", maskSecret(env.getProperty("GEMINI_API_KEY")));
			log.info("CLAUDE_API_KEY: {}", maskSecret(env.getProperty("CLAUDE_API_KEY")));
			log.info("OPENAI_API_KEY: {}", maskSecret(env.getProperty("OPENAI_API_KEY")));
			log.info("API_BASE_URL: {}", env.getProperty("API_BASE_URL"));
			log.info("FRONTEND_URL: {}", env.getProperty("FRONTEND_URL"));
			log.info("========================================");
		});
		app.run(args);
	}

	private static String maskSecret(String value) {
		if (value == null || value.isEmpty()) {
			return "NOT_SET";
		}
		if (value.length() <= 8) {
			return "****";
		}
		return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
	}

}
