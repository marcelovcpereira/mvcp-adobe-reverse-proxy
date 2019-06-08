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

@Component
public class ReverseProxy {

    public static final Logger logger = (Logger) LoggerFactory.getLogger(ReverseProxy.class);
    private static final int REQUEST_TIMEOUT = 5;

    @Autowired
    private Environment env;

    private List<Service> services;

    public void registerService(Service s) {
        if (this.services == null) this.services = new ArrayList<>();
        this.services.add(s);
    }

    public Response processRequest(Request req) throws ServiceHostNotFoundException, NoAvailableEndpointsException {
        return this.discoverService(req).processRequest(req);
    }

    public Service discoverService(Request req) throws ServiceHostNotFoundException {
        logger.info("Discovering service...");
        for (Service service : this.services) {
            if (service.getDomain().trim().equalsIgnoreCase(req.getHostHeader())) {
                logger.info("Found: " + service);
                return service;
            }
        }
        throw new ServiceHostNotFoundException("The value of header 'Host' doesn't match any service domain");
    }

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

    private void loadServices(String str) throws InvalidServiceDefinitionException {
        if (str == null) str = env.getProperty("mvcp.adobe.proxy.services");
        this.services = Service.fromProperty(str);

        logger.info("Services loaded:");
        this.services.forEach(System.out::println);
    }

    //Code Reference: https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability
    @Scheduled(fixedRate = 5000)
    public void pollServiceEndpoints() throws Exception {
        for (Service service : services) {
            logger.info("Polling endpoints statuses for service " + service.toString() + "...");
            for (Endpoint endpoint : service.getEndpoints()) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(endpoint.getIp(), endpoint.getPort()), REQUEST_TIMEOUT);
                    endpoint.setStatus(EndpointStatus.ACTIVE);
                } catch (IOException e) {
                    endpoint.setStatus(EndpointStatus.SUSPENDED);
                }
            }
        }

    }

}
