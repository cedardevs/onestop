# Important ES Note

- bulkData.txt must end in a blank line, or the bulk load will skip the last result.
- For all queries, the full response is requested so we have access to all time fields.
- Max result limit is explicitly declared (page object in query) since more than the default of 10 can be returned since our test data set is fairly large.
