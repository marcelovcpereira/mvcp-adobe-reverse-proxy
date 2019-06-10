# Adobe Reverse Proxy Test

Implementarion and automation of a Reverse Proxy.

## Components
**Entrypoint**
Central controller responsible for intercepting all http requests done to the proxy.
It processes the request, forwards it to the ReverseProxy, then returns the result to the caller. 

**ReverseProxy**
The heart of the flow, the proxy contains the list of the Services that are attached to it. It is responsible for caching 
mechanisms, circuit breaker features and load balancing.
After initialized, the ReverseProxy starts polling its service's endpoints each 10 seconds for re-evaluating their health. 
It marks them as Suspended or Active, depending on the result.

**CacheManager**
Manages HTTP Cache Control logic (Not 100% compliant).
Current implemented properties: no-cache, no-store, private, max-age.
Both the client (request) and the service (response) can return Cache Control headers.
Below goes the Reverse Proxy behavior when each of them uses one of the implemented headers.
  
Behaviors on request:
 * no-cache: Skips cache, execute query and then cache Response
 * no-store: Skips cache, execute query and then cache Response
 * private: Skips cache, execute query and then cache Response
 * max-age: Validate if cached object is not older than max-age in seconds
 
Behaviors on response:
 * no-cache: Does not store Response on cache.
 * no-store: Does not store Response on cache.
 * private: Does not store Response on cache.
 
**Service**
Represents a group of Endpoints that are responding as replicas of an Application.
Each service can have its own load balancing strategies for routing the requests. 

**Endpoint**
Represents a server host & port configuration that is responding for a certain Service.
Each Endpoint has a status value to represent its health:
>*PENDING* - All newly initialized Endpoints

>*ACTIVE* - All endpoints that have a successful last request

>*SUSPENDED* - All endpoints that have a failed last request

>*BLOCKED* - Black listed 

**Balancer**
Responsible for trying to fulfil a request using one of the available Endpoints. It tries all available Endpoints until
some of them fulfils the request or all fail. The strategy of electing which Endpoint should be the next candidate for
attempting the request depends on the routing implementation of the subclasses.

**RoundRobinLoadBalancer**
Balancer that implements the circular strategy for electing the Endpoints.

**RandomLoadBalancer**
Balancer that implements the random strategy for electing the Endpoints.

**HttpForwarder**
Helper for executing HTTP requests in remote hosts.


## Deploy Reverse Proxy in a Kubernetes Cluster via Helm Chart:
>After cloning, notice that inside the project folder there is a "mvcp-adobe-reverse-proxy/src/main/resources/devops" directory containing the necessary files for launching the application in a kubernetes cluster as shown below:
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-reverse-proxy.git
helm install --name marcelo-adobe-reverse-proxy --namespace marcelo-test -f ./mvcp-adobe-reverse-proxy/src/main/resources/devops/values.yaml ./mvcp-adobe-reverse-proxy/src/main/resources/devops
```
The above command will deploy the Reverse Proxy, Prometheus & Grafana.


## Playing with the Reverse Proxy:

### Deploying Mock Services to the Kubernetes Cluster:

#### Service A (3 values prepared for testing load balance strategies):
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-service-a.git
helm install --name marcelo-adobe-service-a --namespace marcelo-test -f ./mvcp-adobe-service-a/src/main/resources/devops/values.yaml ./mvcp-adobe-service-a/src/main/resources/devops
helm install --name marcelo-adobe-service-a2 --namespace marcelo-test -f ./mvcp-adobe-service-a/src/main/resources/devops/values2.yaml ./mvcp-adobe-service-a/src/main/resources/devops
helm install --name marcelo-adobe-service-a3 --namespace marcelo-test -f ./mvcp-adobe-service-a/src/main/resources/devops/values3.yaml ./mvcp-adobe-service-a/src/main/resources/devops
```

#### Service B (3 values prepared for testing load balance strategies):
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-service-b.git
helm install --name marcelo-adobe-service-b --namespace marcelo-test -f ./mvcp-adobe-service-b/src/main/resources/devops/values.yaml ./mvcp-adobe-service-b/src/main/resources/devops
helm install --name marcelo-adobe-service-b2 --namespace marcelo-test -f ./mvcp-adobe-service-b/src/main/resources/devops/values2.yaml ./mvcp-adobe-service-b/src/main/resources/devops
helm install --name marcelo-adobe-service-b3 --namespace marcelo-test -f ./mvcp-adobe-service-b/src/main/resources/devops/values3.yaml ./mvcp-adobe-service-b/src/main/resources/devops
```

### Using K8s Port-forward + cUrl
>Important: Using Postman, you cannot send restricted HTTP headers like "Host". Install Postman Interceptor for it or use cUrl shown below.
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-test 9999:9999
curl -XGET -H "Host: a.my-services.com" http://localhost:9999/marcelo/test/15
curl -XGET -H "Host: b.my-services.com" http://localhost:9999/marcelo/serviceb/15
```
response:
>{"idServiceA": 15}


## Monitoring SLIs

### For verifying overal performance of the application, you can use Prometheus & Grafana:

#### Visiting Prometheus dashboard:
```bash
kubectl port-forward -n marcelo-test svc/prometheus-service 9999:80
```
After the port forward, access in your browser: http://localhost:9999


#### Visiting Grafana dashboard:
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-grafana 9898:9898
```
Then, access in your browser: http://localhost:9898
User: admin
Password: admin
Open the dashboard: Reverse Proxy Visualization
You can change the timeframe to last 5 minutes and activate refreshing every 5s.

With the visualization opened you can then access terminal and execute several requests or maybe use siege (https://www.joedog.org/siege-manual/) to load test the proxy.


#### To clean helm installations:
```bash
helm del --purge marcelo-adobe-reverse-proxy
helm del --purge marcelo-adobe-service-a
helm del --purge marcelo-adobe-service-a2
helm del --purge marcelo-adobe-service-a3
helm del --purge marcelo-adobe-service-b
helm del --purge marcelo-adobe-service-b2
helm del --purge marcelo-adobe-service-b3
```


## Outside k8s (only for debugging)
For running the reverse-proxy as a local docker image, use the following command:
```
docker run -p 9090:9090 -e REVERSE_PROXY_PORT=9090 -e REVERSE_PROXY_SERVICES="ServiceA,servicea.com,RANDOM,localhost:8000,localhost:8001,localhost:8002;ServiceB,serviceb.com,ROUND_ROBIN,localhost:9000"  marcelovcpereira/adobe-test:latest
```

PS: If you use MacOS & your docker container needs to access a local service/port, do not bind to localhost or 127.0.0.1, instead use internal docker DNS, e.g:
>docker.for.mac.host.internal:8080


## Improvements:
- Implement more Cache Control headers
- Make Cache Control 100% compliant to specification
- Implement more Strategies of load balancing
- Increase test coverage
- Externalize the configuration of the "interval of polling servers" (currently: 10s)
- Implement dynamic black list of endpoints for being used with BLOCKED status feature
- Implement persistent volumes for storing Prometheus + Grafana data
- Configure Prometheus Alert Manager
- Generate/expose metrics from attached Services (availability, latency, etc)
- Add authentication for accessing visualization
- Improve marshalling/unmarshalling of message in proxy & cache