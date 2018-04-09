# Persistent Streaming Information: PSI

[![CircleCI](https://circleci.com/gh/cedardevs/psi.svg?style=svg)](https://circleci.com/gh/cedardevs/psi)

The purpose of this project is to build a system which can both store and run processing workflows on the metadata
related to every file ingested and archived by NOAA's National Centers for Environmental Information (NCEI). 

## Development

### Requirements

1. Java
1. Docker
1. Docker Compose

### Quickstart

```bash
git clone https://github.com/cedardevs/psi
cd psi
./gradlew composeStart
```

This will test and compile each component, build docker images for them, then use docker-compose to spin up
docker containers with zookeeper, kafka, and each component.

#### Pipe parsed outputs to Elasticsearch with Kafka Connect

To create the connectors in Kafka Connect to send collections and granules from to Elasticsearch, send these REST requests:

```bash
curl -X POST -H "Content-Type: application/json" localhost:8083/connectors --data-binary @onestop_collection_connector.json
curl -X POST -H "Content-Type: application/json" localhost:8083/connectors --data-binary @onestop_granule_connector.json  
```

## Technical Background

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
