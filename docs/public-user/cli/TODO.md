

## Download and use as Go package
(requires go and the following environment)

```
export PATH=$PATH:/usr/local/go/bin \ export GOPATH=$HOME/go
export GOBIN=$GOPATH/bin
export PATH=$PATH:$GOBIN
export GO111MODULE=on
```

`go get github.com/cedardevs/onestop/cli`

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



## Developer notes

The openapi.go file was generated using the `openapi-cli-generator` tool linked above. If you have `go` set up as mentioned above, you can get it with `go get github.com/danielgtaylor/openapi-cli-generator`. This will make `openapi-cli-generator` available. Refer to that repos documentation for more information.

That library generated a command line client that allows us to inject our own middleware to marshal requests and responses. You can find these custom flags applied in scdr-flags.go and the parsing functions in parsing-util.go.
