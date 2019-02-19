# Facet Filter Schema

Filters results based on given GCMD keywords where returned results MUST have the specified keyword. Multiple facetFilter objects will be combined with logical AND; multiple values in a single facetFilter object will be combined with the logical OR.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| type **(required)**| enum| `facet`|  |
| name **(required)**| enum| `science`, `services`, `locations`, `instruments`, `platforms`, `projects`, `dataCenters`, `horizontalResolution`, `verticalResolution`, `temporalResolution`|  |
| values **(required)**| array(string)| |  |


## type **(required)**

Filter type.

### Values

#### Type:
enum


#### Accepted Values:
* facet





## name **(required)**

One of the GCMD keyword categories: science, services, locations, instruments, platforms, projects, dataCenters, horizontalResolution, verticalResolution, temporalResolution.

### Values

#### Type:
enum


#### Accepted Values:
* science
* services
* locations
* instruments
* platforms
* projects
* dataCenters
* horizontalResolution
* verticalResolution
* temporalResolution





## values **(required)**

List of GCMD keywords.

### Values

#### Type:
array(string)


#### Accepted Values:

* Exact match GCMD keyword(s), e.g., &quot;Atmosphere &gt; Atmospheric Temperature &gt; Surface Temperature&quot;





