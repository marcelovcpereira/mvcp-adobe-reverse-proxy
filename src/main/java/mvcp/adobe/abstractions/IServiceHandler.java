package mvcp.adobe.abstractions;

import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;

/**
 * Defines the basic contract of a Service Handler, that should process a Request
 * and return a Response.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public interface IServiceHandler {
    Response processRequest(Request request) throws Exception, NoAvailableEndpointsException;
}
