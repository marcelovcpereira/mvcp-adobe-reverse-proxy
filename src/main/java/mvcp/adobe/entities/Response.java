package mvcp.adobe.entities;

import java.util.Map;

public class Response {
    private Map<String, String> headers;
    private String body;
    private int status;

    public Response() {}
    public Response(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
