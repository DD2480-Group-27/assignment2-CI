import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;

public class CIServerTest {

    @BeforeClass
    /**
     * Setting up the local server at http://localhost:8027/
     */
    public static void setUp() throws Exception {
        // Run the setup script
        ProcessBuilder builder = new ProcessBuilder("sh", "resources/server_setup.sh");
        builder.inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to set up test repository");
        }
    }

    /**
     * After the test finished, the server is stopped
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {

    }

    /**
     * This test aims to test the following:
     * 1. The data retrieving from JSON payload through a POST
     * 2. The cloning of repo in a tmp directory
     *
     * @throws Exception
     */

}