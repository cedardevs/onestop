<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

## Required Software Components

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

<hr>
<div align="center"><a href="#">Top of Page</a></div>
