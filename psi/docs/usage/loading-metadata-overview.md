## Loading Metadata Into PSI
Metadata can be loaded into the PSI ecosystem in two ways: via HTTP requests with the registry application or by connecting an upstream source to the underlying Kafka system with Kafka Connect.




### Registry Overview

The registry provides a horizontally-scalable API and storage for granule- and collection-level metadata backed by Kafka. 

It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events, merging them with previous events to provide a full picture of the metadata for each granule and collection. 

### Using HTTP
The registry is equipped with a RESTful interface that allows full CRUD control of metadata records stored by PSI.

See the [user docs about the REST API](registry-api.md) for more details.

