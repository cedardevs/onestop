# Persistent Streaming Information: PSI

[![CircleCI](https://circleci.com/gh/cedardevs/psi.svg?style=svg)](https://circleci.com/gh/cedardevs/psi)
[![codecov](https://codecov.io/gh/cedardevs/psi/branch/master/graph/badge.svg?token=mpaqa2QKdv)](https://codecov.io/gh/cedardevs/psi)

The purpose of this project is to build a system which can both store and run processing workflows on the metadata
related to every file ingested and archived by NOAA's National Centers for Environmental Information (NCEI). 

## Deployment

### Requirements

1. [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/), configured to point to any...
1. Kubernetes cluster (e.g. [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/))

### Quickstart

To deploy the system to a k8s cluster, simply run the following from the root of this repository.
```bash
kubectl apply -f kubernetes/*
```

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
./gradlew -t test
```
Which will:
1. Install gradle if needed
1. Compile the system's subprojects
1. Run the unit tests of each subproject
1. Set up a watch on the source code of each subproject which will rerun compilation and tests when they change

Now, in a seperate shell, run:
```bash
skaffold dev
```
Which will:
1. Locally build docker images for each subproject, using the outputs compiled by gradle above
1. Deploy the k8s objects based on those images, as well as dependencies like zookeeper and kafka, to your k8s cluster
1. Set up a watch on the docker image source files which will rebuild and redeploy their corresponding images when they change

At this point, modifying a source file in a subproject will:
1. Compile that subproject
1. Run its unit tests
1. Build its docker image
1. Deploy its objects to the cluster

#### One-off Variant

If you would rather not have gradle and skaffold watching for every change, you can instead use
`./gradlew test` (without the `-t` option) and `skaffold run` to run each step once.

### Accessing Kubernetes Services

The registry api, as well as kafka-ui and kafka-manager are exposed as NodePort services.
When using minikube, you can open the kakfa-ui and kafka-manager pages in a browser like:

```bash
minikube service kafka-ui
minikube service kafka-manager
``` 

You can also use minikube to help do things like curl to our registry api:
```bash
curl -X PUT\
     -H "Content-Type: application/json"\
     $(minikube service registry --url)/metadata/granule\
     --data-binary @regsitry/src/test/resources/test_granule.json
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

### References: Specifics

- [Introduction to Kafka](https://kafka.apache.org/intro)
- [Event sourcing, CQRS, stream processing and Apache Kafka: What’s the connection?](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/)
- [Introducing Kafka Streams](https://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple/)
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
