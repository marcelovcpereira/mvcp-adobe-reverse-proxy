package mvcp.adobe.entities;

import mvcp.adobe.exceptions.ServiceHostNotFoundException;
import mvcp.adobe.proxy.ReverseProxy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ReverseProxyTest extends BaseTest {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(ReverseProxyTest.class);
    @Test
    public void shouldDiscoverService() throws ServiceHostNotFoundException {
        ReverseProxy proxy = new ReverseProxy();
        proxy.registerService(roundrobin);
        proxy.registerService(random);

        Request request = new Request();
        Map<String, String> headers = new HashMap<>();
        headers.put("host", roundrobin.getDomain());
        request.setHeaders(headers);
        assertTrue(proxy.discoverService(request).equals(roundrobin));

        headers.put("host", random.getDomain());
        request.setHeaders(headers);
        assertTrue(proxy.discoverService(request).equals(random));
    }

    @Test(expected = ServiceHostNotFoundException.class)
    public void shouldNotDiscoverService() throws ServiceHostNotFoundException {
        ReverseProxy proxy = new ReverseProxy();
        proxy.registerService(roundrobin);
        proxy.registerService(random);

        Request request = new Request();
        Map<String, String> headers = new HashMap<>();
        headers.put("host", "MyCrazyDomain.com");
        request.setHeaders(headers);
        proxy.discoverService(request);
    }
}
