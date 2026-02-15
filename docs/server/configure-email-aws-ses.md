# Configure Email with AWS SES

This guide explains how to configure Cloudgene to send emails using Amazon Simple Email Service (AWS SES) instead of traditional SMTP.

## Overview

AWS SES is a cloud-based email sending service that provides a reliable and scalable way to send emails. Benefits include:

- **Scalability**: Handle high volumes of email without managing infrastructure
- **Reliability**: Built on Amazon's proven infrastructure
- **Cost-effective**: Pay only for what you use with competitive pricing
- **No SMTP credentials**: Use IAM roles for secure, credential-free authentication
- **Monitoring**: Built-in metrics and CloudWatch integration

## Prerequisites

Before configuring Cloudgene with AWS SES, you need:

1. **AWS Account**: An active AWS account with SES access
2. **Verified Email Address or Domain**: At least one verified sender address
3. **IAM Permissions**: Appropriate permissions to send emails via SES
4. **AWS Credentials**: Either as environment variables, IAM role, or AWS credentials file

## AWS SES Setup

### Step 1: Verify Your Email Address or Domain

AWS SES requires you to verify the email address or domain you'll use to send emails.

**To verify an email address:**

1. Open the [AWS SES Console](https://console.aws.amazon.com/ses/)
2. Click **Verified identities** in the left navigation
3. Click **Create identity**
4. Select **Email address** and enter your email (e.g., `noreply@yourdomain.com`)
5. Click **Create identity**
6. Check your inbox and click the verification link

**To verify a domain (recommended for production):**

1. In the SES Console, click **Verified identities** → **Create identity**
2. Select **Domain** and enter your domain name
3. Follow the instructions to add DNS records (TXT, CNAME for DKIM, and optionally DMARC)
4. Wait for DNS propagation and verification (can take up to 72 hours)

### Step 2: Request Production Access

By default, AWS SES accounts are in **sandbox mode**, which has limitations:

- You can only send to verified email addresses
- Limited to 200 emails per day
- Maximum send rate of 1 email per second

**To move out of sandbox mode:**

1. In the SES Console, click **Account dashboard**
2. Look for the "Sending statistics" section
3. Click **Request production access**
4. Fill out the request form explaining your use case
5. AWS typically responds within 24-48 hours

### Step 3: Configure IAM Permissions

Create an IAM policy with the minimum required permissions:

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

**Option A: IAM Role (Recommended for EC2/ECS)**

Attach the policy to an IAM role and associate it with your EC2 instance or ECS task. This is the most secure option as no credentials need to be stored.

**Option B: IAM User with Access Keys**

1. Create an IAM user
2. Attach the policy to the user
3. Generate access keys
4. Configure credentials (see next section)

### Step 4: Configure AWS Credentials

Cloudgene uses the AWS SDK default credential provider chain. Credentials can be provided via:

**Environment Variables (Recommended for containers):**

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

**AWS Credentials File:**

Create or edit `~/.aws/credentials`:

```ini
[default]
aws_access_key_id = your_access_key
aws_secret_access_key = your_secret_key
```

And `~/.aws/config`:

```ini
[default]
region = us-east-1
```

**IAM Role (Best for EC2/ECS):**

No configuration needed - the SDK automatically retrieves credentials from the instance metadata service.

## Cloudgene Configuration

### Via Admin UI

1. Log in to Cloudgene as an administrator
2. Navigate to **Admin** → **Settings**
3. Click on the **Mail** tab
4. In the **Email Provider** dropdown, select **AWS SES**
5. Configure the following fields:

   - **Region**: The AWS region where SES is configured (e.g., `us-east-1`, `eu-west-1`)
   - **From Address**: The verified email address to use as sender (e.g., `noreply@yourdomain.com`)
   - **Configuration Set** (optional): Name of your SES configuration set for tracking

6. Click **Save**

### Via settings.yaml

Edit your `config/settings.yaml` file:

```yaml
mail:
  provider: aws-ses
  aws-ses-region: us-east-1
  aws-ses-from: noreply@yourdomain.com
  # Optional: specify a configuration set for tracking/monitoring
  aws-ses-configuration-set: my-config-set
```

**Note**: The old SMTP configuration fields (`smtp`, `port`, `user`, `password`, `name`) are ignored when `provider` is set to `aws-ses`.

### Configuration Options

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `provider` | Yes | Email provider (`aws-ses` or `smtp`) | `aws-ses` |
| `aws-ses-region` | Yes | AWS region where SES is configured | `us-east-1` |
| `aws-ses-from` | Yes | Verified sender email address | `noreply@example.com` |
| `aws-ses-configuration-set` | No | SES configuration set name for tracking | `cloudgene-emails` |

## Advanced Configuration

### Email Tracking with Configuration Sets

AWS SES Configuration Sets allow you to track email metrics like opens, clicks, bounces, and complaints.

**To create a configuration set:**

1. In the SES Console, go to **Configuration sets**
2. Click **Create configuration set**
3. Enter a name (e.g., `cloudgene-emails`)
4. Configure event destinations (SNS, CloudWatch, Kinesis Firehose)
5. Update your `settings.yaml` with the configuration set name

### Handling Bounces and Complaints

Set up SNS topics to receive notifications for bounces and complaints:

1. Create SNS topics for bounces and complaints
2. Subscribe your email or Lambda function to these topics
3. In SES Console, configure notifications for verified identities
4. Implement logic to handle these events (e.g., disable user accounts with bounced emails)

### Monitoring and Alarms

Use CloudWatch to monitor SES metrics:

- **Sent**: Number of emails successfully sent
- **Bounces**: Hard and soft bounces
- **Complaints**: Spam complaints
- **Rejects**: Emails rejected by SES

Create CloudWatch alarms for high bounce or complaint rates.

### Rate Limits

AWS SES has sending limits based on your account status:

- **Sandbox**: 200 emails/day, 1 email/second
- **Production**: Varies (starts at 50,000/day, 14 emails/second)

Request limit increases through the AWS Support Center if needed.

## Testing

### Test Email Sending

After configuration, test email functionality:

1. Register a new user account
2. Check that you receive the activation email
3. Try the "Forgot Password" feature
4. Submit a job and verify notification emails

### Sandbox Testing

In sandbox mode, you can only send to verified addresses. To test:

1. Verify your test email address in the SES Console
2. Use that address to register and test Cloudgene

### Production Testing

After moving to production access:

1. Test with unverified addresses
2. Monitor bounce and complaint rates
3. Adjust sending patterns if needed

## Troubleshooting

### Common Issues

**1. Emails Not Being Sent**

- Check AWS credentials are configured correctly
- Verify IAM permissions include `ses:SendEmail` and `ses:SendRawEmail`
- Confirm the region matches where your email/domain is verified
- Check Cloudgene logs for AWS SES errors

**2. "Email address not verified" Error**

- The from address must be verified in the SES Console
- If using a domain, verify the entire domain instead of individual addresses
- Wait for verification links to arrive (can take a few minutes)

**3. "Daily sending limit exceeded"**

- You're in sandbox mode - request production access
- Or you've hit your daily limit - wait 24 hours or request a limit increase

**4. High Bounce Rate**

- Verify email addresses are valid
- Check that MX records exist for recipient domains
- Review bounce notifications to identify problematic addresses

**5. Emails Going to Spam**

- Set up SPF, DKIM, and DMARC records for your domain
- Enable Easy DKIM in SES Console
- Use a professional "From" address and name
- Ensure your content isn't flagged by spam filters

### Debug Logging

To enable debug logging for AWS SES:

```yaml
# In logback.xml or application.yml
logging:
  level:
    cloudgene.mapred.util.AwsSesMailService: DEBUG
    cloudgene.mapred.util.AwsSesEmailProvider: DEBUG
```

Check Cloudgene logs for detailed error messages.

## Switching from SMTP to AWS SES

If you're migrating from SMTP to AWS SES:

1. Complete AWS SES setup (verify domain/email, IAM permissions)
2. Configure AWS credentials
3. Update `settings.yaml`: change `provider` from `smtp` to `aws-ses`
4. Add AWS SES configuration parameters
5. Restart Cloudgene
6. Test email functionality

**Before:**
```yaml
mail:
  provider: smtp  # or omit (defaults to smtp)
  smtp: smtp.gmail.com
  port: 587
  user: your-email@gmail.com
  password: your-password
  name: noreply@yourdomain.com
```

**After:**
```yaml
mail:
  provider: aws-ses
  aws-ses-region: us-east-1
  aws-ses-from: noreply@yourdomain.com
  # Optional
  aws-ses-configuration-set: cloudgene-emails
```

## Best Practices

1. **Use IAM Roles**: Prefer IAM roles over access keys for credential management
2. **Verify Domains**: Verify entire domains rather than individual addresses for flexibility
3. **Monitor Metrics**: Set up CloudWatch monitoring and alarms
4. **Handle Bounces**: Implement bounce and complaint handling
5. **Test Thoroughly**: Test in sandbox before requesting production access
6. **Set Up SPF/DKIM**: Configure email authentication to improve deliverability
7. **Use Configuration Sets**: Track email metrics for better insights
8. **Start Small**: Begin with sandbox testing, then gradually increase volume in production

## Additional Resources

- [AWS SES Documentation](https://docs.aws.amazon.com/ses/)
- [AWS SES Best Practices](https://docs.aws.amazon.com/ses/latest/dg/best-practices.html)
- [AWS SES Pricing](https://aws.amazon.com/ses/pricing/)
- [Email Authentication (SPF, DKIM, DMARC)](https://docs.aws.amazon.com/ses/latest/dg/email-authentication-methods.html)
- [Moving Out of SES Sandbox](https://docs.aws.amazon.com/ses/latest/dg/request-production-access.html)

## Support

If you encounter issues:

1. Check Cloudgene logs for error messages
2. Review AWS SES sending statistics in the AWS Console
3. Verify your AWS credentials and IAM permissions
4. Consult the AWS SES troubleshooting guide
5. Report Cloudgene-specific issues on [GitHub](https://github.com/genepi/cloudgene3)
