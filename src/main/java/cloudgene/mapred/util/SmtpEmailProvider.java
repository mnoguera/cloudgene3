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

/**
 * SMTP email provider implementation using JavaMail.
 * This is the default email provider maintaining backward compatibility.
 */
public class SmtpEmailProvider implements EmailProvider {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailProvider.class);

	private final String smtp;
	private final String port;
	private final String username;
	private final String password;
	private final String fromAddress;

	public SmtpEmailProvider(String smtp, String port, String username, String password, String fromAddress) {
		this.smtp = smtp;
		this.port = port;
		this.username = username;
		this.password = password;
		this.fromAddress = fromAddress;
	}

	@Override
	public void send(String tos, String subject, String text) throws Exception {
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
			message.setFrom(new InternetAddress(fromAddress));
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
