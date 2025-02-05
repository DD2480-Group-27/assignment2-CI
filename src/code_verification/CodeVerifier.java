package code_verification;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeVerifier {

    public static final String TESTED_PROJECT_BASE_PATH = "/tmp/dd2480-builds/";

    private final File projectFolder;
    private boolean isCompiled;
    private String compilationOutput;
    private boolean isTested;
    private List<String> testXml;

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

    public boolean verifyCompilation() throws IOException, InterruptedException {
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

    public boolean runTests() throws IOException, InterruptedException {
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

    private List<String> loadXmlFromFolder(String folder) throws IOException {
        List<String> files;
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
                    .filter(Objects::nonNull)
                    .toList();
        }
        return files;
    }

    public String getCompilationOutput() {
        if (!isCompiled)
            throw new IllegalStateException("No compilation has been done yet.");
        return compilationOutput;
    }

    public List<String> getTestXml() {
        if (!isTested)
            throw new IllegalStateException("No test has been run yet.");
        return testXml;
    }
}
