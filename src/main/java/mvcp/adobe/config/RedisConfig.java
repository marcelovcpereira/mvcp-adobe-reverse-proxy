package mvcp.adobe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Initializes and controls connections to Redis cache.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
@Configuration
public class RedisConfig {

    @Autowired
    private Environment env;

    /**
     * Creates a factory for instancing connections to the target Redis instance.
     *
     * @return RedisConnectionFactory A factory for getting connections to the Redis instance.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
        connectionFactory.setHostName(env.getProperty("REDIS_HOST"));
        connectionFactory.setPort(Integer.parseInt(env.getProperty("REDIS_PORT")));
        return connectionFactory;
    }

    /**
     * Component that manages connection to Redis using String template.
     *
     * @return String template manager for Redis
     */
    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}