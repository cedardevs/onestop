package org.cedar.psi.registry.service

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification


class PublisherSpec extends Specification {

  def mockProducer = Mock(Producer)
  def publisher = new Publisher(mockProducer)

  def 'publishes valid granule xml metadata'(){
    setup:
    String type = 'granule'
    String id = 'abc123'
    String source = 'common-ingest'
    String data = '<text>xml woooo....</text>'
    String requestUrl = "https://localhost:8080/metadata/$source/$type/$id"
    String method = 'POST'
    String contentType = 'application/xml'
    def request = new MockHttpServletRequest(method,requestUrl)
    request.contentType = contentType

    when:
    publisher.publishMetadata( request, type, source, id, data)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && \
      it.key().toString() == id.toString() && \
      it.value().toString().contains(id.toString()) && \
      it.value().toString().contains(request.contentType) && \
      it.value().toString().contains( method )  && \
      it.value().toString().contains( requestUrl )
    })

  }

  def 'publishes valid granule json metadata'(){
    setup:
    String type = 'granule'
    String id = 'abc123'
    String source = 'common-ingest'
    String data = "{\"trackingId\":\"ABC\", \"path\":\"/test/file.txt\"}"
    String requestUrl = "https://localhost:8080/metadata/$source/$type/$id"
    String method = 'POST'
    def request = new MockHttpServletRequest(method,requestUrl)
    request.contentType = 'application/json'

    when:
    publisher.publishMetadata( request, type, source, id, data)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && \
      it.key().toString() == id.toString() && \
      it.value().toString().contains(id.toString()) && \
      it.value().toString().contains(request.contentType) && \
      it.value().toString().contains( method )  && \
      it.value().toString().contains( requestUrl )
    })

  }

  def 'publishes valid collection xml metadata'(){
    setup:
    String type = 'collection'
    String id = 'abc123'
    String source = 'common-ingest'
    String data = '<text>xml woooo....</text>'
    String requestUrl = "https://localhost:8080/metadata/$source/$type/$id"
    String method = 'POST'
    String contentType = 'application/xml'
    def request = new MockHttpServletRequest(method,requestUrl)
    request.contentType = contentType

    when:
    publisher.publishMetadata( request, type, source, id, data)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && \
      it.key().toString() == id.toString() && \
      it.value().toString().contains(id.toString()) && \
      it.value().toString().contains(contentType) && \
      it.value().toString().contains( method )  && \
      it.value().toString().contains( requestUrl )
    })

  }

  def 'publishes valid collection json metadata'(){
    setup:
    String type = 'collection'
    String id = 'abc123'
    String source = 'common-ingest'
    String data = "{\"trackingId\":\"ABC\", \"path\":\"/test/file.txt\"}"
    String requestUrl = "https://localhost:8080/metadata/$source/$type/$id"
    String method = 'POST'
    String contentType = 'application/json'
    def request = new MockHttpServletRequest(method, requestUrl)
    request.contentType = contentType


    when:
    publisher.publishMetadata( request, type, source, id, data )

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && \
      it.key().toString() == id.toString() && \
      it.value().toString().contains(id.toString()) && \
      it.value().toString().contains(contentType) && \
      it.value().toString().contains( method )  && \
      it.value().toString().contains( requestUrl )
    })

  }

}
