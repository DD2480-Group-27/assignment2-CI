import code_verification.CodeVerifier;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server_communication.WebhookJSONAnalyser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class CIServer extends AbstractHandler {

    /**
     * Handles incoming HTTP requests for the CI server.
     * <p>
     * This method processes HTTP requests by reading the JSON payload from the request body,
     * extracting relevant data such as the branch reference, repository URL, and commit hash.
     * It then clones the specified repository to a temporary directory and resets it to the
     * exact state of the specified commit. The method responds to the client with a confirmation
     * message indicating that the server has received the request.
     * <p>
     * Process Overview:
     * 1. Sets the response content type to HTML and the status to OK.
     * 2. Reads the JSON payload from the request body.
     * 3. Parses the JSON data to extract the branch reference, repository URL, and commit hash.
     * 4. Converts the branch reference into a proper branch name.
     * 5. Calls the `cloneRepo` method to clone the repository and reset it to the specified commit.
     * 6. Use the cloned repository for code validation process.
     * 7. Sends a confirmation message back to the client indicating successful handling of the request.
     * </p>
     * @param target      The target URL of the request.
     * @param baseRequest The base request object, which provides access to request and response details.
     * @param request     The HttpServletRequest containing the client's request data.
     * @param response    The HttpServletResponse used to send a response back to the client.
     * @throws IOException      If an I/O error occurs during request processing.
     * @throws ServletException If the request cannot be handled due to a servlet error.
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // Read the JSON payload
        StringBuilder payloadBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                payloadBuilder.append(line).append("\n");
            }
        }

        try {
            WebhookJSONAnalyser payloadAnalyser = new WebhookJSONAnalyser(payloadBuilder.toString());

            // Code Validation
            var codeVerifier = new CodeVerifier(payloadAnalyser.getRepoPath());

            var commitHash = payloadAnalyser.getCommitHash();
            var commitMail = payloadAnalyser.getCommitMail();
            var commitMessage = payloadAnalyser.getCommitMessage();
            var commitRef = payloadAnalyser.getCommitRef();
            var commitBranch = payloadAnalyser.getCommitBranch();
            var commitAuthor = payloadAnalyser.getCommitAuthor();
            var repoURL = payloadAnalyser.getRepoURL();

            // Verifies the code compilation and runs the associated tests if compilation is successful.
            // If the code fails to compile, retrieves the compilation output.
            // If the tests fail, retrieves the test result and output for further notification.
            if (codeVerifier.verifyCompilation()) {
                var testResult = codeVerifier.runTests();
                var testOutputXml = codeVerifier.getTestXml();
                // TODO  notification with test result if testResult != 0 also send testOutputXml
            } else {
                var compilationOutput = codeVerifier.getCompilationOutput();
                // TODO  notification with compilation result
            }

        } catch (InterruptedException e) {
            System.err.println("Compilation or testing process was interrupted");
        } catch (RuntimeException e) {
            System.err.println("Failed to parse payload from last request");
        }

        response.getWriter().println("The CI server says 'Hello!'");
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8027);
        server.setHandler(new CIServer());
        server.start();
        server.join();
    }

}


