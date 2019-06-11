package mvcp.adobe.exceptions;

/**
 * Exception thrown when the Cache Manager cannot access the external Cache mechanism.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-10
 */
public class CacheNotAvailableException extends Throwable {
    public CacheNotAvailableException(String the_cache_server_is_inaccessible) {
        super(the_cache_server_is_inaccessible);
    }
}
