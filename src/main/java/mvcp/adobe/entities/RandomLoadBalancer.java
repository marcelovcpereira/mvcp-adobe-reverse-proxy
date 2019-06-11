package mvcp.adobe.entities;

import mvcp.adobe.abstractions.Balancer;

import java.util.List;
import java.util.Random;
/**
 * Load balancer implementation of a Random election strategy.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class RandomLoadBalancer extends Balancer {
    public RandomLoadBalancer() {
        super();
    }

    public RandomLoadBalancer(List<Endpoint> endpoints) { super(endpoints); }
    /**
     * Decides which of candidate Endpoints should be used.
     *
     * @return Endpoint The elected endpoint to handle the request.
     */
    @Override
    public Endpoint nextEndpoint() {
        int size = this.getEndpointCandidates().size();
        int random =  new Random().nextInt(size);
        return this.getEndpointCandidates().get(random);
    }
}
