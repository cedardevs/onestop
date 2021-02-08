<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

# Upgrade the System

## Elasticsearch mappings

As noted by [mappings](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html) and [settings](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html), some index changes cannot be done without a [reindex](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html).

The Indexer application is tied to the expected mapping. If an incompatible mapping change is requested, the Indexer will not be able to start up, and will exit with an error log message, indicating a reindex is required to make that change.

The expected procedure is to follow the reindexing process for Elasticsearch, then to restart Indexer.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
