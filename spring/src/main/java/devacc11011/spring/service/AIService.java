package devacc11011.spring.service;

public interface AIService {
	String executeTask(String prompt);
	String getProviderName();
	boolean isEnabled();
}
