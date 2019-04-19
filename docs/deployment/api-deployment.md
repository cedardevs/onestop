### API

##### Installation
The APIs are Spring Boot applications that are packaged as both regular and fully executable WARs in the api-search/build/libs and api-metadata/build/libs folders, meaning that they can be run in a standalone webapp container like tomcat (onestop-api-search-[version].war), or with `java -jar api-search-[version]-all.war`, or executed directly like `./api-search-[version]-all.war`.

Probably the simplest way to install the APIs on a machine is to set them up as `init.d` or `systemd` services as described in the [Spring Boot service installation guide](http://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-service).
For example, linking the application into `init.d` like `ln -s [path to jar] /etc/init.d/onestop-search-api` will cause the app to run on startup, allow you to control it with tools like `service` or `systemctl` using the typical `start`, `stop`, `restart`, and `status` commands, and pipes the logging (which uses stdout by default) to `/var/log/onestop-search-api.log`.

##### Dependencies
The APIs require Java 8 to run and must be able to connect to ElasticSearch 5.x.

##### Security
Currently the API apps do not implement any authentication or authorization of their own. We recommend that you
control access to the Metadata API's sensitive endpoints.

If your ElasticSearch cluster has the X-Pack security plugin installed, the APIs can be configured to connect to
it using two sets of credentials -- readonly for the Search API and read/write for the Metadata API. The following is a breakdown of the privileges required by each user. See the configuration settings below for a description of how to provide the app with the credentials for the two users. Note the `{prefix}` on index names below is optional.

Readonly User:
```json
{
  "indices": [ 
    {
      "names": [ "{prefix}search_*" ], 
      "privileges": [ "read" ] 
    }
  ] 
}
```

Read/Write User:
```json
{
  "cluster": [ "manage", "manage_pipeline" ],
  "indices": [ 
    {
      "names": [ "{prefix}search_*", "{prefix}staging_*" ], 
      "privileges": [ "all" ] 
    },
    {
      "names": [ ".tasks*" ],
      "privileges": [ "read", "write" ]
    }
  ] 
}
```

In-depth setup information for X-Pack Security can be found in the [related Elasticsearch documentation](https://www.elastic.co/guide/en/x-pack/5.6/security-getting-started.html).

##### Configuration
There are several configurable values that can be passed to the application. They can be provided to the app in a number of ways as outlined in the [Spring Externalized Configuration Documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

We recommend using a [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config/) to make the config available to the app in a consistent and secure way. Alternatively, a simple way to pass config values is to create a .yml or .properties file and provide its path to the app by setting the `spring.config.additional-location` value, e.g. with an environment variable:

`SPRING_CONFIG_ADDITIONALLOCATION=file:/my/config/path/config.yml ./onestop-search-api.war`

See the [Search API's default config values](https://github.com/cedardevs/onestop/blob/master/api-search/src/main/resources/application.yml) and the [Metadata API's default config values](https://github.com/cedardevs/onestop/blob/master/api-metadata/src/main/resources/application.yml) for a full picture of the values that can be set. 