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

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // System.out.println(target);

        // Read the JSON payload
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line).append("\n");
            }
        }

        // System.out.println("Payload received:\n" + payload.toString());

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

            System.out.println("Ref: " + ref);
            System.out.println("Branch Name: "+ branchName);
            System.out.println("Repo URL: " + htmlUrl);
            System.out.println("Commit Hash: "+ commitHash);
            // Store the path to the cloned repo
            String codePath =  cloneRepo(htmlUrl, commitHash, branchName );

            // Code Validation

            // After

        } catch (Exception e) {
            System.out.println("Error parsing JSON payload: " + e.getMessage());
        }

        response.getWriter().println("The CI server says 'Hello!'");
    }

    /***
     * cloneRepo aims to make a copy of the code in the tmp folder
     * and reset to the exact code state for a commit
     * @param repoURL
     * @param commitHash
     * @param branchName
     * @return the absolute path of the cloned repo in the tmp directory
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

    private boolean deleteDirectory(File directory) {
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
        return directory.delete(); }
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8027);
        server.setHandler(new CIServer());
        server.start();
        server.join();
    }
}
