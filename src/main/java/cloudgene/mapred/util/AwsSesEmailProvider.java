package cloudgene.mapred.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS SES email provider implementation.
 * 
 * This provider uses AWS Simple Email Service for sending emails.
 */
public class AwsSesEmailProvider implements EmailProvider {
    
    private static final Logger log = LoggerFactory.getLogger(AwsSesEmailProvider.class);
    
    private final AwsSesMailService sesService;
    
    /**
     * Create AWS SES email provider.
     * 
     * @param region AWS region
     * @param fromAddress verified email address
     * @param configurationSet optional configuration set (can be null)
     */
    public AwsSesEmailProvider(String region, String fromAddress, String configurationSet) {
        this.sesService = new AwsSesMailService(region, fromAddress, configurationSet);
        log.info("Initialized AWS SES email provider for region: {}", region);
    }
    
    @Override
    public void send(String to, String subject, String body) throws Exception {
        sesService.send(to, subject, body);
    }
    
    @Override
    public void send(String[] recipients, String subject, String body) throws Exception {
        sesService.send(recipients, subject, body);
    }
    
    /**
     * Close and clean up resources.
     */
    public void close() {
        sesService.close();
    }
}
