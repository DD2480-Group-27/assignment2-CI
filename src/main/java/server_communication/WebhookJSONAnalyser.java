package server_communication;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.json.JSONObject;
import java.io.File;
import java.util.List;

/**
 * Public class intended to fetch relevant data from JSON webhook request
 */
public class WebhookJSONAnalyser {

    private final String repoPath;
    private final String commitHash;
    private final String commitMail;
    private final String commitMessage;
    private final String commitRef;
    private final String commitBranch;
    private final String commitAuthor;

    private final String repoURL; // Set after cloning the repo

    /**
     * Class constructor will parse the given JSON String and set its parameters to the relevant values
     *
     * @param payload a String containing a JSON encoded GitHub webhook payload
     */
    public WebhookJSONAnalyser(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            // Get the branch name
            this.commitRef = json.getString("ref");
            // Get the repo URL
            this.repoURL = json.getJSONObject("repository").getString("html_url");
            // Get the commit hash id
            this.commitHash = json.getJSONObject("head_commit").getString("id");
            // Parse the ref into a proper branch name
            this.commitBranch = commitRef.replace("refs/heads/", "");
            // Get the commit mail
            this.commitMail = json.getJSONObject("head_commit").getJSONObject("committer").getString("email");
            // Get the commit author
            this.commitAuthor = json.getJSONObject("head_commit").getJSONObject("committer").getString("name");
            // Get the commit message
            this.commitMessage = json.getJSONObject("head_commit").getString("message");


            System.out.println("The received webhook data:");
            System.out.println("--------------------------------------------------");
            System.out.println("Ref: " + commitRef);
            System.out.println("Branch Name: " + commitBranch);
            System.out.println("Repo URL: " + repoURL);
            System.out.println("Commit Hash: " + commitHash);
            System.out.println("Commit Mail: " + commitMail);
            System.out.println("Commit Author: " + commitAuthor);
            System.out.println("Commit Message: " + commitMessage);
            System.out.println("--------------------------------------------------");

            // Store the path to the cloned repo
            this.repoPath = cloneRepo(repoURL, commitHash, commitBranch);

        } catch (Exception e) {
            throw new RuntimeException("The repo is not cloned successfully");
        }


    }

    /**
     * Clones a Git repository to a temporary directory and resets it to a specific commit.
     * <p>
     * This method creates a unique directory for the specified commit within a base
     * temporary directory. If a directory for the commit already exists, it is deleted
     * before the clone operation. After cloning, the method checks out the specified
     * branch and performs a hard reset to ensure the working directory matches the
     * exact state of the specified commit.
     * </p>
     *
     * @param repoURL    The URL of the Git repository to clone.
     * @param commitHash The hash of the commit to which the repository should be reset.
     * @param branchName The name of the branch to check out after cloning.
     * @return The absolute path of the cloned repository in the temporary directory.
     * Returns null if an error occurs during the cloning process.
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
            if (repoDir.exists()) {
                deleteDirectory(repoDir);
            }
            //then create a new empty directory
            repoDir.mkdir();

            // Clone the repository
            Git git = Git.cloneRepository()
                    .setURI(repoURL)
                    .setDirectory(repoDir)
                    .call();

            // Fetch all branches
            git.fetch().call();
            // List remote branches to confirm if the branch exists
            List<Ref> remoteBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            try {
                // Try exact branch name first
                if (git.getRepository().findRef(branchName) != null) {
                    git.checkout().setName(branchName).call();
                }
                // Try common branch name patterns
                String[] branchPatterns = {
                        branchName,
                        "refs/heads/" + branchName,
                        "refs/remotes/origin/" + branchName,
                        "origin/" + branchName
                };

                for (String pattern : branchPatterns) {
                    if (remoteBranches.stream().anyMatch(ref -> ref.getName().equals(pattern))) {
                        git.checkout().setName(pattern).call();
                    }
                }
            } catch (GitAPIException e) {
                System.err.println("Error checking out branch: " + e.getMessage());
                return null;
            }
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
     * Deletes a repo assuming it has been created using the naming convention of this project and the suitable
     * location according to the same conventions
     * @param commitHash the hash of the commit that triggered the cloning of the repo on the machine for testing
     *                   purposes
     */
    public void deleteRepo(String commitHash){
        // Get system temp directory in a platform-independent way
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File baseDir = new File(tempDir, "dd2480-builds");
        if (!baseDir.exists()) {
            throw new RuntimeException("dd2480-builds directory does not exist!");
        }
        String buildDir = "build-" + commitHash;
        File repoDir = new File(baseDir, buildDir);
        //if the directory does not exist for a commit hash, throw error
        if (!repoDir.exists()) {
            throw new RuntimeException("The repo does not exist!");
        }
        //else delete it
        deleteDirectory(repoDir);

    }

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
        directory.delete();
    }

    /**
     * Attribute getter for the commit email address
     * @return a String containing the commit author's email
     */
    public String getCommitMail() {
        return commitMail;
    }

    /**
     * Attribute getter for the absolute path to the location where the repository has been cloned
     * @return a String representing the absolut path to where the repo has been cloned and set to the right version
     */
    public String getRepoPath() {
        return repoPath;
    }

    /**
     * Attribute getter for the commit hash
     * @return a String containing the commit hash
     */
    public String getCommitHash() {
        return commitHash;
    }

    /**
     * Attribute getter for the commit message
     * @return a String containing the commit message
     */
    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * Attribute getter for the commit reference
     * @return a String containing the commit reference
     */
    public String getCommitRef() {
        return commitRef;
    }

    /**
     * Attribute getter for the commit branch
     * @return a String containing the commit branch
     */
    public String getCommitBranch() {
        return commitBranch;
    }

    /**
     * Attribute getter for the repository's URL
     * @return a String containing the URL
     */
    public String getRepoURL() {
        return repoURL;
    }

    /**
     * Attribute getter for the commit author
     * @return a String containing the commit author's username
     */
    public String getCommitAuthor() {
        return commitAuthor;
    }

}

