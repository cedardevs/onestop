<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

# CSW & OpenSearch

## Overview

[Catalog Services for the Web](https://en.wikipedia.org/wiki/Catalog_Service_for_the_Web) and [OpenSearch](https://en.wikipedia.org/wiki/OpenSearch) APIs for the records in OneStop are provided via the `geoportal-search` module of the [Geoportal project](https://github.com/Esri/geoportal-server-catalog).

## Installation

The `geoportal-search` application is a war file which can be run in any java servlet container. There are two files inside the war that need to be modified in order to a) point it at your custom elasticsearch and b) customize the searchable fields based on your index.

### Docker

Our [Dockerfile](https://github.com/cedardevs/onestop/blob/master/geoportal-search/Dockerfile) creates an image that connects to the OneStop elasticsearch, which it assumes to be running at `elasticsearch:9200`. The image is published to the docker hub with the tag [cedardevs/onestop-geoportal-search](https://hub.docker.com/r/cedardevs/onestop-geoportal-search/).

To simply run the image, use:

```sh
docker run cedardevs/onestop-geoportal-search
```

For an example of running the whole system using Docker Compose, see our [docker-compose.yml](https://github.com/cedardevs/onestop/blob/master/docker-compose.yml) file.


### Manual

The following is a list of steps to set up and customize `geoportal-search` to point to the OneStop index. For more details, see Geoportal's [wiki on the subject](https://github.com/Esri/geoportal-server-catalog/wiki/Geoportal-Search-Component).

1. Download **just the geoportal-search zip** from the [latest release](https://github.com/Esri/geoportal-server-catalog/releases/latest)
1. Unzip it, and then unzip the war inside it so that you can edit files inside the war
1. Inside the war, modify `WEB-INF/classes/gs/config/Config.js` to:
    1. Add an additional "target", like:
    ```js
    var targets = {
      ...
      "onestop": gs.Object.create(gs.target.elastic.CustomElasticTarget).mixin({
        "searchUrl": "http://<onestop elasticsearch host:port>/search/collection/_search"
      })
    };
    ```
    2. Set the "defaultTarget" to point to it:
    ```js
    defaultTarget: {writable: true, value: "onestop"}
    ```
    3. There is an example [Config.js](https://github.com/cedardevs/onestop/blob/master/geoportal-search/conf/WEB-INF/classes/gs/config/Config.js) file in the repository.
1. Inside the war, modify `WEB-INF/classes/gs/target/elastic/CustomElasticSchema.js` to map searches onto the OneStop index.
    - This is a non-trivial exercise which requires support from ESRI to do correctly. This is we've come up with so far: [CustomElasticSchema.js](https://github.com/cedardevs/onestop/blob/master/geoportal-search/conf/WEB-INF/classes/gs/target/elastic/CustomElasticSchema.js)
1. Now deploy the exploded war (or re-zip it if you prefer) into your favorite servlet container, e.g. into `$CATALINA_BASE/webapps/geoportal-search` for a Tomcat installation.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
