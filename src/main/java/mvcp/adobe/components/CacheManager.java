package mvcp.adobe.components;

import mvcp.adobe.abstractions.ICache;
import mvcp.adobe.abstractions.ISerializer;
import mvcp.adobe.entities.CacheItem;
import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.CacheNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Manages HTTP Cache Control logic.
 * Current implemented Cache Control properties: no-cache, no-store, private, max-age
 * <br>
 * CacheManager overall flow:
 * <ol>
 * <li>An incoming Request arrives</li>
 * <li>Verifies if the request can be extracted from Cache based on request's Cache Control headers</li>
 * <li>In case no, return empty</li>
 * <li>In case yes:</li>
 * <li>Hashes the current request (for uniquely identifying a request</li>
 * <li>Verifies if a Response exists in cache for the hashed key</li>
 * <li>In case no, return empty</li>
 * <li>In case yes:</li>
 * <li>Retrieves the cached item</li>
 * <li>Deserializes the stored object</li>
 * <li>Validates if cached object is still valid based on request/response Cache Control headers</li>
 * <li>Returns cached object</li>
 * </ol>
 * <p>
 * <p>
 * Behaviors of headers on request:
 * <ul>
 * <li>no-cache: Skips cache, execute query and then cache Response</li>
 * <li>no-store: Skips cache, execute query and then cache Response</li>
 * <li>private: Skips cache, execute query and then cache Response</li>
 * <li>max-age: Validate if cached object is not older than max-age in seconds</li>
 * </ul>
 * <p>
 * Behaviors of headers on response:
 * <ul>
 * <li>no-cache: Does not store Response on cache.</li>
 * <li>no-store: Does not store Response on cache.</li>
 * <li>private: Does not store Response on cache.</li>
 * </ul>
 *
 * @author Marcelo Pereira
 * @version 1.0.0
 * @since 2019-06-10
 */
@Component
public class CacheManager {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(CacheManager.class);

    @Autowired
    @Qualifier("redis")
    private ICache redisCache;

    @Autowired
    private ISerializer serializer;

    public CacheManager() {
    }

    /**
     * Verifies if a request can be fulfilled with cached data based on Cache Control headers.
     *
     * @param request Incoming HTTP Request
     * @return boolean True in case the Response can be retrieved from Cache
     */
    private boolean canGetFromCache(Request request) {
        Map<String, String> headers = request.getHeaders();
        if (headers != null) {
            String value = headers.get("cache-control");
            if (value != null) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    if (part == null) continue;
                    part = part.trim();
                    if (part.equalsIgnoreCase("no-cache")
                            || part.equalsIgnoreCase("no-store")
                            || part.equalsIgnoreCase("private")) {
                        logger.info("+++++Skipping cache via header: " + part + "+++++");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifies if a response can be cached based on Cache Control headers
     *
     * @param response Incoming HTTP response
     * @return boolean True in case the response can be cached
     */
    private boolean canCache(Response response) {
        Map<String, String> headers = response.getHeaders();
        if (headers != null) {
            for (String header : headers.keySet()) {
                String value = headers.get(header);
                if (header != null && header.trim().equalsIgnoreCase("cache-control")) {
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        part = part.trim();
                        if (part != null && (part.equalsIgnoreCase("no-cache")
                                || part.equalsIgnoreCase("no-store")
                                || part.equalsIgnoreCase("private"))) {
                            logger.info("+++++Skipping cache store via response header: " + part + "+++++");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    /**
     * Verifies if the age of the response respects the max-age limit of the request.
     *
     * @param request Incoming HTTP request
     * @param item    Object retrieved from cache
     * @return boolean True if the object from cache is still valid under Request's configuration of Cache Control
     */
    public boolean cacheItemIsValid(Request request, CacheItem item) {
        Date now = new Date();
        Date created = null;
        try {
            created = item.getCreated();
        } catch (ParseException e) {
            return false;
        }
        long ageInSeconds = (now.getTime() - created.getTime()) / 1000;
        logger.info("+++++Cache Item has Age: " + ageInSeconds + "s+++++");
        if (request.getHeaders() != null) {
            for (String header : request.getHeaders().keySet()) {
                String value = request.getHeaders().get(header);
                if (header.trim().equalsIgnoreCase("cache-control")) {
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        part = part.trim();
                        if (part.contains("max-age")) {
                            long max = Long.parseLong(part.split("=")[1]);
                            if (ageInSeconds >= max) {
                                logger.info("+++++Skipping cache via header: " + part + "+++++");
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tries to return a cached Response based on a Request configuration.
     *
     * @param request Incoming HTTP request
     * @return response Cached response
     * @throws CacheNotAvailableException Thrown when no Cache mechanism is available
     */
    public Response getCached(Request request) throws CacheNotAvailableException {
        if (canGetFromCache(request)) {
            String hash = serializer.hashRequest(request);
            String value = redisCache.get(hash);
            if (value != null) {
                CacheItem item = serializer.deserializeCacheItem(value);
                Response response = item.getResponse();
                if (cacheItemIsValid(request, item)) {
                    return response;
                }
            }
        }
        return null;
    }

    /**
     * Stores a Response in the cache under a key derived from the Request.
     *
     * @param request  The request that should be used as key
     * @param response The response that should be stored in cache
     * @throws CacheNotAvailableException Thrown when no Cache mechanism is available
     */
    public void store(Request request, Response response) throws CacheNotAvailableException {
        if (canCache(response)) {
            String hash = serializer.hashRequest(request);
            logger.info("+++++Caching response in hash: " + hash + "+++++");
            redisCache.put(hash, serializer.serializeCacheItem(new CacheItem(response)));
        }
    }

    /**
     * Sets a Serializer instance.
     *
     * @param s
     */
    public void setSerializer(ISerializer s) {
        this.serializer = s;
    }
}
