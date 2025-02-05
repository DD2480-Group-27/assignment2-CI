package code_verification;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CodeVerifierTest {
    /**
     * This variable is to be set in a way for the path "/tmp/dd2480-builds/" followed
     * by TEST_PROJECT_FOLDER to point to a valid mvn built project with a pom.xml at root
     */
    private static final String TEST_PROJECT_FOLDER = "LaunchInterceptor";

    @BeforeAll
    static void setUp() {
        var folder = new File(CodeVerifier.TESTED_PROJECT_BASE_PATH + TEST_PROJECT_FOLDER);
        if (!folder.exists() || !folder.isDirectory() ||
                Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                        .noneMatch(f -> f.getName().endsWith("pom.xml"))
        ) {
            System.err.println("You do not have the required folder on your computer to run successfully these tests");
            System.exit(1);
            // fail("Make sure to have a project at the designated folder with a pom.xml file in it");
        }
    }

    @Test
    void testConstructorValidPath() {
        assertDoesNotThrow(() -> new CodeVerifier(TEST_PROJECT_FOLDER));
        assertThrows(IllegalStateException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER).getCompilationOutput()
        );
        assertThrows(IllegalStateException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER).getTestXml()
        );
    }

    @Test
    void testConstructorNoPom() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "NoPom")
        );
    }

    @Test
    void testConstructorEmptyFolder() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "Empty")
        );
    }

    @Test
    void testConstructorWrongPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "qwerty")
        );
    }

    @Test
    void testConstructorFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "/pom.xml")
        );
    }

    @Test
    void testVerifyCompilationReturnsFalse() {
        var cVerifier = new CodeVerifier(TEST_PROJECT_FOLDER + "JavaTypo");
        try {
            assertFalse(cVerifier.verifyCompilation());
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
        assertDoesNotThrow(cVerifier::getCompilationOutput);
        assertFalse(cVerifier.getCompilationOutput().isBlank());
        assertThrows(IllegalStateException.class, cVerifier::getTestXml);
    }

    @Test
    void testVerifyCompilationReturnsTrue() {
        var cVerifier = new CodeVerifier(TEST_PROJECT_FOLDER);
        try {
            assertTrue(cVerifier.verifyCompilation());
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
        assertDoesNotThrow(cVerifier::getCompilationOutput);
        assertFalse(cVerifier.getCompilationOutput().isBlank());
        assertThrows(IllegalStateException.class, cVerifier::getTestXml);
    }

    @Test
    void testRunTestsReturnsFalse() {
        var cVerifier = new CodeVerifier(TEST_PROJECT_FOLDER + "TestFail");
        try {
            assertTrue(cVerifier.verifyCompilation());
            assertFalse(cVerifier.runTests());
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
        assertDoesNotThrow(cVerifier::getCompilationOutput);
        assertFalse(cVerifier.getCompilationOutput().isBlank());
        assertDoesNotThrow(cVerifier::getTestXml);
        assertTrue(Objects.nonNull(cVerifier.getTestXml()));
    }

    @Test
    void testRunTestsReturnsTrue() {
        var cVerifier = new CodeVerifier(TEST_PROJECT_FOLDER );
        try {
            assertTrue(cVerifier.verifyCompilation());
            assertTrue(cVerifier.runTests());
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
        assertDoesNotThrow(cVerifier::getCompilationOutput);
        assertFalse(cVerifier.getCompilationOutput().isBlank());
        assertDoesNotThrow(cVerifier::getTestXml);
        assertTrue(Objects.nonNull(cVerifier.getTestXml()));
    }
}