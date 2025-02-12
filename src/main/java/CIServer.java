import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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
     *
     * @param target      The target URL of the request.
     * @param baseRequest The base request object, which provides access to request and response details.
     * @param request     The HttpServletRequest containing the client's request data.
     * @param response    The HttpServletResponse used to send a response back to the client.
     * @throws IOException      If an I/O error occurs during request processing.
     * @throws ServletException If the request cannot be handled due to a servlet error.
     *                          <p>
     *                          Process Overview:
     *                          1. Sets the response content type to HTML and the status to OK.
     *                          2. Reads the JSON payload from the request body.
     *                          3. Parses the JSON data to extract the branch reference, repository URL, and commit hash.
     *                          4. Converts the branch reference into a proper branch name.
     *                          5. Calls the `cloneRepo` method to clone the repository and reset it to the specified commit.
     *                          6. Use the cloned repository for code validation process.
     *                          7. Sends a confirmation message back to the client indicating successful handling of the request.
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
//        System.out.println("In handle process");
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // Read the JSON payload
//        System.out.println("Reading the payload");
        StringBuilder payloadBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                payloadBuilder.append(line).append("\n");
            }
        }

        WebhookJSONAnalyser payloadAnalyser = null;
        try {
            payloadAnalyser = new WebhookJSONAnalyser(payloadBuilder.toString());

            var repoPath = payloadAnalyser.repoPath;

            // TODO call verification and retrieve either compilation failure output
            //  or test results if compilation succeeded


            var commitHash = payloadAnalyser.commitHash;
            var commitMail = payloadAnalyser.commitMail;
            var commitMessage = payloadAnalyser.commitMessage;
            var commitRef = payloadAnalyser.commitRef;
            var commitBranch = payloadAnalyser.commitBranch;
            var commitAuthor = payloadAnalyser.commitAuthor;
            var repoURL = payloadAnalyser.repoURL;

            // TODO call email notification

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

    /**
     * Public class intended to fetch relevant data from JSON webhook request
     */
    public static class WebhookJSONAnalyser {

        private final String repoPath;
        private final String repoURL;
        private final String commitHash;
        private final String commitMail;
        private final String commitMessage;
        private final String commitRef;
        private final String commitBranch;
        private final String commitAuthor;

        /**
         * Class constructor will parse the given JSON String and set its parameters to the relevant values
         *
         * @param payload a String containing a JSON encoded GitHub webhook payload
         */
        public WebhookJSONAnalyser(String payload) {
            try {
                JSONObject json = new JSONObject(payload);
                // Get the branch name
                this.commitRef = json.getString("ref");
                // Get the repo URL
                this.repoURL = json.getJSONObject("repository").getString("html_url");
                // Get the commit hash id
                this.commitHash = json.getJSONObject("head_commit").getString("id");
                // Parse the ref into a proper branch name
                this.commitBranch = commitRef.replace("refs/heads/", "");

                System.out.println("The received webhook data:");
                System.out.println("--------------------------------------------------");
                System.out.println("Ref: " + commitRef);
                System.out.println("Branch Name: " + commitBranch);
                System.out.println("Repo URL: " + repoURL);
                System.out.println("Commit Hash: " + commitHash);
                System.out.println("--------------------------------------------------");

                // Store the path to the cloned repo
                this.repoPath = cloneRepo(repoURL, commitHash, commitBranch);
                this.commitMail = "dumb@email.com";     //TODO set with the commit's email address
                this.commitAuthor = "author";           //TODO set with author
                this.commitMessage = "message";         //TODO set with commit message

            } catch (Exception e) {
                throw new RuntimeException(e);          //TODO make maybe a better handling (printing a little bit of information about the fail if possible (idk))
            }


        }

        /**
         * Clones a Git repository to a temporary directory and resets it to a specific commit.
         * <p>
         * This method creates a unique directory for the specified commit within a base
         * temporary directory. If a directory for the commit already exists, it is deleted
         * before the clone operation. After cloning, the method checks out the specified
         * branch and performs a hard reset to ensure the working directory matches the
         * exact state of the specified commit.
         * </p>
         *
         * @param repoURL    The URL of the Git repository to clone.
         * @param commitHash The hash of the commit to which the repository should be reset.
         * @param branchName The name of the branch to check out after cloning.
         * @return The absolute path of the cloned repository in the temporary directory.
         * Returns null if an error occurs during the cloning process.
         */
        private String cloneRepo(String repoURL, String commitHash, String branchName) {
            try {
                // Get system temp directory in a platform-independent way
                File tempDir = new File(System.getProperty("java.io.tmpdir"));
                File baseDir = new File(tempDir, "dd2480-builds");
                if (!baseDir.exists()) {
                    baseDir.mkdir();
                }

                // Create a unique directory for this build using the commit hash
                String buildDir = "build-" + commitHash;
                File repoDir = new File(baseDir, buildDir);
                //if the directory exists for a commit hash, then delete it
                if (repoDir.exists()) {
                    deleteDirectory(repoDir);
                }
                //then create a new empty directory
                repoDir.mkdir();

                // Clone the repository
                Git git = Git.cloneRepository()
                        .setURI(repoURL)
                        .setDirectory(repoDir)
                        .call();
                // Checkout the right branch
                git.checkout().setName(branchName).call();
                // Hard reset to the specific commit
                git.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef(commitHash)
                        .call();
                git.close();
                System.out.println("Repository cloned and hard reset to commit " + commitHash);
                return repoDir.getAbsolutePath();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private void deleteDirectory(File directory) {
            if (directory.isDirectory()) {
                // List all files and subdirectories
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        // Recursively delete subdirectories and files
                        deleteDirectory(file);
                    }
                }
            }
            // delete the directory itself
            directory.delete();
        }

        /**
         * @return a String containing the commit author's email
         */
        public String getCommitMail() {
            return commitMail;
        }

        /**
         * @return a String representing the absolut path to where the repo has been cloned and set to the right version
         */
        public String getRepoPath() {
            return repoPath;
        }

        /**
         * @return a String containing the commit hash
         */
        public String getCommitHash() {
            return commitHash;
        }

        /**
         * @return a String containing the commit message
         */
        public String getCommitMessage() {
            return commitMessage;
        }

        /**
         * @return a String containing the commit reference
         */
        public String getCommitRef() {
            return commitRef;
        }

        /**
         * @return a String containing the commit branch
         */
        public String getCommitBranch() {
            return commitBranch;
        }

        /**
         * @return a String containing the URL
         */
        public String getRepoURL() {
            return repoURL;
        }

        /**
         * @return a String containing the commit author's username
         */
        public String getCommitAuthor() {
            return commitAuthor;
        }

    }
}
