# OneStop CLI 

This CLI tool was generated from an OpenAPI spec, openapi.yaml, which is based on the OneStop OpenAPI spec with a few extensions. Find the OneStop spec here- https://app.swaggerhub.com/apis/cedardevs/one-stop_search_api/2.0.0 
The go code was generated using this project- https://github.com/danielgtaylor/openapi-cli-generator

## Requirements
Install go - https://golang.org/doc/install

## Install
`cd cli`

`go run . --help`

or 

`go install`

`onestop-cli --help` 

## Usage
Get collection by id - 

`onestop-cli getcollectionbyid ecb087a6-25cf-4bfa-8165-2d374c701646`

Get collection by file identifier - 

`onestop-cli searchcollection --verbose queries[]{type:queryText, value:fileIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

Search granule with query text / parent identifier -

`onestop-cli searchgranule --verbose queries[]{type:queryText, value:parentIdentifier:\"gov.noaa.nodc:NDBC-COOPS\"}`

Search granule with regex -  

`onestop-cli searchgranule --verbose queries[]{type:queryText, value:parentIdentifier:/.*NDBC-COOPS/}`

Search collections by date -  

`onestop-cli searchcollection filters[]{ type:datetime, after:2017-01-01T00:00:00Z}`
`onestop-cli searchcollection filters[]{ type:datetime, after:2017-01-01T00:00:00Z, before:2017-02-01T00:00:00Z} --verbose`

## Troubleshoot 
from onestop/cli/ - 

`go get -u https://github.com/danielgtaylor/openapi-cli-generator`