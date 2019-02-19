# Collection Filter Schema

Filter granules by specific collection(s). If multiple collection filters are used, or multiple values in a single collection filter, then the search is performed with a logical OR.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| type **(required)**| enum| `collection`|  |
| values **(required)**| array(string)| |  |


## type **(required)**

Filter type.

### Values

#### Type:
enum


#### Accepted Values:
* collection





## values **(required)**

List of collection UUIDs.

### Values

#### Type:
array(string)


#### Accepted Values:

* Collection UUID





