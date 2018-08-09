package org.cedar.onestop.api.search.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class LogstashElasticService {
    private final RestClient restClient

    @Autowired
    LogstashElasticService(RestClient restClient) {
        this.restClient = restClient
    }
    
    Map getTopSearchQueries(Integer size, Integer previousIndicies) {
        Map query = queryBuilder(size)
        def headers = new NStringEntity(JsonOutput.toJson(query), ContentType.APPLICATION_JSON)
        String indicies = indiciesBuilder(previousIndicies)
        Response response = restClient.performRequest('GET', "${indicies}/_search", Collections.EMPTY_MAP, headers)

        return responseAsMap(response)
    }

    private String indiciesBuilder(Integer previousIndicies) {
        StringBuilder indexBuilder = new StringBuilder()
        indexBuilder.append("%3Clogstash-%7Bnow%2Fd%7D%3E")

        for (int i = 1; i < previousIndicies; i++) {
            indexBuilder.append("%2C%3Clogstash-%7Bnow%2Fd-${i}d%7D%3E")
        }

        return indexBuilder.toString()
    }

    private Map responseAsMap(Response response) {
        Map result = [:]

        try {
            if (response.getEntity()) {
                result += new JsonSlurper().parse(response?.getEntity()?.getContent()) as Map
            }
        } catch (Exception e) {
            log.info("response error message" + e.toString())
        }
        return result
    }

    private Map queryBuilder(Integer size) {
        return [
                "query": [
                    "bool": [
                        "must_not": [
                            ["terms": [
                                "logParams.queries.value.keyword": [
                                    "weather",
                                    "climate",
                                    "satellites",
                                    "fisheries",
                                    "coasts",
                                    "oceans"
                                ]
                            ]]
                        ]
                    ]
                ],
                "size": 0,
                "aggs": [
                        "group_by_query": [
                                "terms": [
                                        "field": "logParams.queries.value.keyword",
                                        "size": size
                                ]
                        ]
                ]
        ]
    }
}

/*
GET _search
{
  "query": {
    "bool": {
      "must_not": [
        {"terms": {
          "logParams.queries.value.keyword": [
            "weaather",
            "work"
          ]
        }}
      ]
    }
  }, 
  "size": 0,
  "aggs": {
    "group_by_query": {
      "terms": {
        "field": "logParams.queries.value.keyword"
      }
    }
  }
}
*/
