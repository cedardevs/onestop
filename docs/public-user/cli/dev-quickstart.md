# OneStop CLI tool for developers

The `onestop-cli` tool provides a convenient command line interface for the OneStop search API. This tool is partly generated from the OpenAPI spec in the search module. We have added custom middleware for convenient syntax for frequently used filters and queries.

The `onstop-cli/openapi.go` file was generated using [open-cli-generator](https://github.com/danielgtaylor/openapi-cli-generator).

## Requirements -
Either golang, docker, or java.  

## Install and run using a docker container (golang not required)

`./gradlew cli:jibDockerBuild`

And then run commands after like so -

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.0 <CMD>`

For example-

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.0 searchcollection --query="satellite"`

For more commands and flags -

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.0 <CMD> --help`
