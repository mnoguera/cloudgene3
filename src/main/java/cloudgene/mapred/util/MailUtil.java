package cloudgene.mapred.util;

import java.util.Map;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Email utility class with support for multiple email providers (SMTP, AWS SES).
 * 
 * The provider is selected based on the "provider" setting in Settings.mail configuration.
 * Default provider is SMTP for backward compatibility.
 */
public class MailUtil {

	private static final Logger log = LoggerFactory.getLogger(MailUtil.class);

	/**
	 * Get the configured email provider based on settings.
	 * 
	 * @param settings application settings
	 * @return configured EmailProvider instance
	 * @throws Exception if provider configuration is invalid
	 */
	private static EmailProvider getProvider(Settings settings) throws Exception {
		Map<String, String> mailConfig = settings.getMail();
		String provider = mailConfig.getOrDefault("provider", "smtp");
		
		switch (provider.toLowerCase()) {
			case "aws-ses":
			case "ses":
				return createAwsSesProvider(mailConfig);
			case "smtp":
			default:
				return createSmtpProvider(mailConfig);
		}
	}
	
	/**
	 * Create SMTP email provider from settings.
	 */
	private static EmailProvider createSmtpProvider(Map<String, String> mailConfig) {
		String smtp = mailConfig.get("smtp");
		String port = mailConfig.get("port");
		String username = mailConfig.get("user");
		String password = mailConfig.get("password");
		String fromAddress = mailConfig.get("name");
		
		return new SmtpEmailProvider(smtp, port, username, password, fromAddress);
	}
	
	/**
	 * Create AWS SES email provider from settings.
	 */
	private static EmailProvider createAwsSesProvider(Map<String, String> mailConfig) throws Exception {
		String region = mailConfig.get("aws-ses-region");
		String fromAddress = mailConfig.get("aws-ses-from");
		String configurationSet = mailConfig.get("aws-ses-configuration-set");
		
		if (region == null || region.isEmpty()) {
			throw new Exception("AWS SES region not configured (aws-ses-region)");
		}
		if (fromAddress == null || fromAddress.isEmpty()) {
			throw new Exception("AWS SES from address not configured (aws-ses-from)");
		}
		
		return new AwsSesEmailProvider(region, fromAddress, configurationSet);
	}

	public static void notifyAdmin(Settings settings, String subject, String text) throws Exception {

		if (settings.getAdminMail() != null && !settings.getAdminMail().isEmpty()) {
			EmailProvider provider = getProvider(settings);
			provider.send(settings.getAdminMail(), subject, text);
		}

	}

	public static void send(Settings settings, String tos, String subject, String text) throws Exception {
		EmailProvider provider = getProvider(settings);
		provider.send(tos, subject, text);
	}

	/**
	 * Legacy method maintained for backward compatibility.
	 * Directly sends email via SMTP without using provider abstraction.
	 * 
	 * @deprecated Use send(Settings, String, String, String) instead
	 */
	@Deprecated
	public static void send(final String smtp, final String port, final String username, final String password,
			final String name, String tos, String subject, String text) throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.host", smtp);
		props.put("mail.smtp.port", port);

		Session session = null;

		if (username != null && !username.isEmpty()) {

			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			session = Session.getInstance(props, new jakarta.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {

			session = Session.getInstance(props);

		}

		try {

			InternetAddress[] addresses = InternetAddress.parse(tos);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(name));
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(subject);
			message.setText(text);

			Transport.send(message);

			log.debug("E-Mail sent to " + tos + ".");

		} catch (MessagingException e) {
			throw new Exception("mail could not be sent: " + e.getMessage());
		}
	}
}
