package server_communication;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class Email {

    String to;
    String from;
    String username;
    String password;
    String host;

    public Email(String recipient){
        this.to = recipient;
        this.from = "testenkristest@gmail.com";
        this.username = "apikey";
        this.password = "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";
        this.host = "smtp.sendgrid.net";
    }

    public void Send(String subject, String content){
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


        
