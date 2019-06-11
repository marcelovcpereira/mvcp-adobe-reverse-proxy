package mvcp.adobe.abstractions;

import mvcp.adobe.entities.CacheItem;
import mvcp.adobe.entities.Request;
/**
 * Defines the interface of a serializer
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-11
 */
public interface ISerializer {

    String hashRequest(Request request);
    CacheItem deserializeCacheItem(String hash);
    String serializeCacheItem(CacheItem item);
}
