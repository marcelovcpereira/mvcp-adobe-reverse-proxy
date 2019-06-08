package mvcp.adobe.abstractions;

import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;

public interface IServiceHandler {
    Response processRequest(Request request) throws Exception, NoAvailableEndpointsException;
}
