package mvcp.adobe.entities;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class Request {
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

    public String stringifyBody() {
        List<String> params = new ArrayList<>();
        for (String key : this.body.keySet()) {
            String value = this.body.get(key);
            params.add(key + "=" + value);
        }
        return String.join("&", params);
    }

    public String jsonBody() {
        return new Gson().toJson(this.body);
    }

    public static Request fromContextRequest(HttpServletRequest request, Map<String,String> body) {
        Request req = null;
        if (request != null) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                headers.put(name, value);
            }
            req = new Request(request.getProtocol().replace("HTTP/", ""), request.getMethod(), request.getRequestURI(), headers, body);
        }
        return req;
    }

    public String getHostHeader() {
        String host = this.getHeaders().get("host");
        if (host == null) host = this.getHeaders().get("Host");
        return host;
    }
}

