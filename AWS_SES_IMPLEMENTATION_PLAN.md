# AWS SES Implementation Plan

## Current State Analysis

### Existing Email Infrastructure
- **Email Provider**: Standard JavaMail (jakarta.mail) with SMTP
- **Current Implementation**: `MailUtil.java` uses Properties-based SMTP configuration
- **Dependencies**:
  - `io.micronaut.email:micronaut-email-javamail`
  - `org.eclipse.angus:angus-mail` (runtime)
- **Configuration**: Map-based settings stored in Settings.yaml
  ```yaml
  mail:
    smtp: "smtp.example.com"
    port: "587"
    user: "username"
    password: "password"
    name: "sender@example.com"
  ```

### Email Usage Patterns
1. **User Registration**: Sends activation link (`UserService.registerUser()`)
2. **Password Reset**: Sends recovery link (`UserService.resetPassword()`)
3. **Job Notifications**: Notifies users about job completion/retirement (`JobCleanUpService.sendNotification()`)
4. **Admin Notifications**: Alerts administrators (`MailUtil.notifyAdmin()`)
5. **Custom Job Emails**: Workflow steps can send emails via `CloudgeneContext.sendMail()`

### Current Code Locations
- **Mail Utility**: `/src/main/java/cloudgene/mapred/util/MailUtil.java` (80 lines)
- **Settings**: `/src/main/java/cloudgene/mapred/util/Settings.java` (stores mail config)
- **Admin UI**: `/src/main/html/webapp/components/admin/settings/mail/` (SMTP config form)
- **Templates**: `/src/main/java/cloudgene/mapred/core/Template.java` (email templates)

---

## Implementation Plan

### Phase 1: Add AWS SES SDK Dependencies
**Goal**: Include AWS SES v2 SDK for Java

#### Tasks:
1. **Add AWS SES SDK to pom.xml**
   - Current AWS SDK BOM: `aws-java-sdk-bom:1.12.770` (already in dependency management)
   - Add dependency:
     ```xml
     <dependency>
         <groupId>com.amazonaws</groupId>
         <artifactId>aws-java-sdk-ses</artifactId>
     </dependency>
     ```
   - Alternative: Use AWS SDK v2 (recommended for new integrations):
     ```xml
     <dependency>
         <groupId>software.amazon.awssdk</groupId>
         <artifactId>ses</artifactId>
         <version>2.29.29</version>
     </dependency>
     ```

2. **Verify No Conflicts**
   - Run `mvn dependency:tree` to check for conflicts
   - Ensure compatibility with existing AWS SDK S3/STS dependencies

**Estimated Effort**: 1 hour

---

### Phase 2: Create AWS SES Mail Service
**Goal**: Implement a new email service using AWS SES API

#### Tasks:
1. **Create `AwsSesMailService.java`**
   - Location: `/src/main/java/cloudgene/mapred/util/AwsSesMailService.java`
   - Responsibilities:
     - Initialize AWS SES client (with region configuration)
     - Send emails via SES API
     - Handle HTML and plain text emails
     - Support multiple recipients
     - Error handling and logging
   
   ```java
   package cloudgene.mapred.util;
   
   import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
   import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
   import com.amazonaws.services.simpleemail.model.*;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   public class AwsSesMailService {
       private static final Logger log = LoggerFactory.getLogger(AwsSesMailService.class);
       
       private final AmazonSimpleEmailService sesClient;
       private final String fromAddress;
       
       public AwsSesMailService(String region, String fromAddress) {
           this.sesClient = AmazonSimpleEmailServiceClientBuilder
               .standard()
               .withRegion(region)
               .build();
           this.fromAddress = fromAddress;
       }
       
       public void send(String to, String subject, String body) throws Exception {
           // Implementation
       }
   }
   ```

2. **Support Configuration Options**
   - AWS Region (e.g., us-east-1, eu-west-1)
   - From Address (must be verified in SES)
   - Reply-To Address (optional)
   - Configuration Set (for tracking, optional)
   - IAM credentials (via default credentials chain)

**Estimated Effort**: 4 hours

---

### Phase 3: Refactor MailUtil for Pluggable Email Providers
**Goal**: Allow switching between SMTP and AWS SES

#### Tasks:
1. **Create Email Provider Interface**
   - Location: `/src/main/java/cloudgene/mapred/util/EmailProvider.java`
   ```java
   public interface EmailProvider {
       void send(String to, String subject, String body) throws Exception;
       void send(String[] recipients, String subject, String body) throws Exception;
   }
   ```

2. **Create SMTP Provider Implementation**
   - Location: `/src/main/java/cloudgene/mapred/util/SmtpEmailProvider.java`
   - Wrap existing `MailUtil.send()` logic

3. **Create AWS SES Provider Implementation**
   - Location: `/src/main/java/cloudgene/mapred/util/AwsSesEmailProvider.java`
   - Implement using `AwsSesMailService`

4. **Refactor MailUtil to Use Providers**
   - Add factory method: `private static EmailProvider getProvider(Settings settings)`
   - Update all send methods to use provider
   - Maintain backward compatibility

**Estimated Effort**: 6 hours

---

### Phase 4: Update Settings Configuration
**Goal**: Add AWS SES configuration options

#### Tasks:
1. **Extend Settings.java**
   - Add mail provider type field: `private String mailProvider = "smtp"` (default)
   - Add AWS SES specific fields:
     ```java
     private Map<String, String> awsSes;
     ```
   - Add getters/setters for awsSes config

2. **Update settings.yaml Schema**
   - Support new configuration format:
     ```yaml
     mail:
       provider: "aws-ses"  # or "smtp"
       # SMTP configuration (existing)
       smtp: "smtp.example.com"
       port: "587"
       user: "username"
       password: "password"
       name: "sender@example.com"
       # AWS SES configuration (new)
       aws-ses:
         region: "us-east-1"
         from: "noreply@example.com"
         configuration-set: "cloudgene-emails"  # optional
     ```

3. **Validate Configuration**
   - Add validation for required fields based on provider
   - Add warning if SES email address not verified

**Estimated Effort**: 3 hours

---

### Phase 5: Update Admin UI
**Goal**: Allow admins to configure AWS SES via web interface

#### Tasks:
1. **Update mail.stache Template**
   - Location: `/src/main/html/webapp/components/admin/settings/mail/mail.stache`
   - Add provider selection dropdown (SMTP / AWS SES)
   - Conditionally show SMTP fields or AWS SES fields
   - Add help text for SES setup requirements

2. **Update mail.js Controller**
   - Location: `/src/main/html/webapp/components/admin/settings/mail/mail.js`
   - Handle provider selection change
   - Save AWS SES configuration fields

3. **Update ServerService.java**
   - Update `updateSettings()` method to accept AWS SES parameters
   - Save AWS SES configuration to settings

4. **Update ServerResponse.java**
   - Add fields for returning AWS SES configuration to frontend

**Estimated Effort**: 5 hours

---

### Phase 6: Testing
**Goal**: Ensure AWS SES integration works correctly

#### Tasks:
1. **Unit Tests**
   - Create `AwsSesMailServiceTest.java`
   - Mock AWS SES client responses
   - Test success and error scenarios
   - Test email validation

2. **Integration Tests**
   - Update `RegisterUserTest.java` to support SES
   - Update `ResetPasswordTest.java` to support SES
   - Create test configuration with SES credentials
   - Test actual email sending to SES sandbox

3. **Manual Testing**
   - Set up AWS SES sandbox account
   - Verify email address
   - Test registration flow
   - Test password reset flow
   - Test job notification emails
   - Verify email delivery and format

4. **Update TestApplication**
   - Support both mock SMTP and mock SES for tests
   - Ensure existing tests continue to pass

**Estimated Effort**: 8 hours

---

### Phase 7: Documentation
**Goal**: Document AWS SES setup and configuration

#### Tasks:
1. **Create Installation Guide**
   - Location: `/docs/server/configure-email-aws-ses.md`
   - Prerequisites (AWS account, verified domain/email)
   - IAM permissions required
   - Step-by-step SES setup
   - Configuration examples

2. **Update Existing Documentation**
   - Update `/docs/server/configure-email.md` (if exists)
   - Update environment variables documentation
   - Add troubleshooting section

3. **Add Code Comments**
   - Document all new classes thoroughly
   - Add JavaDoc for public methods
   - Include example usage

**Estimated Effort**: 4 hours

---

### Phase 8: Production Deployment Considerations
**Goal**: Ensure smooth production rollout

#### Tasks:
1. **AWS SES Setup Requirements**
   - Move out of SES sandbox (requires AWS approval for production sending)
   - Verify domain (via DNS TXT records)
   - Configure SPF, DKIM, DMARC records
   - Set up bounce and complaint handling (optional but recommended)
   - Configure CloudWatch alarms for send quotas

2. **IAM Credentials Strategy**
   - Use IAM roles (EC2 instance profile) - RECOMMENDED
   - Or use AWS access keys stored in settings
   - Principle of least privilege (ses:SendEmail, ses:SendRawEmail)

3. **High Availability & Scaling**
   - SES automatically scales with demand
   - Monitor send quotas and rate limits
   - Implement retry logic for throttling errors
   - Consider SES sending quota increases if needed

4. **Migration Path**
   - Support gradual migration (both SMTP and SES running)
   - Provide rollback mechanism
   - Test thoroughly in staging environment

5. **Monitoring & Logging**
   - Log all email sends (SUCCESS/FAILURE)
   - Track delivery metrics (via SES events)
   - Alert on high failure rates
   - Dashboard for email statistics

**Estimated Effort**: 6 hours (excluding AWS approval wait time)

---

## Alternative Approaches

### Option 1: Micronaut Email Module with SES
Instead of using AWS SDK directly, use Micronaut's email framework with SES transport:
- **Pros**: More Micronaut-native, potentially simpler configuration
- **Cons**: May be less flexible, requires Micronaut Email SES module (check availability)

### Option 2: AWS SDK v2 (Recommended)
Use AWS SDK v2 instead of v1:
- **Pros**: Modern async API, better resource management, reactive support
- **Cons**: Different API, but project already uses v1 for S3

### Option 3: External Email Service Library
Use a third-party library like SendGrid, Mailgun, or Amazon SES Java client:
- **Pros**: Vendor-agnostic abstraction
- **Cons**: Additional dependency, may not integrate well with AWS infrastructure

---

## Risk Assessment

### Low Risk
- ✅ Backward compatibility (SMTP remains default)
- ✅ Isolated changes (new classes, minimal refactoring)
- ✅ AWS SDK already in use for S3

### Medium Risk
- ⚠️ IAM credential management (need secure handling)
- ⚠️ Email deliverability (SES sandbox limitations, domain verification)
- ⚠️ Configuration complexity (more options for admins)

### High Risk
- 🔴 AWS SES approval process (can take 24-48 hours, may be rejected)
- 🔴 Cost implications (SES charges per email sent)
- 🔴 Production migration (risk of email delivery issues)

---

## Cost Estimate

### AWS SES Pricing (as of 2024)
- First 62,000 emails/month: **$0** (when sent from EC2)
- Otherwise: **$0.10 per 1,000 emails**
- Additional data transfer costs apply

### Development Time Estimate
| Phase | Effort | Developer Days (8h/day) |
|-------|--------|-------------------------|
| Phase 1: Dependencies | 1h | 0.125 |
| Phase 2: SES Service | 4h | 0.5 |
| Phase 3: Refactoring | 6h | 0.75 |
| Phase 4: Settings | 3h | 0.375 |
| Phase 5: Admin UI | 5h | 0.625 |
| Phase 6: Testing | 8h | 1.0 |
| Phase 7: Documentation | 4h | 0.5 |
| Phase 8: Production | 6h | 0.75 |
| **TOTAL** | **37h** | **~5 days** |

---

## Implementation Priority

### Must Have (MVP)
1. ✅ Phase 1: Add dependencies
2. ✅ Phase 2: Create SES service
3. ✅ Phase 3: Refactor MailUtil
4. ✅ Phase 4: Update settings

### Should Have
5. ✅ Phase 5: Admin UI
6. ✅ Phase 6: Testing
7. ✅ Phase 7: Documentation

### Nice to Have
8. ⭐ Phase 8: Production optimizations
9. ⭐ Email templates (HTML support)
10. ⭐ Email tracking and analytics

---

## Success Criteria

### Functional Requirements
- [ ] Users receive registration emails via AWS SES
- [ ] Password reset emails work via AWS SES
- [ ] Job notifications delivered via AWS SES
- [ ] Admin can switch between SMTP and SES via UI
- [ ] Configuration persists across server restarts
- [ ] All existing tests pass with both providers

### Non-Functional Requirements
- [ ] Email delivery within 5 seconds
- [ ] 99.9% email send success rate
- [ ] No plaintext AWS credentials in config files
- [ ] Comprehensive error handling and logging
- [ ] Clear documentation for setup

---

## Next Steps

1. **Immediate Actions**:
   - Review and approve this implementation plan
   - Set up AWS SES sandbox account for development
   - Verify test email address in SES console
   - Create feature branch: `feature/aws-ses-integration`

2. **Development Kickoff**:
   - Start with Phase 1 (dependencies)
   - Build incrementally, testing at each phase
   - Maintain backward compatibility throughout

3. **Before Production**:
   - Request AWS SES production access
   - Verify domain ownership (DNS records)
   - Complete security review of IAM credentials
   - Run full test suite in staging environment

---

## References

- [AWS SES Developer Guide](https://docs.aws.amazon.com/ses/latest/dg/Welcome.html)
- [AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [Micronaut Email Documentation](https://micronaut-projects.github.io/micronaut-email/latest/guide/)
- [JavaMail API](https://jakarta.ee/specifications/mail/)
- [Cloudgene Documentation](https://cloudgene.io)

---

## Appendix: Sample Code Snippets

### A. AWS SES Send Email (SDK v1)
```java
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

public class AwsSesExample {
    public void sendEmail(String from, String to, String subject, String body) {
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder
            .standard()
            .withRegion("us-east-1")
            .build();
        
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(to))
            .withMessage(new Message()
                .withBody(new Body().withText(new Content().withCharset("UTF-8").withData(body)))
                .withSubject(new Content().withCharset("UTF-8").withData(subject)))
            .withSource(from);
        
        client.sendEmail(request);
    }
}
```

### B. Settings Configuration Example
```yaml
!cloudgene.mapred.util.Settings
mail:
  provider: "aws-ses"
  aws-ses:
    region: "us-east-1"
    from: "noreply@cloudgene.io"
    configuration-set: "cloudgene-production"
```

### C. IAM Policy for SES
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ses:SendEmail",
        "ses:SendRawEmail"
      ],
      "Resource": "*"
    }
  ]
}
```

---

**Document Version**: 1.0  
**Last Updated**: February 14, 2026  
**Author**: GitHub Copilot  
**Status**: Draft - Pending Approval
