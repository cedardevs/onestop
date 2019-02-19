# Year Filter Schema

Filter results by before, after, or a date range. Differs from the Datetime Filter in that it handles year values only.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| type **(required)**| enum| `year`|  |
| before| number| |  |
| after| number| |  |
| relation| enum| `contains`, `disjoint`, `intersects`, `within`| intersects |


## type **(required)**

Filter type.

### Values

#### Type:
enum


#### Accepted Values:
* year





## before

End of the date range, e.g., look for all results *before* this year.

### Values

#### Type:
number


#### Accepted Values:





## after

Beginning of the date range, e.g., look for all results *after* this year.

### Values

#### Type:
number


#### Accepted Values:





## relation

How the result time range relates to the query time range, e.g., return all datasets whose time range is within the given time range.

### Values

#### Type:
enum
#### Default:
intersects


#### Accepted Values:
* contains
* disjoint
* intersects
* within






