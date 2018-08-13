## spring-boot-starter-dns-txt

Running highly available micro-services requires a multi-location approach. That can consist of multiple public cloud regions, private clouds, or private data centers. Whatever the locations are, we need a consistent, reliable way for our services to discover where they are located and configure themselves accordingly.

As part of an initiative to build globally connected Kubernetes clusters at CenturyLink, we have a need to distribute our container workloads across multiple locations. Initially we used location labels on our nodes to target deployments using node selectors. However, this means you have to create a deployment per service per location. This strategy not only creates more maintenance work, it also lacks the ability to redistribute our workloads in the event of a total failure at a location.

If we could to totally ignore the location and use anti-affinity policies to schedule our workloads, we could use a single deployment for our service. The problem is our services need to have some detail about where they are located. The services need to know what location specific services they should interact with. 

In researching the problem, I found strategies of using sidecar containers injected with the Kubernetes node identifier to perform a lookup against the Kubernetes api for discovery. This seemed more complex than it needed to be, and the thought of managing security for all the namespaces was going to require additional management or automation.

We could leverage tools like etcd and consul for this environment level discovery. However, this would require a large number of additional processes running across our cluster. And in the case of a multi-tenant cluster, each team or namespace may need these processes configured. We could also use a multi-tenant etcd or consul instance, but yet again, this would require some sort of management and automation of the tools.

Another possible solution was the use of our cluster DNS services. When constructing our clusters, we created custom DNS controllers and deployments at each individual location, configuring the kubelet on each node to resolve using the location specific DNS pods. With this functionality already available to us already, we simply needed a way for teams to define DNS records at these specific locations.

We ended up making a very simple change to our existing DNS controller to allow cluster users to defined Custom Resource Definitions (CRD) to define DNS TXT records. The CRD allows the user to specify a location, and 1 or more key/values that should be made available via DNS query. For example:   

```
apiVersion: cd.ctl.io/v1
kind: ClusterDnsEntry
metadata:
  labels:
    location: ca3
  name: dns-ca3
  namespace: default
spec:
  records:
  - name: env
    value: ca3
```

This CRD specification defines a DNS TXT record called `env` with the fully qualified domain name of `env.default.cluster.local`. The location label is used by the DNS pod to determine which records should exist in that location. This means we can describe all records for all locations in a single namespace. At CenturyLink we have a single cluster spanning multiple data centers, so we have a env record defined for each location. If we query the location specific DNS service for our TXT record, we will get our location specific result. For example:

```
mark@CA3WOPRK8SM02:~$ dig @192.168.253.108 env.default.cluster.local txt

; <<>> DiG 9.10.3-P4-Ubuntu <<>> @192.168.253.108 env.default.cluster.local txt
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 64584
;; flags: qr aa rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;env.default.cluster.local.	IN	TXT

;; ANSWER SECTION:
env.default.cluster.local. 3600	IN	TXT	"ca3"

;; Query time: 1 msec
;; SERVER: 192.168.253.108#53(192.168.253.108)
;; WHEN: Mon Aug 13 14:51:03 UTC 2018
;; MSG SIZE  rcvd: 58
```
```
mark@CA3WOPRK8SM02:~$ dig @192.168.253.124 env.default.cluster.local txt

; <<>> DiG 9.10.3-P4-Ubuntu <<>> @192.168.253.124 env.default.cluster.local txt
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 12874
;; flags: qr aa rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;env.default.cluster.local.	IN	TXT

;; ANSWER SECTION:
env.default.cluster.local. 3600	IN	TXT	"uc1"

;; Query time: 67 msec
;; SERVER: 192.168.253.124#53(192.168.253.124)
;; WHEN: Mon Aug 13 14:52:11 UTC 2018
;; MSG SIZE  rcvd: 58
```

As each of our Kubernetes nodes are configured with the proper cluster DNS address to configure pods, we have a simple way to determine location as well as any other specific configuration our users require. Additionally, all this can be packaged as part of a deployment pipeline, or helm chart.

Now that we had a simple way to configure location specific details for our clusters, it makes sense to have an easy way to perform lookup and configuration within that environment. At CenturyLink we have heavy use of Spring Boot. It made sense for us to write a simple autoconfigured Spring Boot Starter project to get teams going as quickly as possible. 

### Getting Started

The easiest way to get started with this project is to fork, clone or download this repository.

	git clone https://github.com/markramach/spring-boot-starter-dns-txt.git  

### Spring Boot Configuration

If you're developing a Spring Boot application, you can have properties injected into the application context during the bootstrap phase of application startup. Injecting properties at this point allows you to leverage many of the features that provided from the [Spring Cloud Config](http://projects.spring.io/spring-cloud/) project. This includes the ability to refresh your properties at any point during the application lifecycle using the predefined `/refresh` endpoint and the `@RefreshScope` annotation.

This starter project has a `DnsTxtBootstrapConfiguration` class configured in the `META-INF/spring.factories` file that indicates certain beans need to be configured on startup. While bean configuration is automatic, it can be disabled using an environment variable or a bootstrap.yml file like the following:

	dns:
	  txt:
	    enabled: false

The bootstrap configuration creates three components needed to fetch configuration TXT records from DNS. `DnsTxtConfiguration`, `DnsAdapter` as well as a `DnsTxtPropertySourceLocator`. This last locator class implements a Spring Boot `PropertySourceLocator` interface that is automatically detected by the cloud configuration components and makes the property source available to the application context.

Because the bootstrap components are auto-configured, there is no additional code that needs to be written. However, there are a couple of configuration items that you can configure for yourself. The first is the dns `suffix`. By default the suffix is set to `default.skydns.local`. This is used to construct the fully qualified domain name for your TXT records. In our case this always includes the namesapce and cluster domain. You can update this using the bootstrap.yml file.

	dns:
	  suffix: default.cluster.local

You will then want to set the records element to the DNS TXT keys that you want to make available to the application.

	dns:
	  suffix: default.cluster.local
	  records:
	    - env

The final configuration option available is the `failFast` option. This instructs the application to either fail passively logging the failure, or throw an exception if the property load fails. This option defaults to false, or passive failure.

	dns:
	  suffix: default.cluster.local
	  records:
	    - env
	  failFast: false
	  
### Testing Locally

To test locally, you can use skydns and etcd deployed as docker containers. This will allow you to run a very lightweight DNS server to handle api configuration. To get started run an etcd instance exposing the client ports.

```
docker run -d -p 4001:4001 -p 5003:5003 -p 5003:5003/UDP --name etcd_skydns quay.io/coreos/etcd:v2.3.8 --listen-client-urls 'http://0.0.0.0:2379,http://0.0.0.0:4001' --advertise-client-urls 'http://127.0.0.1:2379,http://127.0.0.1:4001'
```

After the etcd instance is up and running, we can start a skydns instance using our etcd backend. 

```
docker run -ti --net=container:etcd_skydns --name skydns skynetservices/skydns:2.5.3a -addr=0.0.0.0:5003 -machines=http://127.0.0.1:4001 -rcache=0 -rcache-ttl=0 -nameservers=8.8.8.8:53
```

Now, lets add a TXT record to the DNS server.

```
curl -XPUT http://127.0.0.1:4001/v2/keys/skydns/local/skydns/default/env -d value='{"text":"localhost"}'
```

To verify that the DNS service is up and running, you should be able to run the following dig command.


```
Marks-MacBook-Pro:~ mramach$ dig @127.0.0.1 -p 5003 env.default.skydns.local txt

; <<>> DiG 9.10.3-P2 <<>> @127.0.0.1 -p 5003 env.default.skydns.local txt
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 64185
;; flags: qr aa rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;env.default.skydns.local.	IN	TXT

;; ANSWER SECTION:
env.default.skydns.local. 3600	IN	TXT	"localhost"

;; Query time: 2 msec
;; SERVER: 127.0.0.1#5003(127.0.0.1)
;; WHEN: Mon Aug 13 11:31:34 CDT 2018
;; MSG SIZE  rcvd: 64
```

Finally, with this testing scenario, you will need to define the DNS endpoint for the DNS TXT property resolver. In this case, you will need set the optional `endpoint` and `port` settings in the bootstrap.yml.

	dns:
	  suffix: default.cluster.local
	  records:
	    - env
	  failFast: false
	  endpoint: 127.0.0.1
	  port: 5003

