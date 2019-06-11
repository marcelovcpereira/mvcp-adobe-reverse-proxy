package mvcp.adobe.entities;

import java.util.Map;
/**
 * Response is the internal representation of a HTTP response.
 * It contains as attributes:
 * <ul>
 *     <li>headers: HTTP response headers</li>
 *     <li>body; HTTP response body payload</li>
 *     <li>status: HTTP response status</li>
 * </ul>
 * <p>
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
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
