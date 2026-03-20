package main.java.com.carrental.util;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 * Only used to send emails to clients, thats it
 */
public class EmailUtil {
	
	final static String fromAddr = "noreply@carrental.com";
	final static String smtpHost = "smtp.gmail.com";
	
	/**
	 * sends an email to given recipient
	 * @param recipient's Email
	 * @param subject of email
	 * @param body of email
	 */
	public static void sendEmail(String recipientEmail, String subject, String body) {
		
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", smtpHost);
		
		Session session = Session.getDefaultInstance(props);
		
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(fromAddr));
			Address[] replyTo = {new InternetAddress(fromAddr)};
			
			message.setReplyTo(replyTo);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
			message.setSubject(subject);
			message.setText(body);
			
			Transport.send(message);
			
		} catch (AddressException e) {
			System.out.println("Email address is not found");
		} catch (MessagingException e) {
			System.out.println("Message could no send");
		}
		
		System.out.println("Message sent...");
		
	}
}
