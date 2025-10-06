package devacc11011.spring.service;

import devacc11011.spring.dto.AIResponse;

public interface AIService {
	AIResponse executeTask(String prompt);
	AIResponse executeTaskWithWebSearch(String prompt);
	String getProviderName();
	boolean isEnabled();
}
