## Project Artifacts

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

| Environment Variable        | Importance | Required? | Default            | Description |
| --------------------------- | ---------- | --------- | ------------------ | ----------- |
| KAFKA_BOOTSTRAP_SERVERS     | High       | No        | localhost:9092     | Comma-separated list of one or more kafka host:port combinations |
| SCHEMA_REGISTRY_URL         | High       | No        | localhost:8081     | The URL of the Schema Registry |
| STATE_DIR                   | High       | No        | /tmp/kafka-streams | Path to the directory under which local state should be stored |
| KAFKA_COMPRESSION_TYPE      | Medium     | No        | gzip               | The compression algorithm to use when publishing kafka messages. Valid values are `none`, `gzip`, `snappy`, `lz4`, or `zstd` |
| API_ROOT_URL                | Medium     | No        | (none)             | The full, public-facing URL at which the root of this API will be exposed [[1]](#a-note-on-proxies)
| SERVER_SERVLET_CONTEXT-PATH | Medium     | No        | ''                 | The context path at which to run the root of this API [[1]](#a-note-on-proxies)
| PUBLISHING_INTERVAL_MS      | Low        | No        | 300000 (5 minutes) | Frequency with which check for changes in entity publish status |

##### A Note On Proxies

Some of the responses from the registry API include links to other relevant endpoints. For example the response when
retrieving the raw input of a collection entity will include a link to its corresponding parsed values and vice versa.
See the [JSON_API](https://jsonapi.org/) specification for more information about this pattern.

If you intend to host the registry API behind a reverse proxy, there are several configuration options to consider in
order for the API to correctly produce URLs which reflect the proxy through with the app is being hosted.

*Proxy Request Headers* - A proxy server can apply additional headers to the internal requests it makes to the service
being proxied (in fact this is often the default behavior) to indicate where the original request came from as well as
the host the request was sent to and the protocol it used. The Registry API can leverage either the
[`X-Forwarded-Host`](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Host) and
[`X-Forwarded-Proto`](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto) header combination
or their more modern replacement, the [`Forwarded`](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded)
header, in order to build URLs that reflect the external host and protocol that requests should be sent to.

*Path-Based Routing* - If your proxy rules are based a path in addition to a host name, the Registry API needs to know
that path as well. One way to do so is to set the servlet's context path with the `SERVER_SERVLET_CONTEXT-PATH` property.

Example:
```
         [proxy server]                      [api server]
  https://external.host/registry ---> http://internal.host:8080/registry
   |                                   |
   |                                   `--<has property>--> SERVER_SERVLET_CONTEXT-PATH="/registry"
   |
   `--<sets header>--> "Forwarded: proto=https;host=external.host"
```

*Static API Root Config* - As an alternative to proxy headers and context paths, you can also use the `API_ROOT_URL`
property to simply provide the full URL at which the API will be hosted.

Example:
```
         [proxy server]                      [api server]
  https://external.host/registry ---> http://internal.host:8080
                                       |
                                       `--<has property>--> API_ROOT_URL="https://external.host/registry"
```

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
| KAFKA_COMPRESSION_TYPE  | Medium     | No        | gzip               | The compression algorithm to use when publishing kafka messages. Valid values are `none`, `gzip`, `snappy`, `lz4`, or `zstd` |

\* This variable was originally named `IM_BOOTSTRAP_SERVERS`. This old name continues to work in 0.1.x versions but
is deprecated and will be removed in a future version.
