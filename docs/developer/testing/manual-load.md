# Manually testing transforms and loading using Test-Common

The test-common module was designed for manual examination of the transform steps, in order to support developer understanding of what happens in each section of the system, aid crafting test data for integration, and to load Elasticsearch directly instead of requiring the entire stack for local development.

## Building and Running

Since this is intended as a standalone utility, it is written with the intention of calling it from the command line.

To build, run `./gradlew test-common:shadowJar`, which creates a standalone fatjar.

To run the utility, `java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main`. It requires arguments to run, and will provide some basic usage information.

## Arguements

The utility lets you chain together one or more transformation steps on a file.

For example `java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main STEP1 STEP2 STEP3 FILENAME`.

The file provide can be:
- ISO XML
- ParsedRecord JSON

The expected output depends on which steps are run.

## Steps

Valid steps are:
- parseISO
- parseAvro
- analyze
- granuleSearch
- collectionSearch
- granuleError
- collectionError

All steps should start with either parseISO or parseAvro. The utility does not know how to read a file otherwise.

### parseISO

INPUT: ISO XML
OUTPUT: ParsedRecord JSON

### parseAvro

INPUT: ParsedRecord JSON
OUTPUT: ParsedRecord JSON

Effectively a NO-OP? TODO

### analyze

INPUT: ParsedRecord JSON
OUTPUT: ParsedRecord JSON with the analysis step completed

### granuleSearch

INPUT: ParsedRecord JSON for a granule (expects analysis step to be completed)
OUTPUT: JSON matching the Elasticsearch mapping for the granule search index

### collectionSearch

INPUT: ParsedRecord JSON for a collection (expects analysis step to be completed)
OUTPUT: JSON matching the Elasticsearch mapping for the collection search index

### granuleError

INPUT: ParsedRecord JSON for a granule (expects analysis step to be completed)
OUTPUT: JSON matching the Elasticsearch mapping for the granule analysis and errors index

### collectionError

INPUT: ParsedRecord JSON for a collection (expects analysis step to be completed)
OUTPUT: JSON matching the Elasticsearch mapping for the collection analysis and errors index

## chaining steps together

You can use any step by itself, assuming you have the correct input. Or you can chain the steps together to produce the ES record based on the original input.

For example, if you have an XML record for a collection, `parseISO analyze collectionSearch record.xml` should produce the JSON you would post to the collection search index.
