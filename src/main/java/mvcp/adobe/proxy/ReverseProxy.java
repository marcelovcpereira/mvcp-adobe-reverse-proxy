package mvcp.adobe.proxy;

import mvcp.adobe.entities.*;
import mvcp.adobe.exceptions.InvalidServiceDefinitionException;
import mvcp.adobe.exceptions.NoAvailableEndpointsException;
import mvcp.adobe.exceptions.ServiceHostNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 * The Reverse Proxy handles all requests that cannot be fulfilled by the Cache Manager
 * It maintains a list of all attached Services and keeps polling them for re-evaluating their Health status constantly.
 * It marks Endpoints as Suspended or Active, depending on the result of the health check
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-07
 */
@Component
public class ReverseProxy {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(ReverseProxy.class);
    //Timeout for health check request
    private static final int REQUEST_TIMEOUT = 5;

    @Autowired
    private Environment env;

    private List<Service> services;

    /**
     * Adds a Service to the service pool
     *
     * @param s Service to be attached to the pool
     */
    public void registerService(Service s) {
        if (this.services == null) this.services = new ArrayList<>();
        this.services.add(s);
    }

    /**
     * Processes a Request by discovering the correct Service and delegating the execution to it.
     *
     * @param req Request to be executed
     * @return Response Response returned from the Service
     * @throws ServiceHostNotFoundException Thrown when the request does not specify the target domain
     * @throws NoAvailableEndpointsException Thrown when no Endpoint is available for executing the request
     */
    public Response processRequest(Request req) throws ServiceHostNotFoundException, NoAvailableEndpointsException {
        return this.discoverService(req).processRequest(req);
    }

    /**
     * Discovers the correct Service to handle the Request based on the HTTP header: 'Host'
     *
     * @param req Request to be executed
     * @return Service Service that should execute the request
     * @throws ServiceHostNotFoundException Thrown when no Service matches the specified Request
     */
    public Service discoverService(Request req) throws ServiceHostNotFoundException {
        logger.info("Discovering service...");
        for (Service service : this.services) {
            logger.info("Comparing service domain " + service.getDomain().trim() + " with " + req.getHostHeader());
            if (service.getDomain().trim().equalsIgnoreCase(req.getHostHeader())) {
                logger.info("Found: " + service);
                return service;
            }
        }
        throw new ServiceHostNotFoundException("The value of header 'Host' doesn't match any service domain");
    }

    /**
     * Loads the Service configuration from the property file.
     *
     */
    @PostConstruct
    private void config() {
        logger.info("Loading services configuration....");
        try {
            loadServices(null);
        } catch (InvalidServiceDefinitionException e) {
            logger.error("Error loading services");
            e.printStackTrace();
        }
    }

    /**
     * Loads the Service configuration from a formatted String
     *
     * @param str String containing a formatted Service configuration
     * @throws InvalidServiceDefinitionException Thrown when the String is invalid
     */
    private void loadServices(String str) throws InvalidServiceDefinitionException {
        if (str == null) str = env.getProperty("mvcp.adobe.proxy.services");
        this.services = Service.fromProperty(str);

        logger.info("Services loaded:");
        this.services.forEach(System.out::println);
    }

    /**
     * Polls Service's Endpoints for checking their Health statuses
     * Code Reference: https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability
     *
     */
    @Scheduled(fixedRate = 10000)
    public void pollServiceEndpoints() {
        for (Service service : services) {
            logger.info("Polling endpoints statuses for service " + service.toString() + "...");
            for (Endpoint endpoint : service.getEndpoints()) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(endpoint.getIp(), endpoint.getPort()), REQUEST_TIMEOUT);
                    endpoint.setStatus(EndpointStatus.ACTIVE);
                    logger.info(endpoint.toJsonString());
                } catch (IOException e) {
                    endpoint.setStatus(EndpointStatus.SUSPENDED);
                    logger.info(endpoint.toJsonString());
                }
            }
        }
    }
}
