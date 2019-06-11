package mvcp.adobe.entities;

import mvcp.adobe.abstractions.ISerializer;
import mvcp.adobe.components.CacheManager;
import mvcp.adobe.components.MD5Serializer;
import mvcp.adobe.components.RedisCache;
import mvcp.adobe.exceptions.CacheNotAvailableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class CacheManagerTest extends BaseTest {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(RedisCache.class);
    public static final String CACHE_CONTROL_HEADER = "cache-control";
    public static final String START_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    @Mock
    private RedisCache redis = new RedisCache();

    private ISerializer serializer = new MD5Serializer();

    @InjectMocks
    private CacheManager cache;


    @Before
    public void setup() throws CacheNotAvailableException {
        MockitoAnnotations.initMocks(this);

        CacheItem item = new CacheItem();
        item.setResponse(getResponse1());
        item.setIsoCreated(START_TIME);
        String hash = serializer.hashRequest(getRequest1());
        String hash2 = serializer.serializeCacheItem(item);
        Mockito.when(redis.get(hash)).thenReturn(hash2);

        cache.setSerializer(serializer);
    }

    @Test
    public void shouldStoreResponse1WithPublic() throws CacheNotAvailableException {
        Request request = getRequest1();
        Response response = getResponse1();
        response.getHeaders().put(CACHE_CONTROL_HEADER, "public");
        cache.store(request, response);
        Mockito.verify(redis, Mockito.times(1)).put(eq(new MD5Serializer().hashRequest(request)), any());
    }

    @Test
    public void shouldNotStoreResponse1WithPrivate() throws CacheNotAvailableException {
        Request request = getRequest1();
        Response response = getResponse1();
        response.getHeaders().put(CACHE_CONTROL_HEADER, "private");
        cache.store(request, response);
        Mockito.verify(redis, Mockito.times(0)).put(eq(new MD5Serializer().hashRequest(request)), any());
    }

    @Test
    public void shouldNotStoreResponse1WithNoCache() throws CacheNotAvailableException {
        Request request = getRequest1();
        Response response = getResponse1();
        response.getHeaders().put(CACHE_CONTROL_HEADER, "no-cache");
        cache.store(request, response);
        Mockito.verify(redis, Mockito.times(0)).put(eq(new MD5Serializer().hashRequest(request)), any());
    }

    @Test
    public void shouldNotStoreResponse1WithNoStore() throws CacheNotAvailableException {
        Request request = getRequest1();
        Response response = getResponse1();
        response.getHeaders().put(CACHE_CONTROL_HEADER, "no-store");
        cache.store(request, response);
        Mockito.verify(redis, Mockito.times(0)).put(eq(new MD5Serializer().hashRequest(request)), any());
    }

    @Test
    public void shouldReturnResponse1() throws CacheNotAvailableException {
        Request empty = getRequest1();
        Response response = getResponse1();
        Response result = cache.getCached(empty);
        assertEquals(response.getBody(), result.getBody());
        assertEquals(response.getStatus(), result.getStatus());
        assertEquals(response.getHeaders(), result.getHeaders());
    }

    @Test
    public void shouldReturnResponse1WithPublic() throws CacheNotAvailableException {
        Request request = getRequest1();
        request.getHeaders().put(CACHE_CONTROL_HEADER, "public");
        Response response = getResponse1();
        Response result = cache.getCached(request);
        assertEquals(response.getBody(), result.getBody());
        assertEquals(response.getStatus(), result.getStatus());
        assertEquals(response.getHeaders(), result.getHeaders());
    }

    @Test
    public void shouldReturnResponse1WithMaxAge() throws CacheNotAvailableException {
        Request request = getRequest1();
        request.getHeaders().put(CACHE_CONTROL_HEADER, "max-age=3");
        Response response = getResponse1();
        Response result = cache.getCached(request);
        assertEquals(response.getBody(), result.getBody());
        assertEquals(response.getStatus(), result.getStatus());
        assertEquals(response.getHeaders(), result.getHeaders());
    }

    @Test
    public void shouldNotReturnWhenMaxAgeExceeded() throws CacheNotAvailableException, InterruptedException {
        Thread.sleep(1000);
        Request request = getRequest1();
        request.getHeaders().put(CACHE_CONTROL_HEADER, "max-age=1");
        Response result = cache.getCached(request);
        assertNull(result);
    }

    @Test
    public void shouldNotReturnWhenInvalidRequest() throws CacheNotAvailableException {
        Request request = getRequest1();
        request.setPath("?????????????");
        Response result = cache.getCached(request);
        assertNull(result);
    }

    @Test
    public void shouldNotReturnWhenNoCacheHeader() throws CacheNotAvailableException {
        Request empty = getRequest1();
        empty.getHeaders().put(CACHE_CONTROL_HEADER, "no-cache");
        Response result = cache.getCached(empty);
        assertNull(result);
    }

    @Test
    public void shouldNotReturnWhenNoStoreHeader() throws CacheNotAvailableException {
        Request empty = getRequest1();
        empty.getHeaders().put(CACHE_CONTROL_HEADER, "no-store");
        Response result = cache.getCached(empty);
        assertNull(result);
    }

    @Test
    public void shouldNotReturnWhenPrivateHeader() throws CacheNotAvailableException {
        Request empty = getRequest1();
        empty.getHeaders().put(CACHE_CONTROL_HEADER, "private");
        Response result = cache.getCached(empty);
        assertNull(result);
    }


    /**
     * Returns mocked Request for testing
     * @return Request mocked request
     */
    private Request getRequest1() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "public");
        Map<String, String> body = new HashMap<>();
        body.put("MY_FIELD", "MY_VALUE");

        Request request = new Request();
        request.setHeaders(headers);
        request.setBody(body);
        request.setPath("PATH_TO_BE_RETRIEVED");

        return request;
    }

    /**
     * Returns mocked Response for testing
     * @return Response mocked response
     */
    private Response getResponse1() {
        Response response = new Response();
        response.setHeaders(new HashMap<>());
        response.setBody("MY_RETURNED_BODY");
        response.setStatus(200);
        return response;
    }
}
