# Text Query Schema

When providing multiple queryText objects in your search request, keep in mind that they will be combined with a logical AND. Terms use Lucene Query Syntax.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| type **(required)**| enum| `queryText`|  |
| value **(required)**| string| |  |


## type **(required)**

Filter type.

### Values

#### Type:
enum


#### Accepted Values:
* queryText





## value **(required)**

Text query to search with that is valid so long as it does not have a leading wildcard (&#x27;*&#x27; or &#x27;?&#x27;)

### Values

#### Type:
string


#### Accepted Values:






