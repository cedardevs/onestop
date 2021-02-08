<div align="center"><a href="/onestop/">Documentation Home</a></div>
<hr>

**Estimated Reading Time: 15 minutes**

# Metadata Manager Documentation
Since this role requires interacting with several OneStop components, you are encouraged to start with the brief [architectural overview](architectural-overview) to get your bearings.

## Table of Contents
- [Creating Metadata](#creating-metadata)
- [Loading Metadata into OneStop](#loading-metadata-into-onestop)
    - [Registry API](#registry-api) - CRUD operations (Create, Read, Update, and Delete) on metadata.
    - [Kafka](#kafka) - Underlying way that Registry communicates when operations happen on metadata.
- [Searching OneStop for Metadata](#searching-onestop-for-metadata) - How to find your metadata within OneStop.
- [References](#references)

## Creating Metadata
Incoming metadata can be in either JSON or ISO-19115-2 XML format.

* [XML template](https://data.noaa.gov/waf/templates/iso_u/xml/ncei_template-clean.xml)
* [Metadata guide](https://drive.google.com/file/d/1RqI3pqYr1vLCjj--7mklkNoclOwOrpDD/view)

Questions specific to these documents should be directed to NCEI Metadata WG at ncei.metadata@noaa.gov.

For a better OneStop discovery experience, please be mindful of what fields are in your metadata. Since you probably want to search for you metadata later it would be wise to look in [Searching OneStop for Metadata](#searching-onestop-for-metadata). This can give you insight of what metadata fields you want to include, so you can search and find your metadata later.

## Loading Metadata into OneStop
There are two ways to upload metadata into the OneStop system:

### [Registry API](/onestop/api/registry-api)
Metadata can be published into the OneStop system via the [Registry API](/onestop/api/registry-api). The Registry application has a RESTful interface that allows for CRUD (Create, Read, Update, and Delete) actions on metadata.   
Keep in mind the Registry REST API is secured via CAS authentication, more detail within the [OneStop Registry Security documentation](/onestop/operator/security/registry-security). 

There's a little more information in [Upload Test Metadata](/onestop/developer/additional-developer-info#upload-test-metadata)
 
### Kafka
  - [Kafka Connect](v3/upstream-kafka-connect)

## Searching OneStop for Metadata
Perhaps you've loaded your metadata through an external tool that pushes its output to OneStop. 

On the other hand, you might be directly pushing metadata to OneStop via the Registry API.

No matter how your metadata gets into OneStop, however, you'll probably be curious about what you can do to take full advantage of the OneStop search functionality.

  - [Indexed ISO Metadata Fields](iso-indexing-mapping)
  - [Search Query Syntax](../api/search-query-syntax)
  - [Search Fields Requiring An Exact Match With An Inexact Query](../api/search-query-syntax#search-fields-requiring-an-exact-match-with-an-inexact-query) - This lists an endpoint 

## References
  - [Architectural Overview](architectural-overview) - In-depth overview of the metadata flow and architecture of OneStop.
  - [OneStop API](/onestop/api/) - How to Create, Read, Update, and Delete(CRUD) metadata and other useful APIs.
  - [Public User Documentation](../public-user/)
  - [External Developer Navigation Guide](../external-developer) - Information pertaining to connecting your own software with a OneStop system.
  - [NOAA NCEI Metadata Homepage](https://ncei.noaa.gov/metadata)

<hr>
<div align="center"><a href="#">Top of Page</a></div>