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

**Service**
Represents a group of Endpoints that are responding as replicas of an Application.
Each service can have its own load balancing strategies for routing the requests. 

**Endpoint**
Represents a server host & port configuration that is responding for a certain Service.
Each Endpoint has a status value to represent its health:
>*PENDING* - All newly initialized Endpoints
*ACTIVE* - All endpoints that have a successful last request
*SUSPENDED* - All endpoints that have a failed last request
*BLOCKED* - Black listed 

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


## Install/Delete Helm Chart:
Inside the project folder there is a "mvcp-adobe-reverse-proxy/src/main/resources/devops" directory containing the necessary files 
for launching the application in a kubernetes cluster. Switch to this directory to find the files and fire the following commands:
```bash
helm install --name marcelo-adobe-reverse-proxy --namespace marcelo-test -f values.yaml .
```

## Playing with the Reverse Proxy:

>Important: Using Postman, you cannot send restricted HTTP headers like "Host". Install Postman Interceptor for it or use cUrl shown below.

### Deploying Mock Services to the Kubernetes Cluster:

#### Service A (3 values prepared for multiplicity):
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-service-a.git
helm install --name marcelo-adobe-service-a --namespace marcelo-test -f values.yaml ./mvcp-adobe-service-a/src/main/resources/devops
helm install --name marcelo-adobe-service-a2 --namespace marcelo-test -f values2.yaml ./mvcp-adobe-service-a/src/main/resources/devops
helm install --name marcelo-adobe-service-a3 --namespace marcelo-test -f values3.yaml ./mvcp-adobe-service-a/src/main/resources/devops
```

#### Service B (3 values prepared for multiplicity):
```bash
git clone https://github.com/marcelovcpereira/mvcp-adobe-service-b.git
helm install --name marcelo-adobe-service-b --namespace marcelo-test -f values.yaml ./mvcp-adobe-service-b/src/main/resources/devops
helm install --name marcelo-adobe-service-b2 --namespace marcelo-test -f values2.yaml ./mvcp-adobe-service-b/src/main/resources/devops
helm install --name marcelo-adobe-service-b3 --namespace marcelo-test -f values3.yaml ./mvcp-adobe-service-b/src/main/resources/devops
```

Then update you ReverseProxy settings (values.yaml), adding the above services and purge/install again the proxy:
```bash
helm del --purge marcelo-adobe-reverse-proxy
helm install --name marcelo-adobe-reverse-proxy --namespace marcelo-test -f values.yaml .
```

### Using K8s Port-forward + cUrl
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-test 8888:8888
curl -XGET -H "Host: a.my-services.com" http://localhost:8888/marcelo/test/15
curl -XGET -H "Host: b.my-services.com" http://localhost:8888/marcelo/serviceb/15
```
response:
>{"idServiceA": 15}


## Monitoring SLIs

#### For verifying overal performance of the application, you can deploy the monitoring part, which is done via Prometheus & Grafana:
```bash
helm install --name marcelo-adobe-prometheus --namespace marcelo-test -f prometheus-values.yaml stable/prometheus
helm install --name marcelo-adobe-grafana --namespace marcelo-test -f grafana-values.yaml stable/grafana
```

#### Then, install the grafana dashboard:
```bash
kubectl apply -n marcelo-test -f ./src/main/resources/devops/grafanaConfigMap.yaml
```

#### Visiting Prometheus dashboard:
```bash
kubectl port-forward -n marcelo-test svc/marcelo-adobe-prometheus-server 9090:80
```
After the port forward, access in your browser: http://localhost:8080


#### Visiting Grafana dashboard:
```bash
kubectl port-forward  marcelo-test svc/marcelo-adobe-grafana 8080:80
```
After the port forward, access in your browser: http://localhost:8080

#### To clean helm installations:
```bash
helm del --purge marcelo-adobe-reverse-proxy
helm del --purge marcelo-adobe-grafana
helm del --purge marcelo-adobe-prometheus 
```


## Outside k8s (only for debugging)
For running the reverse-proxy as a local docker image, use the following command:
```
docker run -p 9090:9090 -e REVERSE_PROXY_PORT=9090 -e REVERSE_PROXY_SERVICES="ServiceA,servicea.com,RANDOM,localhost:8000,localhost:8001,localhost:8002;ServiceB,serviceb.com,ROUND_ROBIN,localhost:9000"  marcelovcpereira/adobe-test:latest
```

PS: If you use MacOS & your docker container needs to access a local service/port, do not bind to localhost or 127.0.0.1, instead use internal docker DNS, e.g:
>docker.for.mac.host.internal:8080


## Improvements:
- Implement more Strategies of load balancing
- Increase test coverage
- Externalize the configuration of the "interval of polling servers" (currently: 10s)
- Implement dynamic black list of endpoints for being used with BLOCKED status feature
- Implement persistent volumes for storing Prometheus + Grafana data
- Configure Swagger for documenting the API
- Embbed Prometheus deployment into the same root chart
- Embbed Grafana deployment into the same root chart
- Configure Alert Manager