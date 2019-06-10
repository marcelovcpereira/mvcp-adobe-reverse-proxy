package mvcp.adobe.exceptions;

public class CacheNotAvailableException extends Throwable {
    public CacheNotAvailableException(String the_cache_server_is_inaccessible) {
        super(the_cache_server_is_inaccessible);
    }
}
