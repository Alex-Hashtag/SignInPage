package org.alex_hashtag;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    private static final String FROM_EMAIL = SecretManager.get("EMAIL_ADDRESS");
    private static final String PASSWORD = SecretManager.get("EMAIL_PASSWORD");

    public static void sendConfirmationEmail(String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Welcome to Our App");
            message.setText("Hello!\n\nThank you for signing up. Your account has been created successfully.\n\n- The Team");

            Transport.send(message);
            System.out.println("Email sent to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
