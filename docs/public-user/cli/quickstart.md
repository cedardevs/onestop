# OneStop CLI tool

The `onestop-cli` tool provides a convenient command line interface for the OneStop search API.

Read the [OneStop OpenAPI spec 2.0.0](https://app.swaggerhub.com/apis/cedardevs/one-stop_search_api/2.0.0).
Or check the [OneStop OpenAPI spec 2.4.0](https://app.swaggerhub.com/apis/cedarbot/OneStop/2.4.0).

## Installation

### Requirements -

Either golang, or docker. Direct downloads of binaries will be available in the future.

### Install and run using a docker container (golang not required)

Pull the image from Docker Hub (version 2.4.2 and up available).

`docker run cedardevs/onestop-cli:2.4.2 <CMD>`

For example-

`docker run cedardevs/onestop-cli:2.4.2 searchcollection --query="satellite"`

For more commands and flags -

`docker run cedardevs/onestop-cli:2.4.2 <CMD> --help`


### Download and use as Go package

Download the cli:

`go get github.com/cedardevs/onestop/cli/cmd/onestop`

or to get it from a specific branch:

`GO111MODULE=on go get github.com/cedardevs/onestop/cli/cmd/onestop@[branchname]`

Run the cli:

`~/go/bin/onestop --help`

or

`PATH=$PATH:~/go/bin onestop`

or

`export PATH=$PATH:~/go/bin` after which you can just call `onestop`

## Configuration

To use a semantic types (i.e.`--type`), the user can use a configuration to map type to UUID. For example-

```
scdr-types:
  ABI-L1b-Rad : 5b58de08-afef-49fb-99a1-9c5d5c003bde
  ABI-L2-CMIP : 22222222-2222-2222-2222-222222222222
  GLM-L2-LCFA : 33333333-3333-3333-3333-333333333333
  ABI-L2-FDC : 44444444-4444-4444-4444-444444444444
```

The CLI binary will look for a configuration named scdr-files-config.yml in  `/etc/scdr-files`, `$HOME/.scdr-files`, and in the current working directory.

The docker image has a built in config containing the example config above.

## Usage

**Note: the following example commands assume you installed the cli using `go get` mentioned above.**

### Verbose

Add `--verbose` to get more complete output. For example, to see what server is being used by default.

### Run against a test or local API

Add the `--server` flag:
- `--server=data.noaa.gov/onestop-search/`
- `--server=https://sciapps.colorado.edu/onestop-search/`
- `--server=localhost:8080/onestop-search/`

### Get

The `getcollectionbyid`, `getgranulebyid` and `getflattenedgranulebyid` work the same way.

Get collection by id:

`onestop getcollectionbyid ecb087a6-25cf-4bfa-8165-2d374c701646`

### Search

The `searchcollection`, `searchgranule` and `searchflattenedgranule` work the same way.

There are two ways to specify queries and filters. A shorthand version and a longhand version. The shorthand uses flags, and the longhand is based on the JSON that would be required to curl the API directly.

#### by identifier

Find collection by file identifier:

`onestop searchcollection --query="fileIdentifier:/gov.noaa.nodc:NDBC-COOPS/"`

which is equivalent to the longhand version:

`onestop searchcollection queries[]{type:queryText, value:fileIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

#### by parent identifier

Search granule with parent identifier:

`onestop searchgranule --query="parentIdentifier:\\"\"gov.noaa.nodc:NDBC-COOPS\\"\""`

which is equivalent to:

`onestop searchgranule queries[]{type:queryText, value:parentIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

#### by date

Search collections start date:

`onestop searchcollection  --start-time=2016/03/02`

which is equivalent to:

`onestop searchcollection filters[]{ type:datetime, after:2016-03-02T00:00:00Z}`

Or search by both start and end date:

`onestop searchcollection  --start-time=2016/03/02 --end-time=2017/02/01`

which is equivalent to:

`onestop searchcollection filters[]{ type:datetime, after:2017-01-01T00:00:00Z, before:2017-02-01T00:00:00Z}`

#### by geometry

Shorthand geometries use WKT, as opposed to the longhand which is a polygon (such as would be represented in geoJSON).

Search collections with a geometry filter:

`onestop searchcollection --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"`

which is equivalent to:

`onestop searchcollection filters[]{ type : geometry }, filters[0].geometry{type : Polygon}, .geometry.coordinates[][]: 22.686768, 34.051522, []: 30.606537, 34.051522, []: 30.606537, 41.280903, []: 22.686768, 41.280903, []: 22.686768, 34.051522`

#### combinations


Collections search with a query and filters:

`onestop searchcollection --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))" --query="satellite"`

Longhand query, including the `--verbose` flag to provide more logging:

`onestop searchcollection filters[]{ type : geometry }, filters[0].geometry{type : Polygon}, .geometry.coordinates[][]: 22.686768, 34.051522, []: 30.606537, 34.051522, []: 30.606537, 41.280903, []: 22.686768, 41.280903, []: 22.686768, 34.051522,  queries[]{type:queryText, value:satellite}  --verbose`

For complex query and filter structure, refer to the [short hand documentation](https://github.com/danielgtaylor/openapi-cli-generator/tree/master/shorthand).

Note: As it is now, you cannot combine the flags with json shorthand. e.g. This will not work - `onestop searchcollection --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))" --query="satellite" filters[]{ type:datetime, after:2017-01-01T00:00:00Z, before:2017-02-01T00:00:00Z} `

<hr>
<div align="center"><a href="/onestop/public-user">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/public-user/cli/scdr-files">Next</a></div>
