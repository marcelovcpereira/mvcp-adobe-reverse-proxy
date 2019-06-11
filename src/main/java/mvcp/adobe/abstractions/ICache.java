package mvcp.adobe.abstractions;

import mvcp.adobe.exceptions.CacheNotAvailableException;

/**
 * Defines the interface of a Cache handler
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-11
 */
public interface ICache {
    void put(String key, String value) throws CacheNotAvailableException;
    String get(String key) throws CacheNotAvailableException;
}
