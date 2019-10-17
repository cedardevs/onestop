FROM golang:1.12.0-alpine3.9 AS builder

COPY . "/go/src/github.com/cedardevs/onestop/cli"
WORKDIR "/go/src/github.com/cedardevs/onestop/cli"
RUN apk update && apk upgrade && \
    apk add --no-cache bash git
RUN go get -u github.com/danielgtaylor/openapi-cli-generator
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build  -o /onestop-cli

CMD ["/onestop-cli"]

#########
# second stage to obtain a very small image
FROM scratch

COPY --from=builder /onestop-cli .

CMD ["/onestop-cli"]
