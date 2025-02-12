import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import org.eclipse.jgit.api.ResetCommand;
import org.json.JSONObject;
import org.eclipse.jgit.api.Git;

public class CIServer extends AbstractHandler {

    /**
     * Handles incoming HTTP requests for the CI server.
     *
     * This method processes HTTP requests by reading the JSON payload from the request body,
     * extracting relevant data such as the branch reference, repository URL, and commit hash.
     * It then clones the specified repository to a temporary directory and resets it to the
     * exact state of the specified commit. The method responds to the client with a confirmation
     * message indicating that the server has received the request.
     *
     * @param target       The target URL of the request.
     * @param baseRequest  The base request object, which provides access to request and response details.
     * @param request      The HttpServletRequest containing the client's request data.
     * @param response     The HttpServletResponse used to send a response back to the client.
     * @throws IOException      If an I/O error occurs during request processing.
     * @throws ServletException If the request cannot be handled due to a servlet error.
     *
     * Process Overview:
     * 1. Sets the response content type to HTML and the status to OK.
     * 2. Reads the JSON payload from the request body.
     * 3. Parses the JSON data to extract the branch reference, repository URL, and commit hash.
     * 4. Converts the branch reference into a proper branch name.
     * 5. Calls the `cloneRepo` method to clone the repository and reset it to the specified commit.
     * 6. Use the cloned repository for code validation process.
     * 7. Sends a confirmation message back to the client indicating successful handling of the request.
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException, ServletException
    {
//        System.out.println("In handle process");
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // Read the JSON payload
//        System.out.println("Reading the payload");
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line).append("\n");
            }
        }

        try {
            JSONObject json = new JSONObject(payload.toString());
            // Get the branch name
            String ref = json.getString("ref");
            // Get the repo URL
            String htmlUrl = json.getJSONObject("repository").getString("html_url");
            // Get the commit hash id
            String commitHash = json.getJSONObject("head_commit").getString("id");
            // Parse the ref into a proper branch name
            String branchName = ref.replace("refs/heads/", "");

            System.out.println("The received webhook data:");
            System.out.println("--------------------------------------------------");
            System.out.println("Ref: " + ref);
            System.out.println("Branch Name: "+ branchName);
            System.out.println("Repo URL: " + htmlUrl);
            System.out.println("Commit Hash: "+ commitHash);
            System.out.println("--------------------------------------------------");
            // Store the path to the cloned repo
            String codePath =  cloneRepo(htmlUrl, commitHash, branchName );

            // Code Validation

            // After

        } catch (Exception e) {
            System.out.println("Error parsing JSON payload: " + e.getMessage());
        }

        response.getWriter().println("The CI server says 'Hello!'");
    }

    /**
     * Clones a Git repository to a temporary directory and resets it to a specific commit.
     *
     * This method creates a unique directory for the specified commit within a base
     * temporary directory. If a directory for the commit already exists, it is deleted
     * before the clone operation. After cloning, the method checks out the specified
     * branch and performs a hard reset to ensure the working directory matches the
     * exact state of the specified commit.
     *
     * @param repoURL    The URL of the Git repository to clone.
     * @param commitHash The hash of the commit to which the repository should be reset.
     * @param branchName The name of the branch to check out after cloning.
     * @return The absolute path of the cloned repository in the temporary directory.
     *         Returns null if an error occurs during the cloning process.
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
            if(repoDir.exists()){
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

    /**
     * Helper Fucntion:
     * this function aims to delete a given directory recursively
     * @param directory
     */
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
        directory.delete(); }


    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8027);
        server.setHandler(new CIServer());
        server.start();
        server.join();
    }
}
