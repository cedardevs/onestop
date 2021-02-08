<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**
# Guidelines to developers regarding API versioning.

For now, the API endpoints are also available under `/v1`.

We need to make a /v2 version of the API when we make breaking changes, as well as figure out how best to continue supporting previous versions of the API so they can be deprecated gracefully.


- [ ] openapi `info` section should specify API version
- [ ] previous versions of API will be copied into a separate openapi file, eg: `openapi_v1.yaml`
- [ ] documentation should make it clear which path to use to reach which version of the API, and what the current version of the "unversioned" API path is
- [ ] specs should be hosted and available at  `/openapi/v1`, `/openapi/v2` endpoints
- [ ] enhance integration / e2e tests to illustrate differences between API versions
