package com.schnarbiesnmeowers.nmsgateway.services;

import com.schnarbiesnmeowers.nmsgateway.email.*;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

import static com.schnarbiesnmeowers.nmsgateway.utilities.Constants.*;


/**
 * Email service class
 * sends emails for:
 * sending the user a new password
 * sending the user their username
 * informing the admins when a user locks themselves out of their account
 * @author Dylan I. Kessler
 *
 */
@Service
public class EmailService {

	private static final Logger emailLogger = LogManager.getLogger("EmailAppender");
	
	@Value("${email.username}")
	private String username;
	
	@Value("${email.waffle}")
	private String waffle;
	
	@Value("${email.cc}")
	private String cc;
	
	@Value("${success.email.link}")
	private String successEmailUrl;
	
	@Value("${success.email.expiration.minutes}")
	private int linkExpirationTime;
	
	static final String FROM = "interviewmailer76@gmail.com";
	static final String TO = "dylanikessler@yahoo.com";
	static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";
	static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
		      + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
		      + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" 
		      + "AWS SDK for Java</a>";
	static final String TEXTBODY = "This email was sent through Amazon SES "
		      + "using the AWS SDK for Java.";
	
	
	/**
	 * method to send an email to the user with a new password in it
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public void sendConfirmEmailEmail(String emailAddress,String uniqueId) throws AddressException, MessagingException {
		logEmailAction("sendConfirmEmailEmail - entering, emailAddress = " + emailAddress);
		successEmailUrl = "http://127.0.0.1:4200";
		String pageLink = successEmailUrl + "/register/confirmemail";
		EmailTemplate template = new VerifyRegistrationEmailTemplate(emailAddress,username,pageLink,uniqueId,linkExpirationTime);
		createAndSendEmail(template);
		logEmailAction("sendConfirmEmailEmail - leaving, emailAddress = " + emailAddress);
	}

	public void sendForgotPasswordEmail(String emailAddress,String uniqueId) throws AddressException, NoSuchProviderException, SendFailedException, MessagingException {
		logEmailAction("sendForgotPasswordEmail - entering, emailAddress = " + emailAddress);
		successEmailUrl = "http://127.0.0.1:4200";
		String pageLink = successEmailUrl + "/login/passwordreset";
		String loginUrl = successEmailUrl + "/login";
		System.out.println(uniqueId);
		EmailTemplate template = new ForgotPasswordEmailTemplate(emailAddress,pageLink,loginUrl,uniqueId,linkExpirationTime);
		//createAndSendEmail(template);
		logEmailAction("sendForgotPasswordEmail - leaving, emailAddress = " + emailAddress);
	}

	/**
	 * method to send an email to the user with their username in it
	 * @param emailAddress
	 * @param username
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmailWithUsername(String emailAddress,String username) throws AddressException, MessagingException {
		logEmailAction("sendEmailWithUsername - entering, emailAddress = " + emailAddress);
		//successEmailUrl = "http://127.0.0.1:4200";
		String loginUrl = successEmailUrl + "/login";
		EmailTemplate template = new ForgotUsernameEmailTemplate(emailAddress, username, loginUrl);
		//createAndSendEmail(template);
		logEmailAction("sendEmailWithUsername - leaving, emailAddress = " + emailAddress);
	}

	/**
	 * method to send an email to the user informing them that no account was found for this email
	 * this is used for both requesting a password reset or a username reminder
	 * @param emailAddress
	 * @throws MessagingException 
	 * @throws SendFailedException 
	 * @throws NoSuchProviderException 
	 * @throws AddressException 
	 */
	public void sendNoAddressFoundEmail(String emailAddress, boolean reset) throws AddressException, NoSuchProviderException, SendFailedException, MessagingException {
		EmailTemplate template = null;
		logEmailAction("sendNoAddressFoundEmail - entering, emailAddress = " + emailAddress);
		if(reset) {
			template = new NoAddressFoundPREmailTemplate(emailAddress);
		} else {
			template = new NoAddressFoundUREmailTemplate(emailAddress);
		}
		//createAndSendEmail(template);
		logEmailAction("sendNoAddressFoundEmail - leaving, emailAddress = " + emailAddress);
	}

	/**
	 * 
	 * @param subject
	 * @param body
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 * @throws SendFailedException
	 * @throws MessagingException
	 */
	public void sendManagementEmail(String subject, String body) throws AddressException, NoSuchProviderException, SendFailedException, MessagingException {
		logEmailAction("sendManagementEmail - entering");
		EmailTemplate template = new ManagementEmailTemplate(subject, body);
		template.setEmailAddress(cc);
		//createAndSendEmail(template);
		logEmailAction("sendManagementEmail - leaving");
	}

	private void createAndSendEmail(EmailTemplate template)
			throws AddressException, MessagingException, NoSuchProviderException, SendFailedException {
		logEmailAction("//createAndSendEmail - sending email");
		Message message = createEmail(template);
		SMTPTransport transport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
		transport.connect(GMAIL_SMTP_SERVER, username, waffle);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
		logEmailAction("//createAndSendEmail - email sent");
	}

	/**
	 * method to create the Message email object
	 * @param template
	 * @return
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private Message createEmail(EmailTemplate template) throws AddressException, MessagingException {
		logEmailAction("createEmail - creating email");
		Message message = new MimeMessage(getEmailSession());
		message.setFrom(new InternetAddress(FROM_EMAIL));
		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(template.getEmailAddress()));
		/** 
		 * TODO - remove
		 */
		message.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc,false));
		message.setSubject(template.getSubject());
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();       
		messageBodyPart.setContent(template.getBody(), "text/html");
		multipart.addBodyPart(messageBodyPart);
	    message.setContent(multipart,"text/html");
		message.setSentDate(new Date());
		message.saveChanges();
		logEmailAction("createEmail - email created");
		return message;
	}

	/**
	 * method to get the email section
	 * @return
	 */
	private Session getEmailSession() {
		Properties properties = System.getProperties();
		properties.put(SMTP_HOST ,GMAIL_SMTP_SERVER );
		properties.put(SMTP_PORT , DEFAULT_PORT);
		properties.put(SMTP_AUTH , true);
		properties.put(SMTP_STARTTLS_ENABLE , true);
		properties.put(SMTP_STARTTLS_REQUIRED , true);
		return Session.getInstance(properties,null);
	}
	
	public void testEmail() throws MessagingException,AddressException,Exception {
		logEmailAction("entering testEmail()");
		try {
			sendConfirmEmailEmail(cc,"XXX");
			sendForgotPasswordEmail(cc,"XXX");
			sendEmailWithUsername(cc,"XXX");
			sendNoAddressFoundEmail(cc,true);
			sendNoAddressFoundEmail(cc,false);
			sendManagementEmail("SUBJECT","BODY");
		logEmailAction("Sent test email to --> " + cc);
		} catch(AddressException ae) {
			logEmailAction("AddressException sending test email --> " + ae.getMessage());
			throw ae;
		} catch(MessagingException me) {
			logEmailAction("MessagingException sending test email  --> " + me.getMessage());
			throw me;
		} catch(Exception ee) {
			logEmailAction("General Exception sending test email  --> " + ee.getMessage());
			throw ee;
		}
		logEmailAction("leaving testEmail()");
	}
	
	/**
	 * logging method
	 * @param message
	 */
	private static void logEmailAction(String message) {
		System.out.println("EmailService: " + message);
		emailLogger.debug("EmailService: " + message);
	}
}
