package mvcp.adobe.entities;

import mvcp.adobe.abstractions.Balancer;

import java.util.List;

public class RoundRobinLoadBalancer extends Balancer {
    private int current;

    public RoundRobinLoadBalancer(List<Endpoint> endpoints) { super(endpoints); }

    @Override
    public Endpoint nextEndpoint() {
        if (this.current >= this.getEndpointCandidates().size()) this.current = 0;
        return this.getEndpointCandidates().get(this.current++);
    }
}
