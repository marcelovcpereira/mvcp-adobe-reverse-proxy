package mvcp.adobe.entities;


import mvcp.adobe.abstractions.BaseLoadBalancer;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.*;

public class LoadBalancerTest extends BaseTest{

    @Test
    public void balancersShouldCorrectlyInitialize() {
        try {
            balancerShouldCorrectlyInitialize(RoundRobinLoadBalancer.class);
            balancerShouldCorrectlyInitialize(RandomLoadBalancer.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void balancerShouldCorrectlyInitialize(Class<? extends BaseLoadBalancer> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        int endpointListSize = 8;
        List<Endpoint> endpoints = createEndpointList(endpointListSize);
        Constructor<? extends BaseLoadBalancer> constructor = clazz.getConstructor(List.class);
        BaseLoadBalancer baseLoadBalancer = constructor.newInstance(endpoints);

        assertEquals(baseLoadBalancer.getEndpoints().size(), endpointListSize);
        for (Endpoint endpoint : endpoints) {
            assertThat(baseLoadBalancer.getEndpoints(), CoreMatchers.hasItem(endpoint));
        }
    }

    @Test
    public void roundRobinShouldCircle() {
        int totalEndpoints = roundrobin.getEndpoints().size();
        Endpoint[] history1 = new Endpoint[totalEndpoints];
        Endpoint[] history2 = new Endpoint[totalEndpoints];
        Endpoint[] history3 = new Endpoint[totalEndpoints];
        for (int i = 0; i < totalEndpoints; i++) {
            history1[i] = roundrobin.getBaseLoadBalancer().nextEndpoint();
        }
        for (int i = 0; i < totalEndpoints; i++) {
            history2[i] = roundrobin.getBaseLoadBalancer().nextEndpoint();
        }
        for (int i = 0; i < totalEndpoints; i++) {
            history3[i] = roundrobin.getBaseLoadBalancer().nextEndpoint();
        }

        for (int i = 0; i < totalEndpoints; i++) {
            //h1 = h2
            assertEquals(history1[i].getIp(), history2[i].getIp());
            assertEquals(history1[i].getPort(), history2[i].getPort());
            //h2 = h3
            assertEquals(history2[i].getIp(), history3[i].getIp());
            assertEquals(history2[i].getPort(), history3[i].getPort());
        }
    }

    @Test
    public void randomShouldNotCircle() {
        int totalEndpoints = random.getEndpoints().size();
        Endpoint[] history1 = new Endpoint[totalEndpoints];
        Endpoint[] history2 = new Endpoint[totalEndpoints];
        Endpoint[] history3 = new Endpoint[totalEndpoints];
        for (int i = 0; i < totalEndpoints; i++) {
            history1[i] = random.getBaseLoadBalancer().nextEndpoint();
        }
        for (int i = 0; i < totalEndpoints; i++) {
            history2[i] = random.getBaseLoadBalancer().nextEndpoint();
        }
        for (int i = 0; i < totalEndpoints; i++) {
            history3[i] = random.getBaseLoadBalancer().nextEndpoint();
        }
        assertTrue(history1 != history2 || history1 != history3 || history2 != history3);
    }

}
