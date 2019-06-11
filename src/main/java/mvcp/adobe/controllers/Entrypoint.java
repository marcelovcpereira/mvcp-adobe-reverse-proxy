package mvcp.adobe.controllers;

import mvcp.adobe.components.CacheManager;
import mvcp.adobe.entities.Request;
import mvcp.adobe.exceptions.CacheNotAvailableException;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;
import mvcp.adobe.proxy.ReverseProxy;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.ServiceHostNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Entrypoint is the unique controller of the Reverse Proxy application.
 * It is responsible for 2 concerns:
 * <ul>
 *     <li>Intercepts all incoming HTTP requests</li>
 *     <li>Decides if request should be handled by Cache or by the Proxy itself</li>
 * </ul>
 * <p>
 * Entrypoint's flow is as follows:
 * <ol>
 * <li>A HTTP request arrives
 * <li>Transforms it to an internal Request object
 * <li>Askes if Cache Manager can handle it.
 * <li>In case yes, asks Cache Manager for the cached data.
 * <li>In case no, asks Reverse Proxy to fetch the data.
 * <li>Returns the response from Cache or Proxy
 * </ol>
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
@RequestMapping("/")
@RestController
public class Entrypoint {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(Entrypoint.class);

    /**
     * Injection for getting environment variables
     */
    @Autowired
    private Environment env;

    /**
     * Reverse Proxy for executing the incoming request
     */
    @Autowired
    private ReverseProxy proxy;

    /**
     * Manager for treating Caching mechanism
     */
    @Autowired
    private CacheManager cache;


    /**
     * Captures all incoming HTTP requests.
     * The requests are processed into internal structure, then forwarded to the target Service where it will be
     * load-balaced. Finally, receives and returns back the response to the caller.
     *
     * This endpoint captures all HTTP method requests.
     *
     * @param body Map  Payload of the request
     * @param r HTTPServletRequest Context object containing information about the HTTP request
     * @return ResponseEntity HTTP response
     */
    @RequestMapping(value = "**")
    public ResponseEntity<String> get(@RequestBody(required = false) Map<String, String> body, HttpServletRequest r) {
        try {
            Request req = Request.fromContextRequest(r, body);
            Response response = null;
            try {
                response = cache.getCached(req);
                if (response != null) {
                    logger.info("+++++CACHE HIT+++++");
                } else {
                    logger.info("+++++CACHE MISS+++++");
                    response = proxy.processRequest(req);
                    cache.store(req, response);
                }
            } catch (CacheNotAvailableException cnae) {
                logger.info("+++++Cache server not available.+++++");
                logger.info("+++++TRACE: (" + cnae.getMessage() + ")+++++");
                response = proxy.processRequest(req);
            }

            HttpHeaders headers = new HttpHeaders();
            if (response.getHeaders() != null) {
                for (String key : response.getHeaders().keySet()) {
                    String value = response.getHeaders().get(key);
                    if (key == null) continue;
                    headers.set(key, value);
                }
            }
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.resolve(response.getStatus()));
        } catch (Exception e) {
            e.printStackTrace();
        } catch (ServiceHostNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), null, HttpStatus.NOT_FOUND);
        } catch (NoAvailableEndpointsException e) {
            return new ResponseEntity<>(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }
}