### Extract, Transform, and Load

Sub-project to orchestrate/perform ETL operations from metadata sources into the search index.

For now, it's just a set of gradle tasks which pull metadata from their sources
and POST them into our API app and/or geoportal.
