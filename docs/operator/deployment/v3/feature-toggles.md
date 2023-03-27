<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 15 minutes**

# Feature Toggling
Features are written to be *disabled by default*. In order to enable features, set the appropriate profile in the application external configuration file. 
The environment variable can contain multiple features by using a comma-delimited list. For example, the following could be used on the search API:
```
export SPRING_PROFILES_ACTIVE="sitemap,login-gov"
```
### Document Structure
* [Registry Service Feature](#registry-service)
* [Admin Service Feature](#admin-service)
* [Search Service Feature](#search-service)
* [Client features Feature](#client-features)

## Registry Service 
Cas profile ensures that the OneStop publishing service only accept queries sent with credentials.  

| Profile | Feature Description | Default Value |
| --- | --- | --- |
| cas |Enables a security filter to enable metadata publishing.| cas

## Search Service

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| login&#8209;gov |Enables a Spring security filter to enable OpenId authentication via login.gov. This also triggers the uiConfig endpoint to show an auth section which indicates to the client to show a login link. Note: This feature will eventually migrate to a new user service with a PostgreSQL backing DB.<br />Requires a configured ICAM keystore and credentials at runtime! We don’t expect this feature to be enabled in 2.1 production, and the production environment would need to create and configure its own keystore as well as register its public key and other Service Provider (SP) metadata to login.gov. | false |
| sitemap | Enables a the /sitemap.xml and /sitemap/{id}.txt public endpoints. | false |

### Sitemap (Additional Config Required)

The search API ultimately constructs the sitemap content by deriving information from Elasticsearch and constructing links to known routes in our OneStop browser client.

To be enabled correctly, the sitemap feature must be enabled in both `search` and `admin`.

Unfortunately, we don’t currently have a way to dynamically determine where the client is hosted (from the search API’s perspective). So this is left to a manual configuration via two additional environment variables (`SITEMAP_CLIENT_PATH` and `SITEMAP_API_PATH`) for the search API to consume.


| ENV | SITEMAP_CLIENT_PATH | SITEMAP_API_PATH |
| --- | --- | --- |
| DEV | https://cedardevs.org/onestop | https://cedardevs.org/onestop/api/search
| TEST | https://`<testClientHost>`/onestop | https://`<testSearchHost>`/onestop/api/search |
| PROD | https://`<prodClientHost>`/onestop | https://`<prodSearchHost>`/onestop/api/search |

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

## Client features
There are two *new* sections to the search API UI config YML.

1. If the cart feature is desired, simply add the `features` config section as seen below. Without it, the UI will default to not showing a cart feature.

1. Google Analytics can be configured by replacing the following parts with the appropriate IDs for the test and prod environments. When using multiple trackers, you **must** provide a name for additional trackers. There can only be one default (without a name), and the one without a name will only receive updates depending on `reactGaOptions.alwaysSendToDefaultTracker`. See Google Analytics and react-ga4 documentation for more details.   
    - `trackingId: 'XXXXXXXXX'`
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

<hr>
<div align="center"><a href="#">Top of Page</a></div>