package mvcp.adobe.exceptions;

/**
 * Exception thrown when the incoming Request has no domain information on the HTTP header: 'Host'
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class ServiceHostNotFoundException extends Throwable {
    public ServiceHostNotFoundException(String s) {
        super(s);
    }
}
