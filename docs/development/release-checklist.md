## Release Checklist

### Code verification

1. Update libraries to the latest, if possible
1. Run the full build (including tests): `./gradlew build`
1. Check for OWASP vulnerabilities: `./gradlew dependencyCheckAggregate --info`

### Test deployment and behavior

1. Deploy the latest candidate to the sciapps demo site
1. Upload the test collection with a test id
    - From project root:
    ```bash
    curl -X PUT \
         -H "Content-Type: application/xml" \
         -u "credentials:redacted" \
         https://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000 \
         --data-binary @registry/src/test/resources/dscovr_fc1.xml
    ```
1. Retrieve the raw input
    - `curl http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000`
    - ensure it returns a 200, and the `content` attribute contains the xml file you just uploaded
1. Retrieve the parsed metadata
    - `curl http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000/parsed`
    - ensure it returns a 200, and that the `discovery` and `analysis` attributes contain values
1. Delete the record
    - `curl -X DELETE http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000`
1. Retrieve the raw input
    - `curl http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000`
    - ensure it returns a 404, with an error object explaining that it has been deleted
1. Retrieve the parsed metadata
    - `curl http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000/parsed`
    - ensure it returns a 404, with an error object explaining that no parsed information exists 
1. Resurrect the record
    - `curl http://sciapps.colorado.edu/registry/metadata/collection/00000000-0000-0000-0000-000000000000/resurrection`
1. Retrieve the raw and parsed metadata again
    - ensure they behave like they initially did, above    
