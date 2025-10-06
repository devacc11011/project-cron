package devacc11011.spring.service;

import devacc11011.spring.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("emailNotificationService")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

	@Override
	public boolean isEnabled() {
		// TODO: 이메일 설정 확인
		return false;
	}

	@Override
	public String getNotificationType() {
		return "email";
	}

	@Override
	public void sendTaskCompletionNotification(Task task) {
		// TODO: 이메일 전송 구현
		log.info("Email notification not yet implemented for task: {}", task.getId());
	}
}
