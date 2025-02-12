package server_communication;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebhookJSONAnalyserTest {
    @Test
    public void testCloneRepositorySample1() throws Exception {
        // Read the JSON payload from the file
        String jsonPayload;
        try {
            jsonPayload = new String(Files.readAllBytes(Paths.get("resources/WebhookJSONAnalyserTestSample.json")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON test sample", e);
        }

        // Parse the JSON array
        JSONArray jsonArray = new JSONArray(jsonPayload);

        // Iterate through each JSON object in the array

        JSONObject jsonObject = jsonArray.getJSONObject(0);
        var webhookHandler = new WebhookJSONAnalyser(jsonObject.toString());

        // Use getters to check values
        assertEquals("refs/heads/main", webhookHandler.getCommitRef());
        assertEquals("main", webhookHandler.getCommitBranch());
        assertEquals("https://github.com/DD2480-Group-27/LaunchInterceptor", webhookHandler.getRepoURL());
        assertEquals("426455fc8993003f1153c034496c7bb27b8ae553", webhookHandler.getCommitHash());
        assertEquals("yoyo.cwi@gmail.com", webhookHandler.getCommitMail());
        assertEquals("Weng Io Cheang (Yoyo)", webhookHandler.getCommitAuthor());
        assertEquals("Empty-Commit", webhookHandler.getCommitMessage());

        // Check if the repo is cloned at the right location
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File baseDir = new File(tempDir, "dd2480-builds");

        // Assert that the base directory exists
        assertTrue("dd2480-builds directory does not exist!", baseDir.exists());

        // Create a unique directory for this build using the commit hash
        String buildDir = "build-426455fc8993003f1153c034496c7bb27b8ae553";
        File repoDir = new File(baseDir, buildDir);

        // Assert that the repo directory exists
        assertTrue("The repo is not cloned", repoDir.exists());

        // Clean up
        webhookHandler.deleteRepo("426455fc8993003f1153c034496c7bb27b8ae553");
    }

    @Test
    public void testCloneRepositorySample2() throws Exception {
        // Read the JSON payload from the file
        String jsonPayload;
        try {
            jsonPayload = new String(Files.readAllBytes(Paths.get("resources/WebhookJSONAnalyserTestSample.json")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON test sample", e);
        }

        // Parse the JSON array
        JSONArray jsonArray = new JSONArray(jsonPayload);

        // Get the second JSON object
        JSONObject jsonObject = jsonArray.getJSONObject(1); // Change index to 1 for second object
        var webhookHandler = new WebhookJSONAnalyser(jsonObject.toString());

        // Use getters to check values
        assertEquals("refs/heads/kristin/doc/readme-add-essence", webhookHandler.getCommitRef());
        assertEquals("kristin/doc/readme-add-essence", webhookHandler.getCommitBranch());
        assertEquals("https://github.com/DD2480-Group-27/LaunchInterceptor", webhookHandler.getRepoURL());
        assertEquals("8ffc04a68d7449d57a28b41876f93e41600f0240", webhookHandler.getCommitHash());
        assertEquals("yoyo.cwi@gmail.com", webhookHandler.getCommitMail());
        assertEquals("Weng Io Cheang (Yoyo)", webhookHandler.getCommitAuthor());
        assertEquals("Empty-Commit-in-kristin/doc/readme-add-essence-branch", webhookHandler.getCommitMessage());

        // Check if the repo is cloned at the right location
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File baseDir = new File(tempDir, "dd2480-builds");

        // Assert that the base directory exists
        assertTrue("dd2480-builds directory does not exist!", baseDir.exists());

        // Create a unique directory for this build using the commit hash
        String buildDir = "build-8ffc04a68d7449d57a28b41876f93e41600f0240";
        File repoDir = new File(baseDir, buildDir);

        // Assert that the repo directory exists
        assertTrue("The repo is not cloned", repoDir.exists());

        // Clean up
        webhookHandler.deleteRepo("8ffc04a68d7449d57a28b41876f93e41600f0240");
    }
}