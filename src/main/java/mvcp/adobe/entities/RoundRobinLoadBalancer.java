package mvcp.adobe.entities;

import mvcp.adobe.abstractions.BaseLoadBalancer;

import java.util.List;

/**
 * Load balancer implementation of a Round Robin election strategy.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class RoundRobinLoadBalancer extends BaseLoadBalancer {
    private int current;

    public RoundRobinLoadBalancer(List<Endpoint> endpoints) { super(endpoints); }

    /**
     * Decides which of candidate Endpoints should be used.
     *
     * @return Endpoint The elected endpoint to handle the request.
     */
    @Override
    public Endpoint nextEndpoint() {
        if (this.current >= this.getEndpointCandidates().size()) this.current = 0;
        return this.getEndpointCandidates().get(this.current++);
    }
}