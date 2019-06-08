package mvcp.adobe.entities;


import mvcp.adobe.exceptions.InvalidServiceDefinitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceTest extends BaseTest{
    public static final Logger logger = (Logger) LoggerFactory.getLogger(ServiceTest.class);
    public static final String FORMAT_FIELD_SEPARATOR = ",";
    public static final String FORMAT_SERVICE_SEPARATOR = ";";

    @Test
    public void shouldParseService() throws InvalidServiceDefinitionException {
        String name = "MyTestService";
        String domain = "mydomain.com";
        LoadBalanceStrategies strategy = LoadBalanceStrategies.ROUND_ROBIN;
        List<Endpoint> endpoints = Arrays.asList(
                createRandomEndpoint(),
                createRandomEndpoint(),
                createRandomEndpoint(),
                createRandomEndpoint()
        );

        String serviceString = buildServiceString(name, domain, strategy, endpoints);
        Service service = Service.parse(serviceString);
        assertEquals(service.getName(), name);
        assertEquals(service.getDomain(), domain);
        assertEquals(service.getStrategy(), strategy);
        assertEquals(service.getEndpoints().size(), endpoints.size());
        assertTrue(service.getEndpoints().containsAll(endpoints));
    }

    @Test
    public void shouldParseMultipleServices() throws InvalidServiceDefinitionException {
        int totalServices = 5;
        Service[] services = new Service[totalServices];
        String[] strings = new String[totalServices];
        for (int i = 0; i < totalServices; i++) {
            String name = i + "MyTestService";
            String domain = i + "mydomain.com";
            LoadBalanceStrategies strategy = i % 2 == 0 ? LoadBalanceStrategies.ROUND_ROBIN : LoadBalanceStrategies.RANDOM;
            List<Endpoint> endpoints = Arrays.asList(
                    createRandomEndpoint(),
                    createRandomEndpoint(),
                    createRandomEndpoint(),
                    createRandomEndpoint()
            );
            services[i] = new Service(name, domain, strategy);
            services[i].setEndpoints(endpoints);
            strings[i] = buildServiceString(name, domain, strategy, endpoints);
        }
        String multiple = String.join(FORMAT_SERVICE_SEPARATOR, strings);
        List<Service> list = Service.fromProperty(multiple);
        assertEquals(totalServices, list.size());
        for (Service service : services) {
            assertTrue(list.contains(service));
        }
    }

    public String buildServiceString(String name, String domain, LoadBalanceStrategies strategy, List<Endpoint> list) {
        String separator = FORMAT_FIELD_SEPARATOR;
        String endpoints = String.join(separator, list.stream().map(e -> e.toString()).collect(Collectors.toList()));
        return name + separator + domain + separator + strategy + separator + endpoints;
    }


}
