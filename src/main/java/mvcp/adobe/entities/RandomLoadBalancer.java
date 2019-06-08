package mvcp.adobe.entities;

import mvcp.adobe.abstractions.Balancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends Balancer {
    public RandomLoadBalancer() {
        super();
    }

    public RandomLoadBalancer(List<Endpoint> endpoints) { super(endpoints); }

    @Override
    public Endpoint nextEndpoint() {
        int size = this.getEndpointCandidates().size();
        int random =  new Random().nextInt(size);
        return this.getEndpointCandidates().get(random);
    }
}
