import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import org.json.JSONObject;


public class CIServer extends AbstractHandler {
    
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException 
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // System.out.println(target);

        // Read the JSON payload
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line).append("\n");
            }
        }

        // System.out.println("Payload received:\n" + payload.toString());

        try {
            JSONObject json = new JSONObject(payload.toString());
            String ref = json.getString("ref");
            String htmlUrl = json.getJSONObject("repository").getString("html_url");

            System.out.println("Ref: " + ref);
            System.out.println("HTML URL: " + htmlUrl);

        } catch (Exception e) {
            System.out.println("Error parsing JSON payload: " + e.getMessage());
        }

        response.getWriter().println("The CI server says 'Hello!'");
    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8027);
        server.setHandler(new CIServer()); 
        server.start();
        server.join();
    }
}
