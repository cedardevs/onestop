In addition to the OneStop API (based on the [JSON API](http://jsonapi.org/) Standard), OneStop supports search using the Open Geospatial Consortium's (OGC) [Catalog Services for the Web](http://www.opengeospatial.org/standards/cat) (CSW) API, and the [OpenSearch](http://www.opensearch.org) specification.

CSW Examples:

* [GetCapabilities](https://sciapps.colorado.edu/onestop/api/csw?service=CSW&version=3.0.0&request=GetCapabilities)
* [GetRecords](https://sciapps.colorado.edu/onestop/api/csw?service=CSW&version=3.0.0&request=GetRecords&typeName=csw:Record&constraintlanguage=CQLTEXT&constraint=%E2%80%9Ccsw:AnyText%20Like%20%E2%80%98%ocean%)


OpenSearch Examples:
* [Ocean search](https://sciapps.colorado.edu/onestop/api/opensearch?q=ocean)

Earth Science communities are increasingly enabling better data discovery support in commercial search engines. To this end, OneStop will support JSON-LD in it's collection detail pages and provide a sitemap that search engines can use to discover the data.

The sitemap is accessible via the following API request:
* onestop/api/sitemap.xml