package mvcp.adobe.connection;

import mvcp.adobe.entities.Endpoint;
import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Executes HTTP requests from a Request Object, forwarding its contents, then returns the HTTP Response
 * back to the caller.
 * 
 */
public class HttpForwarder {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(HttpForwarder.class);

    public static Response execute(Request r, Endpoint e) throws Exception {
        //Request
        String url = "http://" + e.getIp() + ":" + e.getPort() + r.getPath();
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod(r.getMethod());
        logger.info(r.getMethod() + " " + url);

        //Request headers
        for (String key : r.getHeaders().keySet()) {
            con.setRequestProperty(key, r.getHeaders().get(key));
        }

        //Request Body
        if (r.getBody() != null && r.getBody().size() > 0) {
            logger.info(r.stringifyBody());
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(r.jsonBody());
            wr.flush();
            wr.close();
        }

        //Response Body
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        logger.info("Response " + con.getResponseCode() + ": " + response.toString());


        //Response Headers
        Map<String,String> headers = new HashMap<>();
        for (String key : con.getHeaderFields().keySet()) {
            List<String> values = con.getHeaderFields().get(key);
            headers.put(key, String.join(",", values));
        }


        Response s = new Response();
        s.setStatus(con.getResponseCode());
        s.setHeaders(headers);
        s.setBody(response.toString());
        return s;
    }
}