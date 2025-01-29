package com.emt.dms1.emails;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Service
public class EmailNotificationService {

    @Value("${sender.email}")
    private String systemEmail;

    @Value("${sender.password}")
    private String systemPassword;

    @Value("${email.subject.template}")
    private String subjectTemplate;
    @Value("${email.subject.template2}")
    private String subjectTemplate2;

    public void sendNotification(Integer otpValue, String recipientEmail) throws MessagingException, GeneralSecurityException {
        String body = String.format("Your One-time Sign-in Password is: %s", otpValue);
        MimeMessage message = createMimeMessage(systemEmail, recipientEmail,
                String.format(subjectTemplate, otpValue), body);
        try (Transport transport = getSession().getTransport("smtp")) {
            transport.connect(systemEmail, systemPassword);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            // Handle email sending exceptions (e.g., log the error)
            throw new MessagingException("Failed to send email notification", e);
        }
    }

    private MimeMessage createMimeMessage(String from, String to, String subject, String body) throws MessagingException, GeneralSecurityException {
        MimeMessage message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(body, "text/html"); // Set content type as HTML
        return message;
    }

    private Session getSession() throws GeneralSecurityException {
        Properties props = new Properties();
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.socketFactory", sf);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(systemEmail, systemPassword);
            }
        });
    }
    public void sendCreatedNotification(String recipientEmail, String password) throws MessagingException, GeneralSecurityException {
        String body = String.format("Welcome\nAccount created Successfully \nYour Password is:%s",password);
        MimeMessage message = createMimeMessage(systemEmail, recipientEmail,
                String.format(subjectTemplate2), body);
        try (Transport transport = getSession().getTransport("smtp")) {
            transport.connect(systemEmail, systemPassword);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            // Handle email sending exceptions (e.g., log the error)
            throw new MessagingException("Failed to send email notification", e);
        }
    }
    public void  sendEmail( String recipientEmail,String message2,String message3) throws MessagingException, GeneralSecurityException {
        MimeMessage message = createMimeMessage(systemEmail, recipientEmail,
                String.format(message2), message3);
        try (Transport transport = getSession().getTransport("smtp")) {
            transport.connect(systemEmail, systemPassword);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            // Handle email sending exceptions (e.g., log the error)
            throw new MessagingException("Failed to send email notification", e);
        }
    }


}
