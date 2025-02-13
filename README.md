### Continuous Integration Server
![Generated by Adobe Firefly](./resources/CI_cover.jpg)
## Mission Objective: Streamlining Development

The Continuous Integration Server project automates the building and testing of our group project, then notifying the user of the results through email. By continuously compiling code and performing static syntax checks, we can ensure high code quality and seamless integration.

## Why This CI Server Matters

This CI server enhances the development workflow by automating essential tasks. With real-time feedback triggered by webhooks, developers can quickly identify and resolve issues, leading to a more stable codebase.

## Dependencies

### Maven Testing Dependencies

- **JUnit (4.13.1)**: Unit testing framework (test scope).
- **Jetty Server (11.0.9)**: Embedded web server.
- **Jakarta Servlet API (5.0.0)**: HTTP request handling (provided scope).
- **org.json (20210307)**: JSON parsing and manipulation.
- **Angus Mail (2.0.3)**: JavaMail implementation.
- **JGit (6.8.0.202311291450-r)**: Git repository interaction.
- **Maven Surefire Plugin (2.12.4)**: Runs unit tests in Maven.

### Library Dependencies

- `org.junit.Before`
- `org.junit.BeforeClass`
- `org.junit.Test`
- `org.w3c.dom.Document`
- `org.xml.sax.InputSource`
- `org.xml.sax.SAXException`
- `javax.xml.parsers.DocumentBuilder`
- `javax.xml.parsers.DocumentBuilderFactory`
- `javax.xml.parsers.ParserConfigurationException`
- `java.io.BufferedReader`
- `java.io.File`
- `java.io.IOException`
- `java.io.InputStreamReader`
- `java.io.StringReader`
- `java.nio.charset.StandardCharsets`
- `java.nio.file.Files`
- `java.nio.file.Paths`
- `java.util.Arrays`
- `java.util.List`
- `java.util.Objects`
- `java.util.Properties`
- `java.util.regex.Matcher`
- `java.util.regex.Pattern`

### Jakarta Mail Dependencies

- `jakarta.mail.*`
- `jakarta.mail.internet.InternetAddress`
- `jakarta.mail.internet.MimeMessage`
- `jakarta.servlet.ServletException`
- `jakarta.servlet.http.HttpServletRequest`
- `jakarta.servlet.http.HttpServletResponse`

### JGit Dependencies

- `org.eclipse.jgit.api.Git`
- `org.eclipse.jgit.api.ListBranchCommand`
- `org.eclipse.jgit.api.ResetCommand`
- `org.eclipse.jgit.api.errors.GitAPIException`
- `org.eclipse.jgit.lib.Ref`

### Jetty Dependencies

- `org.eclipse.jetty.server.Request`
- `org.eclipse.jetty.server.Server`
- `org.eclipse.jetty.server.handler.AbstractHandler`

### JSON Dependencies

- `org.json.JSONArray`
- `org.json.JSONObject`
## Getting Started: Your Setup

## Need Assistance? Support is Here!

If you encounter issues or have questions about the project, please file an issue in the project's repository. Your feedback is crucial for improving the project!

## Code Insights: Understanding the System

For an in-depth look at the code, please refer to our javaDoc.

## System Requirements

This project requires Java 21 LTS (tested on versions 21.0.1 and 21.0.5).
This project requires ngrok v3 (tested on version 3.19.1).
This project requires Apache Maven 3.9.9

## Running the Server: Instructions

To deploy your Continuous Integration Server, follow these steps:

1. **Clone the repository**: Start by cloning the project's repository to your local machine.
2. **Build the project**: Open your terminal and navigate to the project directory. Run the following command to clean and install the project:
    
    `mvn clean install`
    
3. **Compile the code**: Next, compile the project by executing:
    
    `mvn compile`
    
4. **Run the Java server**: Finally, start the Continuous Integration Server by running:
    
    `mvn exec:java -Dexec.mainClass="CIServer"`

5. **Set ngrok authentication token**: After creating a ngrok account, use the command:

    `ngrok config add-authtoken $YOUR_AUTHTOKEN`
   
6. **Start ngrok service**: Make Java server accessible on public Internet using the command:

    `./ngrok http [optional url] [port]`

## Statement of Contributions

### Project Title: Continuous Integration Server

### Contributors

- **Linus Bälter (**https://github.com/blimpan)
    - **Contributions**:
        - Created and configured CI server on remote machine.
        - Ran tests on the remote machine.
- **Henrik Pendersén (**https://github.com/WatermelonGodz)
    - **Contributions**:
        - Designed and implemented the [`CodeVerifier.java`](http://CodeVerifier.java) class
        - Created unit tests for all methods in `CodeVerifier.java`.
        - Did code refactoring
- Kristen Rosen (https://github.com/KristinRosen)
    - **Contributions**:
        - Designed and Implemented the [`Email.java`](http://Email.java) class.
        - Created the unit tests for all methods from [`Email.java`](http://Email.java) .
- [**C](https://github.com/anotherusername)heang Weng Io, Yoyo (**https://github.com/beginner003)
    - **Contributions**:
        - Authored `README.md` .
        - Designed and Implemented the `WebhookAnalyser.java` .
        - Created unit tests for all methods in `WebhookAnalyser.java` .
- Edgar Wolff (https://github.com/edgarwolff)
    - **Contributions**:
        - Designed and implemented the [`CodeVerifier.java`](http://CodeVerifier.java) class
        - Created unit tests for all methods in `CodeVerifier.java`.
        - Did multiple code refactoring in `WebhookAnalyser.java` , `CIServer.java` , and their respective tests.
        - Created the integration test.

### Acknowledgments

We would like to thank all contributors for their efforts and dedication to making this project successful. Your hard work and collaboration have been invaluable.
