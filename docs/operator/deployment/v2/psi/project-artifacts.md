<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 15 minutes**

## Project Artifacts

The project consists of two Java-based microservices, backed by a Kafka cluster.

### Registry

#### Purpose

The [registry](/onestop/api/registry-api) hosts the public API of the system and stores its persistent state. Specifically, it
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

| Environment Variable            | Importance | Required? | Default             | Description |
| ------------------------------- | ---------- | --------- | ------------------- | ----------- |
| KAFKA_BOOTSTRAP_SERVERS         | High       | No        | localhost:9092      | Comma-separated list of one or more kafka host:port combinations |
| KAFKA_SCHEMA_REGISTRY_URL       | High       | No        | localhost:8081      | The URL of the Schema Registry |
| KAFKA_STATE_DIR                 | High       | No        | /tmp/kafka-streams  | Path to the directory under which local state should be stored |
| KAFKA_REQUEST_TIMEOUT_MS        | High       | No        | 1000 (1 sec)        | The maximum amount of time the client will wait for the response of a request |
| KAFKA_COMPRESSION_TYPE          | Medium     | No        | gzip                | The compression algorithm to use when publishing kafka messages. Valid values are `none`, `gzip`, `snappy`, `lz4`, or `zstd` |
| API_ROOT_URL                    | Medium     | No        | (none)              | The full, public-facing URL at which the root of this API will be exposed [[1]](#a-note-on-proxies)
| SERVER_SERVLET_CONTEXT-PATH     | Medium     | No        | ''                  | The context path at which to run the root of this API [[1]](#a-note-on-proxies)
| PUBLISHING_INTERVAL_MS          | Low        | No        | 300000 (5 minutes)  | Frequency with which check for changes in entity publish status |
| KAFKA_CACHE_MAX_BYTES_BUFFERING | Low        | No        | 104857600 (100 MiB) | Amount to memory to use to buffer messages before flushing them to kafka |
| KAFKA_COMMIT_INTERVAL_MS        | Low        | No        | 30000 (30 sec)      | The frequency with which to save the position of the processor |

#### Providing config values via yaml

In addition to setting environment variables directly, you can provide config to the application with a file containing
yaml versions of the variables, e.g. `kafka.bootstrap.servers: "..."` and setting the `SPRING_CONFIG_ADDITONAL-LOCATION`
environment variable to indicate the path to that file.

(Technically there are [a plethora of other ways](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
to provide config to the registry as it is a Spring application.)

#### Providing arbitrary Kafka client config values

The app passes all supported Kafka config values on to the several Kafka client instances it creates, namely the values for
[producers](https://docs.confluent.io/current/installation/configuration/producer-configs.html),
[streams apps](https://docs.confluent.io/current/streams/developer-guide/config-streams.html), and
[admin clients](https://docs.confluent.io/current/installation/configuration/admin-configs.html).
Simply prefix the variable with `KAFKA_` (or `kafka.` in yaml) and it will be passed on to the relevant clients.

So for example, you could configure an SSL keystore to use in connecting to Kafka by putting...

```yaml
kafka:
  ssl:
    keystore:
      location: ...
      password: ...
```

...in an external yaml file and setting `SPRING_CONFIG_ADDITONAL-LOCATION` with the location of that file.

> IMPORTANT: Do NOT override the application.id kafka property. The app creates and references its own app id and overriding it may break it.

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

The stream manager processes all the metadata that passes through the inventory management system. Metadata that is
not yet well-formed (i.e. in ISO-19115 XML format) is passed off to a Kafka topic to be transformed into well-formed
metadata via domain-specific logic. Once the metadata is well formed, discovery information is parsed out of it, and
that discovery information is then analyzed. All resulting info is then sent back to the registry for storage.


#### Requirements

- Java: 8+
- Storage: None
- Network Connectivity:
    - Initiates connections to the Kafka cluster

#### Config

The following config values are set inside the parsalyzer application if no environment variables, system properties, or YAML config file are available. All of these can be overridden, along with any of the values found in the [Kafka configuration documentation](https://kafka.apache.org/documentation/#configuration) through any of the configuration sources. All Kafka properties are expected to begin with `kafka` when provided to the application.

| Environment Variable            | Importance | Required? | Default             | Description |
| ------------------------------- | ---------- | --------- | ------------------- | ----------- |
| KAFKA_BOOTSTRAP_SERVERS         | High       | No        | localhost:9092      | Comma-separated list of one or more kafka host:port combinations |
| KAFKA_SCHEMA_REGISTRY_URL       | High       | No        | localhost:8081      | The URL of the Schema Registry |
| KAFKA_COMPRESSION_TYPE          | Medium     | No        | gzip                | The compression algorithm to use when publishing kafka messages. Valid values are `none`, `gzip`, `snappy`, `lz4`, or `zstd` |
| KAFKA_AUTO_OFFSET_RESET         | Medium     | No        | earliest            | What to reset the offset to when there is no initial offset in Kafka or if the current offset does not exist anymore on the server. Valid values: `earliest`, `latest`, `none` (which throws an exception to the consumer).
| KAFKA_CACHE_MAX_BYTES_BUFFERING | Low        | No        | 104857600 (100 MiB) | Amount to memory to use to buffer messages before flushing them to kafka |
| KAFKA_COMMIT_INTERVAL_MS        | Low        | No        | 30000 (30 sec)      | The frequency with which to save the position of the processor |

#### Providing config values via yaml

In addition to setting environment variables directly, you can provide config to the application with a file containing
yaml versions of the variables, e.g. `kafka.bootstrap.servers: "..."` and setting the `CONFIG_LOCATION`
environment variable to indicate the path to that file.

#### Providing arbitrary Kafka client config values

The app passes all supported Kafka config values on to the Kafka clients it creates, namely the values for 
[streams apps](https://docs.confluent.io/current/streams/developer-guide/config-streams.html).
Simply prefix the variable with `KAFKA_` (or `kafka.` in yaml) and it will be passed on to the relevant clients.

So for example, you could configure an SSL keystore to use in connecting to Kafka by putting...

```yaml
kafka:
  ssl:
    keystore:
      location: ...
      password: ...
```

...in an external yaml file and setting `CONFIG_LOCATION` with the location of that file.

> IMPORTANT: Do NOT override the application.id kafka property. The app creates and references its own app id and overriding it may break it.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
