# Adobe Reverse Proxy Test

Implementation and automation of a Reverse Proxy.

## Download
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-reverse-proxy.git
```


## Building, Testing, Generating Javadoc And Running locally
Pre requisites:
- `Git`
- `Java 8`
- `Maven`


#### Building And Unit Testing
Run inside project folder root:
```bash
mvn clean install 
``` 
A jar file will be generated at /target folder


#### Generating Javadoc
Run inside project folder root:
```bash
mvn javadoc:javadoc 
``` 
A javadoc folder structure will be generated at /target folder


#### Running Locally
```bash
export REVERSE_PROXY_HOST="0.0.0.0"
export REVERSE_PROXY_PORT=8080
export REVERSE_PROXY_MANAGEMENT_ENDPOINTS="health,prometheus,metrics"
export REVERSE_PROXY_SERVICES="ServiceA,servicea.com,RANDOM,localhost:9000;ServiceB,serviceb.com,ROUND_ROBIN,localhost:8000"
export REDIS_HOST=localhost 
export REDIS_PORT=6379
java -jar mvcp-adobe-reverse-proxy-1.0-SNAPSHOT.jar
``` 
This will run the Reverse Proxy locally, listening for HTTP requests coming on port 8080.


## Deploy to a Kubernetes Cluster via Helm Chart:

```bash
helm install --name marcelo-adobe-reverse-proxy --namespace marcelo-test -f ./src/main/resources/devops/values.yaml ./src/main/resources/devops
```
The above command will deploy the Reverse Proxy, Prometheus, Grafana, Redis, ServiceA & Service B based on the configuration found on file:
>./src/main/resources/devops/values.yaml


#### Helm Values Configuration
`marceloAdobeTest.serviceA.enabled` - Toggles deployment of Mock Service A

`marceloAdobeTest.serviceB.enabled` - Toggles deployment of Mock Service B

`marceloAdobeTest.redis.enabled` - Toggles deployment of a Redis service for Caching

`marceloAdobeTest.monitoring.prometheusExporter` - Toggles deployment of Redis Prometheus Exporter for metrics of Caching



>You can enable/disable or scale the deployment of mock services A and B via values.yaml. 
>As you change these settings, also change the configuration for the proxy variables.

## Monitoring SLIs


#### Visiting Prometheus dashboard:
```bash
kubectl port-forward -n marcelo-test svc/prometheus-service 7777:80
```
After the port forward, access in your browser: http://localhost:7777


#### Visiting Grafana dashboard for metrics visualization:
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-grafana 9898:9898
```
Then, access in your browser: http://localhost:9898
>User: (configurable on values.yaml:monitoring.adminUser)
>Password: (configurable on values.yaml:monitoring.adminPassword)

Open the dashboard: Reverse Proxy Visualization
You can change the timeframe to last 5 minutes and activate refreshing every 5s.

With the visualization opened you can then access terminal and execute several requests or maybe use siege as shown below:

#### SLI Calculation:

- Requests Per Minute:  `rate(http_server_requests_seconds_count[5m])*60`

- Memory Usage: `jvm_memory_used_bytes{area="heap",job="reverse-proxy"}`

- Real time CPU Usage: `system_cpu_usage*100`

- Average Latency/min (ms): `rate(http_server_requests_seconds_max{uri!~"/actuator/.*"}[5m])*1000*60`


## Playing with the Reverse Proxy:

#### Using K8s Port-forward + cUrl
>Important: Using Postman, you cannot send restricted HTTP headers like "Host". Install Postman Interceptor for it or use cUrl shown below.
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-reverse-proxy 9999:9999
curl -XGET -H "Host: a.my-services.com" http://localhost:9999/marcelo/test/15
curl -XGET -H "Host: b.my-services.com" http://localhost:9999/marcelo/serviceb/15
curl -H "Host: a.my-services.com" -H "Cache-Control: max-age=100" localhost:9999/marcelo/test/12345
curl -H "Host: a.my-services.com" -H "Cache-Control: no-cache" localhost:9999/marcelo/test/12345
```
Example response:
>{"idServiceA": 15}



#### Using Siege

##### Firing 10 req/s (600/min) during 5 minutes to Mock Service A without never touching cache
```bash
siege -c 10 -i -t 5M -f ${REVERSE_PROXY_FOLDER}/siege_urls_a.txt -H "Host: a.my-services.com" -H "Cache-Control: no-cache"
```


##### Firing 5 req/s (300/min) during 3 minutes to Mock Service B without never touching cache
```bash
siege -c 10 -i -t 3M -f ${REVERSE_PROXY_FOLDER}/siege_urls_b.txt -H "Host: b.my-services.com" -H "Cache-Control: no-cache"
```


#### To clean helm installations:
```bash
helm del --purge marcelo-adobe-reverse-proxy
```

PS: If you use MacOS & your docker container needs to access a local service/port, do not bind to localhost or 127.0.0.1, instead use internal docker DNS, e.g:
>docker.for.mac.host.internal:8080

## Internal Components
**Entrypoint:**
Central controller responsible for intercepting all http requests done to the proxy.

**ReverseProxy:**
Contains the list of the Services that are attached to it. 
After initialized, the ReverseProxy starts polling its service's endpoints each 10 seconds for re-evaluating their health. 

**CacheManager:**
Manages HTTP Cache Control logic (Not 100% compliant).
Current implemented properties: no-cache, no-store, private, max-age.
Both the client (request) and the service (response) can return Cache Control headers.
Below goes the Reverse Proxy behavior when each of them uses one of the implemented headers.
  
Behaviors on request:
 * `no-cache`: Skips cache, execute query and then cache Response
 * `no-store`: Skips cache, execute query and then cache Response
 * `private`: Skips cache, execute query and then cache Response
 * `max-age`: Validate if cached object is not older than max-age in seconds
 
Behaviors on response:
 * `no-cache`: Does not store Response on cache.
 * `no-store`: Does not store Response on cache.
 * `private`: Does not store Response on cache.
 
**Service:**
Represents a group of Endpoints that are responding as replicas of an Application.
Each service can have its own load balancing strategies for routing the requests. 

**Endpoint:**
Represents a server host & port configuration that is responding for a certain Service.
Each Endpoint has a status value to represent its health:
- `PENDING` - All newly initialized Endpoints

- `ACTIVE` - All endpoints that have a successful last request

- `SUSPENDED` - All endpoints that have a failed last request

- `BLOCKED` - Black listed 

**LoadBalancer:**
Responsible for trying to fulfil a request using one of the available Endpoints (via strategy)

**RoundRobinLoadBalancer:**
Balancer that implements the circular strategy for electing the Endpoints.

**RandomLoadBalancer:**
Balancer that implements the random strategy for electing the Endpoints.

**HttpForwarder:**
Helper for executing HTTP requests in remote hosts.


## Improvements:
- Implement a toggle for enabling/disabling monitoring deployment
- Implement a K8s Operator
- Make Cache Control 100% compliant to specification
- Configure credentials for accessing the Cache
- Implement cluster version of Redis for better scalability (currently standalone)
- Implement more Strategies of load balancing
- Externalize the configuration "polling interval" (currently: 10s)
- Implement dynamic black list of endpoints for being used with BLOCKED status feature
- Implement persistent volumes for storing Prometheus + Grafana data
- Configure Prometheus Alert Manager
- Generate and expose metrics from attached Services (availability, latency, etc)


# References:
>https://helm.sh/docs/chart_best_practices/

>https://github.com/helm/charts/tree/master/stable/grafana

>https://github.com/helm/charts/tree/master/stable/prometheus

>https://github.com/helm/charts/tree/master/stable/redis

>https://stackoverflow.com/questions/47668793/helm-generate-comma-separated-list

>https://itnext.io/simple-redis-cache-on-kubernetes-with-prometheus-metrics-8667baceab6b

>https://groups.google.com/forum/#!topic/prometheus-users/MkyxLiVsJz0

>https://blog.softwaremill.com/practical-monitoring-with-prometheus-ee09a1dd5527

>https://www.joedog.org/siege-manual/

>https://github.com/helm/helm/issues/2798

>https://www.baeldung.com/javadoc