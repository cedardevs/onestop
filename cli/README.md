# OneStop CLI tools

The `onestop-cli` tool provides a convenient command line interface for the OneStop web API. This tool was partly generated from an OpenAPI spec with custom middleware for convenient syntax for frequently used filters and queries.

One of the subcommands, `scdr-files`, was added to demonstrate how the OneStop API can be used to replace existing CLI tools while maintaining their programatic interface.

Find the OneStop OpenAPI spec here- https://app.swaggerhub.com/apis/cedardevs/one-stop_search_api/2.0.0
The `onstop-cli/openapi.go` file was generated using this project- https://github.com/danielgtaylor/openapi-cli-generator

## Install and run using a docker container (go not required locally)

`./gradlew cli:build`

And then run commands after like so -

`docker run cedardevs/onestop-cli:2.4.0 <CMD>`

For example-

`docker run cedardevs/onestop-cli searchcollection --q="satellite"`

For more commands and flags -

`docker run cedardevs/onestop-cli <CMD> --help`

## Download and use as Go package
(requires go and the following environemt)

```
export PATH=$PATH:/usr/local/go/bin \ export GOPATH=$HOME/go
export GOBIN=$GOPATH/bin
export PATH=$PATH:$GOBIN
export GO111MODULE=on
```

`go get  github.com/cedardevs/onestop/cli`

`cli --help`

to get from branch

`go get  github.com/cedardevs/onestop/cli@1020-OneStopCLI`

or use `go run` -

`cd cli`

`go get -u https://github.com/danielgtaylor/openapi-cli-generator`

`go run . --help`

or `go install` it -

`cd cli`

`go install`

`cli --help`


## onestop-cli usage
Get collection by id -

`cli getcollectionbyid ecb087a6-25cf-4bfa-8165-2d374c701646`

Get collection by file identifier -

`cli searchcollection --q="fileIdentifier:/.*NDBC-COOPS/"`

or the longhand version -

`cli searchcollection queries[]{type:queryText, value:fileIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

Search granule with query text / parent identifier -

`cli searchgranule --q="parentIdentifier:\\"\"gov.noaa.nodc:NDBC-COOPS\\"\""`

`cli searchgranule queries[]{type:queryText, value:parentIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

Search granule with regex -  

`cli searchgranule --q="parentIdentifier:/.*NDBC-COOPS/"`

`cli searchgranule --verbose queries[]{type:queryText, value:parentIdentifier:/.*NDBC-COOPS/}`

Search collections by date -  

`cli searchcollection  --date=2017-01-01`

`cli searchcollection filters[]{ type:datetime, after:2017-01-01T00:00:00Z}`

`cli searchcollection filters[]{ type:datetime, after:2017-01-01T00:00:00Z, before:2017-02-01T00:00:00Z}`

Search collections with a geometry filter -

`cli searchcollection --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"`

`cli searchcollection filters[]{ type : geometry }, filters[0].geometry{type : Polygon}, .geometry.coordinates[][]: 22.686768, 34.051522, []: 30.606537, 34.051522, []: 30.606537, 41.280903, []: 22.686768, 41.280903, []: 22.686768, 34.051522`

Complex collections search with a query text, spatial, and temporal filter -

`cli searchcollection --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))" --q="satellite"`

`cli searchcollection filters[]{ type : geometry }, filters[0].geometry{type : Polygon}, .geometry.coordinates[][]: 22.686768, 34.051522, []: 30.606537, 34.051522, []: 30.606537, 41.280903, []: 22.686768, 41.280903, []: 22.686768, 34.051522,  queries[]{type:queryText, value:satellite}  --verbose`

For complex query and filter structure, refer to these docs for the short hand documentation - https://github.com/danielgtaylor/openapi-cli-generator/tree/master/shorthand

## scdr-files usage

Type -

`cli scdr-files --type="gov.noaa.nodc:NDBC-COOPS" --verbose`

Date -

`cli scdr-files --date=2010-10-01 --verbose`

and without year (defaults to current year)-

`cli  scdr-files --date=10-01 --verbose`

Area-

`cli  scdr-files --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"`

Text Query -

`cli  scdr-files --verbose --q="parentIdentifier:/.*NDBC-COOPS/"`

## Developer notes

The openapi.go file was generated using the `openapi-cli-generator` tool linked above. If you have `go` set up as mentioned above, you can get it with `go get github.com/danielgtaylor/openapi-cli-generator`. This will make `openapi-cli-generator` available. Refer to that repos documentation for more information.

That library generated a command line client that allows us to inject our own middleware to marshal requests and responses. You can find these custom flags applied in scdr-flags.go and the parsing functions in parsing-util.go.
