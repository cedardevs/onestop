# Configuration and Requirements

## APIs

OneStop contains two separate APIs.

- `admin` is the write-enabled endpoints, used for administrative tasks.
- `search` is the read-only search and client-specific endpoints, and is intended to be public facing.

### Dependencies

The APIs are Spring Boot applications that are packaged as WARs. It is generally assumed that they will be deployed into an externally managed Tomcat.

The APIs require Java 8 to run and must be able to connect to ElasticSearch 5.x.

## Client

The client is a a pure HTML/JavaScript/CSS application packaged as a tarball. To host it simply expand the tar into the root of your web server.

Note that the app requires NodeJS to be built from source (as it is concatenated, minified, compressed, etc.) but it has no dependency on node at runtime.

### Dependencies
In order to execute searches and display correct information, the client assumes that it can make requests the search api with the path `/onestop-search/` on the same host the client is running on.

Hence, a reverse proxy must be put into place from that path to the Search API application.

Additionally, since the code is a single page application, contained in a single JavaScript file, but has multiple URLs it must resolve, it must be able to load the `index.html` file from any URL after the `/onestop/` prefix.

For example, if you are hosting the client using `httpd` and the Search API application is running on the same host on port 8097, you could add the following to your `httpd.conf`:

```<LocationMatch ^/onestop/(?!api).* >
  RewriteEngine on
  # RewriteRule ^/onestop/(.*) /$1

  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteRule . /onestop/index.html [L]
</LocationMatch>

<LocationMatch ^/(?!onestop)/(?!api).* >
  RewriteEngine on

  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteRule . /onestop/%{REQUEST_FILENAME}
  RewriteRule . /onestop/index.html [L]
</LocationMatch>

<Location /onestop-search >
  ProxyPass http://localhost:8097/onestop-search
  RequestHeader set X-Forwarded-Proto https
  RequestHeader set X-Forwarded-Port 443
  ProxyPreserveHost On
</Location>
```

Note that the Metadata API application has other endpoints, e.g. for uploading metadata and triggering indexing,
but that these endpoints are potentially destructive and should likely not be exposed to the public.


## The Configuration Process
Configuring OneStop to run for development, testing, and production purposes is predominantly done via a configuration file.

Since OneStop is a Spring Boot application, it can easily take advantage of this externalized configuration. Spring will look for properties files, YAML files, environment variables, and command-line arguments in the order specified [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html). The compiled OneStop API applications include an application.yml file ([Search API's default config values](/search/src/main/resources/application.yml) and [Metadata API's default config values](/admin/src/main/resources/application.yml)) that contains the default values. However, if Spring encounters additional configuration parameters in its step-through of specified locations, any or all of these parameters can be overwritten or added to. Likewise, previously unspecified values may be added.

The [2.1 Deployment Guide](/docs/deployment/2-1-guide.md) has a more in-depth discussion of configuration values around feature toggles.

## Running Locally

See [Quickstart](/docs/development/quickstart.md).

## Running on a Server
Wherever you choose to put your configuration values, it is important that some of the default values be replaced and otherwise optional parameters be provided before deploying OneStop to a production environment. Let's go through the categories:

### Server Values
Use to modify the port and context path that the app uses when executed directly. These are used when running `./gradlew springboot` locally, or as a self-executing jar with embedded Tomcat. They are ignored by deployment in an external Tomcat, which uses the WAR name to determine the context-path instead. Defaults result in http://localhost:8097/onestop-search for the search API and http://localhost:8098/onestop-admin for the metadata management API.

### Elasticsearch Values

Elasticsearch configuration should be set the same in both APIs. Otherwise administrative tasks will not correctly make changes for the search.

Depending on how you have Elasticsearch setup, some or all of the available configuration values here may be of interest to you:
- **Host and port** (`elasticsearch.host`, `elasticsearch.port`): Comma-separated list (no spaces) of hosts to connect to the Elasticsearch cluster, and port (i.e., if you have configured it to be something other than the default)
- **Index prefix** (`elasticsearch.index.prefix` - optional): If, for example, your Shield credentials only provide access to a set of prefixed indices in your cluster, include said prefix here. All indexes and aliases used by OneStop will automatically start with this prefix.
- **Max Tasks** (`elasticsearch.max-tasks`): This applies to admin only. This limits the number of granule-reindexing tasks allowed to run in parallel on the cluster. The default value is 10. It is HIGHLY encouraged to not reduce this default as it will result in a large increase in time to reindex new/updated metadata. With a large cluster, a reindexing time improvement is plausible by increasing this number.
- **Requests Per Second** (`elasticsearch.requests-per-second`): This applies to admin only. This limits the number of requests per second that the cluster can execute during reindexing tasks. The default is unset, meaning that the cluster will auto-throttle requests on its own based on memory and CPU consumption at any given moment. It is HIGHLY encouraged to not set this number unless reindexing results in severe search degradation and the cluster absolutely cannot be given extra nodes. Providing this number WILL greatly increase the time required to reindex records. *A more robust cluster should be the preferred choice in the event of search or reindexing degradation!*

### Logging Values
Logging output is quite customizable and all possible configuration settings allowed by Spring boot (see [reference](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html)) can be used.

### UI Values
Some features of the associated UI are customizable via the API.

A disclaimer can be placed at the top of all pages, with a personalized message and color scheme (CSS colors). The `color` and `backgroundColor` fields are optional. The colors fallback to a red-colored background with white text, respectively, when invalid CSS color values are provided. Valid colors include: `ForestGreen`, `springgreen`, `Violet`, `#123456`, `#A11`, `rgb(240, 4, 133)`, `rgba(0, 45, 0, 0.3)`, and any other browser-friendly CSS colors.

#### Disclaimer Configuration Example:

```yml
ui:
  disclaimer:
    message: "This is a demo"
    backgroundColor: yellow
    color: #000
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
