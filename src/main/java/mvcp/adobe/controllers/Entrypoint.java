package mvcp.adobe.controllers;

import mvcp.adobe.entities.Request;
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

@RequestMapping("/")
@RestController
public class Entrypoint {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(Entrypoint.class);

    @Autowired
    private Environment env;

    @Autowired
    private ReverseProxy proxy;


    @RequestMapping(value = "**")
    public ResponseEntity<String> get(@RequestBody(required = false) Map<String, String> body, HttpServletRequest r) {
        try {
            Request req = Request.fromContextRequest(r, body);
            Response res = proxy.processRequest(req);

            HttpHeaders headers = new HttpHeaders();
            if (res.getHeaders() != null) {
                for (String key : res.getHeaders().keySet()) {
                    String value = res.getHeaders().get(key);
                    if (key == null) continue;
                    headers.set(key, value);
                }
            }

            return new ResponseEntity<>(res.getBody(), headers, HttpStatus.resolve(res.getStatus()));
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