package mvcp.adobe.exceptions;

/**
 * Exception thrown when the Property String cannot be parsed into a Service.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class InvalidServiceDefinitionException extends Throwable {
    public InvalidServiceDefinitionException(String s) {
        super(s);
    }
}
