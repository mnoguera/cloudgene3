package cloudgene.mapred.util;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS SES email service implementation.
 * 
 * This service uses AWS Simple Email Service (SES) to send emails.
 * Requires AWS credentials configured via environment variables, IAM roles, or AWS credentials file.
 */
public class AwsSesMailService {
    
    private static final Logger log = LoggerFactory.getLogger(AwsSesMailService.class);
    
    private final AmazonSimpleEmailService sesClient;
    private final String fromAddress;
    private final String configurationSet;
    
    /**
     * Create AWS SES mail service with region and from address.
     * 
     * @param region AWS region (e.g., "us-east-1")
     * @param fromAddress verified email address or domain
     */
    public AwsSesMailService(String region, String fromAddress) {
        this(region, fromAddress, null);
    }
    
    /**
     * Create AWS SES mail service with region, from address, and configuration set.
     * 
     * @param region AWS region (e.g., "us-east-1")
     * @param fromAddress verified email address or domain
     * @param configurationSet optional configuration set for tracking
     */
    public AwsSesMailService(String region, String fromAddress, String configurationSet) {
        this.sesClient = AmazonSimpleEmailServiceClientBuilder
            .standard()
            .withRegion(region)
            .build();
        this.fromAddress = fromAddress;
        this.configurationSet = configurationSet;
        log.info("Initialized AWS SES mail service for region: {} with from address: {}", region, fromAddress);
    }
    
    /**
     * Send email to a single recipient.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     * @throws Exception if email sending fails
     */
    public void send(String to, String subject, String body) throws Exception {
        send(new String[]{to}, subject, body);
    }
    
    /**
     * Send email to multiple recipients.
     * 
     * @param recipients array of recipient email addresses
     * @param subject email subject
     * @param body email body (plain text)
     * @throws Exception if email sending fails
     */
    public void send(String[] recipients, String subject, String body) throws Exception {
        try {
            log.debug("Sending email via AWS SES to {} recipients", recipients.length);
            
            // Create message body
            Body messageBody = new Body()
                .withText(new Content()
                    .withCharset("UTF-8")
                    .withData(body));
            
            // Create message
            Message message = new Message()
                .withSubject(new Content()
                    .withCharset("UTF-8")
                    .withData(subject))
                .withBody(messageBody);
            
            // Create destination
            Destination destination = new Destination()
                .withToAddresses(recipients);
            
            // Build send request
            SendEmailRequest request = new SendEmailRequest()
                .withSource(fromAddress)
                .withDestination(destination)
                .withMessage(message);
            
            // Add configuration set if specified
            if (configurationSet != null && !configurationSet.isEmpty()) {
                request.withConfigurationSetName(configurationSet);
            }
            
            // Send email
            SendEmailResult result = sesClient.sendEmail(request);
            
            log.info("Email sent successfully via AWS SES. MessageId: {}", result.getMessageId());
            
        } catch (AmazonSimpleEmailServiceException e) {
            log.error("AWS SES error: {} - {}", e.getErrorCode(), e.getErrorMessage());
            throw new Exception("Failed to send email via AWS SES: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email via AWS SES", e);
            throw new Exception("Failed to send email via AWS SES: " + e.getMessage(), e);
        }
    }
    
    /**
     * Close the SES client and release resources.
     */
    public void close() {
        if (sesClient != null) {
            sesClient.shutdown();
            log.debug("AWS SES client shut down");
        }
    }
}
