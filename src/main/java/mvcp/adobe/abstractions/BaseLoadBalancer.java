package mvcp.adobe.abstractions;

import mvcp.adobe.connection.HttpForwarder;
import mvcp.adobe.entities.Endpoint;
import mvcp.adobe.enums.EndpointStatus;
import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
/**
 * Abstraction that contains the basic behavior of a Load Balancer.
 * It is responsible for trying to fulfil a request using one of the available Endpoints.
 * <p>
 * It tries all available Endpoints until some of them fulfils the request or all fail.
 * The strategy of electing which Endpoint should be the next candidate depends on the
 * routing strategy implemented in the subclasses.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public abstract class BaseLoadBalancer implements ILoadBalancer {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(BaseLoadBalancer.class);
    protected List<Endpoint> endpoints;

    public BaseLoadBalancer() {
        this.endpoints = new ArrayList<>();
    }

    public BaseLoadBalancer(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Applies the defined strategy to retrieve the response from the elected endpoint
     *
     * @param request Request to be executed
     * @return Response Response sent from the Endpoint
     * @throws NoAvailableEndpointsException Thrown in case no Endpoint is available for executing the request
     */
    public Response balance(Request request) throws NoAvailableEndpointsException {
        if (request != null) {
            while (hasEndpointCandidate()) {
                Endpoint endpoint = nextEndpoint();
                try {
                    logger.info("Trying with endpoint " + endpoint);
                    Response response = HttpForwarder.execute(request, endpoint);
                    logger.info("Sucess. Marking as active.");
                    endpoint.setStatus(EndpointStatus.ACTIVE);
                    return response;
                } catch (Exception e) {
                    logger.error("Failed. Marking as suspended.");
                    endpoint.setStatus(EndpointStatus.SUSPENDED);
                }
            }
            throw new NoAvailableEndpointsException("No endpoint could fulfil the request. Service unavailable");
        }
        return null;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Verifies if any of the registered Endpoint is marked as able to execute the request.
     *
     * @return True in case there is at least one Endpoint candidate
     */
    public boolean hasEndpointCandidate() {
        boolean ret = false;
        for (Endpoint e : endpoints) {
            if (e.getStatus() == EndpointStatus.ACTIVE || e.getStatus() == EndpointStatus.PENDING) return true;
        }
        return ret;
    }

    /**
     * Returns a list of all Endpoints marked as able to execute a Request, meaning ACTIVE or PENDING.
     *
     * @return List A list of Endpoint that are candidates to execute a request
     */
    public List<Endpoint> getEndpointCandidates() {
        List<Endpoint> ret = new ArrayList<>();
        for (Endpoint e : endpoints) {
            if (e.getStatus() == EndpointStatus.ACTIVE || e.getStatus() == EndpointStatus.PENDING) {
                ret.add(e);
            }
        }
        return ret;
    }

    /**
     * Abstract method that is responsible for electing which Endpoint should be used to execute the request.
     *
     * @return Endpoint The Endpoint elected by the Load Balance strategy
     */
    public abstract Endpoint nextEndpoint();
}
