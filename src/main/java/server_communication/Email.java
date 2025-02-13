package server_communication;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * This class provides an implementation to send emails to a constructor given recipient through the send() method
 */
public class Email {

    String to;
    String from;
    String username;
    String password;
    String host;

    /**
     * Class constructor stores the email recipient and sets the other communication related parameters
     *
     * @param recipient the recipient's email address
     */
    public Email(String recipient) {
        this.to = recipient;
        this.from = "testenkristest@gmail.com";
        this.username = "apikey";
        this.password = "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";
        this.host = "smtp.sendgrid.net";
    }

    /**
     * This method will send and email to the already defined recipient containing the information given as parameters
     *
     * @param subject the subject of the email
     * @param content the content of the email
     */
    public void send(String subject, String content) {
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
            message.setSubject(subject);
            // set the content of the email message
            message.setText(content);

            // send the email message
            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


}


        
