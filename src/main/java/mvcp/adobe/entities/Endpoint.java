package mvcp.adobe.entities;
/**
 * Represents a server host and port configuration that is responding for a certain Service.
 * Each Endpoint has a status value to represent its health.
 *
 * @see         EndpointStatus
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-08
 */
public class Endpoint {

    private String ip;
    private int port;
    private EndpointStatus status = EndpointStatus.PENDING;

    public Endpoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns a JSON representation of the current object
     *
     * @return String Json of this object
     */
    public String toJsonString() {
        return "{ \"ip\": \"" + this.ip + "\", \"port\": " + this.port + ", \"status\": \"" + this.status.toString() + "\"}";
    }
    /**
     * Returns a HOST:PORT string for simpler debugging messages.
     *
     * @return String Colon-separated host/port string.
     */
    public String toString() {
        return this.ip + ":" + this.port;
    }

    public EndpointStatus getStatus() {
        return status;
    }

    public void setStatus(EndpointStatus status) {
        this.status = status;
    }

    //Code reference: https://www.geeksforgeeks.org/equals-hashcode-methods-java/
    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        Endpoint endpoint = (Endpoint) obj;
        return endpoint.getIp().equalsIgnoreCase(this.getIp())
                && endpoint.getPort() == this.getPort()
                && endpoint.getStatus().equals(this.status);
    }

}
