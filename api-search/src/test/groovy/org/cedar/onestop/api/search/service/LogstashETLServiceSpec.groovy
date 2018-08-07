package org.cedar.onestop.api.search.service

import groovy.json.JsonSlurper
import spock.lang.Specification

class LogstashETLServiceSpec extends Specification {

    private LogstashETLService etlService

    def setup() {
        this.etlService = new LogstashETLService()
    }

    def "Turns logstash search into our output JSON"() {
        when: "We get an output from Elastic Search as a Map"
        Map esResponse = esOutput()
        Map expected = expectedOutput()

        then: "We filter the result to include only top search results with number of occurrences"
        Map result = etlService.etlResponse(esResponse)

        expect:
        expected == result
    }

    private static Map esOutput() {
        String output = "{\n" +
                "  \"took\": 8,\n" +
                "  \"timed_out\": false,\n" +
                "  \"_shards\": {\n" +
                "    \"total\": 5,\n" +
                "    \"successful\": 5,\n" +
                "    \"failed\": 0\n" +
                "  },\n" +
                "  \"hits\": {\n" +
                "    \"total\": 6799,\n" +
                "    \"max_score\": 0,\n" +
                "    \"hits\": []\n" +
                "  },\n" +
                "  \"aggregations\": {\n" +
                "    \"group_by_query\": {\n" +
                "      \"doc_count_error_upper_bound\": 0,\n" +
                "      \"sum_other_doc_count\": 0,\n" +
                "      \"buckets\": [\n" +
                "        {\n" +
                "          \"key\": \"GHRSST\",\n" +
                "          \"doc_count\": 4\n" +
                "        },\n" +
                "        {\n" +
                "          \"key\": \"climate\",\n" +
                "          \"doc_count\": 3\n" +
                "        },\n" +
                "        {\n" +
                "          \"key\": \"satellite\",\n" +
                "          \"doc_count\": 2\n" +
                "        },\n" +
                "        {\n" +
                "          \"key\": \"weather\",\n" +
                "          \"doc_count\": 1\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}"
        Map mappedOutput = new JsonSlurper().parseText(output) as Map
        return mappedOutput
    }

    private static Map expectedOutput() {
        Map output = [
                "GHRSST": 4,
                "climate": 3,
                "satellite": 2,
                "weather": 1
        ]
    }
}
