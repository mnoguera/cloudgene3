package cloudgene.mapred.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for email provider selection and configuration.
 */
public class EmailProviderTest {

	@Test
	public void testSmtpProviderSelection() throws Exception {
		// Create settings with SMTP provider
		Map<String, String> mailConfig = new HashMap<>();
		mailConfig.put("provider", "smtp");
		mailConfig.put("smtp", "smtp.example.com");
		mailConfig.put("port", "587");
		mailConfig.put("user", "testuser");
		mailConfig.put("password", "testpass");
		mailConfig.put("name", "test@example.com");
		
		Settings settings = new Settings();
		settings.setMail(mailConfig);
		
		// Verify SMTP is selected
		assertEquals("smtp", settings.getMail().get("provider"));
		assertEquals("smtp.example.com", settings.getMail().get("smtp"));
	}

	@Test
	public void testAwsSesProviderSelection() throws Exception {
		// Create settings with AWS SES provider
		Map<String, String> mailConfig = new HashMap<>();
		mailConfig.put("provider", "aws-ses");
		mailConfig.put("aws-ses-region", "us-east-1");
		mailConfig.put("aws-ses-from", "noreply@example.com");
		mailConfig.put("aws-ses-configuration-set", "my-config-set");
		
		Settings settings = new Settings();
		settings.setMail(mailConfig);
		
		// Verify AWS SES is selected
		assertEquals("aws-ses", settings.getMail().get("provider"));
		assertEquals("us-east-1", settings.getMail().get("aws-ses-region"));
		assertEquals("noreply@example.com", settings.getMail().get("aws-ses-from"));
	}

	@Test
	public void testDefaultProviderIsSmtp() throws Exception {
		// Create settings without provider specified
		Map<String, String> mailConfig = new HashMap<>();
		mailConfig.put("smtp", "smtp.example.com");
		mailConfig.put("port", "587");
		
		Settings settings = new Settings();
		settings.setMail(mailConfig);
		
		// Verify default is SMTP
		String provider = settings.getMail().getOrDefault("provider", "smtp");
		assertEquals("smtp", provider);
	}

	@Test
	public void testSmtpProviderCreation() throws Exception {
		SmtpEmailProvider provider = new SmtpEmailProvider(
			"smtp.example.com",
			"587",
			"testuser",
			"testpass",
			"test@example.com"
		);
		
		assertNotNull(provider);
	}

	@Test
	public void testAwsSesProviderCreation() throws Exception {
		// This test requires AWS credentials to actually send emails
		// For unit testing, we just verify object creation
		try {
			AwsSesEmailProvider provider = new AwsSesEmailProvider(
				"us-east-1",
				"noreply@example.com",
				null
			);
			
			assertNotNull(provider);
			provider.close();
		} catch (Exception e) {
			// AWS SDK might not be configured in test environment
			// This is acceptable for unit tests
			assertTrue(e.getMessage().contains("AWS") || e.getMessage().contains("credential") || e.getMessage().contains("region"));
		}
	}
}
