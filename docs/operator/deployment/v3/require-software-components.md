<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

## deployment System Requirements
The core of the OneStop system are Apache Kafka and Elastic search. 
Apache Kafka provides a distributed, durable, ordered, streaming event platform
which OneStop leverages to store all its inputs as well as all derived metadata. It also utilizes the
[Confluent Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html) to store
[Avro](https://avro.apache.org/docs/current/) schemas defining the shapes of its messages and ensure that they evolve
in a backward-compatible way.

[Elastic Search](https://www.elastic.co/guide/index.html) is full-text search and analytics engine, which OneStop leverages for indexing, searching, and analyzing of metadata quickly and in near real time.

For more information about the system architecture and the way it utilizes these tools see the [architecture](#architectural-background) section below.

Each piece of required infrastructure is described in more depth below. For Kafka we recommend using the
open source versions published with the [Confluent Platform](https://docs.confluent.io/current/platform.html#what-is-included-in-cp).
These packages are free, open source, and actively developed and maintained by Confluent.

### Document Structure
* [Elastic Search](#elastic-search)
* [Zookeeper](#zookeeper)
* [Kafka](#kafka)
* [Schema Registry](#schema-registry)


## Elastic-search 

### Zookeeper
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
The system utilizes [Avro](https://avro.apache.org/docs/current/) schemas to define the shapes of the metadata
entities which flow through it. The Schema Registry is a central location where all these schemas are stored; data
producers publish the schemas of data they produce to it so that consumers can then retrieve those schemas and read
the data. It also facilitates the evolution of those schemas over time, for example to enforce backwards compatibility.
#### Requirements
- Java: 8+
- Storage: None
- Network Connectivity:
    - Initiates requests to the Kafka cluster
    - Receives requests from PSI Registry, PSI Stream Manager, and any other downstream data consumers.
    
For more detailed information see the [Confluent deployment guide](https://docs.confluent.io/current/schema-registry/docs/deployment.html).

<hr>
<div align="center"><a href="#">Top of Page</a></div>