package devacc11011.spring.service;

import devacc11011.spring.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("emailNotificationService")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

	private final EmailService emailService;

	@Value("${spring.mail.username:}")
	private String mailUsername;

	@Override
	public boolean isEnabled() {
		return mailUsername != null && !mailUsername.isEmpty();
	}

	@Override
	public String getNotificationType() {
		return "email";
	}

	@Override
	public void sendTaskCompletionNotification(Task task) {
		if (!isEnabled()) {
			log.warn("Email notification is not enabled. Skipping notification for task: {}", task.getId());
			return;
		}

		// Task에 연결된 사용자의 이메일이 있으면 전송
		String userEmail = task.getUser() != null ? task.getUser().getEmail() : null;
		if (userEmail == null || userEmail.isEmpty()) {
			log.warn("No email address found for user. Skipping notification for task: {}", task.getId());
			return;
		}

		try {
			boolean isSuccess = "COMPLETED".equals(task.getStatus());
			String result = task.getResult() != null ? task.getResult() : "No result available";

			emailService.sendTaskResultEmail(
				userEmail,
				task.getTitle(),
				result,
				isSuccess
			);

			log.info("Email notification sent successfully to {} for task: {}", userEmail, task.getId());
		} catch (Exception e) {
			log.error("Failed to send email notification for task: {}", task.getId(), e);
		}
	}

	/**
	 * 특정 이메일 주소로 알림 전송
	 */
	public void sendTaskCompletionNotification(Task task, String emailAddress) {
		if (!isEnabled()) {
			log.warn("Email notification is not enabled. Skipping notification for task: {}", task.getId());
			return;
		}

		if (emailAddress == null || emailAddress.isEmpty()) {
			log.warn("No email address provided. Skipping notification for task: {}", task.getId());
			return;
		}

		try {
			boolean isSuccess = "COMPLETED".equals(task.getStatus());
			String result = task.getResult() != null ? task.getResult() : "No result available";

			emailService.sendTaskResultEmail(
				emailAddress,
				task.getTitle(),
				result,
				isSuccess
			);

			log.info("Email notification sent successfully to {} for task: {}", emailAddress, task.getId());
		} catch (Exception e) {
			log.error("Failed to send email notification to {} for task: {}", emailAddress, task.getId(), e);
		}
	}
}
