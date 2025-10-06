package devacc11011.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationServiceFactory {

	private final Map<String, NotificationService> notificationServices;

	public NotificationService getNotificationService(String type) {
		if (type == null || type.isEmpty()) {
			type = "discord"; // 기본값
		}

		String serviceBeanName = type + "NotificationService";
		NotificationService service = notificationServices.get(serviceBeanName);

		if (service == null) {
			throw new IllegalArgumentException("Unknown notification service type: " + type);
		}

		return service;
	}
}
