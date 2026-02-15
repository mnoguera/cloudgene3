package cloudgene.mapred.util;

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

public class MailUtil {

	private static final Logger log = LoggerFactory.getLogger(MailUtil.class);

	public static void notifyAdmin(Settings settings, String subject, String text) throws Exception {

		if (settings.getAdminMail() != null && !settings.getAdminMail().isEmpty()) {

			send(settings, settings.getAdminMail(), subject, text);
		}

	}

	public static void send(Settings settings, String tos, String subject, String text) throws Exception {

		EmailProvider provider = getProvider(settings);
		provider.send(tos, subject, text);

	}

	/**
	 * Legacy method for backward compatibility - uses SMTP directly
	 */
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

	/**
	 * Factory method to create the appropriate email provider based on settings
	 */
	private static EmailProvider getProvider(Settings settings) {
		String provider = settings.getMailProvider();
		
		if (provider == null) {
			provider = "smtp"; // default to SMTP for backward compatibility
		}

		if ("aws-ses".equalsIgnoreCase(provider) || "ses".equalsIgnoreCase(provider)) {
			// AWS SES provider
			if (settings.getAwsSes() == null) {
				log.warn("AWS SES provider selected but no awsSes configuration found. Falling back to SMTP.");
				return createSmtpProvider(settings);
			}
			
			String region = settings.getAwsSes().get("region");
			String fromAddress = settings.getAwsSes().get("from");
			String configurationSet = settings.getAwsSes().get("configuration-set");
			
			if (region == null || fromAddress == null) {
				log.warn("AWS SES provider requires 'region' and 'from' configuration. Falling back to SMTP.");
				return createSmtpProvider(settings);
			}
			
			log.info("Using AWS SES email provider with region: " + region);
			return new AwsSesEmailProvider(region, fromAddress, configurationSet);
		} else {
			// Default SMTP provider
			return createSmtpProvider(settings);
		}
	}

	/**
	 * Helper method to create SMTP provider from settings
	 */
	private static EmailProvider createSmtpProvider(Settings settings) {
		if (settings.getMail() == null) {
			log.warn("No mail configuration found in settings");
			throw new RuntimeException("No mail configuration found");
		}
		
		log.info("Using SMTP email provider");
		return new SmtpEmailProvider(
			settings.getMail().get("smtp"),
			settings.getMail().get("port"),
			settings.getMail().get("user"),
			settings.getMail().get("password"),
			settings.getMail().get("name")
		);
	}
}
