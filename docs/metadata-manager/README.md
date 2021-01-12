<div align="center"><a href="/onestop/">Documentation Home</a></div>
<hr>
**Estimated Reading Time: 15 minutes**

# Metadata Manager Documentation
Since this role requires interacting with several OneStop components, you are encouraged to start with the brief [architectural overview](architectural-overview) to get your bearings.

## Table of Contents
- [Creating Metadata](#creating-metadata)
- [Loading Metadata into OneStop](#loading-metadata-into-onestop)
    - [Registry API](#registry-api)
    - [Kafka](#kafka)
- [Searching OneStop for Metadata](#searching-onestop-for-metadata)
- [References](#references)

## Creating Metadata
   - [Supported Metadata Formats](metadata-formats)
   
   For a better OneStop discovery experience, please be mindful of what fields are in your metadata compared to the [OneStop indexed fields](iso-indexing-mapping). These indexed fields represent what you can search for via OneStop. While these are not 100% of the available search fields they are the majority.

   - [Search Fields Requiring An Exact Match With An Inexact Query](../api/search-query-syntax#search-fields-requiring-an-exact-match-with-an-inexact-query) - Has information on how to query OneStop for a very useful list of the available search fields.
   
## Loading Metadata into OneStop
There are two ways to upload metadata into the OneStop system:

### Registry API
  - [Upload Test Metadata](../developer/additional-developer-info#upload-test-metadata)
  - [The Registry API Guide](v3/onestop-metadata-loading)
  - [Registry API](../api/registry-api) - More detail about the Registry API.
  
### Kafka
  - [Kafka Connect](v3/upstream-kafka-connect)

## Searching OneStop for Metadata
Perhaps you've loaded your metadata through an external tool that pushes its output to OneStop. 

On the other hand, you might be directly pushing metadata to OneStop via the Registry API.

No matter how your metadata gets into OneStop, however, you'll probably be curious about what you can do to take full advantage of the OneStop search functionality.

  - [Indexed ISO Metadata Fields](iso-indexing-mapping)
  - [Search Query Syntax](../api/search-query-syntax)
  - [Search Fields Requiring An Exact Match With An Inexact Query](../api/search-query-syntax#search-fields-requiring-an-exact-match-with-an-inexact-query)

## References
  - [Architectural Overview](architectural-overview)
  - [Public User Documentation](../public-user/)
  - [External Developer Navigation Guide](../external-developer) - Information pertaining to connecting your own software with a OneStop system.
  - [NOAA NCEI Metadata Homepage](https://ncei.noaa.gov/metadata)
