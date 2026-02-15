package cloudgene.mapred.util;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS SES email provider implementation.
 * Uses AWS Simple Email Service to send emails.
 */
public class AwsSesEmailProvider implements EmailProvider {

	private static final Logger log = LoggerFactory.getLogger(AwsSesEmailProvider.class);

	private final AmazonSimpleEmailService sesClient;
	private final String fromAddress;
	private final String configurationSet;

	/**
	 * Creates a new AWS SES email provider
	 * 
	 * @param region AWS region (e.g., "us-east-1")
	 * @param fromAddress From email address (must be verified in SES)
	 * @param configurationSet Optional configuration set name for tracking
	 */
	public AwsSesEmailProvider(String region, String fromAddress, String configurationSet) {
		this.sesClient = AmazonSimpleEmailServiceClientBuilder
			.standard()
			.withRegion(region)
			.build();
		this.fromAddress = fromAddress;
		this.configurationSet = configurationSet;
	}

	@Override
	public void send(String tos, String subject, String text) throws Exception {
		try {
			// Parse recipients
			String[] recipients = tos.split("[,;]");
			Destination destination = new Destination();
			for (String recipient : recipients) {
				destination.withToAddresses(recipient.trim());
			}

			// Build message
			Content subjectContent = new Content().withCharset("UTF-8").withData(subject);
			Content bodyContent = new Content().withCharset("UTF-8").withData(text);
			Body body = new Body().withText(bodyContent);
			Message message = new Message().withSubject(subjectContent).withBody(body);

			// Build request
			SendEmailRequest request = new SendEmailRequest()
				.withSource(fromAddress)
				.withDestination(destination)
				.withMessage(message);

			// Add configuration set if provided
			if (configurationSet != null && !configurationSet.isEmpty()) {
				request.withConfigurationSetName(configurationSet);
			}

			// Send email
			SendEmailResult result = sesClient.sendEmail(request);

			log.debug("E-Mail sent to " + tos + " via AWS SES. Message ID: " + result.getMessageId());

		} catch (Exception e) {
			log.error("Failed to send email via AWS SES: " + e.getMessage(), e);
			throw new Exception("mail could not be sent via AWS SES: " + e.getMessage());
		}
	}

}
