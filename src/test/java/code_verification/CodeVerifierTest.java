package code_verification;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;


public class CodeVerifierTest {
    /**
     * This variable is to be set in a way for the path "/tmp/dd2480-builds/" followed
     * by TEST_PROJECT_FOLDER to point to a valid mvn built project with a pom.xml at root
     */
    private static final String TEST_PROJECT_FOLDER = CodeVerifier.TESTED_PROJECT_BASE_PATH + "test-LaunchInterceptor";
    private DocumentBuilder documentBuilder;

    /**
     * This method is run before each test
     */
    @BeforeClass
    public static void setUp() {
        var folder = new File(TEST_PROJECT_FOLDER);
        if (!folder.exists() || !folder.isDirectory() ||
                Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                        .noneMatch(f -> f.getName().endsWith("pom.xml"))
        ) {
            System.out.println("Test environment missing test samples\nWill proceed adding them from resources");
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", "sh setup-codeVerifTestRes-linux.sh");
            builder.directory(new File("./resources/"));
            try {
                Process process = builder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Failed setting");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error while extracting resources");
                System.exit(1);
            }
            System.out.println("Test samples successfully added");
        } else System.out.println("Test environment already setup\nWill proceed with tests");
    }

    @Test
    public void testConstructorValidPath() {
        assertDoesNotThrow(() -> new CodeVerifier(TEST_PROJECT_FOLDER));
        assertThrows(IllegalStateException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER).getCompilationOutput()
        );
        assertThrows(IllegalStateException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER).getTestXml()
        );
    }

    @Test
    public void testConstructorNoPom() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "NoPom")
        );
    }

    @Test
    public void testConstructorEmptyFolder() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "Empty")
        );
    }

    @Test
    public void testConstructorWrongPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "qwerty")
        );
    }

    @Test
    public void testConstructorFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new CodeVerifier(TEST_PROJECT_FOLDER + "/pom.xml")
        );
    }

    @Test
    public void testVerifyCompilationReturnsFalse() {
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
    public void testVerifyCompilationReturnsTrue() {
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
    public void testRunTestsReturnsFalse() {
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
    public void testRunTestsReturnsTrue() {
        var cVerifier = new CodeVerifier(TEST_PROJECT_FOLDER);
        try {
            assertTrue(cVerifier.verifyCompilation());
            assertTrue(cVerifier.runTests());
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
        assertDoesNotThrow(cVerifier::getCompilationOutput);
        assertFalse(cVerifier.getCompilationOutput().isBlank());
        assertDoesNotThrow(cVerifier::getTestXml);
        assertFalse(Objects.nonNull(cVerifier.getTestXml()));
    }

    private void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            fail("Caught exception: " + e + "\n\t" + e.getMessage());
        }
    }

    private void initDocumentBuilder() {
        try {
            // XML Parser setup
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            documentBuilder = factory.newDocumentBuilder();
        } catch (Exception e) {
            fail("Failed to initialize DocumentBuilder: " + e.getMessage());
        }
    }

    private Document parseXml(String xmlContent) {
        try {
            return documentBuilder.parse(new InputSource(new StringReader(xmlContent)));
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void testValidXml() {
        initDocumentBuilder();
        String validXml = """
                <root>
                    <child>Hello, World!</child>
                </root>
                """;

        Document document = parseXml(validXml);
        assertNotNull("Document should not be null for valid XML", document);
        assertEquals("Root element should be 'root'", "root",
                document.getDocumentElement().getNodeName()
        );
    }

    @Test
    public void testInvalidXml() {
        initDocumentBuilder();
        String invalidXml = """
                <root>
                    <child>Hello, World!
                </root>
                """;

        Document document = parseXml(invalidXml);
        assertNull("Document should be null for invalid XML", document);
    }

    @Test
    public void testEmptyXml() {
        initDocumentBuilder();
        String emptyXml = "";
        Document document = parseXml(emptyXml);
        assertNull("Document should be null for empty XML", document);
    }

    @Test
    public void testWhitespaceOnlyXml() {
        initDocumentBuilder();
        String whitespaceOnlyXml = "    ";
        Document document = parseXml(whitespaceOnlyXml);
        assertNull("Document should be null for whitespace-only XML", document);
    }
}