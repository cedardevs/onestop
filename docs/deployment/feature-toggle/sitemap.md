# Sitemap Feature Toggle

## Configuration

This feature is disabled by default. It creates a sitemap index, and keeps a full sitemap of all collection details pages. This allows the UI to correctly provide a sitemap, which is critical for SEO. Although this adds endpoints to the Search API, it is intended entirely for use by automated tools.

To turn it on, change the application.yml of *both* the Metadata API and Search API to include:
`features.sitemap: true`

The sitemap is accessible via the following API request:
* onestop/api/sitemap.xml

### Additional Configuration

The size of the documents in the index should be optimized to have a large number of collections in each submap, without exceeding either Elasticsearch or SEO size limitations.

Metadata API can change the timing for how frequently the sitemap is regenerated, as well as how large the submaps are.

```etl:
  sitemap:
    delay:
      initial: 600000
      fixed: 604800000
    scroll-size: 10000
    collections-per-submap: 40000
```

Sitemap delay times are in ms. The entire sitemap index is regenerated when the ETL runs.

The `collections-per-submap` value can be changed to increase or decrease to optimize the generated sitemaps and indices.

## Requirements

The only additional requirement for this feature is the addition of another Elasticsearch index, which is managed by the Metadata API.

## Status of this feature

* This feature has not been load tested.
* This feature has not been analyzed with an SEO tool, or sitemap-checking tool.
