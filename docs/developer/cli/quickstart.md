# OneStop CLI tool for developers

The `onestop-cli` tool provides a convenient command line interface for the OneStop search API. This tool is partly generated from the OpenAPI spec in the search module. We have added custom middleware for convenient syntax for frequently used filters and queries.

The `onstop-cli/openapi.go` file was generated using [open-cli-generator](https://github.com/danielgtaylor/openapi-cli-generator).

The  project structure following the conventions described here-
https://github.com/golang-standards/project-layout

## Build and run using docker

`./gradlew cli:jibDockerBuild`

And then run commands after like so -

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.2 <CMD>`

For example-

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.2 searchcollection --query="satellite"`

For more commands and flags -

`docker run registry.hub.docker.com/cedardevs/onestop-cli:2.4.2 <CMD> --help`

## Build and run using go

`./gradlew cli:gobuild` (or `./gradlew cli:build`)
`cli/.gogradle/cli-darwin-amd64` on OSX (or `cli/.gogradle/cli-linux-amd64` on linux).

Alternatively,
```
export PATH=$PATH:/usr/local/go/bin
export GOPATH=$HOME/go
export GOBIN=$GOPATH/bin
export PATH=$PATH:$GOBIN
export GO111MODULE=off
cd cli
go get ./...
```
`go run .`
(This will put dependencies in ~/go)

or install from source:
```
cd cli
go get ./...
go install
```
and run with `onestop`
(This will put dependencies in ~/go)

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

The docker image has a built in config containing the example config above. To update the config that is in the docker image, update scdr-files-config.yml found in the root of the CLI project.

## Changing openapi.yml

First: `go get -u github.com/danielgtaylor/openapi-cli-generator`

Then:
```
cd cli
~/go/bin/openapi-cli-generator generate ../search/src/main/resources/openapi.yaml
mv openapi.go ./internal/app/generated/
```

## Developer notes

The openapi.go file was generated using the `openapi-cli-generator` tool linked above. If you have `go` set up as mentioned above, you can get it with `go get github.com/danielgtaylor/openapi-cli-generator`. This will make `openapi-cli-generator` available. Refer to that repos documentation for more information.

That library generated a command line client that allows us to inject our own middleware to marshal requests and responses. You can find these custom flags applied in scdr-flags.go and the parsing functions in parsing-util.go.

The names of commands in openapi.go are determined based on the `operationId` in openapi.yaml.
