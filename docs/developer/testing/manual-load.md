# Manually testing transforms and loading using Test-Common

The test-common module was designed for manual examination of the transform steps, in order to support developer understanding of what happens in each section of the system, aid crafting test data for integration, and to load Elasticsearch directly instead of requiring the entire stack for local development.

## Building and Running

Since this is intended as a standalone utility, it is written with the intention of calling it from the command line.

To build, run `./gradlew test-common:shadowJar`, which creates a standalone fatjar.

To run the utility, `java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main`. It requires arguments to run, and will provide some basic usage information.

## Arguements

The utility lets you chain together one or more transformation steps on a file.

For example `java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main STEP1 STEP2 STEP3 FILENAME`.
