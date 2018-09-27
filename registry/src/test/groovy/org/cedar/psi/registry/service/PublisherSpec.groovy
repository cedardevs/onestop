package org.cedar.psi.registry.service

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.cedar.psi.common.constants.Topics
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Future


@Unroll
class PublisherSpec extends Specification {

  def mockProducer = Mock(Producer)
  def publisher = new Publisher(mockProducer)

  def 'publishes valid #type metadata as #contentType with #source id'() {
    setup:
    String requestUri = "/metadata/$type/$source/$id"
    String method = 'POST'
    def request = new MockHttpServletRequest(method,requestUri)
    request.contentType = contentType

    when:
    publisher.publishMetadata(request, type, data, source, id)

    then:
    1 * mockProducer.send({
      it instanceof ProducerRecord &&
          it.topic() == Topics.inputTopic(type, source) &&
          it.key() == id &&
          it.value().input.requestUrl == "http://localhost$requestUri" &&
          it.value().input.method == method  &&
          it.value().input.content == data &&
          it.value().input.contentType == contentType &&
          it.value().input.source == source &&
          it.value().identifiers == [(source): id]
    }) >> Mock(Future)

    where:
    source          | type        | id    | contentType       | data
    'common-ingest' | 'granule'   | 'ABC' | 'application/xml' | '<text>xml woooo....</text>'
    'common-ingest' | 'granule'   | 'ABC' | 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
    'comet'         | 'collection'| 'ABC' | 'application/xml' | '<text>xml woooo....</text>'
    'comet'         | 'collection'| 'ABC' | 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
  }

  def 'publishes valid #type metadata as #contentType with existing id'() {
    setup:
    String requestUri = "/metadata/$type/$id"
    String method = 'POST'
    def request = new MockHttpServletRequest(method,requestUri)
    request.contentType = contentType

    when:
    publisher.publishMetadata(request, type, data, Topics.DEFAULT_SOURCE, id)

    then:
    1 * mockProducer.send({
      it instanceof ProducerRecord &&
          it.topic() == Topics.inputTopic(type, Topics.DEFAULT_SOURCE) &&
          it.key() == id &&
          it.value().input.contentType == contentType &&
          it.value().input.method == method  &&
          it.value().input.requestUrl == "http://localhost$requestUri" &&
          it.value().input.content == data &&
          it.value().identifiers == [(Topics.DEFAULT_SOURCE): id]
    }) >> Mock(Future)

    where:
    type        | id    | contentType       | data
    'granule'   | 'ABC' | 'application/xml' | '<text>xml woooo....</text>'
    'granule'   | 'ABC' | 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
    'collection'| 'ABC' | 'application/xml' | '<text>xml woooo....</text>'
    'collection'| 'ABC' | 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
  }

  def 'publishes valid #type metadata as #contentType with no id'() {
    setup:
    String requestUri = "/metadata/$type"
    String method = 'POST'
    def request = new MockHttpServletRequest(method,requestUri)
    request.contentType = contentType

    when:
    publisher.publishMetadata(request, type, data, Topics.DEFAULT_SOURCE)

    then:
    1 * mockProducer.send({
      it instanceof ProducerRecord &&
          it.topic() == Topics.inputTopic(type, Topics.DEFAULT_SOURCE) &&
          it.key() instanceof String &&
          it.value().input.contentType == contentType &&
          it.value().input.method == method  &&
          it.value().input.requestUrl == "http://localhost$requestUri" &&
          it.value().input.content == data &&
          it.value().identifiers[Topics.DEFAULT_SOURCE] instanceof String
    }) >> Mock(Future)

    where:
    type        |  contentType       | data
    'granule'   | 'application/xml' | '<text>xml woooo....</text>'
    'granule'   | 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
    'collection'| 'application/xml' | '<text>xml woooo....</text>'
    'collection'| 'application/json'| '{"trackingId":"ABC", "path":"/test/file.txt"}'
  }

  def 'publishes nothing for invalid type'() {
    setup:
    String type = 'notarealtype'
    String data = '{"message":"I am incorrect"}'
    String requestUri = "/metadata/$type"
    String method = 'POST'
    def request = new MockHttpServletRequest(method,requestUri)
    request.contentType = 'application/json'

    when:
    publisher.publishMetadata(request, type, data, Topics.DEFAULT_SOURCE)

    then:
    0 * mockProducer.send(_)
  }

}
