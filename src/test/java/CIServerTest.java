import org.junit.*;
import org.eclipse.jetty.server.Server;
import org.json.JSONObject;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class CIServerTest {
    // Creating a local server
    private static Server server;
    private static String repoPath;
    private static String commitHash;
    private static boolean isServerStartedBeforeTest; // true: port in used; false; port not in used

    @BeforeClass
    /**
     * Setting up the local server at http://localhost:8027/
     */
    public static void setUp() throws Exception {
        // Run the setup script
        ProcessBuilder builder = new ProcessBuilder("sh", "src/test/java/server_communication/server_setup.sh");
        builder.inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to set up test repository");
        }
//        System.out.println("Server and repo successfully added");

        // Read the commit hash from file
        commitHash = Files.readString(Path.of("commit_hash.txt")).trim();
//        System.out.println("Commit Hash: "+commitHash);
        // Read the repo path from file
        repoPath = Files.readString(Path.of("repo_path.txt")).trim();
//        System.out.println("Repo Path: "+ repoPath);

        //Check if the port is in used already
        if(!isPortInUse(8027)){ //if port not in used
            server = new Server(8027);
            server.setHandler(new CIServer());
            server.start();
        }
        Thread.sleep(500);
    }

    /**
     * Helper Funciton
     * this funciton aims to check if a port is in used for starting up a server.
     * if the port is in used, the isServerStartedBeforeTest will be set accordingly
     * the variable will be used to determine if the server needs to be stopped after the test.
     * @param port
     * @return
     */
    public static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            isServerStartedBeforeTest = false;
            return false; // Port is not in use
        } catch (IOException e) {
            isServerStartedBeforeTest = true;
            return true; // Port is in use
        }
    }

    /**
     * After the test finished, the server is stopped
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        // if server has already been started before the test,
        // the server should not be stopped here in the test
       if(!isServerStartedBeforeTest) {
           server.stop();
       }
        // Clean up
        ProcessBuilder cleanupBuilder = new ProcessBuilder("rm", "-rf", "test-repo", "commit_hash.txt", "repo_path.txt");
        cleanupBuilder.start().waitFor();
        System.out.println("Server stopped");
    }

    /**
     * This test aims to test the following:
     * 1. The data retrieving from JSON payload through a POST
     * 2. The cloning of repo in a tmp directory
     * @throws Exception
     */
    @Test
    public void testCloneRepository() throws Exception {
        // Prepare the JSON payload (customised)
        String jsonPayload = new JSONObject()
                .put("ref", "refs/heads/feature-branch")
                .put("repository", new JSONObject()
                        //Paste the file location to your local repo
                        .put("html_url", "file://"+repoPath)) // Update this path
                .put("head_commit", new JSONObject()
                        // To get the commit hash, cd to your repo, then do git log, then paste it below
                        .put("id", commitHash))
                .toString();

        // Send the POST request
        URL url = new URL("http://localhost:8027/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
//        System.out.println("Test POST request sent!");
        try (BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream())) {
            out.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            out.flush(); // Ensure data is sent
        }

    // Check the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String response = reader.lines().collect(Collectors.joining("\n"));
//                System.out.println("Response from server: " + response);
                assertEquals("The CI server says 'Hello!'", response);
            }
        } else {
            System.out.println("Failed to send POST request. Response Code: " + responseCode);
        }
        // Wait a bit for processing
        Thread.sleep(1000);

        // Check if the repository was cloned
        File clonedRepoDir = new File(System.getProperty("java.io.tmpdir"), "dd2480-builds/build-"+commitHash);
        assertTrue(clonedRepoDir.exists());
    }
}