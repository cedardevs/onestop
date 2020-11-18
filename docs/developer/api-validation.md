# JSON schema validation

Incoming requests to the Search API get validated against a [JSON Schema](http://json-schema.org/) file.  This file gets generated from `build/resources/open.yaml` by using the file at `search/src/main/groovy/org/cedar/onestop/api/search/controller/JsonValidator.groovy`.
 
To create the yaml file, and skip running tests (sometimes they are problematic), you can do:
 
 `./gradlew search:clean search:build -x test -x integrationTest`
 
## References

- [JSON Schema](http://json-schema.org/)
- [Sample Geometry Schema](https://github.com/fge/sample-json-schemas/blob/master/geojson/geometry.json)
- [JSON Schema Store](http://json.schemastore.org)

<hr>
<div align="center"><a href="/onestop/developer/client">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/developer/contribution-guidelines">Next</a></div>
