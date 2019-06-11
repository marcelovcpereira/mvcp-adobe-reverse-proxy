package mvcp.adobe.abstractions;

import mvcp.adobe.entities.Endpoint;
import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;

import java.util.List;
/**
 * Defines the interface of a load balancer
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-11
 */
public interface ILoadBalancer {
    Response balance(Request request) throws NoAvailableEndpointsException;
    Endpoint nextEndpoint();
    boolean hasEndpointCandidate();
    List<Endpoint> getEndpointCandidates();
}
