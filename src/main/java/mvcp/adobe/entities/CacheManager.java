package mvcp.adobe.entities;

import com.google.gson.Gson;
import io.lettuce.core.RedisConnectionException;
import mvcp.adobe.exceptions.CacheNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Manages HTTP Cache Control logic.
 * Current implemented Cache Control properties: no-cache, no-store, private, max-age
 *
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
 *
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
 *     <li>no-cache: Does not store Response on cache.</li>
 *     <li>no-store: Does not store Response on cache.</li>
 *     <li>private: Does not store Response on cache.</li>
 * </ul>
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-10
 */
@Component
public class CacheManager {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(CacheManager.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Stores a string in a Hash key in the Cache.
     *
     * @param key
     * @param value
     * @throws CacheNotAvailableException
     */
    private void put(String key, String value) throws CacheNotAvailableException {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (RedisConnectionException | RedisConnectionFailureException rce) {
            throw new CacheNotAvailableException("The cache server is inaccessible: " + rce.getMessage());
         }
    }

    /**
     * Retrieves a string from a Hash key in the Cache
     *
     * @param key String hashed index
     * @return String Serialized cached object
     * @throws CacheNotAvailableException Thrown in case Cache is not accessible
     */
    private String get(String key) throws CacheNotAvailableException {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (RedisConnectionException | RedisConnectionFailureException re) {
            throw new CacheNotAvailableException("The cache server is inaccessible: " + re.getMessage());
        }
    }

    /**
     * Verifies if a request can be fulfilled with cached data based on Cache Control headers.
     *
     * @param request Incoming HTTP Request
     * @return boolean True in case the Response can be retrieved from Cache
     */
    private boolean canGetFromCache(Request request) {
        Map<String, String> headers = request.getHeaders();
        for (String header : headers.keySet()) {
            String value = headers.get(header);
            if (header.trim().equalsIgnoreCase("cache-control")) {
                String[] parts = value.split(",");
                for (String part : parts) {
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
        for (String header : headers.keySet()) {
            String value = headers.get(header);
            if (header != null && header.trim().equalsIgnoreCase("cache-control")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    part = part.trim();
                    if (part.equalsIgnoreCase("no-cache")
                            || part.equalsIgnoreCase("no-store")
                            || part.equalsIgnoreCase("private")) {
                        logger.info("+++++Skipping cache store via response header: " + part + "+++++");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Transforms a Request into a Hashed index
     *
     * @param req Request to be transformed in Hash
     * @return String A hash representation of the request
     */
    private String hashRequest(Request req) {
        String s = new Gson().toJson(req);
        Request clone = new Gson().fromJson(s, Request.class);
        clone.setHeaders(null);
        s = new Gson().toJson(clone);
        logger.info("+++++Hash request: " + s + "+++++");
        return getMd5(s);
    }

    /**
     * Hashes a string into a MD5 hex string
     * Ref: https://www.geeksforgeeks.org/md5-hash-in-java/
     *
     * @param input String to be hashed
     * @return String A MD5 Hash representation of the string
     */
    public static String getMd5(String input) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract a response from a Hash
     *
     * @param hash Serialized cached item
     * @return CacheItem Deserialized object
     */
    private CacheItem unhashItem(String hash) {
        return new Gson().fromJson(hash, CacheItem.class);
    }

    /**
     * Generates a hash version of a CacheItem for being stored in the Cache.
     *
     * @param item Item to be cached
     * @return String A hash representation of the object to be cached
     */
    private String hashItem(CacheItem item) {
        return new Gson().toJson(item);
    }

    /**
     * Verifies if the age of the response respects the max-age limit of the request.
     *
     * @param request Incoming HTTP request
     * @param item Object retrieved from cache
     * @return boolean True if the object from cache is still valid under Request's configuration of Cache Control
     */
    private boolean cacheItemIsValid(Request request, CacheItem item) {
        Date now = new Date();
        Date created = null;
        try {
            created = item.getCreated();
        } catch (ParseException e) {
            return false;
        }
        long ageInSeconds = (now.getTime()-created.getTime())/1000;
        logger.info("+++++Cache Item has Age: " + ageInSeconds + "s+++++");
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
            String hash = hashRequest(request);
            String value = get(hash);
            if (value != null) {
                CacheItem item = unhashItem(value);
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
     * @param request The request that should be used as key
     * @param response The response that should be stored in cache
     * @throws CacheNotAvailableException Thrown when no Cache mechanism is available
     */
    public void store(Request request, Response response) throws CacheNotAvailableException {
        if (canCache(response)) {
            String hash = hashRequest(request);
            logger.info("+++++Caching response in hash: " + hash + "+++++");
            put(hash, hashItem(new CacheItem(response, new Date())));
        }
    }
}
