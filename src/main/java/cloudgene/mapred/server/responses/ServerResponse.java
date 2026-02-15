package cloudgene.mapred.server.responses;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;

import cloudgene.mapred.util.Settings;

@JsonClassDescription
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ServerResponse {

	private String name;
	private String backgroundColor;
	private String foregroundColor;
	private String googleAnalytics;
	private String mailSmtp;
	private String mailPort;
	private String mailUser;
	private String mailPassword;
	private String mailName;
	private boolean mail;
	private String mailProvider;
	private String mailAwsSesRegion;
	private String mailAwsSesFrom;
	private String mailAwsSesConfigurationSet;
	private String adminName;
	private String adminMail;
	private String serverUrl;
	private String workspaceType;
	private String workspaceLocation;
	private String baseUrl;

	public static ServerResponse build(Settings settings) {
		ServerResponse response = new ServerResponse();
		response.setName(settings.getName());
		response.setAdminName(settings.getAdminName());
		response.setAdminMail(settings.getAdminMail());
		response.setServerUrl(settings.getServerUrl());
		response.setBaseUrl(settings.getBaseUrl());
		response.setBackgroundColor(settings.getColors().get("background"));
		response.setForegroundColor(settings.getColors().get("foreground"));
		response.setGoogleAnalytics(settings.getGoogleAnalytics());
		response.setWorkspaceType(settings.getExternalWorkspaceType());
		response.setWorkspaceLocation(settings.getExternalWorkspaceLocation());

		Map<String, String> mail = settings.getMail();
		if (mail != null) {
			response.setMail(true);
			response.setMailProvider(mail.getOrDefault("provider", "smtp"));
			// SMTP fields
			response.setMailSmtp(mail.get("smtp"));
			response.setMailPort(mail.get("port"));
			response.setMailUser(mail.get("user"));
			response.setMailPassword(mail.get("password"));
			response.setMailName(mail.get("name"));
			// AWS SES fields
			response.setMailAwsSesRegion(mail.get("aws-ses-region"));
			response.setMailAwsSesFrom(mail.get("aws-ses-from"));
			response.setMailAwsSesConfigurationSet(mail.get("aws-ses-configuration-set"));

		} else {
			response.setMail(false);
			response.setMailProvider("smtp");
			response.setMailSmtp("");
			response.setMailPort("");
			response.setMailUser("");
			response.setMailPassword("");
			response.setMailName("");
			response.setMailAwsSesRegion("");
			response.setMailAwsSesFrom("");
			response.setMailAwsSesConfigurationSet("");

		}

		return response;
	}

	private void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAdminMail() {
		return adminMail;
	}

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public String getGoogleAnalytics() {
		return googleAnalytics;
	}

	public void setGoogleAnalytics(String googleAnalytics) {
		this.googleAnalytics = googleAnalytics;
	}

	public String getMailSmtp() {
		return mailSmtp;
	}

	public void setMailSmtp(String mailSmtp) {
		this.mailSmtp = mailSmtp;
	}

	public String getMailPort() {
		return mailPort;
	}

	public void setMailPort(String mailPort) {
		this.mailPort = mailPort;
	}

	public String getMailUser() {
		return mailUser;
	}

	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getMailName() {
		return mailName;
	}

	public void setMailName(String mailName) {
		this.mailName = mailName;
	}

	public boolean isMail() {
		return mail;
	}

	public void setMail(boolean mail) {
		this.mail = mail;
	}

	public String getMailProvider() {
		return mailProvider;
	}

	public void setMailProvider(String mailProvider) {
		this.mailProvider = mailProvider;
	}

	public String getMailAwsSesRegion() {
		return mailAwsSesRegion;
	}

	public void setMailAwsSesRegion(String mailAwsSesRegion) {
		this.mailAwsSesRegion = mailAwsSesRegion;
	}

	public String getMailAwsSesFrom() {
		return mailAwsSesFrom;
	}

	public void setMailAwsSesFrom(String mailAwsSesFrom) {
		this.mailAwsSesFrom = mailAwsSesFrom;
	}

	public String getMailAwsSesConfigurationSet() {
		return mailAwsSesConfigurationSet;
	}

	public void setMailAwsSesConfigurationSet(String mailAwsSesConfigurationSet) {
		this.mailAwsSesConfigurationSet = mailAwsSesConfigurationSet;
	}

	public void setWorkspaceLocation(String workspaceLocation) {
		this.workspaceLocation = workspaceLocation;
	}

	public String getWorkspaceLocation() {
		return workspaceLocation;
	}

	public void setWorkspaceType(String workspaceType) {
		this.workspaceType = workspaceType;
	}

	public String getWorkspaceType() {
		return workspaceType;
	}

}
