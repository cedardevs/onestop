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
