package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.ResponseListener
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.concurrent.CountDownLatch

@Slf4j
@Service
class ElasticsearchService {

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.staging.name}')
  String STAGING_INDEX

  @Value('${elasticsearch.index.prefix:}${elasticsearch.index.search.name}')
  String SEARCH_INDEX

  @Value('${elasticsearch.index.prefix:}')
  String PREFIX

  private RestClient restClient

  @Autowired
  ElasticsearchService(RestClient restClient) {
    this.restClient =  restClient
  }

  @PostConstruct
  public void ensureIndices() {
    ensureStagingIndex()
    ensureSearchIndex()
  }

  public void ensureStagingIndex() {
    ensureIndex(STAGING_INDEX)
  }

  public void ensureSearchIndex() {
    ensureIndex(SEARCH_INDEX)
  }

  private void ensureIndex(String index) {
    def indexExists = checkAliasExists(index)
    if(!indexExists) {
      def realName = create(index)
      String endPoint = "/${realName}/_alias/${index}"
      performRequest('PUT', endPoint)
    }
  }

  public void disableIndexRefresh(String index) {
    String endpoint = "/${index}/_settings"
    def request = [
        index: [
            refresh_interval: "-1"
        ]
    ]
    performRequest('PUT', endpoint, request)
  }

  public void enableIndexRefresh(String index) {
    String endpoint = "/${index}/_settings"
    def request = [
        index: [
            refresh_interval: "15s"
        ]
    ]
    performRequest('PUT', endpoint, request)
  }

  public String create(String baseName) {
    String indexName = "${baseName}-${System.currentTimeMillis()}"
    def cl = Thread.currentThread().contextClassLoader
    def indexJson = cl.getResourceAsStream("${baseName}Index.json").text
    performRequest('PUT', indexName, indexJson)

    log.debug "Created new index [${indexName}]"
    return indexName
  }

  public void refresh(String... indices) {
    String endpoint = "/${indices.join(',')}/_refresh"
    performRequest('POST', endpoint)
  }

  public void dropStagingIndex() {
    drop(STAGING_INDEX)
  }

  public void dropSearchIndex() {
    drop(SEARCH_INDEX)
  }

  public void drop(String indexName) {
    try {
      performRequest('DELETE', indexName)
      log.debug "Dropped index [${indexName}]"
    } catch(e) {
      log.warn "Failed to drop index [${indexName}] because it was not found"
    }
  }

  private Boolean checkAliasExists(String name) {
    def status = restClient.performRequest('HEAD', name).statusLine.statusCode
    return status == 200
  }

  public Map performRequest(String method, String endpoint, Map requestBody) {
    performRequest(method, endpoint, JsonOutput.toJson(requestBody))
  }

  public Map performRequest(String method, String endpoint, String requestBody = null) {
    try {
      log.debug("Performing request: ${method} ${endpoint} ${requestBody}")
      def response = requestBody ?
          restClient.performRequest(method, endpoint, Collections.EMPTY_MAP, new NStringEntity(requestBody, ContentType.APPLICATION_JSON)) :
          restClient.performRequest(method, endpoint)
      log.debug("Got response: $response")
      return parseResponse(response)
    }
    catch(ResponseException e) {
      return parseResponse(e.response)
    }
  }

  /**
   * @param index
   * @param dataRecords List of Map objects with id, type, and source fields. If id is not populated, record is created; otherwise, it's updated.
   * @return List of records in same order as dataRecords, with a status field and, in event of error, an error object
   */
  public List performMultiLoad(String index, List dataRecords) {
    List resultRecords = Collections.synchronizedList(new ArrayList())
    final CountDownLatch latch = new CountDownLatch(dataRecords.size())

    dataRecords.eachWithIndex { record, i ->
      String endpoint, method
      if (record.id) {
        endpoint = "${index}/${record.type}/${record.id}"
        method = 'PUT'
      }
      else {
        endpoint = "${index}/${record.type}"
        method = 'POST'
      }
      HttpEntity source = new NStringEntity(v.source, ContentType.APPLICATION_JSON)

      restClient.performRequestAsync(method, endpoint, Collections.EMPTY_MAP, source, new ResponseListener() {
        @Override
        void onSuccess(Response response) {
          def result = parseResponse(response)
          resultRecords.add(i, [
              status: response.statusLine.statusCode,
              id: result._id
          ])
          latch.countDown()
        }

        @Override
        void onFailure(Exception exception) {
          def status, error
          if(exception instanceof ResponseException) {
            status = exception.response.statusLine.statusCode
            error = [
                title: 'Load request failed, elasticsearch rejected document',
                detail: exception.message
            ]
          }
          else {
            status = 500
            error = [
                title: 'Load request failed, elasticsearch connection failure',
                detail: exception.message
            ]
          }
          resultRecords.add(i, [
              status: status,
              error: error
          ])
          latch.countDown()
        }
      })
    }

    latch.await()
    return resultRecords
  }

  public Map performDeleteByQuery(String query, List<String> indices, String type = null) {
    Map resultRecords = Collections.synchronizedMap(new HashMap())
    final CountDownLatch latch = new CountDownLatch(indices.size())

    indices.each { i ->
      String endpoint = "${i}/${(type ?: '') + '/'}_delete_by_query"
      HttpEntity queryBody = new NStringEntity(query, ContentType.APPLICATION_JSON)
      restClient.performRequestAsync('POST', endpoint, Collections.EMPTY_MAP, queryBody, new ResponseListener() {
        @Override
        void onSuccess(Response response) {
          resultRecords.put(i, parseResponse(response))
          latch.countDown()
        }

        @Override
        void onFailure(Exception exception) {
          if (exception instanceof ResponseException) {
            resultRecords.put(i, parseResponse(exception.response))
          }
          else {
            resultRecords.put(i, [
                error: [
                    title: "Delete_by_query on ${i} index failed",
                    detail: exception.message
                ]
            ])
          }
          latch.countDown()
        }
      })
    }

    latch.await()
    return resultRecords
  }

  private static Map parseResponse(Response response) {
    Map result = [
        request: response.requestLine,
        statusCode: response.statusLine.statusCode
    ]
    if (response.entity) {
      result += new JsonSlurper().parse(response.entity.content) as Map
    }
    return result
  }

}
