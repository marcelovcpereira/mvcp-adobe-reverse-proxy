package mvcp.adobe.components;

import io.lettuce.core.RedisConnectionException;
import mvcp.adobe.abstractions.ICache;
import mvcp.adobe.exceptions.CacheNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementation of a Cache storage using Redis.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-10
 */
@Component
@Qualifier("redis")
public class RedisCache implements ICache {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(RedisCache.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public RedisCache(){}

    /**
     * Stores a string in a Hash key in the Cache.
     *
     * @param key String Hash key index to store
     * @param value String Value to be store
     * @throws CacheNotAvailableException Thrown when no Cache is set up
     */
    public void put(String key, String value) throws CacheNotAvailableException {
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
    public String get(String key) throws CacheNotAvailableException {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (RedisConnectionException | RedisConnectionFailureException re) {
            throw new CacheNotAvailableException("The cache server is inaccessible: " + re.getMessage());
        }
    }
}

