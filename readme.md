# PSI: The Persistent Streaming Inventory 

[![CircleCI](https://circleci.com/gh/cedardevs/psi.svg?style=svg)](https://circleci.com/gh/cedardevs/psi)
[![codecov](https://codecov.io/gh/cedardevs/psi/branch/master/graph/badge.svg?token=mpaqa2QKdv)](https://codecov.io/gh/cedardevs/psi)

The Persistent Streaming Inventory is a distributed, scalable, event stream and database for the metadata associated
with environmental data granules. It is designed to receive metadata both from automated systems and manual uploads
of e.g. ISO-19115 XML metadata. It implements generic parsing and analysis of that metadata while also enabling arbitrary 
processing flows on it. All metadata entities are retrievable via REST API and are also exposed as streaming events to
support downstream client applications for search, analysis, etc.

## Quickstart w/ Kubernetes + Helm

```bash
helm install ./helm/psi
```

For more details, see the [Kubernetes + Helm](#kubernetes-+-helm).

## Infrastructure Components

The core of the PSI system is Apache Kafka. It provides a distributed, durable, ordered, streaming event platform
which PSI leverages to store all its inputs as well as all derived metadata. It also utilizes the 
[Confluent Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html) to store 
[Avro](https://avro.apache.org/docs/current/) schemas defining the shapes of its messages and ensure that they evolve
in a backward-compatible way.

For more information about the system architecture and the way it utilizes Kafka see the 
[architecture](#architectural-background) section below.

Each piece of required infrastructure is described in more depth below. For all of them we recommend using the 
open source versions published with the [Confluent Platform](https://docs.confluent.io/current/platform.html#what-is-included-in-cp).
These packages are free, open source, and actively developed and maintained by Confluent.

### Zookeeper

#### Purpose

[Zookeeper](http://zookeeper.apache.org/) is a distributed key-value store and is a requirement of Kafka.

#### Requirements

- Java: 6+
- Storage:
    - Local file system for each node
    - Should *not* be shared between nodes
    - Low-latency disk (SSD) strongly recommended
    - Volume grows with the number of topics created in Kafka
- Network Connectivity
    - Does not initiate connections
    - Receives connections from Kafka nodes

For more detailed information, see the [Confluent deployment guide](https://docs.confluent.io/current/zookeeper/deployment.html).

### Kafka

#### Purpose

[Kafka](https://kafka.apache.org/) is a distributed streaming platform. It provides both the messaging and primary storage
for the PSI system. All inputs and outputs are streamed in and out of the system via Kafka, and some data such as raw 
inputs are preserved indefinitely in Kafka topics, which is [perfectly okay](https://www.confluent.io/blog/okay-store-data-apache-kafka/)!

#### Requirements

- Java: 8+
- Storage:
    - Local file system for each node
    - Should *not* be shared between nodes
    - Volume grows linearly with the number of inventoried entities
- Network Connectivity:
    - Initiates requests to Zookeeper and other Kafka nodes
    - Receives requests from other Kafka nodes, PSI Registry, PSI Stream Manager, and any other downstream data consumers
    
For **much** more information, see the [Confluent deployment guide](https://docs.confluent.io/current/kafka/deployment.html). 


### Schema Registry

#### Purpose

The PSI system utilizes [Avro](https://avro.apache.org/docs/current/) schemas to define the shapes of the metadata
entities which flow through it. The Schema Registry is a central location where all these schemas are stored; data
producers publish the schemas of data they produce to it so that consumers can then retrieve those schemas and read
the data. It also facilitates the evolution of those schemas over time, for example to enforce backwards compatibility.

#### Requirements

- Java: 7+
- Storage: None
- Network Connectivity:
    - Initiates requests to the Kafka cluster
    - Receives requests from PSI Registry, PSI Stream Manager, and any other downstream data consumers.

For more detailed information see the [Confluent deployment guide](https://docs.confluent.io/current/schema-registry/docs/deployment.html).

## Components

The project consists of two Java-based microservices, backed by a Kafka cluster.

### Registry

#### Purpose

The [registry](registry) hosts the public API of the system and stores its persistent state. Specifically, it 
serves several basic purposes:

1. Receive metadata input via HTTP and send them to the appropriate Kafka topics
1. Use [Kafka Streams](https://docs.confluent.io/current/streams/index.html) to materialize state stores of both 
raw input and the values parsed from it (by the Stream Manager component)
1. Use [interactive queries](https://docs.confluent.io/current/streams/developer-guide/interactive-queries.html)
to support retrieval of all stored metadata via HTTP.

#### Requirements

- Java: 8+
- Storage:
    - Each node requires a local file system in which to store the current state of each entity
    - File system should *not* be shared between nodes
    - No need to replicate or backup this storage as all data can be re-materialized from Kafka
    - Volume grows linearly with the number of inventoried entities
- Network Connectivity:
    - Initiates connections to the Kafka cluster
    - Receives connections from API clients (e.g. metadata sources)
    
#### Config

| Environment Variable    | Importance | Required? | Default            | Description |
| ----------------------- | ---------- | --------- | ------------------ | ----------- |
| KAFKA_BOOTSTRAP_SERVERS | High       | No        | localhost:9092     | Comma-separated list of one or more kafka host:port combinations |
| SCHEMA_REGISTRY_URL     | High       | No        | localhost:8081     | The URL of the Schema Registry |
| STATE_DIR               | High       | No        | /tmp/kafka-streams | Path to the directory under which local state should be stored |
| PUBLISHING_INTERVAL_MS  | Low        | No        | 300000 (5 minutes) | Frequency with which check for changes in entity publish status |
  

### Stream Manager

#### Purpose

The [stream manager](stream-manager) processes all the metadata that passes through the inventory management system. Metadata that is
not yet well-formed (i.e. in ISO-19115 XML format) is passed off to a Kafka topic to be transformed into well-formed
metadata via domain-specific logic. Once the metadata is well formed, discovery information is parsed out of it, and
that discovery information is then analyzed. All resulting info is then sent back to the registry for storage.


#### Requirements

- Java: 8+
- Storage: None
- Network Connectivity:
    - Initiates connections to the Kafka cluster
    
#### Config

| Environment Variable    | Importance | Required? | Default            | Description |
| ----------------------- | ---------- | --------- | ------------------ | ----------- |
| KAFKA_BOOTSTRAP_SERVERS*| High       | No        | localhost:9092     | Comma-separated list of one or more kafka host:port combinations |
| SCHEMA_REGISTRY_URL     | High       | No        | localhost:8081     | The URL of the Schema Registry |

\* This variable was originally named `IM_BOOTSTRAP_SERVERS`. This old name continues to work in 0.1.x versions but
is deprecated and will be removed in a future version. 

## Build

Requires: Java 8+

Use Gradle to build the components with: `./gradlew build`

The build produces the following artifacts for each component: 

- docker image
- fat, executable jar
- thin war
    - *N/A for stream-manager; it is not a web application*
- sources jar

In addition to these artifacts, there are also some example Kubernetes manifest files under [the kubernetes folder](kubernetes).
These manifests can be used in development with skaffold and are a good starting point, but they **ARE NOT PRODUCTION-READY**.

As a result of these various artifacts, there are a variety of ways to... 

## Deploy

There are several ways to deploy the system, corresponding with the several artifacts that it produces.

#### Executable Jars

Requires:
1. Java 8+
1. A running kafka cluster

You can deploy the components of PSI by setting env variables to point to Kafka and running the `-all` jars with java:

```bash
export KAFKA_BOOTSTRAP_SERVERS=...
java -jar registry/build/libs/psi-registry-$VERSION-all.jar
java -jar stream-manager/build/libs/psi-stream-manager-$VERSION-all.jar
```

##### Variation: External Servlet Container

The registry web application can also be deployed into an externally-managed servlet container using its war artifact.
For example it might look something like this for a Tomcat installation:

```bash
cat << EOF > $CATALINA_HOME/bin/setenv.sh
#!/bin/bash
export KAFKA_BOOTSTRAP_SERVERS=...
EOF

cp registry/build/libs/psi-registry-$VERSION.war $CATALINA_HOME/webapps
$CATALINA_HOME/bin/shutdown.sh && $CATALINA_HOME/bin/startup.sh
```

#### Docker Images

Requires:
1. Docker (or another container engine)

Another option is to use the docker images built by this project. For example, these commands will create and run containers
for the registry and stream-manager, as well as Kafka and Zookeeper using the well-maintained [Confluent Platform images](https://github.com/confluentinc/cp-docker-images).

```bash
docker network create psi

docker run -d \
  --name zookeeper \
  --env ZOOKEEPER_CLIENT_PORT=2181 \
  --network psi \
  --expose 2181 \
  confluentinc/cp-zookeeper

docker run -d \
  --name kafka \
  --env KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  --env KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 \
  --env KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  --network psi \
  --expose 9092 \
  confluentinc/cp-kafka

docker run -d \
  --name schema-registry \
  --env SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=PLAINTEXT://kafka:9092 \
  --env SCHEMA_REGISTRY_HOST_NAME=schema-registry \
  --network psi \
  --expose 8081 \
  confluentinc/cp-schema-registry

docker run -d \
  --name psi-registry \
  --env KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  --env SCHEMA_REGISTRY_URL=http://schema-registry:8081 \
  --network psi \
  -p 8080 \
  cedardevs/psi-registry

docker run -d \
  --name psi-manager \
  --env KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  --env SCHEMA_REGISTRY_URL=schema-registry:8081 \
  --network psi \
  cedardevs/psi-stream-manager
```

Of course higher level tools can be used to manage these containers, like docker-compose, docker swarm, or...

#### Kubernetes + Helm

Requirements:
1. [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/), configured to point to any...
1. Kubernetes cluster (e.g. [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) or [docker desktop](https://www.docker.com/products/docker-desktop))
1. The [helm cli](https://docs.helm.sh/using_helm/#installing-helm) installed locally
1. Tiller running in the cluster, via `helm init`

Finally, this project contains a helm chart located in in the [helm directory](helm). To deploy the entire system simply run:

```bash
helm install ./helm/psi
```

> NOTE: The `psi` chart leverages the official Confluent helm chart to deploy zookeeper, kafka, and schema registry.
> See [cp-helm-charts](https://github.com/confluentinc/cp-helm-charts) for more information.

> NOTE: The images for this project are currently not published publicly. If your kubernetes hosts do not already have the images
> present they will have to pull them from a registry. To do so, you must create a `regcred` secret in your cluster:
>
> ```bash
> kubectl create secret docker-registry regcred --docker-server=... --docker-username=... --docker-password=... --docker-email=...
> ```

## Development

### Requirements

1. Java 8+
1. Docker
1. [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/), configured to point to any...
1. Kubernetes cluster (e.g. [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/))
1. [skaffold](https://github.com/GoogleContainerTools/skaffold#installation)
    - Note: if you already have docker, kubectl, and minikube installed, you only need to do the first step in the linked installation guide
    - Note: skaffold is also available via homebrew

### Recommended Development Cycle

From the root of this repo, run:
```bash
./gradlew -t test bootJar shadowJar
```
Which will:
1. Install gradle if needed
1. Run the unit tests of each subproject
1. Assemble the executable jar used in each component's docker image
1. Set up a watch on the source code of each subproject which will rerun compilation and tests when they change

Now, in a separate shell, run:
```bash
skaffold dev
```
Which will:
1. Locally build docker images for each subproject, using the outputs compiled by gradle above
1. Deploy the k8s objects based on those images, as well as dependencies like zookeeper and kafka, to your k8s cluster
1. Set up a watch on the docker image source files which will rebuild and redeploy their corresponding images when they change

At this point, modifying a source file in a subproject will:
1. Run its unit tests
1. Assemble its jars
1. Build its docker image
1. Deploy its objects to the cluster

#### One-off Variant

If you would rather not have gradle and skaffold watching for every change, you can skip the `-t` option and use
`skaffold run` to deploy the system compiled outputs in the cluster once.

### Accessing Kubernetes Services

The registry api, as well as Landoop's [kafka-topics-ui](https://github.com/Landoop/kafka-topics-ui) and Yahoo's 
[kafka-manager](https://github.com/yahoo/kafka-manager) are exposed through ambassador via a LoadBalancer service.
When using docker desktop, you can access them at:

```
http://localhost/registry       # psi-registry 
http://localhost/kafka-ui       # kafka-ui 
http://localhost/kafka-manager  # kafka-manager 
```

From there you can do things like upload some metadata to test the system:

```bash
curl -X PUT\
     -H "Content-Type: application/xml" \
     http://localhost/registry/metadata/collection \
     --data-binary @regsitry/src/test/resources/dscovr_fc1.xml
```

### Persistent Storage

The zookeeper, kafka, and registry deployments are configured to store there data in volumes allocated by 
persistent volume claims. These claims remain when pods are removed and even when the deployments are deleted.
You can see the claims and their corresponding volumes them with `kubectl`, like this:

```bash
kubectl get pvc,pv
NAME                                            STATUS    VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
persistentvolumeclaim/data-kafka-sts-0          Bound     pvc-e602da5b-5e25-11e8-9f48-080027a6b205   1Gi        RWO            standard       9d
persistentvolumeclaim/data-registry-0           Bound     pvc-dc8c32dc-65c0-11e8-8652-080027a6b205   1Gi        RWO            standard       3h
persistentvolumeclaim/data-zookeeper-sts-0      Bound     pvc-3860e2c5-5de4-11e8-9f48-080027a6b205   1Gi        RWO            standard       10d

NAME                                                        CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS    CLAIM                             STORAGECLASS   REASON    AGE
persistentvolume/pvc-3860e2c5-5de4-11e8-9f48-080027a6b205   1Gi        RWO            Delete           Bound     default/data-zookeeper-sts-0      standard                 10d
persistentvolume/pvc-dc8c32dc-65c0-11e8-8652-080027a6b205   1Gi        RWO            Delete           Bound     default/data-registry-0           standard                 3h
persistentvolume/pvc-e602da5b-5e25-11e8-9f48-080027a6b205   1Gi        RWO            Delete           Bound     default/data-kafka-sts-0          standard                 9d
```

To remove the persistent data, e.g. to wipe everything out and start over, you just need to delete the persistent volume claims:

```bash
kubectl delete pvc data-registry-0
kubectl delete pvc data-kafka-sts-0
kubectl delete pvc data-zookeeper-sts-0
```

Or, to delete all volume claims in the current namespace:

```bash
kubectl delete pvc --all
```

## Architectural Background

This project embraces the event-streaming paradigm reflected in the [Kappa Architecture](www.kappa-architecture.com):

1. All state changes are recorded as immutable events in an ordered, distributed log
1. Business logic is implemented in distributed processing algorithms that operate on the stream of events
1. Views of the output data are deterministically materialized in appropriate data stores to support querying use cases,
e.g. an inverted index for searching, a key-value store for random access, etc.

### References: Concepts

- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) - Martin Fowler
- [Command Query Responsibility Segregation](https://martinfowler.com/bliki/CQRS.html) - Martin Fowler
- [The Log: What every software engineer should know about real-time data's unifying abstraction](https://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying) - Jay Kreps
- [Turning the Database Inside Out](https://www.youtube.com/watch?v=fU9hR3kiOK0) - Martin Kleppmann
- [How to Beat the CAP Theorem](http://nathanmarz.com/blog/how-to-beat-the-cap-theorem.html) - Nathan Marz
- [Questioning the Lambda Architecture](https://www.oreilly.com/ideas/questioning-the-lambda-architecture) - Jay Kreps
- [Why local state is a fundamental primitive in stream processing](https://www.oreilly.com/ideas/why-local-state-is-a-fundamental-primitive-in-stream-processing) - Jay Kreps
- [Why Avro For Kafka Data?](https://www.confluent.io/blog/avro-kafka-data/) - Jay Kreps

### References: Specifics

- [Introduction to Kafka](https://kafka.apache.org/intro)
- [Event sourcing, CQRS, stream processing and Apache Kafka: What’s the connection?](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/)
- [Introducing Kafka Streams](https://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple/)
- [Yes, Virginia, You Really Do Need a Schema Registry](https://www.confluent.io/blog/schema-registry-kafka-stream-processing-yes-virginia-you-really-need-one/)
- This entire [series](https://www.confluent.io/blog/data-dichotomy-rethinking-the-way-we-treat-data-and-services/) 
from [confluent](https://www.confluent.io/blog/build-services-backbone-events/) 
which [culminates](https://www.confluent.io/blog/apache-kafka-for-service-architectures/) 
in a [walkthrough](https://www.confluent.io/blog/chain-services-exactly-guarantees/) 
of [building](https://www.confluent.io/blog/messaging-single-source-truth/) 
a streaming [system](https://www.confluent.io/blog/leveraging-power-database-unbundled/) 
is [great](https://www.confluent.io/blog/building-a-microservices-ecosystem-with-kafka-streams-and-ksql/).

## Legal

This software was developed by Team Foam-Cat,
under the MSN project: 1555839,
NOAA award number NA17OAR4320101 to CIRES.
This code is licensed under GPL version 2.
© 2018 The Regents of the University of Colorado.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation version 2
of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
