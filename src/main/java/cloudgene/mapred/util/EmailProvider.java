package cloudgene.mapred.util;

/**
 * Interface for email providers to enable pluggable email implementations.
 * Implementations can use SMTP, AWS SES, or other email services.
 */
public interface EmailProvider {
	
	/**
	 * Send an email to one or more recipients
	 * 
	 * @param tos Comma-separated list of recipient email addresses
	 * @param subject Email subject
	 * @param text Email body (plain text)
	 * @throws Exception if email sending fails
	 */
	void send(String tos, String subject, String text) throws Exception;
	
}
