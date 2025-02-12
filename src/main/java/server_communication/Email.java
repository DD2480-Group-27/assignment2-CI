package server_communication;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class Email {
    public static void main(String[] args) {

        // provide recipient's email ID
        String to = "kristin.maria.rosen@gmail.com, kristinr@kth.se, kristinr@datasektionen.se";
        // provide sender's email ID
        String from = "testenkristest@gmail.com";

        // provide account credentials
        final String username = "apikey";
        final String password = "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";

        // provide host address
        String host = "smtp.sendgrid.net";

        // configure SMTP details
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // create the mail Session object
        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            // create a MimeMessage object
            Message message = new MimeMessage(session);
            // set From email field
            message.setFrom(new InternetAddress(from));
            // set To email field
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            // set email subject field
            message.setSubject("Sending test email");
            // set the content of the email message
            message.setText("Test email worked!");

            // send the email message
            Transport.send(message);

            System.out.println("Email Message Sent Successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
