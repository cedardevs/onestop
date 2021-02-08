<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 15 minutes**

# Trending Searches Feature Toggle

## Configuration

This feature is disabled by default. It provides an endpoint in the Search API (`/trending/searches`) to identify the most recent searches being performed.

To turn it on, change the application.yml of the Search API to include:
```
spring.profiles.include:
  - default
  - feature-trending-search
```

Since this toggle uses Spring Profiles, the correct way to turn the feature on and provide additional configuration options is:

```
spring.profiles.include:
  - default
  - feature-trending-search

ui:
  featured:
      - title: GOES Data
        searchTerm: '"Gridded Satellite GOES"'
        imageUrl: ...
      - title: Digital Elevation Models
        searchTerm: '"digital elevation"'
        imageUrl: ...
      - title: NWLON and PORTS
        searchTerm: +nwlon +ports
        imageUrl: ...
      - title: Climate Data Record (CDR)
        searchTerm: '"NOAA Climate Data Record"'
        imageUrl: ...
---
spring:
  profiles: feature-trending-search
trending:
  additionalBlacklistedSearchTerms:
  - +nwlon +ports
  - '"digital elevation"'
  - '"Gridded Satellite GOES"'
  - '"NOAA Climate Data Record"'
```


### Additional Configuration Options

| Variable       | Default           |
| ------------- |:-------------:|
| Number of Results     | 10 |
| Number of Days      | 1      |
| Index Name | logstash- |
| Default Blacklisted Search Terms | weather, climate, satellites, fisheries, coasts, oceans    |
| Additional Blacklisted Search Terms | N/A |
| Blacklisted Collections | N/A |

#### Number of Results

This is the maximum number of top results returned by the trending endpoints.

```
trending:
  numResults: 10
```

#### Number of Days

This is the number of days (assuming default logstash and filebeats configuration) to include when calculating most frequently used search terms. Specifically, it is the number of previous logstash indices to include.

```
trending:
  numDays: 1
```

#### Index Name

The name of the logstash index. Note that assumptions are made about the full format of the index name, using the default logstash index naming conventions.

```
elasticsearch:
  index:
    trending:
      name:
        logstash-
```


#### Default Blacklisted Search Terms

It is recommended not to change this configuration. Instead, modify Additional Blacklisted Search Terms to get additive lists of blacklisted terms.

```
trending:
  defaultBlacklistedSearchTerms:
    - weather
    - climate
    - satellites
    - fisheries
    - coasts
    - oceans
```

#### Additional Blacklisted Search Terms

This is empty by default, to allow an easy way to extend the list of blacklisted terms rather than overwrite it entirely.

This is the recommended way to prevent duplicating Featured Data Set searches in the Trending Search results.

```
trending:
  additionalBlacklistedSearchTerms:
    - term1
    - term2
```

#### Blacklisted Collections

```
trending:
  blacklistedCollections:
    - uuid
```


## Requirements

This feature currently requires Logstash (combined with Filebeats) configured to read the Search API logs, use a custom Grok filter, and create logstash-* indices that the Search API has read access to.

## Additional Notes

This search does not distinguish between terms used in collection and granule searches at this time.

This is expected to have no significant impact for now, since the OneStop UI doesn't depend on searching granules with search terms.

Once the search API is in use for thin portals, or other outside searches supporting other groups, the trending searches could be impacted by how others are using the API, since there's currently no way to determine where the search is coming from.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
