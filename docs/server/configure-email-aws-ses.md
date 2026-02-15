# AWS SES Email Configuration

Cloudgene supports Amazon Simple Email Service (AWS SES) as an alternative to SMTP for sending emails. AWS SES provides a reliable, cost-effective email service that can scale with your needs.

## Prerequisites

Before configuring AWS SES in Cloudgene, you need:

1. **AWS Account**: An active AWS account with SES enabled
2. **Verified Email/Domain**: At least one verified email address or domain in AWS SES
3. **IAM Credentials**: Appropriate AWS credentials with SES permissions
4. **Production Access** (Optional): For production use, request to move out of SES sandbox

## IAM Permissions

The AWS credentials used by Cloudgene need the following IAM permissions:

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

## Configuration

### Using AWS SES

To configure Cloudgene to use AWS SES, add the following to your `settings.yaml` file:

```yaml
mailProvider: aws-ses
awsSes:
  region: us-east-1
  from: noreply@example.com
  configuration-set: cloudgene-emails  # optional
```

### Configuration Parameters

- **`mailProvider`**: Set to `aws-ses` to use AWS SES (default: `smtp`)
- **`awsSes.region`**: AWS region where your SES service is configured (required)
- **`awsSes.from`**: From email address - must be verified in AWS SES (required)
- **`awsSes.configuration-set`**: Optional SES configuration set for tracking email metrics

### AWS Credentials

Cloudgene uses the AWS SDK's default credential provider chain to authenticate with AWS SES. The SDK looks for credentials in the following order:

1. **Environment Variables**: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
2. **Java System Properties**: `aws.accessKeyId` and `aws.secretAccessKey`
3. **IAM Instance Profile** (Recommended for EC2): Automatically provided when running on EC2
4. **AWS Credential File**: `~/.aws/credentials`

For production deployments, we recommend using IAM instance profiles when running on EC2, as this is the most secure method.

### Using SMTP (Default)

If you prefer to continue using SMTP, keep the existing configuration:

```yaml
# mailProvider: smtp  # optional, this is the default
mail:
  smtp: localhost
  port: 25
  user: username
  password: password
  name: noreply@domain.com
```

## AWS SES Sandbox vs Production

### Sandbox Mode
By default, new AWS SES accounts are in sandbox mode with the following limitations:
- You can only send emails to verified email addresses
- Maximum of 200 emails per 24-hour period
- Maximum send rate of 1 email per second

### Production Access
To send emails to any recipient:
1. Go to AWS SES Console
2. Request production access
3. Provide details about your use case
4. Wait for AWS approval (typically 24-48 hours)

## Verifying Email Addresses

Before you can send emails from an address, it must be verified in AWS SES:

1. Open the [AWS SES Console](https://console.aws.amazon.com/ses/)
2. Navigate to **Verified identities**
3. Click **Create identity**
4. Select **Email address** and enter your address
5. Click **Create identity**
6. Check your email and click the verification link

## Best Practices

### For Production Deployments

1. **Verify Your Domain**: Instead of individual email addresses, verify your entire domain using DNS records
2. **Configure DKIM**: Set up DomainKeys Identified Mail for better deliverability
3. **Set up SPF**: Add SPF records to your DNS
4. **Configure DMARC**: Implement DMARC for email authentication
5. **Monitor Bounce and Complaints**: Set up SNS topics to track bounces and complaints
6. **Use Configuration Sets**: Track email metrics in CloudWatch
7. **Set up CloudWatch Alarms**: Monitor send quotas and error rates

### Security

1. **Use IAM Roles**: When running on EC2, use IAM instance roles instead of access keys
2. **Principle of Least Privilege**: Grant only `ses:SendEmail` and `ses:SendRawEmail` permissions
3. **Rotate Credentials**: If using access keys, rotate them regularly
4. **Never Commit Credentials**: Do not store AWS credentials in version control

## Troubleshooting

### Email Not Sending

1. **Check AWS Credentials**: Ensure credentials are properly configured
2. **Verify Email Address**: Confirm the from address is verified in SES
3. **Check Logs**: Look for error messages in Cloudgene logs
4. **Sandbox Mode**: If in sandbox, verify recipient email addresses
5. **Check Quotas**: Ensure you haven't exceeded SES sending limits

### Common Error Messages

- **"Email address is not verified"**: The from address must be verified in AWS SES
- **"Invalid AWS credentials"**: Check your AWS access key and secret key
- **"Request has expired"**: System time may be incorrect; sync your server clock
- **"Throttling"**: You've exceeded your sending rate; wait and retry

## Cost

AWS SES pricing (as of 2024):
- **First 62,000 emails/month**: $0 (when sent from EC2)
- **Additional emails**: $0.10 per 1,000 emails
- **Data transfer**: Standard AWS data transfer rates apply

For detailed pricing, see [AWS SES Pricing](https://aws.amazon.com/ses/pricing/)

## Migration from SMTP

To migrate from SMTP to AWS SES:

1. Set up AWS SES and verify your email/domain
2. Update `settings.yaml` with AWS SES configuration
3. Test email delivery with a test user registration
4. Once confirmed working, restart Cloudgene
5. Monitor logs for any issues
6. Keep SMTP configuration as fallback if needed

## Example Configuration

### Minimal Configuration

```yaml
mailProvider: aws-ses
awsSes:
  region: us-east-1
  from: noreply@cloudgene.io
```

### With Configuration Set

```yaml
mailProvider: aws-ses
awsSes:
  region: eu-west-1
  from: noreply@myservice.com
  configuration-set: cloudgene-production
```

### Hybrid Setup (Keep SMTP as Backup)

You can keep both configurations in your `settings.yaml`:

```yaml
# Active provider
mailProvider: aws-ses

# AWS SES configuration
awsSes:
  region: us-east-1
  from: noreply@example.com

# SMTP configuration (as backup)
mail:
  smtp: smtp.example.com
  port: 587
  user: username
  password: password
  name: noreply@example.com
```

To switch back to SMTP, simply change `mailProvider` to `smtp` and restart Cloudgene.

## Additional Resources

- [AWS SES Developer Guide](https://docs.aws.amazon.com/ses/latest/dg/Welcome.html)
- [AWS SDK for Java Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [AWS SES Best Practices](https://docs.aws.amazon.com/ses/latest/dg/best-practices.html)
