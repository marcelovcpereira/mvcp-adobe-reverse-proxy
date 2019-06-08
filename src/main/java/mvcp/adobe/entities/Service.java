package mvcp.adobe.entities;

import mvcp.adobe.abstractions.Balancer;
import mvcp.adobe.abstractions.IServiceHandler;
import mvcp.adobe.exceptions.InvalidServiceDefinitionException;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
public class Service implements IServiceHandler {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(Service.class);

    private String name;
    private String domain;
    private List<Endpoint> endpoints;
    private LoadBalanceStrategies strategy;
    private Balancer balancer;

    public Service() {}

    public Service(String name, String domain, LoadBalanceStrategies strategy) {
        this.name = name;
        this.domain = domain;
        this.strategy = strategy;
        this.endpoints = new ArrayList<>();
        initBalancer();
    }

    public void initBalancer() {
        switch(this.strategy) {
            case RANDOM:
                this.balancer = new RandomLoadBalancer(this.endpoints);
                break;
            case ROUND_ROBIN:
                this.balancer = new RoundRobinLoadBalancer(this.endpoints);
                break;
            default:
                this.balancer = new RandomLoadBalancer(this.endpoints);
        }
    }

    public Balancer getBalancer() {
        return balancer;
    }

    public void setBalancer(Balancer balancer) {
        this.balancer = balancer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public LoadBalanceStrategies getStrategy() {
        return strategy;
    }

    public void setStrategy(LoadBalanceStrategies strategy) {
        this.strategy = strategy;
    }

    @Override
    public Response processRequest(Request request) throws NoAvailableEndpointsException {
        return this.balancer.balance(request);
    }

    public static List<Service> fromProperty(String prop) throws InvalidServiceDefinitionException {
        List<Service> ret = new ArrayList<>();
        String[] services = prop.split(";");
        for (String str  : services) {
            if (str == null || str.trim().equalsIgnoreCase("")) continue;
            ret.add(Service.parse(str));
        }
        return ret;
    }

    public static Service parse(String str) throws InvalidServiceDefinitionException {
        Service ret = null;
        String[] props = str.split(",");
        if (props.length >= 4) {
            String name = props[0].trim();
            String domain = props[1].trim();
            String strategy = props[2].trim();
            ret = new Service(name, domain, Enum.valueOf(LoadBalanceStrategies.class, strategy.toUpperCase()));
            for (int i = 3; i < props.length; i++) {
                String endpoint = props[i];
                if (endpoint == null || endpoint.trim().equalsIgnoreCase("")) continue;
                String ip = endpoint.split(":")[0];
                int port = Integer.parseInt(endpoint.split(":")[1]);
                ret.endpoints.add(new Endpoint(ip, port));
            }

        } else {
            throw new InvalidServiceDefinitionException("Invalid service property: " + str);
        }
        return ret;
    }

    public String toString() {
        String ret = this.getName() + "/" + this.getDomain() + "/" + this.getStrategy().toString() + " - hosts: ";
        for (Endpoint e : this.endpoints) {
            ret += e.toString() + ",";
        }
        return ret.substring(0, ret.length() - 1);
    }

    //Code reference: https://www.geeksforgeeks.org/equals-hashcode-methods-java/
    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        Service service = (Service) obj;
        return service.getName().equalsIgnoreCase(this.getName())
                && service.getDomain().equalsIgnoreCase(this.getDomain())
                && service.getStrategy().equals(this.getStrategy())
                && service.getEndpoints().size() == this.getEndpoints().size()
                && service.getEndpoints().containsAll(this.getEndpoints());
    }

}
