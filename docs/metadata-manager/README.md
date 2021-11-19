<div align="center"><a href="/onestop/">Documentation Home</a></div>
<hr>

**Estimated Reading Time: 25 minutes**

# Metadata Manager Documentation
Since this role requires interacting with several OneStop components, you are encouraged to start with the brief [architectural overview](architectural-overview) to get your bearings.

## Table of Contents
- [Creating Metadata](#creating-metadata)
- [Loading Metadata into OneStop](#loading-metadata-into-onestop)
    - [OneStop Python Clients](#onestop-python-client) - OneStop clients for interfacing with OneStop.
    - [Registry API](#registry-api) - CRUD operations (Create, Read, Update, and Delete) on metadata.
    - [Kafka](#kafka) - Underlying way that Registry communicates when operations happen on metadata.
- [Searching OneStop for Metadata](#searching-onestop-for-metadata) - How to find your metadata within OneStop.
- [References](#references)

## Creating Metadata

If you need to create your metadata manually here are some useful, external, guides and templates for metadata. Questions specific to these documents should be directed to the NCEI Metadata WG at ncei.metadata@noaa.gov.

* [Collection XML template](https://data.noaa.gov/waf/templates/iso/xml/ncei_template-clean.xml)
* [Collection Metadata Guide](https://drive.google.com/file/d/1RqI3pqYr1vLCjj--7mklkNoclOwOrpDD/view)

Keep in mind that however your data is getting into OneStop it supports the metadata formats of either JSON or ISO-19115-2 XML.

For a better OneStop discovery experience please be mindful of what fields are in your metadata. 
* [Searching OneStop for Metadata](#searching-onestop-for-metadata)

## Loading Metadata into OneStop
If your metadata isn't automatically being loading into OneStop (some applications already automatically send metadata to OneStop), there are a few ways to do that:

### OneStop Python Client
The [OneStop Clients](https://cedardevs.github.io/onestop-clients/) repo has a python client that is useful for searching OneStop for metadata.

### [Registry API](/onestop/api/registry-api)
Metadata can be published into OneStop via the [Registry API](/onestop/api/registry-api). The Registry application has a RESTful interface that allows for CRUD (Create, Read, Update, and Delete) actions on metadata.   
Keep in mind the Registry REST API is secured via CAS authentication, more detail within the [OneStop Registry Security documentation](/onestop/operator/security/registry-security). 

There's a little more information in [Upload Test Metadata](/onestop/developer/additional-developer-info#upload-test-metadata)
 
### Kafka
  - [Kafka Connect](v3/upstream-kafka-connect)

## Searching OneStop for Metadata
Perhaps you've loaded your metadata through an external tool that pushes its output to OneStop. On the other hand, you might be directly pushing metadata to OneStop via the Registry API.

No matter how your metadata gets into OneStop, however, you'll probably be curious about what you can do to take full advantage of the OneStop search functionality.

  - [Indexed ISO Metadata Fields](iso-indexing-mapping)
  - [Search Query Syntax](../api/search-query-syntax)
  - [Search Fields Requiring An Exact Match With An Inexact Query](../api/search-query-syntax#search-fields-requiring-an-exact-match-with-an-inexact-query) - This lists an endpoint 

For the purpose of search engines be able to find the data in OneStop we iterate over every collection within OneStop and pull out important fields that end up in a sitemap file that search engines such as Google can use as an index for OneStop's content.

## References
  - [Architectural Overview](architectural-overview) - In-depth overview of the metadata flow and architecture of OneStop.
  - [OneStop API](/onestop/api/) - All the OneStop available APIs(Application Program Interfaces). For example: Create, Read, Update, and Delete(CRUD) metadata.
  - [Public User Documentation](../public-user/)
  - [External Developer Navigation Guide](../external-developer) - Information pertaining to programmatically sending data from your software to OneStop.
  - [NOAA NCEI Metadata Homepage](https://ncei.noaa.gov/metadata)

<hr>
<div align="center"><a href="#">Top of Page</a></div>