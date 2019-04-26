# Configuration

## The Configuration Process
Configuring OneStop to run for development, testing, and production purposes is predominantly done via a configuration file.

Since OneStop is a Spring Boot application, it can easily take advantage of this externalized configuration. Spring will look for properties files, YAML files, environment variables, and command-line arguments in the order specified [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html). The compiled OneStop API applications include an application.yml file ([api-search](https://github.com/cedardevs/onestop/blob/master/api-search/src/main/resources/application.yml) and [api-metadata](https://github.com/cedardevs/onestop/blob/master/api-metadata/src/main/resources/application.yml)) that contains the default values. However, if Spring encounters additional configuration parameters in its step-through of specified locations, any or all of these parameters can be overwritten or added to. Likewise, previously unspecified values may be added.

Environment variables can easily be used to modify the configuration when using docker-compose. See [Docker](/docs/development/local-docker.md) for more detail.

## Running Locally
For development purposes, the included application.yml file should suffice. If you wish to modify or add any configuration parameters though, an application.yml file placed at the onestop/api-[search/metadata] level will be ignored by git, safely allowing you to keep your customizations contained to your workspace. For developers, this is the ideal place to configure the UI banner and featured datasets, logging levels, and -- if you're feeling adventurous -- tinker with the Elasticsearch search configuration.

## Running on a Server
Wherever you choose to put your configuration values, it is important that some of the default values be replaced and otherwise optional parameters be provided before deploying OneStop to a production environment. Let's go through the categories:

### Server Values
Use to modify the port and context path that the app uses when executed directly. Defaults result in http://localhost:8097/onestop/api for the search API and http://localhost:8098/onestop/api for the metadata management API.

### Elasticsearch Values
Depending on how you have Elasticsearch setup, some or all of the available configuration values here may be of interest to you:
- **Cluster name** (elasticsearch.cluster.name; search and metadata): The default name of any Elasticsearch cluster is simply 'elasticsearch'. If you've renamed your cluster, though, it needs to be declared here.
- **Host and port** (elasticsearch.host; elasticsearch.port; search and metadata): Comma-separated list (no spaces) of hosts to connect to the Elasticsearch cluster, and port (i.e., if you have configured it to be something other than 9300)
- **Read-only and read-write credentials** (elasticsearch.[ro|rw].[user|pass]; RO for search, RW for metadata): Providing credentials will inform the OneStop application to connect to the cluster with Shield enabled, using the given credentials. Read-write access is required for admin, ETL, and metadata loading functionality.
- **SSL enabling** (elasticsearch.ssl.enabled; elasticsearch.ssl.keystore.[path|password]; search & metadata): The SSL parameters establish an encrypted connection between the application and cluster. These features also assume that Shield is setup on the cluster.
- **Index prefix** (elasticsearch.index.prefix; search & metadata): If, for example, your Shield credentials only provide access to a set of prefixed indices in your cluster, include said prefix here.
- **Max Tasks** (elasticsearch.max-tasks; metadata): This limits the number of granule-reindexing tasks allowed to run in parallel on the cluster. The default value is 10. It is HIGHLY encouraged to not reduce this default as it will result in a large increase in time to reindex new/updated metadata. With a large cluster, a reindexing time improvement is plausible by increasing this number.
- **Requests Per Second** (elasticsearch.requests-per-second; metadata): This limits the number of requests per second that the cluster can execute during reindexing tasks. The default is unset, meaning that the cluster will auto-throttle requests on its own based on memory and CPU consumption at any given moment. It is HIGHLY encouraged to not set this number unless reindexing results in severe search degradation and the cluster absolutely cannot be given extra nodes. Providing this number WILL greatly increase the time required to reindex records. *A more robust cluster should be the preferred choice in the event of search or reindexing degradation!*

### Logging Values
Logging output is quite customizable and all possible configuration settings allowed by Spring boot (see [reference](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html)) can be used.

### UI Values
Some features of the associated UI are customizable via the API.

A banner can be placed at the top of all pages, with a personalized message and color scheme (CSS colors). For example:

```yml
ui:
  banner:
    message: "This is a demo"
    colors:
      text: white
      background: red
```

Additionally, featured data sets are customizable. Here's an example:

```yml
ui:
  featured:
    - title: GHRSST
      searchTerm: ghrsst
      imageUrl: "https://image.url.png"
    - title: Digital Elevation Models
      searchTerm: '"digital elevation"'
      imageUrl: "https://image.url.jpg"
    - title: NWLON and PORTS
      searchTerm: +nwlon +ports
      imageUrl: "https://image.url.png"
```
