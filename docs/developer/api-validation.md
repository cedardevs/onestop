# JSON schema validation

The requests to api are validated against a JSON schema.

## Schemas

The larger schema is broken down into [components](/search/schema). The build step merges these into a single schema for the code to reference, however some tests validate against specific components directly in order to keep the tests clear. To see the complete schema, check `search/build/resources/main/onestop-request-schema.json` after running `./gradlew search:build`.

## References

- [JSON Schema](http://json-schema.org/)
- [Sample Geometry Schema](https://github.com/fge/sample-json-schemas/blob/master/geojson/geometry.json)
- [JSON Schema Store](http://json.schemastore.org)

<hr>
<div align="center"><a href="/onestop/developer/client">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/developer/contribution-guidelines">Next</a></div>
