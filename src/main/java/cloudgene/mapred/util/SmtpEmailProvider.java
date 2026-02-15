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
 * 
 * This is the traditional email implementation using SMTP protocol.
 */
public class SmtpEmailProvider implements EmailProvider {
    
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailProvider.class);
    
    private final String smtp;
    private final String port;
    private final String username;
    private final String password;
    private final String fromAddress;
    
    /**
     * Create SMTP email provider.
     * 
     * @param smtp SMTP server hostname
     * @param port SMTP server port
     * @param username SMTP username (can be null for unauthenticated)
     * @param password SMTP password
     * @param fromAddress email from address
     */
    public SmtpEmailProvider(String smtp, String port, String username, String password, String fromAddress) {
        this.smtp = smtp;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        log.info("Initialized SMTP email provider for server: {}:{}", smtp, port);
    }
    
    @Override
    public void send(String to, String subject, String body) throws Exception {
        send(new String[]{to}, subject, body);
    }
    
    @Override
    public void send(String[] recipients, String subject, String body) throws Exception {
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
            InternetAddress[] addresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addresses[i] = new InternetAddress(recipients[i]);
            }
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            
            log.debug("Email sent via SMTP to {} recipients", recipients.length);
            
        } catch (MessagingException e) {
            log.error("SMTP error sending email", e);
            throw new Exception("Mail could not be sent via SMTP: " + e.getMessage(), e);
        }
    }
}
