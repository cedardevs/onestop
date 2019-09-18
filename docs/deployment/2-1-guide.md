# OneStop 2.1 Deployment Notes

## Migrating from 2.0 to 2.1

There are some important differences to the OneStop configuration and conventions moving from 2.0 to 2.1. Most significantly, the context path of both APIs have changed.

### API Context Path Changes

The context paths we previously baked into our APIs overlapped the client browser endpoint `/onestop/*`.

While it was possible to configure a web server to distinguish requests to the APIs with an overlapping path, it became increasingly confusing to handle client JavaScript routing once we removed a hash router (e.g. - `/onestop/#/collections`) and replaced it with a browser router (e.g. - `/onestop/collections`). Changing the structure of the URLs was required to support search engine optimization (SEO), however it has some implications for the configuration of the web server.


| API | 2.0 (old) | 2.1 (new) |
| --- | --------- | --------- |
| Search Service (api-search) | `/onestop/api` | `/onestop-search` |
| Admin Service (api-admin) | `/onestop/admin` | `/onestop-admin` |


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

See [Feature Toggles](/docs/deployment/feature-toggles.md) for more information about enabling sitemaps correctly.

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
