import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class Main extends AbstractHandler
{
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        
        String repo = "https://github.com/DD2480-Group-27/assignment2-CI.git";

       try {
            System.out.println("Cloning repository from " + repo);
            Git.cloneRepository()
                    .setURI(repo)
                    .setDirectory(null)
                    .call();
            System.out.println("Repository cloned successfully.");
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        // 2nd compile the code
       
    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        
        Server server = new Server(8080);
        server.setHandler(new Main()); 
        server.start();
        server.join();
        
      
    }
}
