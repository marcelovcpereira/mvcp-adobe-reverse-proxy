package mvcp.adobe.abstractions;

import mvcp.adobe.connection.HttpForwarder;
import mvcp.adobe.entities.Endpoint;
import mvcp.adobe.entities.EndpointStatus;
import mvcp.adobe.entities.Request;
import mvcp.adobe.entities.Response;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Balancer {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(Balancer.class);
    protected List<Endpoint> endpoints;

    public Balancer() {
        this.endpoints = new ArrayList<>();
    }

    public Balancer(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    //Applies the defined strategy to retrieve the response from the elected endpoint
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

    protected boolean hasEndpointCandidate() {
        boolean ret = false;
        for (Endpoint e : endpoints) {
            if (e.getStatus() == EndpointStatus.ACTIVE || e.getStatus() == EndpointStatus.PENDING) return true;
        }
        return ret;
    }

    protected List<Endpoint> getEndpointCandidates() {
        List<Endpoint> ret = new ArrayList<>();
        for (Endpoint e : endpoints) {
            if (e.getStatus() == EndpointStatus.ACTIVE || e.getStatus() == EndpointStatus.PENDING) {
                ret.add(e);
            }
        }
        return ret;
    }

    public abstract Endpoint nextEndpoint();
}
