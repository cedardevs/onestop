<div align="center"><a href="/onestop/developer/testing/integration-tests/">Integration Tests Documentation Home</a></div>
<hr>

**Estimated Reading Time: 5 minutes**
# Bulk Data in Search API's ES Integration Tests

## Important ES Note

- Our bulkData.txt must end in a blank line, or the bulk load will skip the last result.
- For all queries, the full response is requested so we have access to all time fields.
- Max result limit is explicitly declared (page object in query) since more than the default of 10 can be returned because our test dataset is fairly large.

<hr>
<div align="center"><a href="#">Top of Page</a></div>
