import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.server.Server;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CIServerTest {
    // Creating a local server
    private Server server;

    @Before
    /**
     * Setting up the local server at http://localhost:8027/
     */
    public void setUp() throws Exception {
        server = new Server(8027);
        server.setHandler(new CIServer());
        server.start();
    }

    /**
     * After the test finished, the server is stopped
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        server.stop();
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
        // You have to create a local repo first (shell script)
        /*
        mkdir test-repo
        cd test-repo
        git init
        echo "Hello World" > README.md
        git add README.md
        git commit -m "Initial commit"
         */
        // then create a branch and commit
        /*
        git checkout -b feature-branch
        echo "Feature 1" >> feature.txt
        git add feature.txt
        git commit -m "Add feature 1"
         */
        String jsonPayload = new JSONObject()
                .put("ref", "refs/heads/feature-branch")
                .put("repository", new JSONObject()
                        //Paste the file location to your local repo
                        .put("html_url", "file:///Users/searching.../Library/CloudStorage/OneDrive-Personal/HKUST/Year_3/Exchange_KTH/DD2480/Lab2/assignment2-CI/test-repo")) // Update this path
                .put("head_commit", new JSONObject()
                        // To get the commit hash, cd to your repo, then do git log, then paste it below
                        .put("id", "b9d18b79e1f5cd1c804a67ea7f3da628468c999b"))
                .toString();

        // Send the POST request
        URL url = new URL("http://localhost:8027/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));

//        // Read the response
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
//            String response = reader.lines().collect(Collectors.joining("\n"));
//            assertEquals("The CI server says 'Hello!'", response);
//        }

        // Check if the repository was cloned
        // You need to change the directory name here also, since the cloned repo is put in a directory name build-[commithash]
        File clonedRepoDir = new File(System.getProperty("java.io.tmpdir"), "dd2480-builds/build-b9d18b79e1f5cd1c804a67ea7f3da628468c999b");
        assertTrue(clonedRepoDir.exists());
    }
}