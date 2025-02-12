package server_communication;

public class Main {
    public static void main(String[] args) {
        String recipient = "kristinr@kth.se";
        String subject = "From main";
        String content = "This is me sending a message from the main method in the main class";
        Email email = new Email(recipient);
        email.Send(subject, content);
    }
}
