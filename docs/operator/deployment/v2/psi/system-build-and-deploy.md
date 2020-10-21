## Build

Requires: Java 8+

Use Gradle to build the components with: `./gradlew build`

The build produces the following artifacts for each component:

- docker image
- fat, executable jar
- thin war
    - *N/A for parsalyzer; it is not a web application*
- sources jar

Note: To build only the jar and war artifacts, e.g. if docker is unavailable, use:

 `./gradlew build -x BuildDockerImage`


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
java -jar parsalyzer/build/libs/psi-parsalyzer-$VERSION-all.jar
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
for the registry and parsalyzer, as well as Kafka and Zookeeper using the well-maintained [Confluent Platform images](https://github.com/confluentinc/cp-docker-images).

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
  cedardevs/psi-parsalyzer
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
