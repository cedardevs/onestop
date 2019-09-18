
# Feature Toggling

## Spring Profiles
Our APIs are now consistently leveraging Spring Profiles to enable certain features in the deployed environment and as a fail-safe to back off a feature.

Features are written to be *disabled by default*. They are only activated by telling Spring which feature-specific profile names are active. There are many mechanisms Spring allows to do this, but we prefer the simplicity and priority of the `SPRING_PROFILES_ACTIVE` environment variable.

The environment variable can contain multiple features by using a comma-delimited list. For example, the following could be used on the search API:

```
export SPRING_PROFILES_ACTIVE="sitemap,login-gov"
```

### Admin Service (admin)

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| icam | Enables a Spring security filter to require ICAM CAC authentication and authorization to hit particular endpoints. Requires a configured ICAM keystore and credentials at runtime! We don’t expect this feature to be enabled in 2.1 production, and the production environment would need to create and configure its own keystore as well as register its public key and other Service Provider (SP) metadata to ICAM. | false |
| manual-upload | Enables the UploadController which opens browser endpoints for manual metadata upload. This feature should always be set with the icam profile in production to ensure manual upload is CAC secured. | false |
| kafka-ingest | Enables the KafkaConsumerService to upload metadata via PSI. This feature should never be enabled at the same time as the manual-upload feature as they are mutually exclusive approaches to metadata upload. | false |
| sitemap | Enables the SitemapETLService to create the sitemap index and periodically refresh it. | false |

### Search Service (search)

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| login-gov |Enables a Spring security filter to enable OpenId authentication via login.gov. This also triggers the uiConfig endpoint to show an auth section which indicates to the client to show a login link. Note: This feature will eventually migrate to a new user service with a PostgreSQL backing DB.

Requires a configured ICAM keystore and credentials at runtime! We don’t expect this feature to be enabled in 2.1 production, and the production environment would need to create and configure its own keystore as well as register its public key and other Service Provider (SP) metadata to login.gov. | false |
| sitemap | Enables a the /sitemap.xml and /sitemap/{id}.txt public endpoints. | false |

## Sitemap (Additional Config Required)

The search API ultimately constructs the sitemap content by deriving information from Elasticsearch and constructing links to known routes in our OneStop browser client.

To be enabled correctly, the sitemap feature must be enabled in both `search` and `admin`.

Unfortunately, we don’t currently have a way to dynamically determine where the client is hosted (from the search API’s perspective). So this is left to a manual configuration via two additional environment variables (`SITEMAP_CLIENT_PATH` and `SITEMAP_API_PATH`) for the search API to consume.


| ENV | SITEMAP_CLIENT_PATH | SITEMAP_API_PATH |
| --- | --- | --- |
| DEV | https://sciapps.colorado.edu/onestop | https://sciapps.colorado.edu/onestop-search
| TEST | https://<testClientHost>/onestop | https://<testSearchHost>/onestop-search |
| PROD | https://<prodClientHost>/onestop | https://<prodSearchHost>/onestop-search |



> Note: Sitemap is a new feature and we haven’t had the ability to see how it performs at scale. We’ve only been able to see it run on the order of 10K entries versus the millions of entries or more we would encounter in production.
>
> If it is determined the feature is inefficient with Elasticsearch resources during our trial runs in the test environment, we have two options:
> 1. Configure the re-indexing delay, frequency, scroll size, and submap size of the sitemap
>     1. Update the admin config YML with
>         1. `etl.sitemap.delay.initial: <delay in ms>`
>         1. `etl.sitemap.delay.fixed: <frequency in ms>`
>         1. `etl.sitemap.scroll-size: <ES scroll size>`
>         1. `etl.sitemap.collections-per-submap: <submap size limit>`
>     1. Disable the feature entirely (see the "Feature Toggling" section below)
>
> In the future, we could prevent some performance concerns in a couple of ways:
> 1. **Improvement**: Use the upcoming features of PSI to populate the Elasticsearch indices in a streaming way (prevent batching).
> 1. **Preferred**: Have the ability for our API to manage a static file server so that we don’t have to store sitemap information in Elasticsearch.

## Google Analytics and UI Cart Toggle


There are two *new* sections to the search API UI config YML.

1. If the cart feature is desired, simply add the `features` config section as seen below. Without it, the UI will default to not showing a cart feature.

1. Google Analytics can be configured by replacing the following parts with the appropriate IDs for the test and prod environments. When using multiple trackers, you **must** provide a name for additional trackers. There can only be one default (without a name), and the one without a name will only receive updates depending on `reactGaOptions.alwaysSendToDefaultTracker`. See Google Analytics and react-ga documentation for more details.   
    - `trackingId: 'UA-XXXXXXXXX-X'`
    - `name: XXXXXXXXX`
    

```
 ui:
    googleAnalytics:
      profiles:
        - trackingId: 'UA-XXXXXXXXX-X'
          gaOptions:
            name: XXXXXXXXX
      reactGaOptions:
        alwaysSendToDefaultTracker: false
    features:
      - cart
    disclaimer:
      message: DEMO - This site is not running on NCEI hardware, does not contain NCEI's full data holdings, and contains a limited set of its intended functionality.
    featured:
      - title: Super Important Featured Dataset 1
        searchTerm: '"Gridded Satellite GOES"'
        imageUrl: "https://www.ncdc.noaa.gov/gridsat/images/sample.png"
      - ...
```

## Sanity Checks

A runtime sanity check to determine if you’ve properly enabled a Spring profile is to check the first few lines of the Spring application log output. For example, if you enabled the `sitemap` profile, you would see a log like:

```
The following profiles are active: sitemap
```

## Trending Searches

See [Trending Searches](/docs/deployment/trending-searches.md). Note, however, that this feature is stale.
