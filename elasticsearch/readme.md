# Helpful ES links

1. View indicies - http://localhost:9200/_cat/indices?v
2. Delete all indicies (Use carefully!) - curl -XDELETE 'http://localhost:9200/_all
3. Delete specific index - curl -XDELETE 'http://localhost:9200/{indexName}
4. Stop the ES instance - gradlew elasticsearch:stop
