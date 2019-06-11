package mvcp.adobe.entities;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
/**
 * Request is the internal representation of a HTTP request.
 * It contains as attributes:
 * <ul>
 *     <li>headers: HTTP headers</li>
 *     <li>body; HTTP body payload</li>
 *     <li>path: Path of the HTTP request</li>
 *     <li>version: HTTP version used by the caller</li>
 *     <li>method: HTTP method </li>
 * </ul>
 * <p>
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class Request {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(Request.class);
    private Map<String, String> headers;
    private Map<String, String> body;
    private String path;
    private String method;
    private String version;


    public Request() {}

    public Request(String version, String method, String path, Map<String, String> headers, Map<String, String> body) {
        this.version = version;
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns a query string representation of the Request's body.
     *
     * @return String Query string of request body
     */
    public String stringifyBody() {
        List<String> params = new ArrayList<>();
        for (String key : this.body.keySet()) {
            String value = this.body.get(key);
            params.add(key + "=" + value);
        }
        return String.join("&", params);
    }

    /**
     * Returns a JSON representation of the Request's body.
     *
     * @return String Json of request body
     */
    public String jsonBody() {
        return new Gson().toJson(this.body);
    }

    /**
     * Builds and returns a Request object from a HttpServletRequest object and the body payload.
     *
     * @param request Internal servlet http request
     * @param body Payload coming in the request
     * @return Request Internal request object generated from servlet context
     */
    public static Request fromContextRequest(HttpServletRequest request, Map<String,String> body) {
        Request req = null;
        logger.info("Creating request object...");
        if (request != null) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                headers.put(name, value);
            }
            logger.info("Found the following headers:");
            String h = "";
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                h += key + ":" + value + ",";
            }
            logger.info(h.substring(0, h.length()-1));
            req = new Request(request.getProtocol().replace("HTTP/", ""), request.getMethod(), request.getRequestURI(), headers, body);
            logger.info("Request created: " + new Gson().toJson(req));
        }
        return req;
    }

    /**
     * Returns the HTTP 'Host' header value from the request.
     *
     * @return String Value of the 'Host' header
     */
    public String getHostHeader() {
        String host = this.getHeaders().get("host");
        if (host == null) host = this.getHeaders().get("Host");
        return host;
    }
}