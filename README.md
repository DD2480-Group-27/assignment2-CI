## Mission Objective: Streamlining Development

The Continuous Integration Server project automates the building and testing of our group project, then notifying the user of the results through email. By continuously compiling code and performing static syntax checks, we can ensure high code quality and seamless integration.

## Why This CI Server Matters

This CI server enhances the development workflow by automating essential tasks. With real-time feedback triggered by webhooks, developers can quickly identify and resolve issues, leading to a more stable codebase.

## Dependencies

## Getting Started: Your Setup

## Need Assistance? Support is Here!

If you encounter issues or have questions about the project, please file an issue in the project's repository. Your feedback is crucial for improving the project!

## Code Insights: Understanding the System

For an in-depth look at the code, please refer to our javaDoc.

## System Requirements

This project requires Java 21 LTS (tested on versions 21.0.1 and 21.0.5).

## Running the Server: Instructions

To deploy your Continuous Integration Server, follow these steps:

1. **Clone the Repository**: Start by cloning the project's repository to your local machine.
2. **Install Apache Maven**: Ensure you have **Apache Maven 3.9.9** installed. If you haven't installed it yet, you can download it from [Apache Maven Download](https://maven.apache.org/download.cgi).
3. **Build the Project**: Open your terminal and navigate to the project directory. Run the following command to clean and install the project:
    
    `mvn clean install`
    
4. **Compile the Code**: Next, compile the project by executing:
    
    `mvn compile`
    
5. **Run the Server**: Finally, start the Continuous Integration Server by running:
    
    `mvn exec:java -Dexec.mainClass="CIServer"`
    

## Statement of Contributions

### Project Title: Continuous Integration Server

### Contributors

- **Linus Bälter (**https://github.com/blimpan)
    - **Contributions**:
        - Created the remote server.
        - Running tests on the remote server.
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
