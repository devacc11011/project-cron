package devacc11011.spring.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendTaskResultEmail(String toEmail, String taskTitle, String result, boolean isSuccess) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject(buildSubject(taskTitle, isSuccess));
			helper.setText(buildHtmlContent(taskTitle, result, isSuccess), true);

			mailSender.send(message);
			log.info("Email sent successfully to: {}", toEmail);
		} catch (MessagingException e) {
			log.error("Failed to send email to: {}", toEmail, e);
			throw new RuntimeException("Failed to send email", e);
		}
	}

	private String buildSubject(String taskTitle, boolean isSuccess) {
		String status = isSuccess ? "✅ Success" : "❌ Failed";
		return String.format("[ACron] %s - %s", status, taskTitle);
	}

	private String buildHtmlContent(String taskTitle, String result, boolean isSuccess) {
		String statusColor = isSuccess ? "#10b981" : "#ef4444";
		String statusText = isSuccess ? "Successfully Completed" : "Execution Failed";
		String statusIcon = isSuccess ? "✅" : "❌";

		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8">
			    <meta name="viewport" content="width=device-width, initial-scale=1.0">
			</head>
			<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #0a0a0a;">
			    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
			        <!-- Header -->
			        <div style="text-align: center; padding: 30px 0;">
			            <h1 style="color: #ffffff; font-size: 28px; margin: 0; font-weight: 700;">
			                ACron Scheduler
			            </h1>
			            <p style="color: #71717a; margin: 10px 0 0 0; font-size: 14px;">
			                Task Execution Report
			            </p>
			        </div>

			        <!-- Content Card -->
			        <div style="background: linear-gradient(to bottom, #18181b, #09090b); border: 1px solid #27272a; border-radius: 12px; padding: 30px; margin: 20px 0;">
			            <!-- Status Badge -->
			            <div style="text-align: center; margin-bottom: 30px;">
			                <div style="display: inline-block; padding: 8px 20px; background-color: %s; border-radius: 20px;">
			                    <span style="color: #ffffff; font-size: 14px; font-weight: 600;">
			                        %s %s
			                    </span>
			                </div>
			            </div>

			            <!-- Task Title -->
			            <div style="margin-bottom: 25px;">
			                <h2 style="color: #a1a1aa; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin: 0 0 8px 0;">
			                    Task Name
			                </h2>
			                <p style="color: #ffffff; font-size: 18px; font-weight: 600; margin: 0;">
			                    %s
			                </p>
			            </div>

			            <!-- Divider -->
			            <div style="height: 1px; background-color: #27272a; margin: 25px 0;"></div>

			            <!-- Result -->
			            <div>
			                <h2 style="color: #a1a1aa; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin: 0 0 12px 0;">
			                    Execution Result
			                </h2>
			                <div style="background-color: #09090b; border: 1px solid #27272a; border-radius: 8px; padding: 16px; max-height: 400px; overflow-y: auto;">
			                    <pre style="color: #e4e4e7; font-family: 'Courier New', monospace; font-size: 13px; line-height: 1.6; margin: 0; white-space: pre-wrap; word-wrap: break-word;">%s</pre>
			                </div>
			            </div>
			        </div>

			        <!-- Footer -->
			        <div style="text-align: center; padding: 20px 0;">
			            <p style="color: #52525b; font-size: 12px; margin: 0;">
			                This is an automated notification from ACron Scheduler
			            </p>
			            <p style="color: #52525b; font-size: 12px; margin: 5px 0 0 0;">
			                <a href="https://acron.lisan-al-gaib.top" style="color: #6366f1; text-decoration: none;">Visit ACron</a>
			            </p>
			        </div>
			    </div>
			</body>
			</html>
			""",
			statusColor,
			statusIcon,
			statusText,
			escapeHtml(taskTitle),
			escapeHtml(result)
		);
	}

	private String escapeHtml(String text) {
		if (text == null) {
			return "";
		}
		return text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;");
	}
}
