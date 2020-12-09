# Community Standards

In addition to the OneStop API, which is based on the [JSON API](http://jsonapi.org/) Standard, OneStop also supports the [Open Geospatial Consortium's](https://www.ogc.org/) (OGC) service standard, [Catalog Services for the Web](http://www.opengeospatial.org/standards/cat) (CSW) API, and the [OpenSearch](http://www.opensearch.org) specification.

CSW Examples:

* [GetCapabilities](https://sciapps.colorado.edu/onestop/api/csw?service=CSW&version=3.0.0&request=GetCapabilities)
* [GetRecords](https://sciapps.colorado.edu/onestop/api/csw?service=CSW&version=3.0.0&request=GetRecords&typeName=csw:Record&constraintlanguage=CQLTEXT&constraint=%E2%80%9Ccsw:AnyText%20Like%20%E2%80%98%ocean%)


OpenSearch Examples:
* [Ocean search](https://sciapps.colorado.edu/onestop/api/opensearch?q=ocean)

Earth Science communities are increasingly enabling better data discovery support in commercial search engines. To this end, OneStop will support JSON-LD in its collection detail pages and provide a sitemap that search engines can use to discover the data.

The sitemap is accessible via the following API request:
* onestop/sitemap.xml
