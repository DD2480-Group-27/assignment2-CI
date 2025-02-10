package code_verification;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;



public class CodeVerifier {

    public static final String TESTED_PROJECT_BASE_PATH = "/tmp/dd2480-builds/";

    private final File projectFolder;
    private boolean isCompiled;
    private String compilationOutput;
    private boolean isTested;
    private List<Document> testXml;

    /**
     * The CodeVerifier constructor makes sure the given project folder path points to a valid maven projects
     *
     * @param projectFolderPath the absolute path to the folder containing the project (a String)
     * @throws IllegalArgumentException if the path points to no existing resource
     * @throws IllegalArgumentException if the path points to a file instead of a folder
     * @throws IllegalArgumentException if the path points to a folder that contains no pom.xml file
     */
    public CodeVerifier(String projectFolderPath) {
        var folder = new File(TESTED_PROJECT_BASE_PATH + projectFolderPath);

        if (!folder.exists())
            throw new IllegalArgumentException("The given project path does not exist.");
        if (!folder.isDirectory())
            throw new IllegalArgumentException("The given project path is not a directory.");

        var files = folder.listFiles();

        assert files != null;
        if (Arrays.stream(files).noneMatch((file -> file.getName().equals("pom.xml"))))
            throw new IllegalArgumentException("No pom.xml file found.");

        this.projectFolder = folder;
        this.isCompiled = false;
        this.isTested = false;
    }

    /**
     * Tries to compile the project in the folder given to the class constructor
     *
     * @return true if the compilation did not return any error, false otherwise
     * @throws IOException           if the standard output of the process gets interrupted
     * @throws InterruptedException  if the subprocess compiling the source gets interrupted before completion
     * @throws IllegalStateException if the method is called a second time on the same project
     */
    public boolean verifyCompilation() throws IOException, InterruptedException {
        if (isCompiled)
            throw new IllegalStateException("The code has already been compiled.");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", "mvn compile");
        builder.directory(projectFolder);
        Process process = builder.start();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        result.append("Exit Code: ").append(exitCode).append("\n");

        this.compilationOutput = result.toString();
        this.isCompiled = true;
        return exitCode == 0;
    }

    /**
     * Tries to run all the tests in the folder previously given to the constructor
     * This method relies on the maven library surefire to run JUnit tests
     *
     * @return true if every test succeeded, false otherwise
     * @throws IOException           if the standard output of the process gets interrupted
     * @throws InterruptedException  if the subprocess compiling the source gets interrupted before completion
     * @throws IllegalStateException if the method is called a second time on the same project
     */
    public boolean runTests() throws IOException, InterruptedException {
        if (isTested)
            throw new IllegalStateException("The tests have already been run.");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", "mvn test");
        builder.directory(projectFolder);
        Process process = builder.start();

        Pattern reportPathPattern = Pattern.compile("Surefire report directory: (.+)");
        String reportPath = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = reportPathPattern.matcher(line);
                if (matcher.find()) {
                    reportPath = matcher.group(1);
                    break;
                }
            }
        }

        int exitCode = process.waitFor();


        // TODO testXml is a list of Strings, could be made into list of xml object for easier use in the following steps (notifications)
        this.testXml = loadXmlFromFolder(reportPath);
        this.isTested = true;
        return exitCode == 0;
    }
    
    
    

    private Document parseXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlContent)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("Failed to parse XML content: " + e.getMessage());
            return null;
        }
    }

    
    /**
     * Walks through the given folder and its subfolders to find all xml files and loads their content
     *
     * @param folder the folder in which surefire placed its test reports
     * @return a List of String representing each the content of one xml report produced by maven::surefire
     * @throws IOException if the execution encounters an error while reading one xml file
     */
    private List<Document> loadXmlFromFolder(String folder) throws IOException {
        List<Document> files;
        try (var pathStream = Files.walk(Paths.get(folder))) {
            files = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .map(path -> {
                        String xmlFileContent = null;
                        try {
                            xmlFileContent = Files.readString(path);
                        } catch (IOException e) {
                            System.err.println("Failed to read file: " + path + " - " + e.getMessage());
                        }
                        return xmlFileContent;
                    })
                    .map(this::parseXml)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return files;
    }

    /**
     * If the code has already been compiled, this returns the console output
     *
     * @return the console output of the maven compilation
     * @throws IllegalStateException if the project has not been compiled yet
     */
    public String getCompilationOutput() {
        if (!isCompiled)
            throw new IllegalStateException("No compilation has been done yet.");
        return compilationOutput;
    }

    /**
     * If the tests have already been run, this returns the test result xml files
     *
     * @return the test result xml files from surefire
     * @throws IllegalStateException if the tests have not been run yet
     */
    public List<Document> getTestXml() {
        if (!isTested)
            throw new IllegalStateException("No test has been run yet.");
        return testXml;
    }
}
