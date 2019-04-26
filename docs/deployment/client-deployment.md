# Client Deployment

## Installation
The client is a a pure HTML/JavaScript/CSS application packaged as a tarball. To host it simply expand the tar into the root of your web server.

Note that the app requires NodeJS to be built from source (as it is concatenated, minified, compressed, etc.)
but it has no dependency on node at runtime.

## Dependencies
In order to execute searches and display correct information, the client assumes that it can make requests to:
- `/onestop/api/actuator/info`
- `/onestop/api/uiConfig`
- `/onestop/api/collection`
- `/onestop/api/granule`
- `/onestop/api/search`

Hence, a reverse proxy must be put into place from that path to the Search API application. For example, if you are hosting the client using `httpd` and the Search API application is running on the same host, you could add the following to your `httpd.conf`:

```
ProxyPass        /onestop/api/search http://localhost:8097/onestop/api/search
ProxyPassReverse /onestop/api/search http://localhost:8097/onestop/api/search
```

Note that the Metadata API application has other endpoints, e.g. for uploading metadata and triggering indexing,
but that these endpoints are potentially destructive and should likely not be exposed to the public.

## Configuration
For available client configuration and instructions, see [here](/docs/deployment/application-configuration.md#ui-values).
