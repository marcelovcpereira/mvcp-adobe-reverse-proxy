#Marcelo Adobe Reverse Proxy Test

##Components
Entrypoint
Central controller responsible for intercepting all http requests done to the proxy.
It processes the request, forwards it to the ReverseProxy, then returns the result to the caller. 

ReverseProxy
The heart of the flow, the proxy contains the list of the Services that are attached to it. It is responsible for caching 
mechanisms, circuit breaker features and load balancing.
After initialized, the ReverseProxy starts polling its service's endpoints each 10 seconds for re-evaluating their health. 
It marks them as Suspended or Active, depending on the result.

Service
Represents a group of Endpoints that are responding as replicas of an Application.
Each service can have its own load balancing strategies for routing the requests. 

Endpoint
Represents a server host & port configuration that is responding for a certain Service.
Each Endpoint has a status value to represent its health:
PENDING - All newly initialized Endpoints
ACTIVE - All endpoints that have a successful last request
SUSPENDED - All endpoints that have a failed last request
BLOCKED - Black listed 

Balancer
Responsible for trying to fulfil a request using one of the available Endpoints. It tries all available Endpoints until
some of them fulfils the request or all fail. The strategy of electing which Endpoint should be the next candidate for
attempting the request depends on the routing implementation of the subclasses.

RoundRobinLoadBalancer
Balancer that implements the circular strategy for electing the Endpoints.

RandomLoadBalancer
Balancer that implements the random strategy for electing the Endpoints.

HttpForwarder
Helper for executing HTTP requests in remote hosts.


##Improvements:
Implement more Strategies of load balancing
Externalize the configuration of the "interval of polling servers"
Implement dynamic black list of endpoints for being used with BLOCKED status feature