package cloudgene.mapred.util;

/**
 * Interface for email providers.
 * 
 * Allows pluggable email implementations (SMTP, AWS SES, etc.)
 */
public interface EmailProvider {
    
    /**
     * Send email to a single recipient.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     * @throws Exception if email sending fails
     */
    void send(String to, String subject, String body) throws Exception;
    
    /**
     * Send email to multiple recipients.
     * 
     * @param recipients array of recipient email addresses
     * @param subject email subject
     * @param body email body (plain text)
     * @throws Exception if email sending fails
     */
    void send(String[] recipients, String subject, String body) throws Exception;
}
