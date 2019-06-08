package mvcp.adobe.entities;

import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

abstract public class BaseTest {

    protected static final String SERVICE_A_NAME = "Service A";
    protected static final String SERVICE_A_DOMAIN = "servicea.com";
    protected static final LoadBalanceStrategies SERVICE_A_STRATEGY = LoadBalanceStrategies.ROUND_ROBIN;

    protected static final String SERVICE_B_NAME = "Service B";
    protected static final String SERVICE_B_DOMAIN = "serviceb.com";
    protected static final LoadBalanceStrategies SERVICE_B_STRATEGY = LoadBalanceStrategies.RANDOM;

    protected static Service roundrobin;
    protected static Service random;

    @Before
    public void createRoundRobinService() {
        roundrobin = new Service(SERVICE_A_NAME, SERVICE_A_DOMAIN, SERVICE_A_STRATEGY);
        roundrobin.getEndpoints().add(createRandomEndpoint());
        roundrobin.getEndpoints().add(createRandomEndpoint());
        roundrobin.getEndpoints().add(createRandomEndpoint());
        roundrobin.getEndpoints().add(createRandomEndpoint());
    }

    @Before
    public void createRandomService() {
        random = new Service(SERVICE_B_NAME, SERVICE_B_DOMAIN, SERVICE_B_STRATEGY);
        random.getEndpoints().add(createRandomEndpoint());
        random.getEndpoints().add(createRandomEndpoint());
        random.getEndpoints().add(createRandomEndpoint());
        random.getEndpoints().add(createRandomEndpoint());
    }


    public Endpoint createRandomEndpoint() {
        int random =  new Random().nextInt(999);
        random = random + 8000;
        return new Endpoint("0.0.0.0", random);
    }

    public List<Endpoint> createEndpointList(int size) {
        List<Endpoint> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(createRandomEndpoint());
        }
        return list;
    }
}
