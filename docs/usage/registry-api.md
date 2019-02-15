### Registry Overview

The registry provides a horizontally-scalable API and storage for granule- and collection-level metadata backed by Kafka. 

It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events,
merging them with previous events to provide a full picture of the metadata for each granule and collection. 

### The RESTful Interface
