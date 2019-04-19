## Getting Started

After building the project, you can run it locally with the following command:
```
env VERSION=1.2.0-SNAPSHOT VCS_REF="$(git rev-parse --short HEAD)" DATE="$(env TZ=0 date +"%Y-%m-%dT%H:%M:%SZ")" docker-compose up -d
```

In order to mount a disk to the elastic search container, to either save the data between runs or to provide preloaded data, copy docker-compose.override.yml.sample to docker-compose.override.yml. Configuration in the override will automatically be applied when running docker-compose up.

## Overwrite Configuration with Docker Compose

Spring configuration can be changed by setting environment variables in the compose file.

For example, setting the service environment variable elasticsearch_host will overwrite the elasticsearch.host value in the application.yml.

## Default Local Configuration

The default docker-compose file provides a simple full-stack version of the OneStop project, including a single-node elastic search, the api, and the client. The client can be accessed at localhost:8080, the api at localhost:8097, and ElasticSearch at localhost:9200.