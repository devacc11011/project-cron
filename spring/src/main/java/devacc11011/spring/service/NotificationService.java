package devacc11011.spring.service;

import devacc11011.spring.entity.Task;

public interface NotificationService {

	/**
	 * Task 완료 알림 전송
	 */
	void sendTaskCompletionNotification(Task task);

	/**
	 * 알림 서비스 타입 반환
	 */
	String getNotificationType();

	/**
	 * 알림 서비스 활성화 여부
	 */
	boolean isEnabled();
}
