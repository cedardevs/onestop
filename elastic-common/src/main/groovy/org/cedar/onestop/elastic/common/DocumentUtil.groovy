package org.cedar.onestop.elastic.common

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.RequestLine
import org.elasticsearch.client.Response

@Slf4j
class DocumentUtil {

    static Map parseSearchResponse(Response response) {
        int statusCode = response.statusLine.statusCode
        Map result = [
            statusCode: statusCode ?: 500
        ]
        try {
            if (response.entity) {
                HttpEntity entity = response.entity
                InputStream content = entity.content
                result += new JsonSlurper().parse(content) as Map
            }
        }
        catch (e) {
            log.warn("Failed to parse Elasticsearch response as JSON", e)
        }
        return result
    }

    static Map parseAdminResponse(Response response) {
        RequestLine requestLine = response.requestLine
        int statusCode = response.statusLine.statusCode
        Map result = [
            request   : requestLine,
            statusCode: statusCode
        ]
        if (response.entity) {
            result += new JsonSlurper().parse(response.entity.content) as Map
        }
        return result
    }

    static int getTook(Map parsedResponse) {
        return parsedResponse.took as int
    }

    static Map getHits(Map parsedResponse) {
        return parsedResponse.hits as Map
    }

    static int getHitsTotal(Map parsedResponse) {
        Map hits = getHits(parsedResponse)
        return hits.total as int
    }

    static List<Map> getDocuments(Map parsedResponse) {
        Map hits = getHits(parsedResponse)
        return hits.hits as List<Map>
    }

    static int getCount(Map parsedResponse) {
        return parsedResponse.count as int
    }

    static String getScrollId(Map parsedResponse) {
        return parsedResponse._scroll_id
    }

    static String getId(Map document) {
        return document._id
    }

    static String getIndex(Map document) {
        return document._index
    }

    static int getVersion(Map document) {
        return document._version as int
    }

    static Map getSource(Map document) {
        return document._source as Map
    }

    static String getFileIdentifier(Map document) {
        return getSource(document).fileIdentifier
    }

    static String getFileIdentifierFromSource(Map source) {
        return source.fileIdentifier
    }

    static String getParentIdentifier(Map document) {
        return getSource(document).parentIdentifier
    }

    static String getParentIdentifierFromSource(Map source) {
        return source.parentIdentifier
    }

    static String getInternalParentIdentifier(Map document) {
        return getSource(document).internalParentIdentifier
    }

    static String getInternalParentIdentifierFromSource(Map source) {
        return source.internalParentIdentifier
    }

    static String getDOI(Map document) {
        return getSource(document).doi
    }

    static String getDOIFromSource(Map source) {
        return source.doi
    }

    static Map getAggregations(Map document) {
        return document.aggregations as Map
    }

    static Long getMaxStagedDateValue(Map document) {
        Map maxStagedDate = getAggregations(document).maxStagedDate as Map
        return maxStagedDate.value as Long
    }

    static List<Map> getCollectionBuckets(Map document) {
        Map collections = getAggregations(document).collections as Map
        return collections.buckets as List<Map>
    }
    static List<Map> getCollectionBucketsFromAggregations(Map aggregations) {
        Map collections = aggregations.collections as Map
        return collections.buckets as List<Map>
    }

}