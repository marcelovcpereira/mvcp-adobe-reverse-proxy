package mvcp.adobe.exceptions;

/**
 * Exception thrown when there is no Endpoint in a Service that is able to handle the request
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class NoAvailableEndpointsException extends Throwable {
    public NoAvailableEndpointsException(String s) {
        super(s);
    }
}
