# The Search API
## Table of Contents
- [Search Requests](search-requests) - What requests can be done on the Search endpoints.
- [Search Query Syntax](search-query-syntax) - In-depth on the ElasticSearch query syntax used by our Search API.
- [Search Fields](search-fields) - Search fields you can reference in a search query.
- [Search Responses](search-responses) - Format of the search response.

The Search API provides three endpoints for OneStop data discovery. Either a POST (with a correctly formatted **JSON request body**) or GET request  will work. All three endpoints expect the same request schema format to be used but return data of the type in the URL.

Examples:

`/onestop-search/search/collection`

`/onestop-search/search/granule`

`/onestop-search/search/flattened-granule` (version 2.1+) resource endpoints.


For more detailed information see the full [OpenAPI specification](/static/openapi.yaml).


