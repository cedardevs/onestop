# OneStop 2.1 Deployment Notes

## Migrating from 2.0 to 2.1

There are some important differences to the OneStop configuration and conventions moving from 2.0 to 2.1. Most significantly, the context path of both APIs have changed.

### API Context Path Changes

The context paths we previously baked into our APIs overlapped the client browser endpoint `/onestop/*`.

While it was possible to configure a web server to distinguish requests to the APIs with an overlapping path, it became increasingly confusing to handle client JavaScript routing once we removed a hash router (e.g. - `/onestop/#/collections`) and replaced it with a browser router (e.g. - `/onestop/collections`). Changing the structure of the URLs was required to support search engine optimization (SEO), however it has some implications for the configuration of the web server.


| API | 2.0 (old) | 2.1 (new) |
| --- | --------- | --------- |
| Search Service (api-search) | `/onestop/api` | `/onestop-search` |
| Admin Service (api-metadata) | `/onestop/admin` | `/onestop-admin` |


### Apache Configuration
Changing the context paths allows to disambiguate how an endpoint will be proxied and redirected from the perspective of Apache, Nginx, or whatever web server is used.

In addition to deploying our development APIs to tomcat containers, we started using an Apache `http.conf` rather than nginx to more closely match production and what issues may arise during deployment to d=production. The relevant config is shown below:

```
 <LocationMatch ^/ONESTOP_PREFIX_PATH/(?!api).* >
  RewriteEngine on
  # RewriteRule ^/ONESTOP_PREFIX_PATH/(.*) /$1

  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteRule . /ONESTOP_PREFIX_PATH/index.html [L]
</LocationMatch>

<LocationMatch ^/(?!ONESTOP_PREFIX_PATH)/(?!api).* >
  RewriteEngine on

  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteRule . /ONESTOP_PREFIX_PATH/%{REQUEST_FILENAME}
  RewriteRule . /ONESTOP_PREFIX_PATH/index.html [L]
</LocationMatch>

<Location /ONESTOP_PREFIX_PATH-search >
  ProxyPass http://ONESTOP_SEARCH_API_ENDPOINT
  RequestHeader set X-Forwarded-Proto https
  RequestHeader set X-Forwarded-Port 443
  ProxyPreserveHost On
</Location>
```

We replace strings in this configuration during our container creation time. I’m using it here as a reference for what would be different in the production deployment environment.



| ENV | ONESTOP_PREFIX_PATH | ONESTOP_SEARCH_API_ENDPOINT |
| --- | ------------------- | --------------------------- |
| DEV | onestop | api-search:8080/onestop-search |
| TEST | onestop | <testSearchProxyHost>:<testSearchProxyPort>/onestop-search |
| PROD | onestop | <prodSearchProxyHost>:<prodSearchProxyPort>/onestop-search |

The idea of the prefix path is for our stack to be deployed (in the future) to an arbitrary endpoint more flexibly and to accommodate multiple instances. For now, making different paths is not a concern for the 2.1 release.

> Note: It is critical that the web server load the index.html for all document requests (`/onestop/*`). This allows the JavaScript client to resolve the paths with internal routing logic.


### Search Engine Optimization (sitemap.xml)
To improve SEO, we’ve added endpoints to the search API to retrieve a `sitemap.xml` that can be seen by the client in the browser.


For example, a browser should resolve this link:
https://sciapps.colorado.edu/onestop/sitemap.xml


To an XML with references to one or more sitemap files:
```
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <sitemap>
      <loc>https://sciapps.colorado.edu/onestop-search/sitemap/AWon1qKrfrc1sUL9qn1j.txt</loc>
      <lastmod>2019-04-16T20:28:58.378Z</lastmod>
    </sitemap>
</sitemapindex>
```

The sitemap reference should resolve also:
https://sciapps.colorado.edu/onestop-search/sitemap/AWon1qKrfrc1sUL9qn1j.txt

 To a file containing a list of references which can resolve to unique content on OneStop:
 ```
https://sciapps.colorado.edu/onestop/collections/details/AWoDH_Psve2FB-bXY1sh
https://sciapps.colorado.edu/onestop/collections/details/AWoDH-jbve2FB-bXY1r3
https://sciapps.colorado.edu/onestop/collections/details/AWoDH-lZve2FB-bXY1r5
https://sciapps.colorado.edu/onestop/collections/details/AWoDH-ikve2FB-bXY1r2
...
```


> Note: Sitemap is a new feature and we haven’t had the ability to see how it performs at scale. We’ve only been able to see it run on the order of 10K entries versus the millions of entries or more we would encounter in production.
>
> If it is determined the feature is inefficient with Elasticsearch resources during our trial runs in the test environment, we have two options:
> 1. Configure the re-indexing delay, frequency, scroll size, and submap size of the sitemap
>     1. Update the api-metadata config YML with
>         1. `etl.sitemap.delay.initial: <delay in ms>`
>         1. `etl.sitemap.delay.fixed: <frequency in ms>`
>         1. `etl.sitemap.scroll-size: <ES scroll size>`
>         1. `etl.sitemap.collections-per-submap: <submap size limit>`
>     1. Disable the feature entirely (see the "Feature Toggling" section below)
>
> In the future, we could prevent some performance concerns in a couple of ways:
> 1. **Improvement**: Use the upcoming features of PSI to populate the Elasticsearch indices in a streaming way (prevent batching).
> 1. **Preferred**: Have the ability for our API to manage a static file server so that we don’t have to store sitemap information in Elasticsearch.

### Feature Toggling

#### Spring Profiles
Our APIs are now consistently leveraging Spring Profiles to enable certain features in the deployed environment and as a fail-safe to back off a feature.

Features are written to be *disabled by default*. They are only activated by telling Spring which feature-specific profile names are active. There are many mechanisms Spring allows to do this, but we prefer the simplicity and priority of the `SPRING_PROFILES_ACTIVE` environment variable.

The environment variable can contain multiple features by using a comma-delimited list. For example, the following could be used on the search API:

```
export SPRING_PROFILES_ACTIVE="sitemap,login-gov"
```

#### Admin Service (api-metadata)

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| icam | Enables a Spring security filter to require ICAM CAC authentication and authorization to hit particular endpoints. Requires a configured ICAM keystore and credentials at runtime! We don’t expect this feature to be enabled in 2.1 production, and the production environment would need to create and configure its own keystore as well as register its public key and other Service Provider (SP) metadata to ICAM. | false |
| manual-upload | Enables the UploadController which opens browser endpoints for manual metadata upload. This feature should always be set with the icam profile in production to ensure manual upload is CAC secured. | false |
| kafka-ingest | Enables the KafkaConsumerService to upload metadata via PSI. This feature should never be enabled at the same time as the manual-upload feature as they are mutually exclusive approaches to metadata upload. | false |
| sitemap | Enables the SitemapETLService to create the sitemap index and periodically refresh it. | false |

#### Search Service (api-search)

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| login-gov |Enables a Spring security filter to enable OpenId authentication via login.gov. This also triggers the uiConfig endpoint to show an auth section which indicates to the client to show a login link. Note: This feature will eventually migrate to a new api-user service with a PostgreSQL backing DB.

Requires a configured ICAM keystore and credentials at runtime! We don’t expect this feature to be enabled in 2.1 production, and the production environment would need to create and configure its own keystore as well as register its public key and other Service Provider (SP) metadata to login.gov. | false |
| sitemap | Enables a the /sitemap.xml and /sitemap/{id}.txt public endpoints. | false |

##### Sitemap (Additional Config Required)

The search API ultimately constructs the sitemap content by deriving information from Elasticsearch and constructing links to known routes in our OneStop browser client.

Unfortunately, we don’t currently have a way to dynamically determine where the client is hosted (from the search API’s perspective). So this is left to a manual configuration via two additional environment variables (`SITEMAP_CLIENT_PATH` and `SITEMAP_API_PATH`) for the search API to consume.


| ENV | SITEMAP_CLIENT_PATH | SITEMAP_API_PATH |
| --- | --- | --- |
| DEV | https://sciapps.colorado.edu/onestop | https://sciapps.colorado.edu/onestop-search
| TEST | https://<testClientHost>/onestop | https://<testSearchHost>/onestop-search |
| PROD | https://<prodClientHost>/onestop | https://<prodSearchHost>/onestop-search |


##### Google Analytics and UI Cart Toggle


There are two *new* sections to the search API UI config YML.

1. Google Analytics can be configured by replacing the following parts with the appropriate IDs for the test and prod environments. See Google Analytics documentation for more details.
    - `trackingId: 'UA-XXXXXXXXX-X'`
    - `userId: XXXXXXXXX`
1. If the cart feature is desired, simply add the `enableFeatureToggles` config section as seen below. Without it, the UI will default to not showing a cart feature.
```
 ui:
    googleAnalytics:
      profiles:
        - trackingId: 'UA-XXXXXXXXX-X'
          gaOptions:
            userId: XXXXXXXXX
      reactGaOptions:
        alwaysSendToDefaultTracker: false
    enabledFeatureToggles:
      - featureName: cart
    banner:
      message: TEST - This site is not running on NCEI hardware, does not contain NCEI's full data holdings, and contains a limited set of its intended functionality.
    featured:
      - title: Super Important Featured Dataset 1
        searchTerm: '"Gridded Satellite GOES"'
        imageUrl: "https://www.ncdc.noaa.gov/gridsat/images/sample.png"
      - ...
```

##### Sanity Checks

A runtime sanity check to determine if you’ve properly enabled a Spring profile is to check the first few lines of the Spring application log output. For example, if you enabled the `sitemap` profile, you would see a log like:

```
The following profiles are active: sitemap
```

##### Onestop Client Configuration

Because the client is a static JavaScript bundle (no server-side Node, for example), it does *not* benefit from the same profile mechanism as the APIs (or even environment variables, currently).

The OneStop client hits the `/uiConfig` endpoint of the search API in order to initialize features in the browser. This means that little should be necessary other than ensuring that the static JavaScript bundle is hosted in a way that the `ONESTOP_PREFIX_PATH` (discussed in the "Apache Configuration" section) hosts the `index.html` file of the client.

## Preparing for future 3.x releases

* Granule filtering
    * Improved user experience means more requests to Elasticsearch
    * Allocating more nodes and resources to our stack is advisable.
* New `api-user` service will be introduced to the OneStop stack (with a backing store)
    * Support browser client login features.
    * Move some endpoints and features that had been shoehorned into `api-search`
* `api-metadata` will be renamed to `api-admin`.
    * This only affects our source code and verbal communication of the service
    * This transition is already incrementally underway, but will be made official for 3.x
* Ideally, authentication and authorization will be delegated to a service like CAS so that we can make our application security configurations more consistent and worry less about maintaining custom security filters for different identity providers like ICAM and login.gov.
* "banner" in UI config will be named "disclaimer" for clarity
* And there’s always more...
