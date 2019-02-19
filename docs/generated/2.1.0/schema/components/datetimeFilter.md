# Datetime Filter Schema

Filter results by before, after, or a date range.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| type **(required)**| enum| `datetime`|  |
| before| string| |  |
| after| string| |  |
| relation| enum| `contains`, `disjoint`, `intersects`, `within`| intersects |


## type **(required)**

Filter type.

### Values

#### Type:
enum


#### Accepted Values:
* datetime





## before

End of the date range, e.g., look for all results *before* this date.

### Values

#### Type:
string

#### Format:
date-time

#### Accepted Values:





## after

Beginning of the date range, e.g., look for all results *after* this date.

### Values

#### Type:
string

#### Format:
date-time

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






